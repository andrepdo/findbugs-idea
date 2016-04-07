/*
 * Copyright 2016 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.gui.settings;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

final class AdvancedSettingsAction extends DefaultActionGroup {

	AdvancedSettingsAction() {
		super("Advanced Settings", true);
		getTemplatePresentation().setIcon(AllIcons.General.GearPlain);
		add(new ResetToDefault());
		add(new ImportSettings());
		add(new ExportSettings());
	}

	private class ResetToDefault extends AbstractAction {
		ResetToDefault() {
			super(
					"Reset To Default",
					"Set all settings to default",
					AllIcons.Actions.Reset_to_default
			);
		}

		@Override
		public void actionPerformed(final AnActionEvent e) {
			// TODO
		}
	}

	private class ImportSettings extends AbstractAction {
		ImportSettings() {
			super(
					"Import",
					"Import Settings from file",
					AllIcons.ToolbarDecorator.Import
			);
		}

		@Override
		public void actionPerformed(final AnActionEvent e) {
			// TODO
		}
	}

	private class ExportSettings extends AbstractAction {
		ExportSettings() {
			super(
					"Export",
					"Export Settings to file",
					AllIcons.ToolbarDecorator.Export
			);
		}

		@Override
		public void actionPerformed(final AnActionEvent e) {
			// TODO
		}
	}

	private abstract class AbstractAction extends AnAction implements DumbAware {
		AbstractAction(@Nullable final String text, @Nullable final String description, @Nullable final Icon icon) {
			super(text, description, icon);
		}

		@Override
		public boolean isDumbAware() {
			return true;
		}
	}
}
