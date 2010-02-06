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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


/**
 * $Date$
 *
 * @author todo: your name and mail?
 * @version $Revision$
 * @since 0.9.95
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class ExportFileDialog extends JPanel {

	private JLabel _label;
	private JTextField _path;
	private JButton _open;
	private File _selectedFile;
	private final DialogBuilder _dialogBuilder;


	public ExportFileDialog(final String defaultValue, final DialogBuilder dialogBuilder) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		_dialogBuilder = dialogBuilder;

		_label = new JLabel("Export to directory: ");
		_label.setMinimumSize(new Dimension(100, 20));
		_label.setMaximumSize(new Dimension(120, 20));
		_label.setPreferredSize(new Dimension(100, 20));
		_label.setMinimumSize(new Dimension(200, 20));
		_label.setMaximumSize(new Dimension(250, 20));
		_label.setMinimumSize(new Dimension(50, 20));
		_label.setMaximumSize(new Dimension(150, 20));

		add(_label);
		_path = new JTextField(defaultValue);
		_path.setPreferredSize(new Dimension(200, 20));
		add(_path);
		add(Box.createHorizontalStrut(5));

		_open = new JButton("Browse");
		_open.setPreferredSize(new Dimension(80, 20));
		_open.addActionListener(new MyFileChooserActionListener());
		add(_open);
		add(Box.createVerticalGlue());
		dialogBuilder.setCenterPanel(this);

		_path.getDocument().addDocumentListener(new MyDocumentAdapter());
		dialogBuilder.setOkActionEnabled(false);
	}


	public String getText() {
		return _path.getText();
	}


	public void setText(final String s) {
		_path.setText(s);
	}


	private class MyDocumentAdapter extends DocumentAdapter {

		public MyDocumentAdapter() {
		}


		@Override
		protected void textChanged(final DocumentEvent e) {
			try {
				final Document doc = e.getDocument();
				_dialogBuilder.setOkActionEnabled((_selectedFile != null && _selectedFile.isDirectory() && _selectedFile.canWrite()) || doc.getText(0, doc.getLength()).trim().length() > 0);
			} catch (BadLocationException ignored) {
			}
		}
	}

	private class MyFileChooserActionListener implements ActionListener {

		public void actionPerformed(final ActionEvent e) {
			final JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			final Component parent = SwingUtilities.getRoot(_path);
			fc.showDialog(parent, "Select");
			_selectedFile = fc.getSelectedFile();
			if (_selectedFile != null && _selectedFile.isDirectory() && _selectedFile.canWrite()) {
				final String newLocation = _selectedFile.getPath();
				_path.setText(newLocation);
				_dialogBuilder.setOkActionEnabled(true);

			}
		}
	}
}
