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

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.gui.common.HAlignment;
import org.twodividedbyzero.idea.findbugs.gui.common.VAlignment;
import org.twodividedbyzero.idea.findbugs.gui.common.VerticalFlowLayout;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JPanel;
import java.awt.BorderLayout;

final class GeneralTab extends JPanel implements SettingsOwner<AbstractSettings> {
	private JBCheckBox compileBeforeAnalyze;
	private JBCheckBox analyzeAfterCompile;
	private JBCheckBox analyzeAfterAutoMake;
	private JBCheckBox runInBackground;
	private JBCheckBox toolWindowToFront;
	private PluginTablePane plugin;

	GeneralTab(final boolean addPlugin) {
		super(new BorderLayout());
		compileBeforeAnalyze = new JBCheckBox(ResourcesLoader.getString("general.compileBeforeAnalyze.title"));
		analyzeAfterCompile = new JBCheckBox(ResourcesLoader.getString("general.analyzeAfterCompile.title"));
		analyzeAfterAutoMake = new JBCheckBox(ResourcesLoader.getString("general.analyzeAfterAutoMake.title"));
		runInBackground = new JBCheckBox(ResourcesLoader.getString("general.runInBackground.title"));
		toolWindowToFront = new JBCheckBox(ResourcesLoader.getString("general.toolWindowToFront.title"));
		if (addPlugin) {
			plugin = new PluginTablePane();
		}

		final JPanel topPane = new JPanel(new VerticalFlowLayout(HAlignment.Left, VAlignment.Top, 0, UIUtil.DEFAULT_VGAP, false, false));
		topPane.add(compileBeforeAnalyze);
		topPane.add(analyzeAfterCompile);
		topPane.add(analyzeAfterAutoMake);
		topPane.add(runInBackground);
		topPane.add(toolWindowToFront);

		add(topPane, BorderLayout.NORTH);
		if (plugin != null) {
			add(plugin);
		}
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		compileBeforeAnalyze.setEnabled(enabled);
		analyzeAfterCompile.setEnabled(enabled);
		analyzeAfterAutoMake.setEnabled(enabled);
		runInBackground.setEnabled(enabled);
		toolWindowToFront.setEnabled(enabled);
		if (plugin != null) {
			plugin.setEnabled(enabled);
		}
	}

	@Override
	public boolean isModified(@NotNull final AbstractSettings settings) {
		return compileBeforeAnalyze.isSelected() != settings.compileBeforeAnalyze ||
				analyzeAfterCompile.isSelected() != settings.analyzeAfterCompile ||
				analyzeAfterAutoMake.isSelected() != settings.analyzeAfterAutoMake ||
				runInBackground.isSelected() != settings.runInBackground ||
				toolWindowToFront.isSelected() != settings.toolWindowToFront ||
				(settings instanceof ProjectSettings && plugin != null && plugin.isModified((ProjectSettings) settings));
	}

	@Override
	public void apply(@NotNull final AbstractSettings settings) throws ConfigurationException {
		settings.compileBeforeAnalyze = compileBeforeAnalyze.isSelected();
		settings.analyzeAfterCompile = analyzeAfterCompile.isSelected();
		settings.analyzeAfterAutoMake = analyzeAfterAutoMake.isSelected();
		settings.runInBackground = runInBackground.isSelected();
		settings.toolWindowToFront = toolWindowToFront.isSelected();
		if (settings instanceof ProjectSettings && plugin != null) {
			plugin.apply((ProjectSettings) settings);
		}
	}

	@Override
	public void reset(@NotNull final AbstractSettings settings) {
		compileBeforeAnalyze.setSelected(settings.compileBeforeAnalyze);
		analyzeAfterCompile.setSelected(settings.analyzeAfterCompile);
		analyzeAfterAutoMake.setSelected(settings.analyzeAfterAutoMake);
		runInBackground.setSelected(settings.runInBackground);
		toolWindowToFront.setSelected(settings.toolWindowToFront);
		if (settings instanceof ProjectSettings && plugin != null) {
			plugin.reset((ProjectSettings) settings);
		}
	}

	@Nullable
	PluginTablePane getPluginTablePane() {
		return plugin;
	}

	@NotNull
	static String getSearchPath() {
		return ResourcesLoader.getString("settings.general");
	}

	@NotNull
	static String[] getSearchResourceKey() {
		return new String[]{
				"general.compileBeforeAnalyze.title",
				"general.analyzeAfterCompile.title",
				"general.analyzeAfterAutoMake.title",
				"general.runInBackground.title",
				"general.toolWindowToFront.title"
		};
	}
}
