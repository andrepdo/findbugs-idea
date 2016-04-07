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
import edu.umd.cs.findbugs.config.ProjectFilterSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;

final class MinPriorityPane extends JPanel implements SettingsOwner<AbstractSettings> {
	private JLabel label;
	private ComboBox comboBox;

	MinPriorityPane(final int indent) {
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		label = new JLabel(ResourcesLoader.getString("minPriority.text"));
		label.setToolTipText(ResourcesLoader.getString("minPriority.description"));
		label.setPreferredSize(new JBDimension(indent, label.getPreferredSize().height));
		comboBox = new ComboBox(new Object[]{
				ProjectFilterSettings.HIGH_PRIORITY,
				ProjectFilterSettings.MEDIUM_PRIORITY,
				ProjectFilterSettings.LOW_PRIORITY
		});
		add(label);
		add(comboBox);
	}

	@NotNull
	private String getValue() {
		return (String) comboBox.getSelectedItem();
	}

	private void setValue(@Nullable final String value) {
		comboBox.setSelectedItem(value);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		label.setEnabled(enabled);
		comboBox.setEnabled(enabled);
	}

	@Override
	public boolean isModified(@NotNull final AbstractSettings settings) {
		return !getValue().equalsIgnoreCase(settings.minPriority);
	}

	@Override
	public void apply(@NotNull final AbstractSettings settings) throws ConfigurationException {
		settings.minPriority = getValue();
	}

	@Override
	public void reset(@NotNull final AbstractSettings settings) {
		setValue(settings.minPriority);
	}
}
