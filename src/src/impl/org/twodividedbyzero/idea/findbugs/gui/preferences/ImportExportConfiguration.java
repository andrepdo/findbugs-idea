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

import com.intellij.openapi.ui.MessageType;
import info.clearthought.layout.TableLayout;
import org.twodividedbyzero.idea.findbugs.common.ui.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.96
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class ImportExportConfiguration implements ConfigurationPage {

	private final FindBugsPreferences _preferences;
	private final ConfigurationPanel _parent;
	private Component _component;
	private JPanel _mainPanel;

	private JPanel _exportDirPanel;
	private JTextField _exportDirTextField;
	private JCheckBox _exportDirFormaCheckbox;

	private JPanel _fileFormatPanel;
	private JCheckBox _writeXmlCheckbox;
	private JCheckBox _writeHtmlCheckbox;

	private JPanel _browserPanel;
	private JCheckBox _openBrowserCheckbox;
	private String _currentExportDir;


	public ImportExportConfiguration(final ConfigurationPanel parent, final FindBugsPreferences preferences) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_preferences = preferences;
		_parent = parent;
	}


	public Component getComponent() {
		if (_component == null) {
			final double border = 5;
			final double rowsGap = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayout.PREFERRED, border}, // Columns
									 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);

			_mainPanel = new JPanel(tbl);
			_mainPanel.add(getExportDirPanel(), "1, 1, 1, 1");
			_mainPanel.add(getFileFormatPanel(), "1, 3, 1, 3");
			_mainPanel.add(getBrowserPanel(), "1, 5, 1, 5");

			_component = _mainPanel;
		}
		//updatePreferences();
		return _component;
	}


	public void updatePreferences() {
		getExportDirFormatCheckbox().setSelected(Boolean.valueOf(_preferences.getProperty(FindBugsPreferences.EXPORT_CREATE_ARCHIVE_DIR)));
		getWriteHtmlCheckbox().setSelected(Boolean.valueOf(_preferences.getProperty(FindBugsPreferences.EXPORT_AS_HTML)));
		getWriteXmlCheckbox().setSelected(Boolean.valueOf(_preferences.getProperty(FindBugsPreferences.EXPORT_AS_XML)));
		getOpenBrowserCheckbox().setSelected(Boolean.valueOf(_preferences.getProperty(FindBugsPreferences.EXPORT_OPEN_BROWSER)));
		_currentExportDir = _preferences.getProperty(FindBugsPreferences.EXPORT_BASE_DIR);
		getExportDirTextField().setText(_currentExportDir);
	}


	public void setEnabled(final boolean enabled) {
		getExportDirFormatCheckbox().setEnabled(enabled);
		getExportDirTextField().setEnabled(enabled);
		getOpenBrowserCheckbox().setEnabled(enabled);
		getWriteHtmlCheckbox().setEnabled(enabled);
		getWriteXmlCheckbox().setEnabled(enabled);
	}


	public boolean showInModulePreferences() {
		return false;
	}


	private Component getExportDirPanel() {
		if (_exportDirPanel == null) {
			final double border = 5;
			final double rowsGap = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.FILL, colsGap, TableLayout.PREFERRED, border}, // Columns
									 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_exportDirPanel = new JPanel(tbl);
			_exportDirPanel.setBorder(BorderFactory.createTitledBorder("Export directory settings"));
			final JComponent exportDirLabel = new JLabel("Export Dir");
			exportDirLabel.setToolTipText("Set the default export directory. if not set selection dialog will be shown.");
			_exportDirPanel.add(exportDirLabel, "1, 1, 1, 1");
			_exportDirPanel.add(getExportDirTextField(), "3, 1, 3, 1");

			final AbstractButton browseButton = new JButton("Browse");
			browseButton.setPreferredSize(new Dimension(80, 20));
			browseButton.addActionListener(new FileChooserActionListener());
			_exportDirPanel.add(browseButton, "5, 1, 5, 1");

			_exportDirPanel.add(getExportDirFormatCheckbox(), "1, 3, 1, 3");
			_exportDirPanel.add(new JLabel("Create archive sub dir(s) in the following format (MM_TT_YYYY)"), "3, 3, 3, 3");
		}

		return _exportDirPanel;
	}


	private static void showToolWindowNotifier(final String message, final MessageType type) {
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			public void run() {
				FindBugsPluginImpl.showToolWindowNotifier(message, type);
			}
		});
	}


	private JTextComponent getExportDirTextField() {
		if (_exportDirTextField == null) {
			_exportDirTextField = new JTextField(30);
			_exportDirTextField.setText(_currentExportDir);
			_exportDirTextField.getDocument().addDocumentListener(new DocumentListener() {
				public void insertUpdate(final DocumentEvent e) {
					_preferences.setProperty(FindBugsPreferences.EXPORT_BASE_DIR, _exportDirTextField.getText());
				}


				public void removeUpdate(final DocumentEvent e) {
				}


				public void changedUpdate(final DocumentEvent e) {
					_preferences.setProperty(FindBugsPreferences.EXPORT_BASE_DIR, _exportDirTextField.getText());
				}
			});
		}
		return _exportDirTextField;
	}


	private AbstractButton getExportDirFormatCheckbox() {
		if (_exportDirFormaCheckbox == null) {
			_exportDirFormaCheckbox = new JCheckBox();
			_exportDirFormaCheckbox.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					_preferences.setProperty(FindBugsPreferences.EXPORT_CREATE_ARCHIVE_DIR, e.getStateChange() == ItemEvent.SELECTED);
				}
			});
		}
		return _exportDirFormaCheckbox;
	}


	private Component getFileFormatPanel() {
		if (_fileFormatPanel == null) {
			final double border = 5;
			final double rowsGap = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.FILL, border}, // Columns
									 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_fileFormatPanel = new JPanel(tbl);
			_fileFormatPanel.setBorder(BorderFactory.createTitledBorder("Enable export file format"));

			_fileFormatPanel.add(getWriteHtmlCheckbox(), "1, 1, 1, 1");
			_fileFormatPanel.add(new JLabel("Export bug collection as HTML"), "3, 1, 3, 1");

			_fileFormatPanel.add(getWriteXmlCheckbox(), "1, 3, 1, 3");
			_fileFormatPanel.add(new JLabel("Export bug collection as XML"), "3, 3, 3, 3");

		}
		return _fileFormatPanel;
	}


	private Component getBrowserPanel() {
		if (_browserPanel == null) {
			final double border = 5;
			final double rowsGap = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.FILL, border}, // Columns
									 {border, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_browserPanel = new JPanel(tbl);
			_browserPanel.setBorder(BorderFactory.createTitledBorder("Result view settings"));

			_browserPanel.add(getOpenBrowserCheckbox(), "1, 1, 1, 1");
			_browserPanel.add(new JLabel("Open exported bug collection (only Html) in the configured browser"), "3, 1, 3, 1");
		}
		return _browserPanel;
	}


	private AbstractButton getWriteXmlCheckbox() {
		if (_writeXmlCheckbox == null) {
			_writeXmlCheckbox = new JCheckBox();
			_writeXmlCheckbox.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					_preferences.setProperty(FindBugsPreferences.EXPORT_AS_XML, e.getStateChange() == ItemEvent.SELECTED);
				}
			});
		}
		return _writeXmlCheckbox;
	}


	private AbstractButton getWriteHtmlCheckbox() {
		if (_writeHtmlCheckbox == null) {
			_writeHtmlCheckbox = new JCheckBox();
			_writeHtmlCheckbox.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					_preferences.setProperty(FindBugsPreferences.EXPORT_AS_HTML, e.getStateChange() == ItemEvent.SELECTED);
				}
			});
		}
		return _writeHtmlCheckbox;
	}


	private AbstractButton getOpenBrowserCheckbox() {
		if (_openBrowserCheckbox == null) {
			_openBrowserCheckbox = new JCheckBox();
			_openBrowserCheckbox.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					_preferences.setProperty(FindBugsPreferences.EXPORT_OPEN_BROWSER, e.getStateChange() == ItemEvent.SELECTED);
				}
			});
		}
		return _openBrowserCheckbox;
	}


	private class FileChooserActionListener implements ActionListener {

		public void actionPerformed(final ActionEvent e) {
			final JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.showDialog(_parent, "Select");
			fc.setCurrentDirectory(new File(getExportDirTextField().getText()));
			final File selectedFile = fc.getSelectedFile();
			if (selectedFile != null && selectedFile.isDirectory() && selectedFile.canWrite()) {
				final String newLocation = selectedFile.getPath();
				getExportDirTextField().setText(newLocation);
			} else {
				showToolWindowNotifier("Invalid directory.", MessageType.ERROR);
			}
		}
	}
}