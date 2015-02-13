/*
 * Copyright 2008-2015 Andre Pfeiler
 *
 * This file is part of FindBugs-IDEA.
 *
 * FindBugs-IDEA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FindBugs-IDEA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FindBugs-IDEA.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.twodividedbyzero.idea.findbugs.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.Plugin;
import edu.umd.cs.findbugs.ProjectStats;
import edu.umd.cs.findbugs.SortedBugCollection;
import org.dom4j.DocumentException;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.event.EventListener;
import org.twodividedbyzero.idea.findbugs.common.event.EventManagerImpl;
import org.twodividedbyzero.idea.findbugs.common.event.filters.BugReporterEventFilter;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent.Operation;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEventFactory;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEventImpl;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.gui.PluginGuiCallback;
import org.twodividedbyzero.idea.findbugs.gui.common.ImportFileDialog;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
import org.twodividedbyzero.idea.findbugs.tasks.BackgroundableTask;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.96
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class ImportBugCollection extends BaseAction implements EventListener<BugReporterEvent> {

	private static final Logger LOGGER = Logger.getInstance(ImportBugCollection.class.getName());

	private boolean _enabled;
	private SortedBugCollection _bugCollection;
	private boolean _running;
	private SortedBugCollection _importBugCollection;


	@Override
	protected boolean isEnabled() {
		return _enabled;
	}


	@Override
	protected boolean setEnabled(final boolean enabled) {
		final boolean was = _enabled;
		if (_enabled != enabled) {
			_enabled = enabled;
		}
		return was;
	}


	@SuppressWarnings({"AssignmentToStaticFieldFromInstanceMethod"})
	@Override
	public void actionPerformed(final AnActionEvent e) {
		final Project project = IdeaUtilImpl.getProject(e.getDataContext());
		assert project != null;
		final Presentation presentation = e.getPresentation();

		// check a project is loaded
		if (isProjectNotLoaded(project, presentation)) {
			Messages.showWarningDialog("Project not loaded.", "FindBugs");
			return;
		}

		//Ask the user for a export directory
		final DialogBuilder dialogBuilder = new DialogBuilder(project);
		dialogBuilder.addOkAction();
		dialogBuilder.addCancelAction();
		dialogBuilder.setTitle("Import previous saved bug collection xml");

		final FindBugsPreferences preferences = getPluginInterface(project).getPreferences();
		final String exportDir = preferences.getProperty(FindBugsPreferences.EXPORT_BASE_DIR, FindBugsPluginConstants.DEFAULT_EXPORT_DIR) + File.separatorChar + project.getName();

		final ImportFileDialog importFileDialog = new ImportFileDialog(exportDir, dialogBuilder);
		dialogBuilder.showModal(true);
		if (dialogBuilder.getDialogWrapper().getExitCode() == DialogWrapper.CANCEL_EXIT_CODE) {
			return;
		}
		final String fileToImport = importFileDialog.getText();
		if (fileToImport == null || fileToImport.trim().isEmpty()) {
			return;
		}


		final FindBugsPlugin findBugsPlugin = getPluginInterface(project);
		final BugCollection bugCollection = findBugsPlugin.getToolWindowPanel().getBugCollection();
		if (bugCollection != null && !bugCollection.getCollection().isEmpty()) {
			//noinspection DialogTitleCapitalization
			final int result = Messages.showYesNoDialog(project, "Current result in the 'Found bugs view' will be deleted. Continue ?", "Delete found bugs?", Messages.getQuestionIcon());
			if (result == 1) {
				return;
			}
		}

		//Create a task to export the bug collection to html
		final AtomicReference<Task> importTask = new AtomicReference<Task>(new BackgroundableTask(project, "Importing Findbugs Result", true) {
			private ProgressIndicator _indicator;


			@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {

				EventManagerImpl.getInstance().fireEvent(new BugReporterEventImpl(Operation.ANALYSIS_STARTED, null, 0, project.getName()));
				setProgressIndicator(indicator);
				indicator.setFraction(0.0);
				indicator.setIndeterminate(false);
				indicator.setText(fileToImport);
				try {
					_bugCollection = new SortedBugCollection();
					final FindBugsPlugin pluginComponent = IdeaUtilImpl.getPluginComponent(project);
					_importBugCollection = _bugCollection.createEmptyCollectionWithMetadata();
					final edu.umd.cs.findbugs.Project importProject = _importBugCollection.getProject();
					importProject.setGuiCallback(new PluginGuiCallback(pluginComponent));
					_importBugCollection.setDoNotUseCloud(true);
					for (final Plugin plugin : Plugin.getAllPlugins()) {
						importProject.setPluginStatusTrinary(plugin.getPluginId(), !preferences.isPluginDisabled(plugin.getPluginId()));
					}
					_importBugCollection.readXML(fileToImport);

					final ProjectStats projectStats = _importBugCollection.getProjectStats();
					int bugCount = 0;
					for (final BugInstance bugInstance : _importBugCollection) {
						if (indicator.isCanceled()) {
							EventManagerImpl.getInstance().fireEvent(BugReporterEventFactory.newAborted(project));
							Thread.currentThread().interrupt();
							return;
						}
						final Integer bugCounter = bugCount++;
						final double fraction = bugCounter.doubleValue() / projectStats.getTotalBugs();
						indicator.setFraction(fraction);
						indicator.setText2("Importing bug '" + bugCount + "' of '" + projectStats.getTotalBugs() + "' - " + bugInstance.getMessageWithoutPrefix());
						EventManagerImpl.getInstance().fireEvent(new BugReporterEventImpl(Operation.NEW_BUG_INSTANCE, bugInstance, bugCounter, projectStats, project.getName()));
					}

					showToolWindowNotifier(project, "Imported bug collection from '" + fileToImport + "'.", MessageType.INFO);

					_importBugCollection.setDoNotUseCloud(false);
					_importBugCollection.setTimestamp(System.currentTimeMillis());
					_importBugCollection.reinitializeCloud();
				} catch (final IOException e1) {
					EventManagerImpl.getInstance().fireEvent(BugReporterEventFactory.newAborted(project));
					final String message = "Import failed";
					showToolWindowNotifier(project, message, MessageType.ERROR);
					LOGGER.error(message, e1);

				} catch (final DocumentException e1) {
					EventManagerImpl.getInstance().fireEvent(BugReporterEventFactory.newAborted(project));
					final String message = "Import failed";
					showToolWindowNotifier(project, message, MessageType.ERROR);
					LOGGER.error(message, e1);

				} finally {
					EventManagerImpl.getInstance().fireEvent(new BugReporterEventImpl(Operation.ANALYSIS_FINISHED, null, _importBugCollection, project.getName()));
					_importBugCollection = null;
					Thread.currentThread().interrupt();
				}
			}


			@Override
			public void setProgressIndicator(@NotNull final ProgressIndicator indicator) {
				_indicator = indicator;
			}


			@Override
			public ProgressIndicator getProgressIndicator() {
				return _indicator;
			}
		});

		importTask.get().setCancelText("Cancel");
		importTask.get().asBackgroundable();
		importTask.get().queue();
	}


	private static void showToolWindowNotifier(final Project project, final String message, final MessageType type) {
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			public void run() {
				FindBugsPluginImpl.showToolWindowNotifier(project, message, type);
			}
		});
	}


	@Override
	public void update(final AnActionEvent event) {
		try {
			final com.intellij.openapi.project.Project project = DataKeys.PROJECT.getData(event.getDataContext());
			final Presentation presentation = event.getPresentation();

			// check a project is loaded
			if (isProjectNotLoaded(project, presentation)) {
				return;
			}

			isPluginAccessible(project);

			// check if tool window is registered
			final ToolWindow toolWindow = isToolWindowRegistered(project);
			if (toolWindow == null) {
				presentation.setEnabled(false);
				presentation.setVisible(false);

				return;
			}

			registerEventListener(project);

			if (!_running) {
				_enabled = _importBugCollection == null;
			}

			presentation.setEnabled(toolWindow.isAvailable() && isEnabled());
			presentation.setVisible(true);

		} catch (final Throwable e) {
			final FindBugsPluginException processed = FindBugsPluginImpl.processError("Action update failed", e);
			LOGGER.error("Action update failed", processed);
		}
	}


	@SuppressWarnings({"AssignmentToNull"})
	public void onEvent(@NotNull final BugReporterEvent event) {
		switch (event.getOperation()) {
			case ANALYSIS_STARTED:
				_bugCollection = null;
				setEnabled(false);
				setRunning(true);
				break;
			case ANALYSIS_ABORTED:
				_bugCollection = null;
				setEnabled(true);
				setRunning(false);
				break;
			case ANALYSIS_FINISHED:
				_bugCollection = (SortedBugCollection) event.getBugCollection();
				setEnabled(true);
				setRunning(false);
				break;
			case NEW_BUG_INSTANCE:
				break;
			default:
		}
	}


	private void registerEventListener(final Project project) {
		final String projectName = project.getName();
		if (!isRegistered(projectName)) {
			EventManagerImpl.getInstance().addEventListener(new BugReporterEventFilter(projectName), this);
			addRegisteredProject(projectName);
		}
	}


	protected boolean isRunning() {
		return _running;
	}


	boolean setRunning(final boolean running) {
		final boolean was = _running;
		if (_running != running) {
			_running = running;
		}
		return was;
	}
}
