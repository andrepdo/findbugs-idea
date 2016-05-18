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
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.core.WorkspaceSettings;

public final class LegacyProjectSettingsConverter extends AbstractProjectComponent {
	public LegacyProjectSettingsConverter(@NotNull final Project project) {
		super(project);
	}

	@Override
	public void projectOpened() {
		final LegacyProjectSettings legacy = LegacyProjectSettings.getInstance(myProject);
		final ProjectSettings current = ProjectSettings.getInstance(myProject);
		final WorkspaceSettings currentWorkspace = WorkspaceSettings.getInstance(myProject);
		if (legacy != null) {
			legacy.applyTo(current, currentWorkspace);
		}
	}
}
