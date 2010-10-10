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
package org.twodividedbyzero.idea.findbugs.gui.toolwindow.view;

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


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.96
 */
@SuppressWarnings({"HardCodedStringLiteral", "AnonymousInnerClass"})
class PreviewPanel {

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
				release();
				_delegate.add(NO_BUG_SELECTED_LABEL, BorderLayout.CENTER);
				final Dimension preferredSize = NO_BUG_SELECTED_LABEL.getPreferredSize();
				preferredSize.width = _delegate.getPreferredSize().width;
				NO_BUG_SELECTED_LABEL.setPreferredSize(preferredSize);
				_delegate.setVisible(true);
				_delegate.revalidate();
				_delegate.repaint();
			}
		});
		_labelPanel.add(closeButton, "5, 1, 5, 1, R, T");

		_delegate.add(NO_BUG_SELECTED_LABEL, BorderLayout.CENTER);
		_delegate.add(_labelPanel, BorderLayout.NORTH);
	}


	public void add(final Editor editor, final PsiFile psiFile) {
		releaseEditor();
		_delegate.setVisible(true);
		_delegate.removeAll();
		_editor = editor;
		_psiFile = psiFile;
		final JComponent editorComponent = editor.getComponent();
		_delegate.add(editorComponent, BorderLayout.CENTER);
		_delegate.add(_labelPanel, BorderLayout.NORTH);
		_label.setText(psiFile.getName() + ":");

		_delegate.revalidate();
		_delegate.repaint();
	}


	public void releaseEditor() {
		if(_editor != null) {
			EditorFactory.getInstance().releaseEditor(_editor);
			_editor = null;
			_psiFile = null;
		}
	}


	public void release() {
		releaseEditor();
		_delegate.setVisible(false);
		_delegate.removeAll();
	}


	@Nullable
	public Editor getEditor() {
		return _editor;
	}


	@Nullable
	public PsiFile getPsiFile() {
		return _psiFile;
	}


	void setLabelText(final String text) {
		_label.setText(text);
	}


	public void adaptSize(final int width, final int height) {
		//final int newWidth = (int) (width * 0.4);
		_delegate.setPreferredSize(new Dimension(width, height));
		_delegate.setSize(new Dimension(width, height));
		_delegate.validate();
	}


	public JComponent getComponent() {
		return _delegate;
	}
}
