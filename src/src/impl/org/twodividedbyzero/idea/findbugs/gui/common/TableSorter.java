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
package org.twodividedbyzero.idea.findbugs.gui.common;


import com.intellij.ui.JBColor;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * TableSorter is a decorator for TableModels; adding sorting
 * functionality to a supplied TableModel. TableSorter does
 * not store or copy the data in its TableModel; instead it maintains
 * a map from the row indexes of the view to the row indexes of the
 * model. As requests are made of the sorter (like getValueAt(row, col))
 * they are passed to the underlying model after the row numbers
 * have been translated via the internal mapping array. This way,
 * the TableSorter appears to hold another copy of the table
 * with the rows in a different order.
 * <p/>
 * TableSorter registers itself as a listener to the underlying model,
 * just as the JTable itself would. Events recieved from the model
 * are examined, sometimes manipulated (typically widened), and then
 * passed on to the TableSorter's listeners (typically the JTable).
 * If a change to the model has invalidated the order of TableSorter's
 * rows, a note of this is made and the sorter will resort the
 * rows the next time a value is requested.
 * <p/>
 * When the tableHeader property is set, either by using the
 * setTableHeader() method or the two argument constructor, the
 * table header may be used as a complete UI for TableSorter.
 * The default renderer of the tableHeader is decorated with a renderer
 * that indicates the sorting status of each column. In addition,
 * a mouse listener is installed with the following behavior:
 * <ul>
 * <li>
 * Mouse-click: Clears the sorting status of all other columns
 * and advances the sorting status of that column through three
 * values: {NOT_SORTED, ASCENDING, DESCENDING} (then back to
 * NOT_SORTED again).
 * <li>
 * SHIFT-mouse-click: Clears the sorting status of all other columns
 * and cycles the sorting status of the column through the same
 * three values, in the opposite order: {NOT_SORTED, DESCENDING, ASCENDING}.
 * <li>
 * CONTROL-mouse-click and CONTROL-SHIFT-mouse-click: as above except
 * that the changes to the column do not cancel the statuses of columns
 * that are already sorting - giving a way to initiate a compound
 * sort.
 * </ul>
 * <p/>
 * This is a long overdue rewrite of a class of the same name that
 * first appeared in the swing table demos in 1997.
 *
 * @author Philip Milne
 * @author Brendon McLean
 * @author Dan van Enckevort
 * @author Parwinder Sekhon
 * @version 2.0 02/27/04
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"SE_TRANSIENT_FIELD_NOT_RESTORED", "SE_BAD_FIELD", "SE_BAD_FIELD"})
public class TableSorter extends AbstractTableModel {

	private static final long serialVersionUID = 0L;

	public static final int DESCENDING = -1;
	public static final int NOT_SORTED = 0;
	public static final int ASCENDING = 1;

	private static final Directive EMPTY_DIRECTIVE = new Directive(-1, NOT_SORTED);

	public static final Comparator<?> COMPARABLE_COMAPRATOR;
	public static final Comparator<?> LEXICAL_COMPARATOR;

	private TableModel _tableModel;
	private transient Row[] _viewToModel;
	private int[] _modelToView;
	private JTableHeader _tableHeader;
	private final transient MouseListener _mouseListener;
	private final transient TableModelListener _tableModelListener;
	private final Collection<JTableHeader> _registredTableHeaders = new ArrayList<JTableHeader>();
	private final Map<Class<?>, Comparator<?>> _columnComparators = new HashMap<Class<?>, Comparator<?>>();
	private final List<Directive> _sortingColumns = new ArrayList<Directive>();
	private boolean _disableNotSorted;


	static {
		COMPARABLE_COMAPRATOR = new Comparator() {
			@SuppressWarnings({"unchecked"})
			public int compare(final Object o1, final Object o2) {
				return ((Comparable<Object>) o1).compareTo(o2);
			}
		};
		LEXICAL_COMPARATOR = new Comparator() {
			private final Collator col = Collator.getInstance();


			{
				col.setStrength(Collator.SECONDARY);
			}


			public int compare(final Object o1, final Object o2) {
				return col.compare(o1.toString(), o2.toString());
			}
		};
	}


	public static TableSorter makeSortable(final JTable jTable) {
		final TableSorter result = new TableSorter(jTable.getModel(), jTable.getTableHeader());
		jTable.setModel(result);
		return result;
	}


	public TableSorter() {
		_mouseListener = new MouseHandler();
		_tableModelListener = new TableModelHandler();
	}


	public TableSorter(final TableModel tableModel) {
		this();
		setTableModel(tableModel);
	}


