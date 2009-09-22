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
package org.twodividedbyzero.idea.findbugs.gui.preferences;

import info.clearthought.layout.TableLayout;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsUtil;
import org.twodividedbyzero.idea.findbugs.gui.common.ExtensionFileFilter;
import org.twodividedbyzero.idea.findbugs.gui.preferences.BrowseAction.BrowseActionCallback;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.9
 */
public class FilterConfiguration implements ConfigurationPage {

	private final FindBugsPreferences _preferences;
	private final ConfigurationPanel _parent;
	private Component _component;
	private JPanel _includePanel;
	private JList _includeList;
	private JPanel _excludePanel;
	private JList _excludeList;
	private JList _baselineList;
	private JPanel _baselinePanel;


	public FilterConfiguration(final ConfigurationPanel parent, final FindBugsPreferences preferences) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_preferences = preferences;
		_parent = parent;
	}


	public Component getComponent() {
		if (_component == null) {
			final double border = 5;
			final double rowsGap = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayout.FILL, border}, // Columns
									 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);

			final JPanel mainPanel = new JPanel(tbl);
			mainPanel.add(getIncludePanel(), "1, 1, 1, 1");
			mainPanel.add(getExcludePanel(), "1, 3, 1, 3");
			mainPanel.add(getBaseLinePanel(), "1, 5, 1, 5");

			_component = mainPanel;
		}
		//updatePreferences();
		return _component;
	}


	public void updatePreferences() {
		clearModels();
		syncModels();
	}


	private void syncModels() {
		for (final String s : _preferences.getIncludeFilters()) {
			getModel(getIncludeList()).addElement(s);
		}

		for (final String s : _preferences.getExcludeFilters()) {
			getModel(getExcludeList()).addElement(s);
		}

		for (final String s : _preferences.getExcludeBaselineBugs()) {
			getModel(getBaselineList()).addElement(s);
		}
	}


	private void clearModels() {
		getModel(getIncludeList()).clear();
		getModel(getExcludeList()).clear();
		getModel(getBaselineList()).clear();
	}


	public JPanel getIncludePanel() {
		if (_includePanel == null) {
			
			final double border = 5;
			final double rowsGap = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayout.FILL, colsGap, TableLayout.PREFERRED, border}, // Columns
									 {border, TableLayout.FILL, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_includePanel = new JPanel(tbl);
			_includePanel.setBorder(BorderFactory.createTitledBorder("Include filter files"));  // NON-NLS

			final DefaultListModel model = new DefaultListModel();

			_includeList = new JList(model);
			_includeList.setVisibleRowCount(7);
			_includeList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			final JScrollPane scrollPane = new JScrollPane(_includeList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			_includePanel.add(scrollPane, "1, 1, 1, 1"); // col ,row, col, row


			final double[][] bPanelSize = {{border, TableLayout.PREFERRED}, // Columns
									 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout btbl = new TableLayout(bPanelSize);

			final JPanel buttonPanel = new JPanel(btbl);
			_includePanel.add(buttonPanel, "3, 1, 3, 1");

			final JButton addButton = new JButton();
			final BrowseAction action = new BrowseAction(_parent, "Add...", new ExtensionFileFilter(FindBugsUtil.XML_EXTESIONS_SET),  new BrowseActionCallback() {
				public void addSelection(final File selectedFile) {
					((DefaultListModel) _includeList.getModel()).addElement(selectedFile.getAbsolutePath());
					_preferences.getIncludeFilters().add(selectedFile.getAbsolutePath());
					_preferences.setModified(true);
				}
			});  // NON-NLS
			addButton.setAction(action);
			buttonPanel.add(addButton, "1, 1, 1, 1");

			final JButton removeButton = new JButton("Remove") {  // NON-NLS
				@Override
				public boolean isEnabled() {
					return super.isEnabled() && _includeList.getSelectedIndex() > -1;
				}
			};
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final int index = _includeList.getSelectedIndex();
					getModel(_includeList).remove(index);
					_preferences.removeIncludeFilter(index);
				}
			});
			buttonPanel.add(removeButton, "1, 3, 1, 3");

			_includeList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(final ListSelectionEvent e) {
					removeButton.setEnabled(!e.getValueIsAdjusting() && e.getFirstIndex() >= 0);

				}
			});
		}

		return _includePanel;
	}


	public JPanel getExcludePanel() {
		if (_excludePanel == null) {

			final double border = 5;
			final double rowsGap = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayout.FILL, colsGap, TableLayout.PREFERRED, border}, // Columns
									 {border, TableLayout.FILL, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_excludePanel = new JPanel(tbl);
			_excludePanel.setBorder(BorderFactory.createTitledBorder("Exclude filter files"));  // NON-NLS

			final DefaultListModel model = new DefaultListModel();

			_excludeList = new JList(model);
			_excludeList.setVisibleRowCount(7);
			_excludeList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			final JScrollPane scrollPane = new JScrollPane(_excludeList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			_excludePanel.add(scrollPane, "1, 1, 1, 1"); // col ,row, col, row


			final double[][] bPanelSize = {{border, TableLayout.PREFERRED}, // Columns
									 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout btbl = new TableLayout(bPanelSize);

			final JPanel buttonPanel = new JPanel(btbl);
			_excludePanel.add(buttonPanel, "3, 1, 3, 1");

			final JButton addButton = new JButton();
			final BrowseAction action = new BrowseAction(_parent, "Add...", new ExtensionFileFilter(FindBugsUtil.XML_EXTESIONS_SET), new BrowseActionCallback() {
				public void addSelection(final File selectedFile) {
					((DefaultListModel) _excludeList.getModel()).addElement(selectedFile.getAbsolutePath());
					_preferences.getExcludeFilters().add(selectedFile.getAbsolutePath());
					_preferences.setModified(true);
				}
			});  // NON-NLS
			addButton.setAction(action);
			buttonPanel.add(addButton, "1, 1, 1, 1");

			final JButton removeButton = new JButton("Remove") {  // NON-NLS
				@Override
				public boolean isEnabled() {
					return super.isEnabled() && _excludeList.getSelectedIndex() > -1;
				}
			};
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final int index = _excludeList.getSelectedIndex();
					getModel(_excludeList).remove(index);
					_preferences.removeExcludeFilter(index);
				}
			});
			buttonPanel.add(removeButton, "1, 3, 1, 3");

			_excludeList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(final ListSelectionEvent e) {
					removeButton.setEnabled(!e.getValueIsAdjusting() && e.getFirstIndex() >= 0);

				}
			});
		}

		return _excludePanel;
	}


	public JPanel getBaseLinePanel() {
		if (_baselinePanel == null) {

			final double border = 5;
			final double rowsGap = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayout.FILL, colsGap, TableLayout.PREFERRED, border}, // Columns
									 {border, TableLayout.FILL, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_baselinePanel = new JPanel(tbl);
			_baselinePanel.setBorder(BorderFactory.createTitledBorder("Exclude baseline bugs"));  // NON-NLS

			final DefaultListModel model = new DefaultListModel();

			_baselineList = new JList(model);
			_baselineList.setVisibleRowCount(7);
			_baselineList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			final JScrollPane scrollPane = new JScrollPane(_baselineList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			_baselinePanel.add(scrollPane, "1, 1, 1, 1"); // col ,row, col, row


			final double[][] bPanelSize = {{border, TableLayout.PREFERRED}, // Columns
									 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout btbl = new TableLayout(bPanelSize);

			final JPanel buttonPanel = new JPanel(btbl);
			_baselinePanel.add(buttonPanel, "3, 1, 3, 1");

			final JButton addButton = new JButton();
			final BrowseAction action = new BrowseAction(_parent, "Add...", new ExtensionFileFilter(FindBugsUtil.XML_EXTESIONS_SET), new BrowseActionCallback() {
				public void addSelection(final File selectedFile) {
					((DefaultListModel) _baselineList.getModel()).addElement(selectedFile.getAbsolutePath());
					_preferences.getExcludeBaselineBugs().add(selectedFile.getAbsolutePath());
					_preferences.setModified(true);
				}
			});  // NON-NLS
			addButton.setAction(action);
			buttonPanel.add(addButton, "1, 1, 1, 1");

			final JButton removeButton = new JButton("Remove") {  // NON-NLS
				@Override
				public boolean isEnabled() {
					return super.isEnabled() && _baselineList.getSelectedIndex() > -1;
				}
			};
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final int index = _baselineList.getSelectedIndex();
					getModel(_baselineList).remove(index);
					_preferences.removeIncludeFilter(index);
				}
			});
			buttonPanel.add(removeButton, "1, 3, 1, 3");

			_baselineList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(final ListSelectionEvent e) {
					removeButton.setEnabled(!e.getValueIsAdjusting() && e.getFirstIndex() >= 0);
				}
			});
		}

		return _baselinePanel;
	}


	public JList getIncludeList() {
		if(_includeList == null) {
			getIncludePanel();
		}
		return _includeList;
	}


	public JList getExcludeList() {
		if(_excludeList == null) {
			getExcludePanel();
		}
		return _excludeList;
	}


	public JList getBaselineList() {
		if(_baselineList == null) {
			getBaseLinePanel();
		}
		return _baselineList;
	}


	static DefaultListModel getModel(final JList list) {
		return (DefaultListModel) list.getModel();
	}


	public void setEnabled(final boolean enabled) {
		getBaselineList().setEnabled(enabled);
		getExcludeList().setEnabled(enabled);
		getIncludeList().setEnabled(enabled);
	}


	public boolean showInModulePreferences() {
		return true;
	}
}
