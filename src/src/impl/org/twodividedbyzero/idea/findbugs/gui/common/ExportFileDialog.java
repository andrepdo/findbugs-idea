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

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import info.clearthought.layout.TableLayout;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
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
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"SE_TRANSIENT_FIELD_NOT_RESTORED"})
@SuppressWarnings({"HardCodedStringLiteral"})
public class ExportFileDialog extends JPanel {

	private final JLabel _label;
	private final JTextField _path;
	private final JButton _browseButton;
	private File _selectedFile;
	private final transient DialogBuilder _dialogBuilder;
	private final JRadioButton _html;
	private final JRadioButton _xml;
	private final JRadioButton _both;


	public ExportFileDialog(final String defaultValue, final DialogBuilder dialogBuilder) {
		final double border = 5;
		final double rowsGap = 0;
		final double colsGap = 10;
		final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.FILL, colsGap, TableLayout.PREFERRED, border}, // Columns
								 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
		final LayoutManager tbl = new TableLayout(size);
		setLayout(tbl);

		_dialogBuilder = dialogBuilder;

		_label = new JLabel("Directory: ");
		add(_label, "1, 1, 1, 1");
		
		_path = new JTextField(defaultValue);
		add(_path, "3, 1, 3, 1");

		_browseButton = new JButton("Browse");
		_browseButton.addActionListener(new MyFileChooserActionListener());
		add(_browseButton, "5, 1, 5, 1");

		add(new JLabel("Format"), "1, 3, 1, 3");

		_html = new JRadioButton("HTML", false);
		_xml = new JRadioButton("XML", false);
		_both = new JRadioButton("Both (XML & HTML)", true);
		final ButtonGroup group = new ButtonGroup();
		group.add(_html);
		group.add(_xml);
		group.add(_both);

		final Container buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonsPanel.add(_html);
		buttonsPanel.add(_xml);
		buttonsPanel.add(_both);

		add(buttonsPanel, "3 3, 3, 3");

		dialogBuilder.setCenterPanel(this);

		_path.getDocument().addDocumentListener(new MyDocumentAdapter());
		if (!_path.getText().isEmpty()) {
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


	public boolean isBoth() {
		return _both.isSelected();
	}


	private boolean validateDirectory(final Document doc) {
		try {
			return _selectedFile != null && _selectedFile.isDirectory() && _selectedFile.canWrite() || !doc.getText(0, doc.getLength()).trim().isEmpty();
		} catch (final BadLocationException ignore) {
			return false;
		}
	}


	private class MyDocumentAdapter extends DocumentAdapter {


		@Override
		protected void textChanged(final DocumentEvent e) {
			final Document doc = e.getDocument();
			_dialogBuilder.setOkActionEnabled(validateDirectory(doc));
		}
	}

	private class MyFileChooserActionListener implements ActionListener {

		public void actionPerformed(final ActionEvent e) {
      final FileChooserDescriptor descriptor = new FilterFileChooserDescriptor(
          "Select",
          "Select an export directory");
      final VirtualFile toSelect = LocalFileSystem.getInstance().findFileByPath(_path.getText());
      final Component parent = SwingUtilities.getRoot(_path);
      final VirtualFile chosen = FileChooser.chooseFile(descriptor, parent, null, toSelect);
      if (chosen != null) {
        final File selectedFile = VfsUtilCore.virtualToIoFile(chosen);
        if (selectedFile.isDirectory() && selectedFile.canWrite()) {
          _selectedFile = selectedFile;
				  final String newLocation = _selectedFile.getPath();
				  _path.setText(newLocation);
				  _dialogBuilder.setOkActionEnabled(true);
        }
			}
		}
	}
}
