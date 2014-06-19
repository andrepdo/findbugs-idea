/*
 * Copyright 2008-2013 Andre Pfeiler
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
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.event.EventListener;
import org.twodividedbyzero.idea.findbugs.common.event.EventManagerImpl;
import org.twodividedbyzero.idea.findbugs.common.event.filters.BugReporterEventFilter;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsWorker;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class AnalyzeCurrentEditorFile extends BaseAction implements EventListener<BugReporterEvent> {

	private static final Logger LOGGER = Logger.getInstance(AnalyzeCurrentEditorFile.class.getName());

	private AnActionEvent _actionEvent;
	private DataContext _dataContext;
	private boolean _enabled;
	private boolean _running;


	@Override
	public void actionPerformed(final AnActionEvent e) {
		_actionEvent = e;
		_dataContext = e.getDataContext();

		final com.intellij.openapi.project.Project project = DataKeys.PROJECT.getData(_dataContext);
		final Presentation presentation = e.getPresentation();

		// check a project is loaded
		if (isProjectNotLoaded(project, presentation)) {
			Messages.showWarningDialog("Project not loaded.", getPluginInterface(project).getInternalToolWindowId());
			return;
		}

		final FindBugsPreferences preferences = getPluginInterface(project).getPreferences();
		if (preferences.getBugCategories().containsValue("true") && preferences.getDetectors().containsValue("true")) {
			initWorker();
		} else {
			FindBugsPluginImpl.showToolWindowNotifier(project, "No bug categories or bug pattern detectors selected. analysis aborted.", MessageType.WARNING);
			ShowSettingsUtil.getInstance().editConfigurable(project, IdeaUtilImpl.getPluginComponent(project));
		}
	}


	@SuppressWarnings("ConstantConditions")
	@Override
	public void update(final AnActionEvent event) {
		try {
			_actionEvent = event;
			_dataContext = event.getDataContext();
			final com.intellij.openapi.project.Project project = DataKeys.PROJECT.getData(_dataContext);
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

			final VirtualFile[] selectedSourceFiles = IdeaUtilImpl.getVirtualFiles(_dataContext);

			if (!_running) {
				_enabled = selectedSourceFiles != null && selectedSourceFiles.length > 0 && selectedSourceFiles[0].isValid() && IdeaUtilImpl.isValidFileType(selectedSourceFiles[0].getFileType());
			}

			presentation.setEnabled(toolWindow.isAvailable() && isEnabled());
			presentation.setVisible(true);

		} catch (final Throwable e) {
			final FindBugsPluginException processed = FindBugsPluginImpl.processError("Action update failed", e);
			LOGGER.error("Action update failed", processed);
		}
	}


	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"}) // action is not enabled
	@SuppressWarnings("ConstantConditions")
	private void initWorker() {

		//final UserPreferences userPrefs = UserPreferences.createDefaultUserPreferences();
		//userPrefs.enableAllDetectors(true);

		final com.intellij.openapi.project.Project project = IdeaUtilImpl.getProject(_dataContext);
		final Module module = IdeaUtilImpl.getModule(_dataContext);

		final FindBugsPreferences preferences = getPluginInterface(project).getPreferences();
		if (Boolean.valueOf(preferences.getProperty(FindBugsPreferences.TOOLWINDOW_TO_FRONT))) {
			IdeaUtilImpl.activateToolWindow(getPluginInterface(project).getInternalToolWindowId(), _dataContext);
		}

		final FindBugsWorker worker = new FindBugsWorker(project, module);

		/*final FindBugsProject findBugsProject = new FindBugsProject();
		final com.intellij.openapi.project.Project project = IdeaUtilImpl.getProject(_dataContext);
		findBugsProject.setProjectName(project.getName());
		final Reporter bugReporter = new Reporter(project);
		bugReporter.setPriorityThreshold(userPrefs.getUserDetectorThreshold());*/


		// set aux classpath
		final VirtualFile[] files = IdeaUtilImpl.getProjectClasspath(_dataContext);
		worker.configureAuxClasspathEntries(files);

		// set source dirs
		//final VirtualFile[] selectedSourceFiles = IdeaUtilImpl.getSelectedFiles(_dataContext);
		final VirtualFile[] selectedSourceFiles = IdeaUtilImpl.getVirtualFiles(_dataContext);
		worker.configureSourceDirectories(selectedSourceFiles);


		// set class files
		worker.configureOutputFiles(selectedSourceFiles);
		worker.work("Running FindBugs analysis for editor files...");

		// Create IFindBugsEngine
		/*final FindBugs2 engine = new FindBugs2();
		engine.setNoClassOk(true);
		engine.setBugReporter(bugReporter);
		engine.setProject(findBugsProject);
		engine.setProgressCallback(bugReporter);*/


		// add plugins to detector collection
		//final DetectorFactoryCollection dfc = DetectorFactoryCollection.instance();
		//Plugin fakePlugin = new Plugin("edu.umd.cs.findbugs.fakeplugin", null);
		//fakePlugin.setEnabled(true);
		//dfc.setPlugins(new Plugin[]{fakePlugin});
		//dfc.setPluginList(FindBugsPluginLoader.determinePlugins(new String[] {""}));

		//engine.setDetectorFactoryCollection(dfc);

		// configure detectors.
		//engine.setUserPreferences(userPrefs);


		//runFindBugs(engine);

		/*final StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
		final JLabel jLabel = new JLabel(ResourcesLoader.loadIcon(GuiResources.FINDBUGS_ICON));
		statusBar.addCustomIndicationComponent(jLabel);

		final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(FindBugsPluginConstants.TOOL_WINDOW_ID);
		final Content content = toolWindow.getContentManager().getContent(0);
		if (content != null) {
			final ToolWindowPanel panel = (ToolWindowPanel) content.getComponent();


			InfoPopup.popup(jLabel, "Found 99 Bugs", ResourcesLoader.loadIcon(GuiResources.FINDBUGS_ICON), "Click here to clear this alert and open the Changes toolwindow.", new AbstractAction() {
				public void actionPerformed(final ActionEvent e) {
					//showChangesToolwindow(_projectComponent.getProject());
					statusBar.removeCustomIndicationComponent(jLabel);
				}
			}, 0);
		}*/
	}


	/*private void runFindBugs(final FindBugs2 findBugs) {
		// bug 1828973 was fixed by findbugs engine, so that workaround to start the
		// analysis in an extra thread is not more needed
		try {
			// Perform the analysis! (note: This is not thread-safe)
			//CommandProcessor.getInstance().executeCommand();
			findBugs.execute();
			
		} catch (InterruptedException e) {
			FindBugsPlugin.processError("Worker interrupted.", e);
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			FindBugsPlugin.processError("Error performing FindBugs analysis", e);
		} finally {
			findBugs.dispose();
		}
	}*/


	private void registerEventListener(final Project project) {
		final String projectName = project.getName();
		if (!isRegistered(projectName)) {
			EventManagerImpl.getInstance().addEventListener(new BugReporterEventFilter(projectName), this);
			addRegisteredProject(projectName);
		}
	}


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


	protected boolean isRunning() {
		return _running;
	}


	protected boolean setRunning(final boolean running) {
		final boolean was = _running;
		if (_running != running) {
			_running = running;
		}
		return was;
	}


	public void onEvent(@NotNull final BugReporterEvent event) {
		//if (isProjectEvent(IdeaUtilImpl.getProject().getName(), event.getProjectName())) {
		switch (event.getOperation()) {
			case ANALYSIS_STARTED:
				setEnabled(false);
				setRunning(true);
				break;
			case ANALYSIS_ABORTED:
			case ANALYSIS_FINISHED:
				setEnabled(true);
				setRunning(false);
				break;
			case NEW_BUG_INSTANCE:
				break;
		}
		//}
	}
}
