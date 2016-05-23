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

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.ModuleSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

final class ModuleSettingsPane extends SettingsPane {

	private JBCheckBox overrideProjectSettingsCheckbox;

	ModuleSettingsPane(@NotNull Project project) {
		super(project);
	}

	@NotNull
	@Override
	JComponent createHeaderPane() {
		overrideProjectSettingsCheckbox = new JBCheckBox(ResourcesLoader.getString("settings.module.overrideProjectSettings"));
		overrideProjectSettingsCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				updateControls();
			}
		});

		final JPanel topPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		topPane.add(overrideProjectSettingsCheckbox);
		topPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		return topPane;
	}

	private void updateControls() {
		setProjectSettingsEnabled(overrideProjectSettingsCheckbox.isSelected());
	}

	@Override
	boolean isModifiedModule(@NotNull final ModuleSettings settings) {
		return settings.overrideProjectSettings != overrideProjectSettingsCheckbox.isSelected();
	}

	@Override
	void applyModule(@NotNull final ModuleSettings settings) {
		settings.overrideProjectSettings = overrideProjectSettingsCheckbox.isSelected();
		updateControls();
	}

	@Override
	void resetModule(@NotNull final ModuleSettings settings) {
		overrideProjectSettingsCheckbox.setSelected(settings.overrideProjectSettings);
		updateControls();
	}

	@Nullable
	@Override
	ShareTab createShareTab() {
		return null;
	}
}
