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
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.core.WorkspaceSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JComponent;

abstract class AbstractConfigurableImpl<V extends AbstractSettings> implements Configurable {

	static final String DISPLAY_NAME = ResourcesLoader.getString("findbugs.plugin.configuration.name");

	@NotNull
	final Project project;

	@NotNull
	final V settings;

	@NotNull
	private final WorkspaceSettings workspaceSettings;

	SettingsPane pane;

	AbstractConfigurableImpl(@NotNull final Project project, @NotNull final V settings) {
		this.project = project;
		this.settings = settings;
		workspaceSettings = WorkspaceSettings.getInstance(project);
	}

	@Nls
	@Override
	public final String getDisplayName() {
		return DISPLAY_NAME;
	}

	@Nullable
	@Override
	public final String getHelpTopic() {
		return null;
	}

	@Nullable
	@Override
	public final JComponent createComponent() {
		if (pane == null) {
			pane = createSettingsPane();
		}
		return pane;
	}

	@NotNull
	abstract SettingsPane createSettingsPane();

	@Override
	public boolean isModified() {
		return pane.isModified(settings) || pane.isModifiedWorkspace(workspaceSettings);
	}

	@Override
	public void apply() throws ConfigurationException {
		pane.apply(settings);
		pane.applyWorkspace(workspaceSettings);
	}

	@Override
	public void reset() {
		pane.reset(settings);
		pane.resetWorkspace(workspaceSettings);
	}

	@Override
	public final void disposeUIResources() {
		if (pane != null) {
			Disposer.dispose(pane);
			pane = null;
		}
	}

	private void requestFocusOnShareImportFile() {
		createComponent();
		pane.requestFocusOnShareImportFile();
	}

	private void showFileFilterAndAddRFilerFilter() {
		createComponent();
		pane.showFileFilterAndAddRFilerFilter();
	}

	static void showImpl(@NotNull final Project project, @NotNull final AbstractConfigurableImpl configurable, @Nullable final Runnable advancedInitialization) {
		ShowSettingsUtil.getInstance().editConfigurable(project, configurable, advancedInitialization);
	}

	static void showShareImpl(@NotNull final Project project, @NotNull final AbstractConfigurableImpl configurable) {
		showImpl(project, configurable, new Runnable() {
			@Override
			public void run() {
				configurable.requestFocusOnShareImportFile();
			}
		});
	}

	static void showFileFilterAndAddRFilerFilterImpl(@NotNull final Project project, @NotNull final AbstractConfigurableImpl configurable) {
		showImpl(project, configurable, new Runnable() {
			@Override
			public void run() {
				configurable.showFileFilterAndAddRFilerFilter();
			}
		});
	}
}
