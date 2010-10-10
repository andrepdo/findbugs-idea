package org.twodividedbyzero.idea.findbugs.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
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
public class AnalyzeProjectFiles extends BaseAction implements EventListener<BugReporterEvent> {

	private static final Logger LOGGER = Logger.getInstance(AnalyzeProjectFiles.class.getName());

	private DataContext _dataContext;
	private AnActionEvent _actionEvent;
	private boolean _enabled;
	private boolean _running;


	@Override
	public void actionPerformed(final AnActionEvent e) {
		_actionEvent = e;
		_dataContext = e.getDataContext();

		final com.intellij.openapi.project.Project project = DataKeys.PROJECT.getData(_dataContext);
		final Presentation presentation = e.getPresentation();

		// check a project is loaded
		if (isProjectLoaded(project, presentation)) {
			Messages.showWarningDialog("Project not loaded.", "FindBugs");
			return;
		}

		final FindBugsPreferences preferences = getPluginInterface(project).getPreferences();
		if (preferences.getBugCategories().containsValue("true") && preferences.getDetectors().containsValue("true")) {
			initWorker();
		} else {
			FindBugsPluginImpl.showToolWindowNotifier("No bug categories or bug pattern detectors selected. analysis aborted.", MessageType.WARNING);
			ShowSettingsUtil.getInstance().editConfigurable(project, IdeaUtilImpl.getPluginComponent(project));
		}
	}


	@Override
	public void update(final AnActionEvent event) {
		try {
			_actionEvent = event;
			_dataContext = event.getDataContext();
			final Project project = DataKeys.PROJECT.getData(_dataContext);
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

			// enable ?
			if (!_running) {
				_enabled = project != null && project.isInitialized() && project.isInitialized();
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


	private void initWorker() {
		final com.intellij.openapi.project.Project project = IdeaUtilImpl.getProject(_dataContext);

		final FindBugsPreferences preferences = getPluginInterface(project).getPreferences();
		if (Boolean.valueOf(preferences.getProperty(FindBugsPreferences.TOOLWINDOW_TO_FRONT))) {
			IdeaUtilImpl.activateToolWindow(getPluginInterface(project).getInternalToolWindowId(), _dataContext);
		}

		final FindBugsWorker worker = new FindBugsWorker(project);

		// set aux classpath
		final VirtualFile[] files = IdeaUtilImpl.getProjectClasspath(_dataContext);
		worker.configureAuxClasspathEntries(files);

		// set source dirs
		final VirtualFile[] sourceRoots = IdeaUtilImpl.getModulesSourceRoots(_dataContext);
		worker.configureSourceDirectories(sourceRoots);

		// set class files
		final String[] outPath = IdeaUtilImpl.getCompilerOutputUrls(project);
		worker.configureOutputFiles(outPath);
		worker.work();

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


	protected boolean setRunning(final boolean running) {
		final boolean was = _running;
		if (_running != running) {
			_running = running;
		}
		return was;
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


	public void onEvent(@NotNull final BugReporterEvent event) {
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
	}
}
