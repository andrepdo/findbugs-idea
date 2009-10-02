/*
 * Copyright 2009 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.twodividedbyzero.idea.findbugs.gui.preferences.model;

import javax.swing.table.AbstractTableModel;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.90-dev
 */
public class BugCategoryTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 0L;

	private final Object _guardedLock = new Object();
	private final List<BugCategory> _entries;


	public BugCategoryTableModel() {
		_entries = Collections.synchronizedList(new LinkedList<BugCategory>());
	}


	public int getRowCount() {
		return _entries.size();
	}


	public int getColumnCount() {
		return 3;
	}


	public void add(final String name, final String description, final boolean enabled) {
		synchronized (_guardedLock) {
			//noinspection ThrowableResultOfMethodCallIgnored
			final BugCategory entry = new BugCategory(name, description, enabled);
			add(entry);
		}
	}


	public void add(final BugCategory entry) {
		synchronized (_guardedLock) {
			_entries.add(entry);
			final int index = indexOf(entry);
			fireTableRowsInserted(index, index);
		}
	}


	@Override
	public void fireTableRowsInserted(final int firstRow, final int lastRow) {
		if (EventQueue.isDispatchThread()) {
			super.fireTableRowsInserted(firstRow, lastRow);
		} else {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					fireTableRowsInserted(firstRow, lastRow);
				}
			});
		}
	}


	@Override
	public void fireTableRowsDeleted(final int firstRow, final int lastRow) {
		if (EventQueue.isDispatchThread()) {
			super.fireTableRowsDeleted(firstRow, lastRow);
		} else {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					fireTableRowsDeleted(firstRow, lastRow);
				}
			});
		}
	}


	public void remove(final BugCategory entry) {
		synchronized (_guardedLock) {
			final int index = indexOf(entry);
			_entries.remove(entry);
			fireTableRowsDeleted(index, index);
		}
	}


	public void remove(final Collection<BugCategory> entries) {
		synchronized (_guardedLock) {
			if (!entries.isEmpty()) {
				_entries.removeAll(entries);
				//fireTableStructureChanged();
				fireTableDataChanged();
			}
		}
	}


	public void remove(final int[] rows) {
		final Collection<BugCategory> result = new ArrayList<BugCategory>();
		synchronized (_guardedLock) {
			for (final int row : rows) {
				result.add(_entries.get(row));
			}
			_entries.removeAll(result);
			fireTableRowsDeleted(rows[0], rows[rows.length - 1]);
		}
	}


	public void remove(final int row) {
		synchronized (_guardedLock) {
			_entries.remove(row);
			fireTableRowsDeleted(row, row);
		}
	}


	public int getEntryRow(final BugCategory logEntry) {
		synchronized (_guardedLock) {
			return _entries.indexOf(logEntry);
		}
	}


	public int[] getCheckedRows() {
		synchronized (_guardedLock) {
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
	}


	public List<BugCategory> getCheckedEntries() {
		synchronized (_guardedLock) {
			final List<BugCategory> result = new ArrayList<BugCategory>();
			for (final BugCategory entry : _entries) {
				final boolean selected = entry.isEnabled();
				if (selected) {
					result.add(entry);
				}
			}
			return result;
		}
	}


	public BugCategory getNewestEntry() {
		synchronized (_guardedLock) {
			return _entries.get(_entries.size() - 1);
		}
	}


	public void removeCheckedRows() {
		remove(getCheckedRows());
	}


	public void removeCheckedEntries() {
		remove(getCheckedEntries());
	}


	public void clear() {
		synchronized (_guardedLock) {
			if (!_entries.isEmpty()) {
				_entries.clear();
				fireTableRowsDeleted(0, 0);
			}
		}
	}


	private int indexOf(final BugCategory entry) {
		return _entries.indexOf(entry);
	}


	public Object getValueAt(final int rowIndex, final int columnIndex) {
		synchronized (_guardedLock) {
			switch (columnIndex) {
				case 0:
					return _entries.get(rowIndex).isEnabled();
				case 1:
					return _entries.get(rowIndex).getName();
				case 2:
					return _entries.get(rowIndex).getDescription();
				default:
					return "???";
			}
		}
	}


	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
		synchronized (_guardedLock) {
			switch (columnIndex) {
				case 0:
					_entries.get(rowIndex).setEnabled((Boolean) aValue);
					fireTableCellUpdated(rowIndex, columnIndex);
					break;
			}
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
		//fireTableDataChanged();
		//fireTableStructureChanged();
	}


	public List<BugCategory> getEntries() {
		return Collections.unmodifiableList(_entries);
	}


	public static class BugCategory {

		private String _name;
		private String _description;
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
