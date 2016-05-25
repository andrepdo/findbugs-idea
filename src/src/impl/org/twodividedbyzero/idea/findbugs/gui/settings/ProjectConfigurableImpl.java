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
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;

public final class ProjectConfigurableImpl extends AbstractConfigurableImpl<ProjectSettings> implements SearchableConfigurable, Configurable.NoScroll {
	static final String ID = "settings.findbugs.project";

	public ProjectConfigurableImpl(@NotNull final Project project) {
		super(project, ProjectSettings.getInstance(project));
	}

	@NotNull
	@Override
	SettingsPane createSettingsPane() {
		return new ProjectSettingsPane(project);
	}

	@Override
	public boolean isModified() {
		return super.isModified() || pane.isModifiedProject(settings);
	}

	@Override
	public void apply() throws ConfigurationException {
		super.apply();
		pane.applyProject(settings);
	}

	@Override
	public void reset() {
		super.reset();
		pane.resetProject(settings);
	}

	@NotNull
	@Override
	public String getId() {
		return ID;
	}

	@Nullable
	@Override
	public Runnable enableSearch(final String option) {
		return new Runnable() {
			@Override
			public void run() {
				if (pane != null) {
					pane.setFilter(option);
				}
			}
		};
	}

	public static void show(@NotNull final Project project) {
		ShowSettingsUtil.getInstance().showSettingsDialog(project, ProjectConfigurableImpl.DISPLAY_NAME);
	}

	public static void showShare(@NotNull final Project project) {
		/**
		 * It is correct to create a configurable instance,
		 * see java doc of ShowSettingsUtil#findProjectConfigurable (deprecated).
		 */
		showShareImpl(project, new ProjectConfigurableImpl(project));
	}

	public static void showFileFilterAndAddRFilerFilter(@NotNull final Project project) {
		showFileFilterAndAddRFilerFilterImpl(project, new ProjectConfigurableImpl(project));
	}
}
