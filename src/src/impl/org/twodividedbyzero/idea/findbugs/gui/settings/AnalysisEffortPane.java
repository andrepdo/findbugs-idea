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
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBDimension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.preferences.AnalysisEffort;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.FlowLayout;

final class AnalysisEffortPane extends JPanel implements SettingsOwner<AbstractSettings> {
	private JLabel label;
	private ComboBox comboBox;

	AnalysisEffortPane(final int indent) {
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		label = new JLabel(ResourcesLoader.getString("effort.text"));
		label.setToolTipText(ResourcesLoader.getString("effort.description"));
		label.setPreferredSize(new JBDimension(indent, label.getPreferredSize().height));
		comboBox = new ComboBox(AnalysisEffort.values());
		add(label);
		add(comboBox);
	}

	@NotNull
	private AnalysisEffort getValue() {
		return (AnalysisEffort) comboBox.getSelectedItem();
	}

	private void setValue(@Nullable AnalysisEffort effort) {
		if (effort == null) {
			effort = AnalysisEffort.DEFAULT;
		}
		comboBox.setSelectedItem(effort);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		label.setEnabled(enabled);
		comboBox.setEnabled(enabled);
	}

	@Override
	public boolean isModified(@NotNull final AbstractSettings settings) {
		return !getValue().equals(AnalysisEffort.of(settings.analysisEffort));
	}

	@Override
	public void apply(@NotNull final AbstractSettings settings) throws ConfigurationException {
		settings.analysisEffort = getValue().getEffortLevel();
	}

	@Override
	public void reset(@NotNull final AbstractSettings settings) {
		setValue(AnalysisEffort.of(settings.analysisEffort));
	}
}
