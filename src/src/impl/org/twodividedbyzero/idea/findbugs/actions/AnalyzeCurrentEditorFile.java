/**
 * Copyright 2008 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
import org.twodividedbyzero.idea.findbugs.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.FindBugsWorker;
import org.twodividedbyzero.idea.findbugs.common.event.EventListener;
import org.twodividedbyzero.idea.findbugs.common.event.EventManagerImpl;
import org.twodividedbyzero.idea.findbugs.common.event.filters.BugReporterEventFilter;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
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


	public AnalyzeCurrentEditorFile() {
		//final Presentation presentation = ActionManager.getInstance().getAction(FindBugsPluginConstants.CURRENT_FILE_ACTION).getTemplatePresentation();
		//getTemplatePresentation().setText("123");
		//getTemplatePresentation().setDescription("12345");
		//getTemplatePresentation().setIcon(presentation.getIcon());
	}


	@Override
	public void actionPerformed(final AnActionEvent e) {
		_actionEvent = e;
		_dataContext = e.getDataContext();

		final com.intellij.openapi.project.Project project = DataKeys.PROJECT.getData(_dataContext);
		final Presentation presentation = e.getPresentation();

		// check a project is loaded
		if (isProjectLoaded(project, presentation)) {
			Messages.showWarningDialog("Project not loaded.", getPluginInterface(project).getInternalToolWindowId());  // NON-NLS
			return;
		}

		final FindBugsPreferences preferences = getPluginInterface(project).getPreferences();
		if(preferences.getBugCategories().containsValue("true") && preferences.getDetectors().containsValue("true")) {  // NON-NLS
			initWorker();
		} else {
			FindBugsPluginImpl.showToolWindowNotifier("No bug categories or bug pattern detectors selected. analysis aborted.", MessageType.WARNING);  // NON-NLS
			ShowSettingsUtil.getInstance().editConfigurable(project, IdeaUtilImpl.getPluginComponent(project));
		}
	}


	@Override
	public void update(final AnActionEvent event) {
		try {
			_actionEvent = event;
			_dataContext = event.getDataContext();
			final com.intellij.openapi.project.Project project = DataKeys.PROJECT.getData(_dataContext);
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

			final VirtualFile[] selectedSourceFiles = IdeaUtilImpl.getVirtualFiles(_dataContext);

			if (!_running) {
				_enabled = (selectedSourceFiles != null) && (selectedSourceFiles.length > 0) && selectedSourceFiles[0].isValid() && IdeaUtilImpl.isValidFileType(selectedSourceFiles[0].getFileType());
			}

			presentation.setEnabled(toolWindow.isAvailable() && isEnabled());
			presentation.setVisible(true);

		} catch (Throwable e) {
			final FindBugsPluginException processed = FindBugsPluginImpl.processError("Action update failed", e);// NON-NLS
			if (processed != null) {
				LOGGER.error("Action update failed", processed);
			}
		}
	}


	private void initWorker() {

		//final UserPreferences userPrefs = UserPreferences.createDefaultUserPreferences();
		//userPrefs.enableAllDetectors(true);

		final com.intellij.openapi.project.Project project = IdeaUtilImpl.getProject(_dataContext);
		final Module module = IdeaUtilImpl.getModule(_dataContext);

		final FindBugsPreferences preferences = getPluginInterface(project).getPreferences();
		if(Boolean.valueOf(preferences.getProperty(FindBugsPreferences.TOOLWINDOW_TOFRONT))) {
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
		worker.work();

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
			FindBugsPlugin.processError("Worker interrupted.", e);// NON-NLS
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			FindBugsPlugin.processError("Error performing FindBugs analysis", e);// NON-NLS
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
				setEnabled(true);
				setRunning(false);
				break;
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
