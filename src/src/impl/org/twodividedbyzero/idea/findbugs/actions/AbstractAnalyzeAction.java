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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsState;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.core.WorkspaceSettings;
import org.twodividedbyzero.idea.findbugs.gui.settings.ProjectConfigurableImpl;

abstract class AbstractAnalyzeAction extends AbstractAction {

	@Override
	final void actionPerformedImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@Nullable final Module module,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state,
			@NotNull final ProjectSettings projectSettings,
			@NotNull final AbstractSettings settings
	) {

		EventDispatchThreadHelper.checkEDT();

		final String importFilePath = WorkspaceSettings.getInstance(project).importFilePath;
		if (!StringUtil.isEmptyOrSpaces(importFilePath)) {
			//AnalyzeUtil.importPreferences(plugin, importFilePath); // TODO
		}

		if (areAllBugCategoriesDisabled(settings)) {
			FindBugsPluginImpl.showToolWindowNotifier(project, "All bug categories are disabled. Analysis aborted.", MessageType.WARNING);
			ShowSettingsUtil.getInstance().showSettingsDialog(project, ProjectConfigurableImpl.DISPLAY_NAME);
			return;
		}

		if (areAllDetectorsDisabled(settings)) {
			FindBugsPluginImpl.showToolWindowNotifier(project, "All detectors are disabled. Analysis aborted.", MessageType.WARNING);
			ShowSettingsUtil.getInstance().showSettingsDialog(project, ProjectConfigurableImpl.DISPLAY_NAME);
		}

		analyze(
				e,
				project,
				module,
				toolWindow,
				state,
				projectSettings,
				settings
		);
	}

	abstract void analyze(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@Nullable final Module module,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state,
			@NotNull final ProjectSettings projectSettings,
			@NotNull final AbstractSettings settings
	);

	private static boolean areAllBugCategoriesDisabled(@NotNull final AbstractSettings settings) {
		return false; // TODO
	}

	private static boolean areAllDetectorsDisabled(@NotNull final AbstractSettings settings) {
		return false; // TODO
	}
}
