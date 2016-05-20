/*
 * Copyright 2008-2016 Andre Pfeiler
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
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.core.FindBugsState;

import java.awt.Component;
import java.awt.event.InputEvent;

public final class GroupByFilter extends AbstractAction {

	@Override
	void updateImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		e.getPresentation().setEnabled(state.isIdle());
		e.getPresentation().setVisible(true);
	}

	@Override
	void actionPerformedImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

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
