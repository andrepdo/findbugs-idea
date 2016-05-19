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
package org.twodividedbyzero.idea.findbugs.gui.toolwindow.view;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.psi.PsiFile;
import info.clearthought.layout.TableLayout;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings({"HardCodedStringLiteral", "AnonymousInnerClass"})
final class PreviewPanel implements Disposable {

	private final JPanel _delegate;
	private final JLabel _label;
	private Editor _editor;
	private final JPanel _labelPanel;
	private PsiFile _psiFile;
	private static final JLabel NO_BUG_SELECTED_LABEL;

	static {
		NO_BUG_SELECTED_LABEL = new JLabel("Select a bug to preview", GuiResources.FINDBUGS_ICON, SwingConstants.CENTER);
	}


	PreviewPanel() {
		_delegate = new JPanel(new BorderLayout());
		_label = new JLabel();
		_label.setFont(_label.getFont().deriveFont(Font.BOLD));

		final double border = 5;
		final double colsGap = 2;
		final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.PREFERRED, colsGap, TableLayout.FILL, border}, // Columns
				{border, TableLayout.PREFERRED, border}};// Rows
		final LayoutManager tbl = new TableLayout(size);
		_labelPanel = new JPanel(tbl);
		_labelPanel.add(new JLabel("Preview"), "1, 1, 1, 1");
		_labelPanel.add(_label, "3, 1, 3, 1");

		final AbstractButton closeButton = new JButton(GuiResources.CLOSE_EDITOR_ICON);
		closeButton.setToolTipText("close preview");
		closeButton.setBorderPainted(false);
		closeButton.setFocusable(false);
		closeButton.setFocusPainted(false);
		closeButton.setContentAreaFilled(false);
		closeButton.setRequestFocusEnabled(false);
		closeButton.setRolloverEnabled(false);
		closeButton.setRolloverIcon(GuiResources.CLOSE_EDITOR_HOVER_ICON);
		closeButton.setRolloverSelectedIcon(GuiResources.CLOSE_EDITOR_HOVER_ICON);
		closeButton.setPressedIcon(GuiResources.CLOSE_EDITOR_HOVER_ICON);

		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				clear();
			}
		});
		_labelPanel.add(closeButton, "5, 1, 5, 1, R, T");

		_delegate.add(NO_BUG_SELECTED_LABEL, BorderLayout.CENTER);
		_delegate.add(_labelPanel, BorderLayout.NORTH);
	}

	void add(final Editor editor, final PsiFile psiFile) {
		releaseEditor();
		_delegate.setVisible(true);
		_delegate.removeAll();
		_editor = editor;
		_psiFile = psiFile;
		final JComponent editorComponent = editor.getComponent();
		_delegate.add(editorComponent, BorderLayout.CENTER);
		_delegate.add(_labelPanel, BorderLayout.NORTH);
		_label.setText(psiFile.getName() + ':');

		_delegate.revalidate();
		_delegate.repaint();
	}

	private void releaseEditor() {
		if (_editor != null) {
			EditorFactory.getInstance().releaseEditor(_editor);
			_editor = null;
			_psiFile = null;
		}
	}

	void release() {
		releaseEditor();
		_delegate.setVisible(false);
		_delegate.removeAll();
	}

	@Nullable
	Editor getEditor() {
		return _editor;
	}

	@Nullable
	PsiFile getPsiFile() {
		return _psiFile;
	}

	void setLabelText(final String text) {
		_label.setText(text);
	}

	void adaptSize(final int width, final int height) {
		//final int newWidth = (int) (width * 0.4);
		_delegate.setPreferredSize(new Dimension(width, height));
		_delegate.setSize(new Dimension(width, height));
		_delegate.validate();
	}

	JComponent getComponent() {
		return _delegate;
	}

	void clear() {
		release();
		_delegate.add(NO_BUG_SELECTED_LABEL, BorderLayout.CENTER);
		final Dimension preferredSize = NO_BUG_SELECTED_LABEL.getPreferredSize();
		preferredSize.width = _delegate.getPreferredSize().width;
		NO_BUG_SELECTED_LABEL.setPreferredSize(preferredSize);
		_delegate.setVisible(true);
		_delegate.revalidate();
		_delegate.repaint();
	}

	@Override
	public void dispose() {
		// Otherwise IDEA will log a warning on IDEA shutdown (at least with IDEA 2016.1.2).
		releaseEditor();
	}
}
