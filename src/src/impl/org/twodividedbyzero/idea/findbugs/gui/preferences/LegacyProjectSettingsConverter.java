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
package org.twodividedbyzero.idea.findbugs.gui.preferences;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.core.ModuleSettings;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.core.WorkspaceSettings;
import org.twodividedbyzero.idea.findbugs.preferences.PersistencePreferencesBean;

import java.util.List;

public final class LegacyProjectSettingsConverter extends AbstractProjectComponent {
	public LegacyProjectSettingsConverter(@NotNull final Project project) {
		super(project);
	}

	@Override
	public void projectOpened() {

		final LegacyProjectSettings legacy = LegacyProjectSettings.getInstance(myProject);
		if (legacy == null) {
			return;
		}
		final PersistencePreferencesBean legacyBean = legacy.getState();
		if (legacyBean == null) {
			return;
		}

		final WorkspaceSettings currentWorkspace = WorkspaceSettings.getInstance(myProject);

		final List<String> enabledModuleConfigs = legacyBean.getEnabledModuleConfigs();

		if (enabledModuleConfigs != null && !enabledModuleConfigs.isEmpty()) {
			// first convert module settings if necessary
			for (final Module module : ModuleManager.getInstance(myProject).getModules()) {
				final LegacyModuleSettings legacyModuleSettings = LegacyModuleSettings.getInstance(module);
				if (legacyModuleSettings != null) {
					final ModuleSettings currentModule = ModuleSettings.getInstance(module);
					if (enabledModuleConfigs.contains(module.getName())) {
						legacyModuleSettings.applyTo(currentModule, currentWorkspace);
						currentModule.overrideProjectSettings = true;
					}
				}
			}
		}

		// convert project- after module-settings if necessary
		final ProjectSettings current = ProjectSettings.getInstance(myProject);
		legacy.applyTo(current, currentWorkspace);

	}
}
