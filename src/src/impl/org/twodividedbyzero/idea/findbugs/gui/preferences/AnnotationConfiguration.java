/*
 * Copyright 2010 Andre Pfeiler
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
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
public class AnnotationConfiguration implements ConfigurationPage {

	private final FindBugsPreferences _preferences;
	private final ConfigurationPanel _parent;
	private Component _component;
	private JPanel _mainPanel;
	private JTextField _annotationPathField;
	private JCheckBox _enableGutterIcon;
	private JCheckBox _enableTextRangeMarkUp;
	private JPanel _annotationPathPanel;
	private JPanel _markUpPanel;


	public AnnotationConfiguration(final ConfigurationPanel parent, final FindBugsPreferences preferences) {
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
									 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);

			final Container mainPanel = new JPanel(tbl);
			mainPanel.add(getMarkUpPanel(), "1, 1, 1, 1");
			mainPanel.add(getAnnotationPathPanel(), "1, 3, 1, 3");

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
		/*for (final String s : _preferences.getPlugins()) {
			getModel(getPluginList()).addElement(s);
		}*/
	}


	private void clearModels() {
		//getModel(getPluginList()).clear();
	}


	JPanel getAnnotationPathPanel() {
		if (_annotationPathPanel == null) {

			final double border = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.PREFERRED, border}, // Columns
									 {border, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_annotationPathPanel = new JPanel(tbl);
			_annotationPathPanel.setBorder(BorderFactory.createTitledBorder("Path to FindBugs Annotation class (@SuppressWarning) need to be on the classpath"));

			_annotationPathPanel.add(getAnnotationPathField(), "1, 1, 1, 1"); // col ,row, col, row


			final double rowsGap = 5;
			final double[][] bPanelSize = {{border, TableLayout.PREFERRED}, // Columns
									 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tableLayout = new TableLayout(bPanelSize);

			final Container buttonPanel = new JPanel(tableLayout);
			_annotationPathPanel.add(buttonPanel, "3, 1, 3, 1");

			/*final AbstractButton addButton = new JButton();
			final Action action = new BrowseAction(_parent, "Browse...", new ExtensionFileFilter(Collections.singletonMap(".java")), new BrowseActionCallback() {
				public void addSelection(final File selectedFile) {

					_preferences.setModified(true);
				}
			});*/
			/*addButton.setAction(action);
			buttonPanel.add(addButton, "1, 1, 1, 1");*/


		}

		return _annotationPathPanel;
	}


	private JTextField getAnnotationPathField() {
		if (_annotationPathField == null) {
			_annotationPathField = new JTextField(30);
			_annotationPathField.getDocument().addDocumentListener(new DocumentListener() {
				public void insertUpdate(final DocumentEvent e) {
					//_preferences.setProperty(FindBugsPreferences.EXPORT_BASE_DIR, _exportDirTextField.getText());
				}


				public void removeUpdate(final DocumentEvent e) {
				}


				public void changedUpdate(final DocumentEvent e) {
					//_preferences.setProperty(FindBugsPreferences.EXPORT_BASE_DIR, _exportDirTextField.getText());
				}
			});
		}
		return _annotationPathField;
	}


	JPanel getMarkUpPanel() {
		if (_markUpPanel == null) {
			final double border = 5;
			final double colsGap = 10;
			final double rowsGap = 5;
			final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.PREFERRED, border}, // Columns
									 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_markUpPanel = new JPanel(tbl);
			_markUpPanel.setBorder(BorderFactory.createTitledBorder("Annotation/MarkUp Settings"));

			_markUpPanel.add(getGutterIconCheckbox(), "1, 1, 1, 1"); // col ,row, col, row
			_markUpPanel.add(getTextRangeMarkupCheckbox(), "1, 3, 1, 3"); // col ,row, col, row

		}

		return _markUpPanel;
	}


	JPanel getAnnotaionColorsPanel() {
		// todo:
		return null;
	}


	private JCheckBox getTextRangeMarkupCheckbox() {
		if (_enableTextRangeMarkUp == null) {
			_enableTextRangeMarkUp = new JCheckBox("Enable editor TextRange markup");
			_enableTextRangeMarkUp.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					//_preferences.setProperty(FindBugsPreferences.EXPORT_AS_XML, e.getStateChange() == ItemEvent.SELECTED);
				}
			});
		}
		return _enableTextRangeMarkUp;
	}


	private JCheckBox getGutterIconCheckbox() {
		if (_enableGutterIcon == null) {
			_enableGutterIcon = new JCheckBox("Enable editor GutterIcon markup");
			_enableGutterIcon.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					//_preferences.setProperty(FindBugsPreferences.EXPORT_AS_XML, e.getStateChange() == ItemEvent.SELECTED);
				}
			});
		}
		return _enableGutterIcon;
	}


	public void setEnabled(final boolean enabled) {

	}


	public boolean showInModulePreferences() {
		return false;
	}


	public boolean isAdvancedConfig() {
		return true;
	}


	public String getTitle() {
		return "Annotations";
	}

}