	public TableSorter(final TableModel tableModel, final JTableHeader tableHeader) {
		this();
		setTableHeader(tableHeader);
		setTableModel(tableModel);
	}


	private void clearSortingState() {
		_viewToModel = null;
		_modelToView = null;
	}


	public TableModel getTableModel() {
		return _tableModel;
	}


	public final void setTableModel(final TableModel tableModel) {
		if (_tableModel != null) {
			_tableModel.removeTableModelListener(_tableModelListener);
		}

		_tableModel = tableModel;
		if (_tableModel != null) {
			_tableModel.addTableModelListener(_tableModelListener);
		}

		clearSortingState();
		fireTableStructureChanged();
	}


	public JTableHeader getTableHeader() {
		return _tableHeader;
	}


	/**
	 * For more than one table use this method to register the
	 * table header's.
	 *
	 * @param newTableHeader to register
	 */
	public void addTableHeader(final JTableHeader newTableHeader) {
		if (!_registredTableHeaders.contains(newTableHeader)) {
			if (_tableHeader == null) {
				_tableHeader = newTableHeader;
			}
			newTableHeader.addMouseListener(_mouseListener);
			newTableHeader.setDefaultRenderer(new SortableHeaderRenderer(newTableHeader.getDefaultRenderer()));
			_registredTableHeaders.add(newTableHeader);
		}
	}


	public final void setTableHeader(final JTableHeader tableHeader) {
		if (_tableHeader != null) {
			_tableHeader.removeMouseListener(_mouseListener);
			final TableCellRenderer defaultRenderer = _tableHeader.getDefaultRenderer();
			if (defaultRenderer instanceof SortableHeaderRenderer) {
				_tableHeader.setDefaultRenderer(((SortableHeaderRenderer) defaultRenderer)._tableCellRenderer);
			}
		}
		_tableHeader = tableHeader;
		if (_tableHeader != null) {
			_tableHeader.addMouseListener(_mouseListener);
			_tableHeader.setDefaultRenderer(new SortableHeaderRenderer(_tableHeader.getDefaultRenderer()));
		}
	}


	public boolean isSorting() {
		return !_sortingColumns.isEmpty();
	}


	private Directive getDirective(final int column) {
		for (final Directive directive : _sortingColumns) {
			if (directive._column == column) {
				return directive;
			}
		}
		return EMPTY_DIRECTIVE;
	}


	public int getSortingStatus(final int column) {
		return getDirective(column)._direction;
	}


	public void sortingStatusChanged() {
		_viewToModel = calculateNewSorting();
		_modelToView = null;
		fireTableDataChanged();
		if (_tableHeader != null) {
			_tableHeader.repaint();
		}
		for (final JTableHeader registredTableHeader : _registredTableHeaders) {
			registredTableHeader.repaint();
		}
	}


	public void setSortingStatus(final int column, final int status) {
		final Directive directive = getDirective(column);
		if (directive != EMPTY_DIRECTIVE) {
			_sortingColumns.remove(directive);
		}
		if (status != NOT_SORTED) {
			_sortingColumns.add(new Directive(column, status));
		}
		sortingStatusChanged();
	}


	protected Icon getHeaderRendererIcon(final int column, final int size) {
		final Directive directive = getDirective(column);
		if (directive == EMPTY_DIRECTIVE) {
			return null;
		}
		return new Arrow(directive._direction == DESCENDING, size, _sortingColumns.indexOf(directive));
	}


	private void cancelSorting() {
		_sortingColumns.clear();
		_viewToModel = calculateNewSorting();
		_modelToView = null;
	}


	public void setColumnComparator(final Class type, final Comparator comparator) {
		if (comparator == null) {
			_columnComparators.remove(type);
		} else {
			_columnComparators.put(type, comparator);
		}
	}


	protected Comparator getComparator(final int column) {
		final Class<?> columnType = _tableModel.getColumnClass(column);
		final Comparator comparator = _columnComparators.get(columnType);
		if (comparator != null) {
			return comparator;
		}
		if (Comparable.class.isAssignableFrom(columnType)) {
			return COMPARABLE_COMAPRATOR;
		}
		return LEXICAL_COMPARATOR;
	}


	private Row[] getViewToModel() {
		if (_viewToModel == null) {
			_viewToModel = calculateNewSorting();
		}
		return _viewToModel;
	}


	private Row[] calculateNewSorting() {
		final int tableModelRowCount = _tableModel.getRowCount();
		final Row[] rows = new Row[tableModelRowCount];
		for (int row = 0; row < tableModelRowCount; row++) {
			rows[row] = new Row(row);
		}
		if (isSorting()) {
			Arrays.sort(rows);
		}
		return rows;
	}


