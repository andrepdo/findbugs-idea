/*
 * Copyright 2008-2015 Andre Pfeiler
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
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.GuiUtil;
import org.twodividedbyzero.idea.findbugs.gui.common.AaComboBox;
import org.twodividedbyzero.idea.findbugs.gui.common.ScrollPaneFacade;
import org.twodividedbyzero.idea.findbugs.gui.common.TableFacade;
import org.twodividedbyzero.idea.findbugs.gui.preferences.model.BugCategoryTableModel;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Map.Entry;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @since 0.9.9-dev
 */
public final class ReportConfiguration implements ConfigurationPage {

	public static final String DEFAULT_PRIORITY = ProjectFilterSettings.MEDIUM_PRIORITY;

	private JLabel _priorityLabel;
	private AaComboBox<String> _priorityBox;
	private JPanel _categoryPanel;
	private JTable _categoryTable;
	private JPanel _component;

	private final FindBugsPreferences _preferences;
	private final ConfigurationPanel _parent;


	public ReportConfiguration(final ConfigurationPanel parent, final FindBugsPreferences preferences) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_preferences = preferences;
		_parent = parent;
	}


	@NotNull
	@Override
	public Component getComponent() {
		if (_component == null) {

			final JPanel priorityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			priorityPanel.add(getPriorityLabel());
			priorityPanel.add(getPriorityComboBox());

			_component = new JPanel(new BorderLayout());
			_component.add(priorityPanel, BorderLayout.NORTH);
			_component.add(getCategoryPanel());
		}
		return _component;
	}


	@Override
	public void updatePreferences() {
		getPriorityComboBox().setSelectedItem(_preferences.getProperty(FindBugsPreferences.MIN_PRIORITY_TO_REPORT), false);
		getModel().clear();
		syncTableModel(getModel());
	}


	@NotNull
	private JLabel getPriorityLabel() {
		if (_priorityLabel == null) {
			_priorityLabel = new JLabel("Minimum confidence to report");
		}
		return _priorityLabel;
	}


	@NotNull
	private AaComboBox getPriorityComboBox() {
		if (_priorityBox == null) {
			_priorityBox = new AaComboBox<String>();
			_priorityBox.addItem(ProjectFilterSettings.HIGH_PRIORITY);
			_priorityBox.addItem(ProjectFilterSettings.MEDIUM_PRIORITY);
			_priorityBox.addItem(ProjectFilterSettings.LOW_PRIORITY);
			_priorityBox.addSelectionChangeListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					_preferences.setProperty(FindBugsPreferences.MIN_PRIORITY_TO_REPORT, (String)_priorityBox.getSelectedItem());
				}
			});
		}
		return _priorityBox;
	}


	@NotNull
	private JPanel getCategoryPanel() {
		if (_categoryPanel == null) {
			_categoryPanel = new JPanel(new BorderLayout());
			_categoryPanel.setBorder(BorderFactory.createTitledBorder("Reported (visible) bug categories"));
			_categoryPanel.add(ScrollPaneFacade.createScrollPane(getBugCategoriesTable(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		}
		return _categoryPanel;
	}


	@NotNull
	private JTable getBugCategoriesTable() {
		if (_categoryTable == null) {
			_categoryTable = TableFacade.createTable();
			_categoryTable.setShowGrid(false);
			_categoryTable.setShowHorizontalLines(false);
			_categoryTable.setShowVerticalLines(false);
			_categoryTable.setRowHeight(GuiUtil.SCALE_FACTOR*25);
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
				@Override
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


	@Override
	public void setEnabled(final boolean enabled) {
		getPriorityLabel().setEnabled(enabled);
		getPriorityComboBox().setEnabled(enabled);
		getCategoryPanel().setEnabled(enabled);
		getBugCategoriesTable().setEnabled(enabled);
	}


	@Override
	public boolean showInModulePreferences() {
		return true;
	}


	@Override
	public boolean isAdvancedConfig() {
		return false;
	}


	@Override
	public String getTitle() {
		return "Reporting";
	}


	@Override
	public void filter(final String filter) {
		// TODO support search
	}
}
