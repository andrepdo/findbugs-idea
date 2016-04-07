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

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.core.WorkspaceSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JComponent;

public final class ProjectConfigurableImpl implements Configurable, Configurable.NoScroll {

	@NotNull
	private final ProjectSettings settings;

	@NotNull
	private final WorkspaceSettings workspaceSettings;

	private ProjectSettingsPane pane;

	public ProjectConfigurableImpl(@NotNull final Project project) {
		settings = ProjectSettings.getInstance(project);
		workspaceSettings = WorkspaceSettings.getInstance(project);
	}

	@Nls
	@Override
	public String getDisplayName() {
		return ResourcesLoader.getString("findbugs.plugin.configuration.name");
	}

	@Nullable
	@Override
	public String getHelpTopic() {
		return FindBugsPluginConstants.FINDBUGS_EXTERNAL_HELP_URI;
	}

	@Nullable
	@Override
	public JComponent createComponent() {
		if (pane == null) {
			pane = new ProjectSettingsPane();
		}
		return pane;
	}

	@Override
	public boolean isModified() {
		return pane.isModified(settings, workspaceSettings);
	}

	@Override
	public void apply() throws ConfigurationException {
		pane.apply(settings, workspaceSettings);
	}

	@Override
	public void reset() {
		pane.reset(settings, workspaceSettings);
	}

	@Override
	public void disposeUIResources() {
		if (pane != null) {
			Disposer.dispose(pane);
			pane = null;
		}
	}
}
