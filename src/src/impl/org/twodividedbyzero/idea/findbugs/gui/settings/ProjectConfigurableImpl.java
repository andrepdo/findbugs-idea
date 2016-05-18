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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.core.WorkspaceSettings;

import javax.swing.JComponent;

public final class ProjectConfigurableImpl implements SearchableConfigurable, Configurable.NoScroll {
	public static final String ID = "settings.findbugs.project";
	public static final String DISPLAY_NAME = "FindBugs-IDEA";

	@NotNull
	private final Project project;

	@NotNull
	private final ProjectSettings settings;

	@NotNull
	private final WorkspaceSettings workspaceSettings;

	private SettingsPane pane;

	public ProjectConfigurableImpl(@NotNull final Project project) {
		this.project = project;
		settings = ProjectSettings.getInstance(project);
		workspaceSettings = WorkspaceSettings.getInstance(project);
	}

	@Nls
	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	@Nullable
	@Override
	public String getHelpTopic() {
		return null;
	}

	@Nullable
	@Override
	public JComponent createComponent() {
		if (pane == null) {
			pane = new SettingsPane(project) {
				@NotNull
				@Override
				GeneralTab createGeneralTab() {
					return new GeneralTab();
				}

				@Nullable
				@Override
				AnnotateTab createAnnotateTab() {
					return new AnnotateTab(project);
				}

				@Nullable
				@Override
				ShareTab createShareTab() {
					return new ShareTab();
				}

				@NotNull
				@Override
				AbstractSettings createSettings() {
					return new ProjectSettings();
				}
			};
		}
		return pane;
	}

	@Override
	public boolean isModified() {
		return pane.isModified(settings) ||
				pane.isModifiedProject(settings) ||
				pane.isModifiedWorkspace(workspaceSettings);
	}

	@Override
	public void apply() throws ConfigurationException {
		pane.apply(settings);
		pane.applyProject(settings);
		pane.applyWorkspace(workspaceSettings);
	}

	@Override
	public void reset() {
		pane.reset(settings);
		pane.resetProject(settings);
		pane.resetWorkspace(workspaceSettings);
	}

	@Override
	public void disposeUIResources() {
		if (pane != null) {
			Disposer.dispose(pane);
			pane = null;
		}
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
}