	public int modelIndex(final int viewIndex) {
		final TableSorter.Row[] model = getViewToModel();
		if (model.length == 0) {
			return 0;
		}
		return model[viewIndex]._modelIndex;
	}


	private int[] getModelToView() {
		if (_modelToView == null) {
			final int n = getViewToModel().length;
			_modelToView = new int[n];
			for (int i = 0; i < n; i++) {
				_modelToView[modelIndex(i)] = i;
			}
		}
		return _modelToView;
	}


	public int viewIndex(final int modelIndex) {
		return getModelToView()[modelIndex];
	}

	// TableModel interface methods


	public int getRowCount() {
		return _tableModel == null ? 0 : _tableModel.getRowCount();
	}


	public int getColumnCount() {
		return _tableModel == null ? 0 : _tableModel.getColumnCount();
	}


	@Override
	public String getColumnName(final int column) {
		return _tableModel.getColumnName(column);
	}


	@Override
	public Class getColumnClass(final int column) {
		return _tableModel.getColumnClass(column);
	}


	@Override
	public boolean isCellEditable(final int row, final int column) {
		return _tableModel.isCellEditable(modelIndex(row), column);
	}


	public Object getValueAt(final int row, final int column) {
		return _tableModel.getValueAt(modelIndex(row), column);
	}


	@Override
	public void setValueAt(final Object aValue, final int row, final int column) {
		_tableModel.setValueAt(aValue, modelIndex(row), column);
	}


	public void setNotSortedDisabled(final boolean disableNotSorted) {
		_disableNotSorted = disableNotSorted;
	}


	public void setNotSortedDisabled(final boolean disableNotSorted, final int initialSortedColummn, final int initialSortingMode) {
		setSortingStatus(initialSortedColummn, initialSortingMode);
		_disableNotSorted = disableNotSorted;
	}

	// Helper classes

	private class Row implements Comparable<Object> {

		private final int _modelIndex;


		private Row(final int index) {
			_modelIndex = index;
		}


