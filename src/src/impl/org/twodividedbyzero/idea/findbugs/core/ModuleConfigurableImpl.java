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
package org.twodividedbyzero.idea.findbugs.core;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.FlowLayout;

public final class ModuleConfigurableImpl implements Configurable {

	@NotNull
	private final Module module;
	private final ModuleSettings settings;
	private JPanel panel;
	private JCheckBox overrideProjectSettings;
	private JCheckBox compileBeforeAnalyse;

	public ModuleConfigurableImpl( @NotNull final Module module) {
		this.module = module;
		settings = ModuleSettings.getInstance(module);
		System.out.println();
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
		if (panel == null) {
			overrideProjectSettings = new JCheckBox("Override Project Settings");
			compileBeforeAnalyse = new JCheckBox("Compile before analyze");
			panel = new JPanel(new FlowLayout());
			panel.add(overrideProjectSettings);
			panel.add(compileBeforeAnalyse);
		}
		return panel;
	}

	@Override
	public boolean isModified() {
		return settings.overrideProjectSettings != overrideProjectSettings.isSelected() ||
				settings.compileBeforeAnalyze != compileBeforeAnalyse.isSelected();
	}

	@Override
	public void apply() throws ConfigurationException {
		settings.overrideProjectSettings = overrideProjectSettings.isSelected();
		settings.compileBeforeAnalyze = compileBeforeAnalyse.isSelected();
	}

	@Override
	public void reset() {
		overrideProjectSettings.setSelected(settings.overrideProjectSettings);
		compileBeforeAnalyse.setSelected(settings.compileBeforeAnalyze);
	}

	@Override
	public void disposeUIResources() {
		panel = null;
		overrideProjectSettings = null;
		compileBeforeAnalyse = null;
	}
}
