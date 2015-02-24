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
package org.twodividedbyzero.idea.findbugs.gui.tree.view;

import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;

import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
public abstract class Tree extends JTree {

	private final TreeQuickSearch _quickSearch;
	private TreePath _highlightPath;
	public static final Color NON_HIGHLIGHT_COLOR = UIManager.getColor("Tree.textBackground");
	private TreePath _lastHighlightPath;


	public Tree(final TreeModel treeModel) {
		super(treeModel);
		_quickSearch = TreeQuickSearch.install(this);
		ViewTooltips.register(this);
	}


	//@Override
	//public native TreePath getNextMatch(String s, int i, javax.swing.text.Position.Bias bias);


	//public final native void setLineStyleAngled();

	/*@Override
	public TreePath getNextMatch(String prefix, int startingRow, Position.Bias bias) {
		return null;
	}*/


	public TreePath getHighlightPath() {
		return _highlightPath;
	}


	public void setHighlightPath(@Nullable final TreePath highlightPath) {
		_highlightPath = highlightPath;
		treeDidChange();
	}


	@Override
	protected void paintComponent(final Graphics g) {
		// paint background your self
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());

		if (_highlightPath != null && getRowForPath(_highlightPath) > 0) {
			paintHighlightForPath(g, _highlightPath, GuiResources.HIGHLIGHT_COLOR_LIGHTER);
			_lastHighlightPath = _highlightPath;

		} else if (_lastHighlightPath != null) {
			paintHighlightForPath(g, _lastHighlightPath, NON_HIGHLIGHT_COLOR);
			_lastHighlightPath = null;
		}

		setOpaque(false); // trick not to paint background
		super.paintComponent(g);
		setOpaque(true);
	}


	private void paintHighlightForPath(final Graphics g, final TreePath highlightPath, final Color highlightColor) {
		// paint the highlight if any

		for (int i = 0; i < highlightPath.getPathCount(); i++) {

		}

		final TreePath leadParentPath = new TreePath(new Object[] {highlightPath.getPathComponent(0), highlightPath.getPathComponent(1)});
		final int fromRow = getRowForPath(leadParentPath);

		if (fromRow != -1) {
			g.setColor(highlightColor);
			int toRow = fromRow;
			final int rowCount = getRowCount();
			while (toRow < rowCount) {
				final TreePath path = getPathForRow(toRow);
				if (leadParentPath.isDescendant(path)) {
					toRow++;
				} else {
					break;
				}
			}
			final Rectangle fromBounds = getRowBounds(fromRow);
			final Rectangle toBounds = getRowBounds(toRow - 1);
			g.fillRect(0, fromBounds.y, getWidth(), toBounds.y - fromBounds.y + toBounds.height);
		}
	}


	public final void showQuickSearchPopup(final String text) {
		_quickSearch.showPopup(text);
	}
}
