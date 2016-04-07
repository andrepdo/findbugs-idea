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
import edu.umd.cs.findbugs.BugRankCategory;
import edu.umd.cs.findbugs.BugRanker;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;

final class MinRankPane extends JPanel implements SettingsOwner<AbstractSettings> {
	private JLabel label;
	private ComboBox comboBox;

	MinRankPane(final int indent) {
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		label = new JLabel(ResourcesLoader.getString("minRank.text"));
		label.setToolTipText(ResourcesLoader.getString("minRank.description"));
		label.setPreferredSize(new JBDimension(indent, label.getPreferredSize().height));

		final DefaultComboBoxModel model = new DefaultComboBoxModel();
		for (int minRank = BugRanker.VISIBLE_RANK_MIN; minRank <= BugRanker.VISIBLE_RANK_MAX; minRank++) {
			final BugRankCategory category = BugRankCategory.getRank(minRank);
			model.addElement(new Item(minRank, minRank + " - " + category));
		}
		comboBox = new ComboBox(model);

		add(label);
		add(comboBox);
	}

	private int getValue() {
		return ((Item) comboBox.getSelectedItem()).minRank;
	}

	private void setValue(final int minRank) {
		final DefaultComboBoxModel model = (DefaultComboBoxModel) comboBox.getModel();
		for (int i = 0; i < model.getSize(); i++) {
			final Item item = (Item) model.getElementAt(i);
			if (minRank == item.minRank) {
				comboBox.setSelectedItem(item);
				return;
			}
		}
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		label.setEnabled(enabled);
		comboBox.setEnabled(enabled);
	}

	@Override
	public boolean isModified(@NotNull final AbstractSettings settings) {
		return getValue() != settings.minRank;
	}

	@Override
	public void apply(@NotNull final AbstractSettings settings) throws ConfigurationException {
		settings.minRank = getValue();
	}

	@Override
	public void reset(@NotNull final AbstractSettings settings) {
		setValue(settings.minRank);
	}

	private static class Item {
		private final int minRank;
		@NotNull
		private final String text;

		private Item(final int minRank, @NotNull final String text) {
			this.minRank = minRank;
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}
}
