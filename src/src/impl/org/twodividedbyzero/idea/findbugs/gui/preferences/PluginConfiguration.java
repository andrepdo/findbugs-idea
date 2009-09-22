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
public class PluginConfiguration implements ConfigurationPage {

	private final FindBugsPreferences _preferences;
	private final ConfigurationPanel _parent;
	private Component _component;
	private JPanel _pluginsPanel;
	private JList _pluginList;


	public PluginConfiguration(final ConfigurationPanel parent, final FindBugsPreferences preferences) {
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
									 {border, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);

			final JPanel mainPanel = new JPanel(tbl);
			mainPanel.add(getPluginPanel(), "1, 1, 1, 1");

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
		for (final String s : _preferences.getPlugins()) {
			getModel(getPluginList()).addElement(s);
		}
	}


	private void clearModels() {
		getModel(getPluginList()).clear();
	}


	public JPanel getPluginPanel() {
		if (_pluginsPanel == null) {

			final double border = 5;
			final double rowsGap = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayout.FILL, colsGap, TableLayout.PREFERRED, border}, // Columns
									 {border, TableLayout.FILL, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_pluginsPanel = new JPanel(tbl);
			_pluginsPanel.setBorder(BorderFactory.createTitledBorder("Plugins"));  // NON-NLS

			final DefaultListModel model = new DefaultListModel();

			_pluginList = new JList(model);
			_pluginList.setVisibleRowCount(30);
			_pluginList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			final JScrollPane scrollPane = new JScrollPane(_pluginList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			_pluginsPanel.add(scrollPane, "1, 1, 1, 1"); // col ,row, col, row


			final double[][] bPanelSize = {{border, TableLayout.PREFERRED}, // Columns
									 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout btbl = new TableLayout(bPanelSize);

			final JPanel buttonPanel = new JPanel(btbl);
			_pluginsPanel.add(buttonPanel, "3, 1, 3, 1");

			final JButton addButton = new JButton();
			final BrowseAction action = new BrowseAction(_parent, "Add...", new ExtensionFileFilter(FindBugsUtil.ARCHIVE_EXTENSION_SET), new BrowseActionCallback() {
				public void addSelection(final File selectedFile) {
					((DefaultListModel) _pluginList.getModel()).addElement(selectedFile.getAbsolutePath());
					_preferences.getPlugins().add(selectedFile.getAbsolutePath());
					_preferences.setModified(true);
				}
			});  // NON-NLS
			addButton.setAction(action);
			buttonPanel.add(addButton, "1, 1, 1, 1");

			final JButton removeButton = new JButton("Remove") {  // NON-NLS
				@Override
				public boolean isEnabled() {
					return super.isEnabled() && _pluginList.getSelectedIndex() > -1;
				}
			};
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final int index = _pluginList.getSelectedIndex();
					getModel(_pluginList).remove(index);
					_preferences.removePlugin(index);
				}
			});
			buttonPanel.add(removeButton, "1, 3, 1, 3");

			_pluginList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(final ListSelectionEvent e) {
					removeButton.setEnabled(!e.getValueIsAdjusting() && e.getFirstIndex() >= 0);

				}
			});
		}

		return _pluginsPanel;
	}





	public JList getPluginList() {
		if(_pluginList == null) {
			getPluginPanel();
		}
		return _pluginList;
	}


	static DefaultListModel getModel(final JList list) {
		return (DefaultListModel) list.getModel();
	}


	public void setEnabled(final boolean enabled) {
		getPluginList().setEnabled(enabled);
	}


	public boolean showInModulePreferences() {
		return false;
	}
}