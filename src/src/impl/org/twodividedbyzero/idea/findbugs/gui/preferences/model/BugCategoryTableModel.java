/*
 * Copyright 2008-2013 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.gui.preferences.model;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.90-dev
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"SE_TRANSIENT_FIELD_NOT_RESTORED"})
public class BugCategoryTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 0L;

	private final transient List<BugCategory> _entries;


	public BugCategoryTableModel() {
		_entries = new ArrayList<BugCategory>();
	}


	public int getRowCount() {
		return _entries.size();
	}


	public int getColumnCount() {
		return 3;
	}


	public void add(final String name, final String description, final boolean enabled) {
		//noinspection ThrowableResultOfMethodCallIgnored
		final BugCategory entry = new BugCategory(name, description, enabled);
		add(entry);
	}


	public void add(final BugCategory entry) {
		_entries.add(entry);
		final int index = indexOf(entry);
		fireTableRowsInserted(index, index);
	}


	public void remove(final BugCategory entry) {
		final int index = indexOf(entry);
		_entries.remove(entry);
		fireTableRowsDeleted(index, index);
	}


	public void remove(final Collection<BugCategory> entries) {
		if (!entries.isEmpty()) {
			_entries.removeAll(entries);
			fireTableDataChanged();
		}
	}


	public void remove(final int[] rows) {
		final Collection<BugCategory> result = new ArrayList<BugCategory>();
		for (final int row : rows) {
			result.add(_entries.get(row));
		}
		_entries.removeAll(result);
		fireTableRowsDeleted(rows[0], rows[rows.length - 1]);
	}


	public void remove(final int row) {
		_entries.remove(row);
		fireTableRowsDeleted(row, row);
	}


	public int getEntryRow(final BugCategory logEntry) {
		return _entries.indexOf(logEntry);
	}


	public int[] getCheckedRows() {
		final int[] result = new int[_entries.size()];
		for (int i = 0; i < _entries.size(); i++) {
			final BugCategory entry = _entries.get(i);
			final boolean selected = entry.isEnabled();
			if (selected) {
				result[i] = i;
			}
		}
		return result;
	}


	public Collection<BugCategory> getCheckedEntries() {
		final List<BugCategory> result = new ArrayList<BugCategory>();
		for (final BugCategory entry : _entries) {
			final boolean selected = entry.isEnabled();
			if (selected) {
				result.add(entry);
			}
		}
		return result;
	}


	public BugCategory getNewestEntry() {
		return _entries.get(_entries.size() - 1);
	}


	public void removeCheckedRows() {
		remove(getCheckedRows());
	}


	public void removeCheckedEntries() {
		remove(getCheckedEntries());
	}


	public void clear() {
		if (!_entries.isEmpty()) {
			_entries.clear();
			fireTableRowsDeleted(0, 0);
		}
	}


	private int indexOf(final BugCategory entry) {
		return _entries.indexOf(entry);
	}


	public Object getValueAt(final int rowIndex, final int columnIndex) {
		switch (columnIndex) {
			case 0:
				return _entries.get(rowIndex).isEnabled();
			case 1:
				return _entries.get(rowIndex).getName();
			case 2:
				return _entries.get(rowIndex).getDescription();
			default:
				throw new IllegalArgumentException("Not supported columnIndex: " + columnIndex);
		}
	}


	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
		switch (columnIndex) {
			case 0:
				_entries.get(rowIndex).setEnabled((Boolean) aValue);
				fireTableCellUpdated(rowIndex, columnIndex);
				break;
			default: // ignore
		}
	}


	@Override
	public Class<?> getColumnClass(final int col) {
		return col == 0 ? Boolean.class : String.class;
	}


	@Override
	public String getColumnName(final int col) {
		return "";
	}


	@Override
	public boolean isCellEditable(final int rowIndex, final int colIndex) {
		return colIndex == 0;
	}


	public boolean isEmpty() {
		return _entries.isEmpty();
	}


	public int size() {
		return _entries.size();
	}


	public void selectAll(final boolean select) {
		for (final BugCategory entry : _entries) {
			entry.setEnabled(select);
			fireTableCellUpdated(indexOf(entry), 0);
		}
	}


	public List<BugCategory> getEntries() {
		return Collections.unmodifiableList(_entries);
	}


	public static class BugCategory {

		private final String _name;
		private final String _description;
		private boolean _enabled;


		public BugCategory(final String name, final String description, final boolean enabled) {
			_name = name;
			_description = description;
			_enabled = enabled;
		}


		public String getName() {
			return _name;
		}


		public String getDescription() {
			return _description;
		}


		public boolean isEnabled() {
			return _enabled;
		}


		public void setEnabled(final boolean enabled) {
			_enabled = enabled;
		}
	}
}
