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
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
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
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"SE_TRANSIENT_FIELD_NOT_RESTORED"})
@SuppressWarnings({"HardCodedStringLiteral"})
public class ImportFileDialog extends JPanel {

	private final JTextField _importFile;
	private File _selectedFile;
	private final transient DialogBuilder _dialogBuilder;
	private final String _importDir;


	public ImportFileDialog(final String defaultValue, final DialogBuilder dialogBuilder) {
		setLayout(new GridBagLayout());

		_importDir = defaultValue;

		final GridBagConstraints c = new GridBagConstraints();
		c.gridy = 1;
		c.insets = new Insets(5, 5, 5, 5);
		c.anchor = GridBagConstraints.NORTHWEST;

		_dialogBuilder = dialogBuilder;

		final Component label = new JLabel("Import BugCollection from: ");

		c.weightx = 0;
		c.gridwidth = 2;
		add(label, c);
		_importFile = new JTextField("");
		_importFile.setEditable(false);
		_importFile.setPreferredSize(new Dimension(200, 20));
		c.weightx = 1;
		c.gridwidth = 1;
		add(_importFile, c);

		final AbstractButton browseButton = new JButton("Browse");
		browseButton.addActionListener(new MyFileChooserActionListener());
		c.weightx = 0;
		add(browseButton, c);

		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 2;
		c.gridheight = 2;

		dialogBuilder.setCenterPanel(this);

		_importFile.getDocument().addDocumentListener(new MyDocumentAdapter());
		if (!_importFile.getText().isEmpty()) {
			_selectedFile = new File(_importFile.getText());
		}
		_importFile.addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(final HierarchyEvent e) {
				if (_importFile.isVisible()) {
					_dialogBuilder.setOkActionEnabled(validateFile(_importFile.getDocument()));
				}
			}
		});
		_dialogBuilder.setOkActionEnabled(_selectedFile != null && _selectedFile.isDirectory());
	}


	public String getText() {
		return _importFile.getText();
	}


	public void setText(final String s) {
		_importFile.setText(s);
	}


	private boolean validateFile(final Document doc) {
		try {
			return _selectedFile != null &&
					_selectedFile.isFile() &&
					_selectedFile.canRead() &&
					"xml".equalsIgnoreCase(FileUtilRt.getExtension(_selectedFile.getAbsolutePath())) &&
					!doc.getText(0, doc.getLength()).trim().isEmpty();
		} catch (final BadLocationException ignore) {
			return false;
		}
	}


	private class MyDocumentAdapter extends DocumentAdapter {


		@Override
		protected void textChanged(final DocumentEvent e) {
			final Document doc = e.getDocument();
			_dialogBuilder.setOkActionEnabled(validateFile(doc));
		}
	}

	private class MyFileChooserActionListener implements ActionListener {

		public void actionPerformed(final ActionEvent e) {
      final FileChooserDescriptor descriptor = new FilterFileChooserDescriptor(
          "Select",
          "Select a file to import",
          new FileFilter() {
            @Override
            public boolean accept(final File f) {
              return f.isDirectory() || "xml".equalsIgnoreCase(FileUtilRt.getExtension(f.getAbsolutePath()));
            }

            @Override
            public String getDescription() {
              return "*.xml";
            }
          });

      final Component parent = SwingUtilities.getRoot(_importFile);
      final VirtualFile toSelect = LocalFileSystem.getInstance().findFileByPath(_importDir);
      final VirtualFile chosen = FileChooser.chooseFile(descriptor, parent, null, toSelect);
      if (chosen != null) {
        _selectedFile = VfsUtilCore.virtualToIoFile(chosen);
        final String newLocation = _selectedFile.getPath();
        _importFile.setText(newLocation);
        _dialogBuilder.setOkActionEnabled(true);
      }
		}
	}
}
