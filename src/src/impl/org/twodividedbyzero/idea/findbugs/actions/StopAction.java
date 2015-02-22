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
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.event.EventListener;
import org.twodividedbyzero.idea.findbugs.common.event.EventManagerImpl;
import org.twodividedbyzero.idea.findbugs.common.event.filters.BugReporterEventFilter;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEventFactory;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.messages.MessageBusManager;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class StopAction extends BaseAction implements EventListener<BugReporterEvent> {

	private static final Logger LOGGER = Logger.getInstance(StopAction.class.getName());

	private boolean _enabled;


	@Override
	public void actionPerformed(@NotNull final AnActionEvent e) {
		final com.intellij.openapi.project.Project project = DataKeys.PROJECT.getData(e.getDataContext());
		if (project != null) {
			MessageBusManager.publishAnalysisAborted(project);
			EventManagerImpl.getInstance().fireEvent(BugReporterEventFactory.newAborted(project));
		} else {
			LOGGER.error("No active project");
		}
	}


	@Override
	public void update(final AnActionEvent event) {
		try {
			final DataContext dataContext = event.getDataContext();
			final Project project = DataKeys.PROJECT.getData(dataContext);
			final Presentation presentation = event.getPresentation();

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

			// enable ?
			presentation.setEnabled(toolWindow.isAvailable() && isEnabled());
			presentation.setVisible(true);

		} catch (final Throwable e) {
			final FindBugsPluginException processed = FindBugsPluginImpl.processError("Action update failed", e);
			LOGGER.error("Action update failed", processed);
		}
	}


	private void registerEventListener(final Project project) {
		final String projectName = project.getName();
		if (!isRegistered(projectName)) {
			EventManagerImpl.getInstance().addEventListener(new BugReporterEventFilter(projectName), this);
			addRegisteredProject(projectName);
		}
	}


	@Override
	public boolean setEnabled(final boolean enabled) {
		final boolean was = _enabled;
		if (_enabled != enabled) {
			_enabled = enabled;
		}
		return was;
	}


	@Override
	protected boolean isEnabled() {
		return _enabled;
	}


	public void onEvent(@NotNull final BugReporterEvent event) {
		switch (event.getOperation()) {
			case ANALYSIS_STARTED:
				setEnabled(true);
				break;
			case ANALYSIS_ABORTED:
			case ANALYSIS_FINISHED:
				setEnabled(false);
				break;
		}
	}
}
