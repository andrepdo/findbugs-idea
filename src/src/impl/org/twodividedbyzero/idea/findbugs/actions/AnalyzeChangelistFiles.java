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
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListAdapter;
import com.intellij.openapi.vcs.changes.ChangeListManager;
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

import java.util.Collection;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.90-dev
 */
public class AnalyzeChangelistFiles extends BaseAction implements EventListener<BugReporterEvent> {

	private static final Logger LOGGER = Logger.getInstance(AnalyzeSelectedFiles.class.getName());

	private DataContext _dataContext;
	private AnActionEvent _actionEvent;
	private boolean _enabled;
	private boolean _running;
	private ChangeList _activeChangeList;
	private Project _project;
	private ChangeListAdapter _changelistAdapter;


	@Override
	public void actionPerformed(final AnActionEvent e) {
		_actionEvent = e;
		_dataContext = e.getDataContext();
		//final Presentation presentation = e.getPresentation();

		final com.intellij.openapi.project.Project project = DataKeys.PROJECT.getData(_dataContext);
		assert project != null;
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
			FindBugsPluginImpl.showToolWindowNotifier(project, "No bug categories or bug pattern detectors selected. analysis aborted.", MessageType.WARNING);
			ShowSettingsUtil.getInstance().editConfigurable(project, IdeaUtilImpl.getPluginComponent(project));
		}
	}


	@Override
	public void update(final AnActionEvent event) {
		try {
			_actionEvent = event;
			_dataContext = event.getDataContext();
			_project = DataKeys.PROJECT.getData(_dataContext);
			final Presentation presentation = event.getPresentation();

			// check a project is loaded
			if (isProjectLoaded(_project, presentation)) {
				return;
			}

			isPluginAccessible(_project);

			// check if tool window is registered
			final ToolWindow toolWindow = isToolWindowRegistred(_project);
			if (toolWindow == null) {
				presentation.setEnabled(false);
				presentation.setVisible(false);

				return;
			}

			registerEventListener(_project);

			// enable ?
			final List<VirtualFile> modifiedFiles = IdeaUtilImpl.getAllModifiedFiles(_dataContext);
			if (!_running && modifiedFiles != null && !modifiedFiles.isEmpty()) {
				for (final VirtualFile virtualFile : modifiedFiles) {
					if (IdeaUtilImpl.isValidFileType(virtualFile.getFileType())) {
						_enabled = true;
						break;
					} else {
						_enabled = false;
					}
				}
			} else {
				_enabled = false;
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
		//final VirtualFile[] files1 = IdeaUtilImpl.getSelectedFiles(_dataContext);
		final Collection<VirtualFile> modifiedFiles = IdeaUtilImpl.getModifiedFilesByList(getActiveChangeList(), _dataContext);
		if (modifiedFiles != null) {
			final VirtualFile[] selectedSourceFiles = modifiedFiles.toArray(new VirtualFile[modifiedFiles.size()]);
			worker.configureSourceDirectories(selectedSourceFiles);

			// set class files
			worker.configureOutputFiles(selectedSourceFiles);
			worker.work();
		}
	}


	private void registerEventListener(final Project project) {
		final String projectName = project.getName();
		if (!isRegistered(projectName)) {
			EventManagerImpl.getInstance().addEventListener(new BugReporterEventFilter(projectName), this);
			addRegisteredProject(projectName);
		}
	}


	public ChangeListAdapter getChangelistAdapter() {
		if(_changelistAdapter == null) {
			_changelistAdapter =  new MyChangeListAdapter();
		}

		return _changelistAdapter;
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


	public ChangeList getActiveChangeList() {
		return _activeChangeList == null ? ChangeListManager.getInstance(_project).getDefaultChangeList() : _activeChangeList;
	}


	private class MyChangeListAdapter extends ChangeListAdapter {

		@Override
		public void defaultListChanged(final ChangeList oldDefaultList, final ChangeList newDefaultList) {
			changeListChanged(newDefaultList);
		}


		@Override
		public void changeListChanged(final ChangeList list) {
			_activeChangeList = list;
		}


		@Override
		public void changeListAdded(final ChangeList list) {
			changeListChanged(list);
		}


		@Override
		public void changeListRemoved(final ChangeList list) {
			changeListChanged(list);
		}


		@Override
		public void changeListRenamed(final ChangeList list, final String oldName) {
			changeListChanged(list);
		}
	}
}

