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

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.event.EventListener;
import org.twodividedbyzero.idea.findbugs.common.event.EventManagerImpl;
import org.twodividedbyzero.idea.findbugs.common.event.filters.BugReporterEventFilter;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;

import java.awt.Component;
import java.awt.event.InputEvent;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class GroupByFilter extends BaseAction implements EventListener<BugReporterEvent>/*, CustomComponentAction*/ {

	private static final Logger LOGGER = Logger.getInstance(GroupByFilter.class.getName());

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
			Messages.showWarningDialog("Project not loaded.", "FindBugs");
			return;
		}


		/*_actionEvent.getPresentation().putClientProperty("button", _actionEvent.getPresentation());
		final JComponent jcomponent = (JComponent) presentation.getClientProperty("button");
		final DefaultActionGroup defaultactiongroup = filterApplierGroup();
		final ActionPopupMenu actionpopupmenu = ActionManager.getInstance().createActionPopupMenu("FindBugs.ToolBarActions.right", defaultactiongroup);
		actionpopupmenu.getComponent().show(jcomponent, jcomponent.getWidth(), 0);*/

		final InputEvent inputEvent = e.getInputEvent();
		final Component component = inputEvent.getComponent();
		final DefaultActionGroup defaultactiongroup = filterApplierGroup();
		final ActionPopupMenu actionpopupmenu = ActionManager.getInstance().createActionPopupMenu("FindBugs.GroupByFilter.PopupGroup", defaultactiongroup);
		//defaultactiongroup.add(ActionManager.getInstance().getAction("FindBugs.GroupByFilter"));
		//ActionManager.getInstance().getAction("FindBugs.GroupByFilter").getTemplatePresentation().
		//actionpopupmenu.getComponent().setVisible(true);
		actionpopupmenu.getComponent().show(component, component.getWidth(), 0);

	}


	/*public JComponent createCustomComponent(final Presentation presentation) {
		*//*final ActionButton actionbutton = new ActionButtonPresentation(true, "TodoViewToolbar", false);
		presentation.putClientProperty("button", actionbutton);

		return actionbutton;*//*

		final DefaultActionGroup defaultactiongroup = filterApplierGroup();
		final ActionPopupMenu actionpopupmenu = ActionManager.getInstance().createActionPopupMenu("FindBugs.ToolBarActions.right", defaultactiongroup);
		return actionpopupmenu.getComponent();
	}*/


	private DefaultActionGroup filterApplierGroup() {
		final DefaultActionGroup group = new DefaultActionGroup("FindBugs.GroupByFilter.PopupGroup", true);
		group.add(new FilterApplyAction());
		return group;
	}


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

			_enabled = !isRunning();

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


	private static class FilterApplyAction extends AnAction {

		private FilterApplyAction() {
			super("test1", "test description", IconLoader.getIcon("/general/ideOptions.png"));
		}


		@Override
		public void actionPerformed(final AnActionEvent anactionevent) {
			//ShowSettingsUtil.getInstance().editConfigurable(myProject, TodoConfigurable.getInstance());
		}

		//final MySetTodoFilterAction this$1;


		//{
			// this$1 = MySetTodoFilterAction.this;
			//super(s, s1, icon);
		//}
	}
}
