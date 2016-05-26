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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.core.FindBugsState;
import org.twodividedbyzero.idea.findbugs.core.ModuleSettings;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.ToolWindowPanel;

abstract class AbstractToggleAction extends ToggleAction {

	@Override
	public final void update(@NotNull AnActionEvent e) {
		final Project project = IdeaUtilImpl.getProject(e.getDataContext());
		if (project == null || !project.isInitialized() || !project.isOpen()) {
			e.getPresentation().setEnabled(false);
			e.getPresentation().setVisible(false);
			return;
		}
		final ToolWindow toolWindow = ToolWindowPanel.getWindow(project);
		if (toolWindow == null || !toolWindow.isAvailable()) {
			e.getPresentation().setEnabled(false);
			e.getPresentation().setVisible(false);
			return;
		}
		final ToolWindowPanel panel = ToolWindowPanel.getInstance(toolWindow);
		if (panel == null) {
			e.getPresentation().setEnabled(false);
			e.getPresentation().setVisible(false);
			return;
		}
		final Module module = IdeaUtilImpl.getModule(e.getDataContext(), project);
		final ProjectSettings projectSettings = ProjectSettings.getInstance(project);
		AbstractSettings settings = projectSettings;
		if (module != null) {
			final ModuleSettings moduleSettings = ModuleSettings.getInstance(module);
			if (moduleSettings.overrideProjectSettings) {
				settings = moduleSettings;
			}
		}
		final boolean select = isSelectedImpl(
				e,
				project,
				module,
				toolWindow,
				panel,
				FindBugsState.get(project),
				projectSettings,
				settings
		);
		final Boolean selected = select ? Boolean.TRUE : Boolean.FALSE;
		e.getPresentation().putClientProperty(SELECTED_PROPERTY, selected);
		e.getPresentation().setEnabled(true);
		e.getPresentation().setVisible(true);
	}

	@Override
	public final boolean isSelected(@NotNull AnActionEvent e) {
		final Project project = IdeaUtilImpl.getProject(e.getDataContext());
		if (project == null || !project.isInitialized() || !project.isOpen()) {
			e.getPresentation().setEnabled(false);
			e.getPresentation().setVisible(false);
			return false;
		}
		final ToolWindow toolWindow = ToolWindowPanel.getWindow(project);
		if (toolWindow == null || !toolWindow.isAvailable()) {
			e.getPresentation().setEnabled(false);
			e.getPresentation().setVisible(false);
			return false;
		}
		final ToolWindowPanel panel = ToolWindowPanel.getInstance(toolWindow);
		if (panel == null) {
			e.getPresentation().setEnabled(false);
			e.getPresentation().setVisible(false);
			return false;
		}
		final Module module = IdeaUtilImpl.getModule(e.getDataContext(), project);
		final ProjectSettings projectSettings = ProjectSettings.getInstance(project);
		AbstractSettings settings = projectSettings;
		if (module != null) {
			final ModuleSettings moduleSettings = ModuleSettings.getInstance(module);
			if (moduleSettings.overrideProjectSettings) {
				settings = moduleSettings;
			}
		}
		return isSelectedImpl(
				e,
				project,
				module,
				toolWindow,
				panel,
				FindBugsState.get(project),
				projectSettings,
				settings
		);
	}

	abstract boolean isSelectedImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@Nullable final Module module,
			@NotNull final ToolWindow toolWindow,
			@NotNull final ToolWindowPanel panel,
			@NotNull final FindBugsState state,
			@NotNull final ProjectSettings projectSettings,
			@NotNull final AbstractSettings settings
	);

	@Override
	public final void setSelected(@NotNull AnActionEvent e, boolean select) {
		final Project project = IdeaUtilImpl.getProject(e.getDataContext());
		if (project == null || !project.isInitialized() || !project.isOpen()) {
			e.getPresentation().setEnabled(false);
			e.getPresentation().setVisible(false);
			return;
		}
		final ToolWindow toolWindow = ToolWindowPanel.getWindow(project);
		if (toolWindow == null || !toolWindow.isAvailable()) {
			e.getPresentation().setEnabled(false);
			e.getPresentation().setVisible(false);
			return;
		}
		final ToolWindowPanel panel = ToolWindowPanel.getInstance(toolWindow);
		if (panel == null) {
			e.getPresentation().setEnabled(false);
			e.getPresentation().setVisible(false);
			return;
		}
		final Module module = IdeaUtilImpl.getModule(e.getDataContext(), project);
		final ProjectSettings projectSettings = ProjectSettings.getInstance(project);
		AbstractSettings settings = projectSettings;
		if (module != null) {
			final ModuleSettings moduleSettings = ModuleSettings.getInstance(module);
			if (moduleSettings.overrideProjectSettings) {
				settings = moduleSettings;
			}
		}
		setSelectedImpl(
				e,
				project,
				module,
				toolWindow,
				panel,
				FindBugsState.get(project),
				projectSettings,
				settings,
				select
		);
	}

	abstract void setSelectedImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@Nullable final Module module,
			@NotNull final ToolWindow toolWindow,
			@NotNull final ToolWindowPanel panel,
			@NotNull final FindBugsState state,
			@NotNull final ProjectSettings projectSettings,
			@NotNull final AbstractSettings settings,
			boolean select
	);
}