		@SuppressWarnings({"unchecked"})
		public int compareTo(final Object o) {
			final int row1 = _modelIndex;
			final int row2 = ((Row) o)._modelIndex;

			for (final Directive directive : _sortingColumns) {
				final int column = directive._column;
				final Object o1 = _tableModel.getValueAt(row1, column);
				final Object o2 = _tableModel.getValueAt(row2, column);

				final int comparison;
				// Define null less than everything, except null.
				if (o1 == null && o2 == null) {
					comparison = 0;
				} else if (o1 == null) {
					comparison = -1;
				} else if (o2 == null) {
					comparison = 1;
				} else {
					final Comparator<Object> comp = getComparator(column);
					if (comp instanceof TableColumnComparator) {
						comparison = ((TableColumnComparator<Object>) comp).compare(column, o1, o2);
					} else {
						comparison = comp.compare(o1, o2);
					}
				}
				if (comparison != 0) {
					return directive._direction == DESCENDING ? -comparison : comparison;
				}
			}
			return 0;
		}


		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Row)) {
				return false;
			}

			final Row row = (Row) o;

			return _modelIndex == row._modelIndex;

		}


		@Override
		public int hashCode() {
			return _modelIndex;
		}
	}

	private class TableModelHandler implements TableModelListener {

		public void tableChanged(final TableModelEvent e) {
			// If we're not sorting by anything, just pass the event along.
			if (!isSorting()) {
				clearSortingState();
				fireTableChanged(e);
				return;
			}

			// If the table structure has changed, cancel the sorting; the
			// sorting columns may have been either moved or deleted from
			// the model.
			if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
				cancelSorting();
				fireTableChanged(e);
				return;
			}

			// We can map a cell event through to the view without widening
			// when the following conditions apply:
			//
			// a) all the changes are on one row (e.getFirstRow() == e.getLastRow()) and,
			// b) all the changes are in one column (column != TableModelEvent.ALL_COLUMNS) and,
			// c) we are not sorting on that column (getSortingStatus(column) == NOT_SORTED) and,
			// d) a reverse lookup will not trigger a sort (modelToView != null)
			//
			// Note: INSERT and DELETE events fail this test as they have column == ALL_COLUMNS.
			//
			// The last check, for (modelToView != null) is to see if modelToView
			// is already allocated. If we don't do this check; sorting can become
			// a performance bottleneck for applications where cells
			// change rapidly in different parts of the table. If cells
			// change alternately in the sorting column and then outside of
			// it this class can end up re-sorting on alternate cell updates -
			// which can be a performance problem for large tables. The last
			// clause avoids this problem.
			final int column = e.getColumn();
			if (e.getFirstRow() == e.getLastRow() && column != TableModelEvent.ALL_COLUMNS && getSortingStatus(column) == NOT_SORTED && _modelToView != null) {
				final int viewIndex = getModelToView()[e.getFirstRow()];
				fireTableChanged(new TableModelEvent(TableSorter.this, viewIndex, viewIndex, column, e.getType()));
				return;
			}

			// Something has happened to the data that may have invalidated the row order.
			clearSortingState();
			fireTableDataChanged();
		}
	}

	@SuppressWarnings({"AssignmentToMethodParameter", "AssignmentReplaceableWithOperatorAssignment"})
	private class MouseHandler extends MouseAdapter {

		@Override
		public void mouseClicked(final MouseEvent e) {
			if (!e.isConsumed()) {
				final JTableHeader h = (JTableHeader) e.getSource();
				final TableColumnModel columnModel = h.getColumnModel();
				final int viewColumn = columnModel.getColumnIndexAtX(e.getX());
				if (viewColumn != -1) {
					final int column = columnModel.getColumn(viewColumn).getModelIndex();
					if (column != -1) {
						int status = getSortingStatus(column);
						if (!e.isControlDown()) {
							cancelSorting();
						}
						status = getNextStatus(status, e.isShiftDown());
						setSortingStatus(column, status);
					}
				}
			}
		}


		@SuppressWarnings("TailRecursion")
		private int getNextStatus(int actStatus, final boolean isShiftDown) {
			// Cycle the sorting states through {NOT_SORTED, ASCENDING, DESCENDING} or
			// {NOT_SORTED, DESCENDING, ASCENDING} depending on whether shift is pressed.
			actStatus = actStatus + (isShiftDown ? -1 : 1);
			actStatus = (actStatus + 4) % 3 - 1; // signed mod, returning {-1, 0, 1}
			if (_disableNotSorted && actStatus == NOT_SORTED) {
				return getNextStatus(actStatus, isShiftDown);
			} else {
				return actStatus;
			}
		}
	}

	private static class Arrow implements Icon {

		private final boolean _descending;
		private final int _size;
		private final int _priority;


		private Arrow(final boolean descending, final int size, final int priority) {
			_descending = descending;
			_size = size;
			_priority = priority;
		}


		public void paintIcon(final Component c, final Graphics g, final int x, int y) {
			final Color color = c == null ? JBColor.GRAY : c.getBackground();
			// In a compound sort, make each succesive triangle 20%
			// smaller than the previous one.
			final int dx = _size / 2 * (int) Math.pow(0.8, _priority);
			final int dy = _descending ? dx : -dx;
			// Align icon (roughly) with font baseline.
			//noinspection AssignmentToMethodParameter
			y = y + 5 * _size / 6 + (_descending ? -dy : 0);
			final int shift = _descending ? 1 : -1;
			g.translate(x, y);

			// Right diagonal.
			g.setColor(color.darker());
			g.drawLine(dx / 2, dy, 0, 0);
			g.drawLine(dx / 2, dy + shift, 0, shift);

			// Left diagonal.
			g.setColor(color.brighter());
			g.drawLine(dx / 2, dy, dx, 0);
			g.drawLine(dx / 2, dy + shift, dx, shift);

			// Horizontal line.
			if (_descending) {
				g.setColor(color.darker().darker());
			} else {
				g.setColor(color.brighter().brighter());
			}
			g.drawLine(dx, 0, 0, 0);

			g.setColor(color);
			g.translate(-x, -y);
		}


		public int getIconWidth() {
			return _size;
		}


		public int getIconHeight() {
			return _size;
		}
	}

	private class SortableHeaderRenderer implements TableCellRenderer {

		private final TableCellRenderer _tableCellRenderer;


		private SortableHeaderRenderer(final TableCellRenderer tableCellRenderer) {
			_tableCellRenderer = tableCellRenderer;
		}


		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
			final Component c = _tableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (c instanceof JLabel) {
				final JLabel l = (JLabel) c;
				l.setHorizontalTextPosition(JLabel.LEFT);
				final int modelColumn = table.convertColumnIndexToModel(column);
				l.setIcon(getHeaderRendererIcon(modelColumn, l.getFont().getSize()));
			}
			return c;
		}
	}

	private static class Directive {

		private final int _column;
		private final int _direction;


		private Directive(final int column, final int direction) {
			_column = column;
			_direction = direction;
		}
	}


	public interface TableColumnComparator<T> extends Comparator<T> {

		int compare(int column, T o1, T o2);
	}
}

