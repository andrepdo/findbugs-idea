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
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.table.JBTable;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.I18N;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.GuiUtil;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Code based on Debugger Stepping Filter:
 * {@link com.intellij.debugger.settings.DebuggerSteppingConfigurable}
 * {@link com.intellij.ui.classFilter.ClassFilterEditor}
 * Renderer based on InjectionSettingsUI (IntelliLang Plugin)
 */
final class BugCategoryPane extends JPanel implements SettingsOwner<AbstractSettings> {
	private JBTable table;

	BugCategoryPane() {
		super(new BorderLayout());
		setBorder(GuiUtil.createTitledBorder(ResourcesLoader.getString("bugCategory.title")));
		table = GuiUtil.createCheckboxTable(
				new Model(New.<Item>arrayList()),
				Model.IS_ENABLED_COLUMN,
				new ActionListener() {
					@Override
					public void actionPerformed(@NotNull final ActionEvent e) {
						swapEnabled();
					}
				}
		);
		table.getColumnModel().getColumn(Model.NAME_COLUMN).setCellRenderer(new TableCellRenderer() {
			final SimpleColoredComponent label = new SimpleColoredComponent();
			final SimpleColoredText text = new SimpleColoredText();

			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
				final Item item = getModel().rows.get(row);
				label.clear();
				text.append(item.description, SimpleTextAttributes.REGULAR_ATTRIBUTES);
				text.append(" (" + item.category + ")", isSelected ? SimpleTextAttributes.REGULAR_ATTRIBUTES : SimpleTextAttributes.GRAY_ATTRIBUTES);
				text.appendToComponent(label);
				text.clear();
				label.setOpaque(true);
				label.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
				label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
				return label;
			}
		});
		add(table);
	}

	@NotNull
	private Model getModel() {
		return (Model) table.getModel();
	}

	private void swapEnabled() {
		final int rows[] = table.getSelectedRows();
		for (final int row : rows) {
			getModel().rows.get(row).enabled = !getModel().rows.get(row).enabled;
		}
		getModel().fireTableDataChanged();
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		table.setEnabled(enabled);
	}

	@Override
	public boolean isModified(@NotNull final AbstractSettings settings) {
		final Model model = getModel();
		for (final Item item : model.rows) {
			if (item.enabled) {
				if (settings.hiddenBugCategory.contains(item.category)) {
					return true;
				}
			} else {
				if (!settings.hiddenBugCategory.contains(item.category)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void apply(@NotNull final AbstractSettings settings) throws ConfigurationException {
		final Model model = getModel();
		for (final Item item : model.rows) {
			if (item.enabled) {
				settings.hiddenBugCategory.remove(item.category);
			} else {
				settings.hiddenBugCategory.add(item.category);
			}
		}

	}

	@Override
	public void reset(@NotNull final AbstractSettings settings) {
		final List<Item> rows = getModel().rows;
		rows.clear();
		for (final String category : DetectorFactoryCollection.instance().getBugCategories()) {
			rows.add(new Item(
					category,
					I18N.instance().getBugCategoryDescription(category),
					!settings.hiddenBugCategory.contains(category) // see comment in ProjectFilterSettings#containsCategory(String)
			));
		}
		getModel().fireTableDataChanged();
	}

	boolean isEnabled(@NotNull final String category) {
		final Model model = getModel();
		for (final Item item : model.rows) {
			if (item.category.equals(category)) {
				return item.enabled;
			}
		}
		return false;
	}

	private class Item {
		@NotNull
		private final String category;
		@NotNull
		private final String description;
		private boolean enabled;

		private Item(@NotNull final String category, @NotNull final String description, final boolean enabled) {
			this.category = category;
			this.description = description;
			this.enabled = enabled;
		}
	}

	private class Model extends AbstractTableModel {
		private static final int IS_ENABLED_COLUMN = 0;
		private static final int NAME_COLUMN = 1;
		@NotNull
		private final List<Item> rows;

		private Model(@NotNull final List<Item> rows) {
			this.rows = rows;
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return columnIndex == IS_ENABLED_COLUMN;
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			switch (columnIndex) {
				case IS_ENABLED_COLUMN:
					return Boolean.class;
				case NAME_COLUMN:
					return String.class;
				default:
					throw new IllegalArgumentException("Column " + columnIndex);
			}
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case IS_ENABLED_COLUMN:
					return rows.get(rowIndex).enabled;
				case NAME_COLUMN:
					return rows.get(rowIndex).description;
				default:
					throw new IllegalArgumentException("Column " + columnIndex);
			}
		}

		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
			if (columnIndex == IS_ENABLED_COLUMN) {
				rows.get(rowIndex).enabled = (Boolean) aValue;
			}
		}
	}
}
