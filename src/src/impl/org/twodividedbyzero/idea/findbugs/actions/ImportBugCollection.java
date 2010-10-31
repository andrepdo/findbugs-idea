/*
 * Copyright 2010 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEventImpl;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.gui.common.ImportFileDialog;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
import org.twodividedbyzero.idea.findbugs.tasks.BackgroundableTask;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;


/**
 * $Date: 2010-10-10 16:30:08 +0200 (Sun, 10 Oct 2010) $
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision: 107 $
 * @since 0.9.96
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class ImportBugCollection extends BaseAction implements EventListener<BugReporterEvent> {

	private static final Logger LOGGER = Logger.getInstance(ImportBugCollection.class.getName());

	private boolean _enabled;
	private BugCollection _bugCollection;
	private boolean _running;
	private BugCollection _importBugCollection;


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
		if (isProjectLoaded(project, presentation)) {
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
		if (fileToImport == null || fileToImport.trim().length() == 0) {
			return;
		}


		final FindBugsPlugin findBugsPlugin = getPluginInterface(project);
		final BugCollection bugCollection = findBugsPlugin.getToolWindowPanel().getBugCollection();
		if (bugCollection != null && !bugCollection.getCollection().isEmpty()) {
			final int result = Messages.showYesNoDialog(project, "Current result in the 'Found bugs view' will be deleted. Continue ?", "Delete found bugs?", Messages.getQuestionIcon());
			if(result == 1) {
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
					_importBugCollection = _bugCollection.createEmptyCollectionWithMetadata();
					_importBugCollection.readXML(fileToImport);

					final ProjectStats projectStats = _importBugCollection.getProjectStats();
					int bugCount = 0;
					for (final BugInstance bugInstance : _importBugCollection) {
						if(indicator.isCanceled()) {
							EventManagerImpl.getInstance().fireEvent(new BugReporterEventImpl(Operation.ANALYSIS_ABORTED, project.getName()));
							Thread.currentThread().interrupt();
							return;
						}
						final Integer bugCounter = bugCount++;
						final double fraction = bugCounter.doubleValue() / projectStats.getTotalBugs();
						indicator.setFraction(fraction);
						indicator.setText2("Importing bug '" + bugCount + "' of '" + projectStats.getTotalBugs() + "' - " + bugInstance.getMessageWithoutPrefix());
						EventManagerImpl.getInstance().fireEvent(new BugReporterEventImpl(Operation.NEW_BUG_INSTANCE, bugInstance, bugCounter, projectStats, project.getName()));
					}

					showToolWindowNotifier("Imported bug collection from '" + fileToImport + "'.", MessageType.INFO);
				} catch (IOException e1) {
					EventManagerImpl.getInstance().fireEvent(new BugReporterEventImpl(Operation.ANALYSIS_ABORTED, project.getName()));
					final String message = "Import failed";
					showToolWindowNotifier(message, MessageType.ERROR);
					LOGGER.error(message, e1);

				} catch (DocumentException e1) {
					EventManagerImpl.getInstance().fireEvent(new BugReporterEventImpl(Operation.ANALYSIS_ABORTED, project.getName()));
					final String message = "Import failed";
					showToolWindowNotifier(message, MessageType.ERROR);
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


	private static void showToolWindowNotifier(final String message, final MessageType type) {
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			public void run() {
				FindBugsPluginImpl.showToolWindowNotifier(message, type);
			}
		});
	}


	@Override
	public void update(final AnActionEvent event) {
		try {
			final com.intellij.openapi.project.Project project = DataKeys.PROJECT.getData(event.getDataContext());
			final Presentation presentation = event.getPresentation();

			// check a project is loaded
			if (isProjectLoaded(project, presentation)) {
				return;
			}

			isPluginAccessible(project);

			// check if tool window is registered
			final ToolWindow toolWindow = isToolWindowRegistred(project);
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

		} catch (Throwable e) {
			final FindBugsPluginException processed = FindBugsPluginImpl.processError("Action update failed", e);
			if (processed != null) {
				LOGGER.error("Action update failed", processed);
			}
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
				_bugCollection = event.getBugCollection();
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
