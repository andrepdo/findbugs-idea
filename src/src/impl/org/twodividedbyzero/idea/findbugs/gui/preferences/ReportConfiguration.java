/*
 * Copyright 2010 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.gui.preferences;

import edu.umd.cs.findbugs.I18N;
import edu.umd.cs.findbugs.config.ProjectFilterSettings;
import info.clearthought.layout.TableLayout;
import org.twodividedbyzero.idea.findbugs.gui.common.ScrollPaneFacade;
import org.twodividedbyzero.idea.findbugs.gui.preferences.model.BugCategoryTableModel;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.Map.Entry;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.9-dev
 */
public class ReportConfiguration implements ConfigurationPage {

	public static final String DEFAULT_PRIORITY = ProjectFilterSettings.MEDIUM_PRIORITY;

	private JComboBox _priorityBox;
	private JTable _categoryTable;
	private Component _component;

	private final FindBugsPreferences _preferences;
	private final ConfigurationPanel _parent;


	public ReportConfiguration(final ConfigurationPanel parent, final FindBugsPreferences preferences) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_preferences = preferences;
		_parent = parent;
	}


	public Component getComponent() {
		if (_component == null) {

			final double border = 5;
			final double rowsGap = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.PREFERRED}, // Columns
									 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);

			final JPanel mainPanel = new JPanel(tbl);
			mainPanel.add(new JLabel("Minimum priority to report"), "1, 1, 1, 1");
			final JPanel comp = new JPanel(new FlowLayout(FlowLayout.LEFT));
			comp.add(getPriorityCombobox());
			mainPanel.add(comp, "3, 1, 3, 1");

			final JPanel categoryPanel = new JPanel();
			categoryPanel.setBorder(BorderFactory.createTitledBorder("Reported (visible) bug categories"));
			categoryPanel.add(ScrollPaneFacade.getComponent(getBugCategoriesTable(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

			mainPanel.add(categoryPanel, "1, 3, 3, 3");

			_component = mainPanel;
		}
		return _component;
	}


	public void updatePreferences() {
		getPriorityCombobox().setSelectedItem(_preferences.getProperty(FindBugsPreferences.MIN_PRIORITY_TO_REPORT));
		getModel().clear();
		syncTableModel(getModel());
	}


	private JComboBox getPriorityCombobox() {
		if (_priorityBox == null) {
			_priorityBox = new JComboBox();
			_priorityBox.addItem(ProjectFilterSettings.HIGH_PRIORITY);
			_priorityBox.addItem(ProjectFilterSettings.MEDIUM_PRIORITY);
			_priorityBox.addItem(ProjectFilterSettings.LOW_PRIORITY);
			_priorityBox.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					_preferences.setProperty(FindBugsPreferences.MIN_PRIORITY_TO_REPORT, (String) e.getItem());
				}
			});
		}
		return _priorityBox;
	}


	private JTable getBugCategoriesTable() {
		if (_categoryTable == null) {
			_categoryTable = new JTable();
			_categoryTable.setShowGrid(false);
			_categoryTable.setShowHorizontalLines(false);
			_categoryTable.setShowVerticalLines(false);
			_categoryTable.setRowHeight(25);
			_categoryTable.setTableHeader(null);
			_categoryTable.setCellSelectionEnabled(false);
			_categoryTable.setRowSelectionAllowed(true);
			_categoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			_categoryTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			final BugCategoryTableModel model = new BugCategoryTableModel();
			_categoryTable.setModel(model);
			_categoryTable.getColumnModel().getColumn(0).setPreferredWidth(20);
			_categoryTable.getColumnModel().getColumn(1).setPreferredWidth(180);
			_categoryTable.getColumnModel().getColumn(2).setPreferredWidth(170);
			_categoryTable.setOpaque(false);

			syncTableModel(model);

			model.addTableModelListener(new TableModelListener() {
				public void tableChanged(final TableModelEvent e) {
					if (e.getColumn() == 0 && TableModelEvent.UPDATE == e.getType()) {
						final String enabled = String.valueOf(model.getValueAt(e.getFirstRow(), 0));
						final String category = String.valueOf(model.getValueAt(e.getFirstRow(), 2));

						_parent.getDetectorConfig().getModel().enableCategory(category, Boolean.valueOf(enabled));

						updatePreferences(category, enabled);
					}
				}
			});
			/*final List<String> bugCategoryList = new LinkedList<String>(I18N.instance().getBugCategories());
			for (final String category : bugCategoryList) {
				model.addRow(new Object[] {true, I18N.instance().getBugCategoryDescription(category), category});
			}*/
		}

		return _categoryTable;

	}


	private void syncTableModel(final BugCategoryTableModel model) {
		final Map<String, String> bugCategoryMap = _preferences.getBugCategories();
		for (final Entry<String, String> entry : bugCategoryMap.entrySet()) {
			final String category = entry.getKey();
			model.add(I18N.instance().getBugCategoryDescription(category), category, Boolean.valueOf(entry.getValue()));
		}
	}


	private void updatePreferences(final String category, final String value) {
		for (final Entry<String, String> entry : _preferences.getBugCategories().entrySet()) {
			if (entry.getKey().equals(category)) {
				entry.setValue(value);
				_preferences.setModified(true);
			}
		}
	}


	private BugCategoryTableModel getModel() {
		return (BugCategoryTableModel) getBugCategoriesTable().getModel();
	}


	public void setEnabled(final boolean enabled) {
		getBugCategoriesTable().setEnabled(enabled);
		getPriorityCombobox().setEnabled(enabled);
	}


	public boolean showInModulePreferences() {
		return true;
	}


	public boolean isAdvancedConfig() {
		return false;
	}


	public String getTitle() {
		return "Reporting";
	}
}
