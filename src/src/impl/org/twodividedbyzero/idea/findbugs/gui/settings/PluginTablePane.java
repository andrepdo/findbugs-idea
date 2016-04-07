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
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.table.JBTable;
import edu.umd.cs.findbugs.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.GuiUtil;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.plugins.AbstractPluginLoader;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

final class PluginTablePane extends JPanel implements SettingsOwner<ProjectSettings> {
	private JBTable table;
	private JScrollPane scrollPane;
	private String lastPluginError;

	PluginTablePane() {
		super(new BorderLayout());
		setBorder(GuiUtil.createTitledBorder(ResourcesLoader.getString("plugins.title")));
		table = new JBTable();
		table = GuiUtil.createCheckboxTable(
				new Model(New.<Item>arrayList()),
				Model.IS_ENABLED_COLUMN,
				new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						swapEnabled();
					}
				}
		);
		//table.setCellSelectionEnabled(false);
		//table.setRowSelectionAllowed(false);
		table.getColumnModel().getColumn(Model.NAME_COLUMN).setCellRenderer(new TableCellRenderer() {
			private PluginPane pane;
			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
				if (pane == null) {
					pane = new PluginPane();
				}
				pane.load(((Item) value).plugin);
				return pane;
			}
		});

		scrollPane = ScrollPaneFactory.createScrollPane(table);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane);
	}

	@NotNull
	private Model getModel() {
		return (Model) table.getModel();
	}

	@Nullable
	private String loadRows() {
		lastPluginError = null;
		getModel().rows.clear();
		final PluginLoaderImpl pluginLoader = new PluginLoaderImpl();
		pluginLoader.load( // TODO
				Collections.<String>emptyList(),
				Collections.<String>emptyList(),
				Collections.<String>emptyList(),
				Collections.<String>emptyList()
		);
		lastPluginError = pluginLoader.makeErrorMessage();
		return lastPluginError;
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
		scrollPane.setEnabled(enabled);
	}

	@Override
	public boolean isModified(@NotNull final ProjectSettings settings) {
		return false; // TODO
	}

	@Override
	public void apply(@NotNull final ProjectSettings settings) throws ConfigurationException {
		if (lastPluginError != null) {
			throw new ConfigurationException(lastPluginError); // TODO: set title ?
		}
	}

	@Override
	public void reset(@NotNull final ProjectSettings settings) {
		loadRows();
	}

	private class Item {
		@NotNull
		private final PluginInfo plugin;
		private boolean enabled;

		private Item(@NotNull final PluginInfo plugin) {
			this.plugin = plugin;
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
					return Component.class;
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
					return rows.get(rowIndex);
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

	private class PluginLoaderImpl extends AbstractPluginLoader {

		private PluginLoaderImpl() {
			super(true);
		}

		@Override
		protected void seenCorePlugin(@NotNull final Plugin plugin) {
			seenPlugin(plugin, false);
		}

		@Override
		protected void seenBundledPlugin(@NotNull final Plugin plugin) {
			seenPlugin(plugin, false);
		}

		@Override
		protected void seenUserPlugin(@NotNull final Plugin plugin) {
			seenPlugin(plugin, true);
		}

		private void seenPlugin(@NotNull final Plugin plugin, final boolean userPlugin) {
			getModel().rows.add(new Item(PluginInfo.create(plugin)));
		}
	}
}
