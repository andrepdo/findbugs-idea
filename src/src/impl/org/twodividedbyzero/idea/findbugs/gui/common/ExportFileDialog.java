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

package org.twodividedbyzero.idea.findbugs.gui.common;

import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.ui.DocumentAdapter;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @author Keith Lea <keithl@gmail.com>
 * @version $Revision$
 * @since 0.9.96
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings({"SE_TRANSIENT_FIELD_NOT_RESTORED"})
@SuppressWarnings({"HardCodedStringLiteral"})
public class ExportFileDialog extends JPanel {

	private final JLabel _label;
	private final JTextField _path;
	private final JButton _browseButton;
	private File _selectedFile;
	private final transient DialogBuilder _dialogBuilder;
	private final JRadioButton _html;
	private final JRadioButton _xml;


	public ExportFileDialog(final String defaultValue, final DialogBuilder dialogBuilder) {
		setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.gridy = 1;
		c.insets = new Insets(5, 5, 5, 5);
		c.anchor = GridBagConstraints.NORTHWEST;

		_dialogBuilder = dialogBuilder;

		_label = new JLabel("Directory: ");
		_label.setMinimumSize(new Dimension(100, 20));
		_label.setMaximumSize(new Dimension(120, 20));
		_label.setPreferredSize(new Dimension(100, 20));
		_label.setMinimumSize(new Dimension(200, 20));
		_label.setMaximumSize(new Dimension(250, 20));
		_label.setMinimumSize(new Dimension(50, 20));
		_label.setMaximumSize(new Dimension(150, 20));

		c.weightx = 0;
		c.gridwidth = 2;
		add(_label, c);
		_path = new JTextField(defaultValue);
		_path.setPreferredSize(new Dimension(200, 20));
		c.weightx = 1;
		c.gridwidth = 1;
		add(_path, c);

		_browseButton = new JButton("Browse");
		_browseButton.setPreferredSize(new Dimension(80, 20));
		_browseButton.addActionListener(new MyFileChooserActionListener());
		c.weightx = 0;
		add(_browseButton, c);

		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 2;
		c.gridheight = 2;
		add(new JLabel("Format:"), c);
		c.insets = new Insets(0, 0, 0, 0);

		_html = new JRadioButton("HTML", true);
		_xml = new JRadioButton("XML", false);
		final ButtonGroup group = new ButtonGroup();
		group.add(_html);
		group.add(_xml);

		c.gridheight = 1;
		add(_html, c);
		c.gridy = 3;
		add(_xml, c);

		dialogBuilder.setCenterPanel(this);

		_path.getDocument().addDocumentListener(new MyDocumentAdapter());
		if (_path.getText().length() > 0) {
			_selectedFile = new File(_path.getText());
		}
		_path.addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(final HierarchyEvent e) {
				if (_path.isVisible()) {
					_dialogBuilder.setOkActionEnabled(validateDirectory(_path.getDocument()));
				}
			}
		});
		_dialogBuilder.setOkActionEnabled(_selectedFile != null && _selectedFile.isDirectory());
	}


	public String getText() {
		return _path.getText();
	}


	public void setText(final String s) {
		_path.setText(s);
	}


	public boolean isXml() {
		return _xml.isSelected();
	}


	private boolean validateDirectory(final Document doc) {
		try {
			return _selectedFile != null && _selectedFile.isDirectory() && _selectedFile.canWrite() || doc.getText(0, doc.getLength()).trim().length() > 0;
		} catch (BadLocationException ignore) {
			return false;
		}
	}


	private class MyDocumentAdapter extends DocumentAdapter {

		private MyDocumentAdapter() {
		}


		@Override
		protected void textChanged(final DocumentEvent e) {
			final Document doc = e.getDocument();
			_dialogBuilder.setOkActionEnabled(validateDirectory(doc));
		}
	}

	private class MyFileChooserActionListener implements ActionListener {

		public void actionPerformed(final ActionEvent e) {
			final JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			final Component parent = SwingUtilities.getRoot(_path);
			fc.showDialog(parent, "Select");
			_selectedFile = fc.getSelectedFile();
			if (_selectedFile != null && _selectedFile.isDirectory()) {
				final String newLocation = _selectedFile.getPath();
				_path.setText(newLocation);
				_dialogBuilder.setOkActionEnabled(true);

			}
		}
	}
}
