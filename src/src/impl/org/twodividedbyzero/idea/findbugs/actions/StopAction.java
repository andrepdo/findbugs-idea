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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.common.event.EventListener;
import org.twodividedbyzero.idea.findbugs.common.event.EventManagerImpl;
import org.twodividedbyzero.idea.findbugs.common.event.filters.BugReporterEventFilter;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent.Operation;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEventImpl;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;


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
	private AnActionEvent _actionEvent;
	private DataContext _dataContext;


	public StopAction() {
		//EventManagerImpl.getInstance().addEventListener(new BugReporterEventFilter(IdeaUtilImpl.getProject().getName()), this);
	}


	@Override
	public void actionPerformed(final AnActionEvent e) {
		_actionEvent = e;
		_dataContext = e.getDataContext();
		final com.intellij.openapi.project.Project project = DataKeys.PROJECT.getData(_dataContext);
		if (project != null) {
			EventManagerImpl.getInstance().fireEvent(new BugReporterEventImpl(Operation.ANALYSIS_ABORTED, project.getName()));
		}
	}


	@Override
	public void update(final AnActionEvent event) {
		try {
			final DataContext dataContext = event.getDataContext();
			final Project project = DataKeys.PROJECT.getData(dataContext);
			final Presentation presentation = event.getPresentation();

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
			presentation.setEnabled(toolWindow.isAvailable() && isEnabled());
			presentation.setVisible(true);

		} catch (Throwable e) {
			final FindBugsPluginException processed = FindBugsPluginImpl.processError("Action update failed", e);// NON-NLS
			if (processed != null) {
				LOGGER.error("Action update failed", processed);
			}
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
				setEnabled(false);
				break;
			case ANALYSIS_FINISHED:
				setEnabled(false);
				break;
			case NEW_BUG_INSTANCE:
				break;
		}
	}
}
