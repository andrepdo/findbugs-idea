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

import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.I18N;
import org.twodividedbyzero.idea.findbugs.gui.preferences.DetectorConfiguration;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

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
@edu.umd.cs.findbugs.annotations.SuppressWarnings({"SE_TRANSIENT_FIELD_NOT_RESTORED"})
public class BugPatternTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 0L;

	private final Object _guardedLock = new Object();
	private final transient List<DetectorFactory> _entries;
	private final FindBugsPreferences _preferences;


	public BugPatternTableModel(final FindBugsPreferences preferences) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_preferences = preferences;
		_entries = Collections.synchronizedList(new LinkedList<DetectorFactory>());
	}


	public int getRowCount() {
		return _entries.size();
	}


	public int getColumnCount() {
		return 5;
	}


	public void add(final DetectorFactory entry) {
		synchronized (_guardedLock) {
			if (entry != null) {
				_entries.add(entry);
				final int index = indexOf(entry);
				fireTableRowsInserted(index, index);
			}
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


	public void enableCategory(final String categoryString, final boolean enable) {
		synchronized (_guardedLock) {
			for (final DetectorFactory factory : _entries) {
				if (DetectorConfiguration.getBugsCategories(factory).contains(I18N.instance().getBugCategoryDescription(categoryString))) {
					_preferences.getUserPreferences().enableDetector(factory, enable);
					setValueAt(enable, indexOf(factory), 0);
				}
			}
			fireTableDataChanged();
		}
	}


	public void remove(final DetectorFactory entry) {
		synchronized (_guardedLock) {
			final int index = indexOf(entry);
			_entries.remove(entry);
			fireTableRowsDeleted(index, index);
		}
	}


	public void remove(final Collection<DetectorFactory> entries) {
		synchronized (_guardedLock) {
			if (!entries.isEmpty()) {
				_entries.removeAll(entries);
				//fireTableStructureChanged();
				fireTableDataChanged();
			}
		}
	}


	public void remove(final int[] rows) {
		final Collection<DetectorFactory> result = new ArrayList<DetectorFactory>();
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


	public int getEntryRow(final DetectorFactory logEntry) {
		synchronized (_guardedLock) {
			return _entries.indexOf(logEntry);
		}
	}


	public int[] getCheckedRows() {
		synchronized (_guardedLock) {
			final int[] result = new int[_entries.size()];
			for (int i = 0; i < _entries.size(); i++) {
				final DetectorFactory entry = _entries.get(i);
				final boolean selected = _preferences.getUserPreferences().isDetectorEnabled(entry);
				if (selected) {
					result[i] = i;
				}
			}
			return result;
		}
	}


	public List<DetectorFactory> getCheckedEntries() {
		synchronized (_guardedLock) {
			final List<DetectorFactory> result = new ArrayList<DetectorFactory>();
			for (final DetectorFactory entry : _entries) {
				final boolean selected = _preferences.getUserPreferences().isDetectorEnabled(entry);
				if (selected) {
					result.add(entry);
				}
			}
			return result;
		}
	}


	public DetectorFactory getNewestEntry() {
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


	private int indexOf(final DetectorFactory entry) {
		return _entries.indexOf(entry);
	}


	public Object getValueAt(final int rowIndex, final int columnIndex) {
		synchronized (_guardedLock) {
			switch (columnIndex) {
				case 0:
					//return _entries.get(rowIndex).isReportingDetector();
					return _preferences.getUserPreferences().isDetectorEnabled(_entries.get(rowIndex));
				case 1:
					return _entries.get(rowIndex).getShortName();
				case 2:
					return DetectorConfiguration.createBugsAbbreviation(_entries.get(rowIndex));
				case 3:
					return _entries.get(rowIndex).getSpeed();
				case 4:
					return DetectorConfiguration.getBugsCategories(_entries.get(rowIndex));
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
					_preferences.getUserPreferences().enableDetector(_entries.get(rowIndex), (Boolean) aValue);
					//DetectorConfiguration.isDetectorEnabled(_preferences, _entries.get(rowIndex));
					//_entries.get(rowIndex).set
					fireTableCellUpdated(rowIndex, columnIndex);
					break;
				default:
			}
		}
	}


	@Override
	public Class<?> getColumnClass(final int col) {
		return col == 0 ? Boolean.class : String.class;
	}


	@Override
	public String getColumnName(final int col) {
		switch (col) {
			case 1:
				return "Detector id";
			case 2:
				return "Pattern(s)";
			case 3:
				return "Speed";
			case 4:
				return "Category";
			default:
				return "";
		}
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
		for (final DetectorFactory entry : _entries) {
			//entry.setEnabled(select);
			_preferences.getUserPreferences().enableDetector(entry, select);
			fireTableCellUpdated(indexOf(entry), 0);
		}
		//fireTableDataChanged();
		//fireTableStructureChanged();
	}


	public List<DetectorFactory> getEntries() {
		return Collections.unmodifiableList(_entries);
	}


}

