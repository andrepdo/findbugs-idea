/**
 * Copyright 2009 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.twodividedbyzero.idea.findbugs.gui.tree.view;

import org.jetbrains.annotations.Nullable;

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

	private TreePath _highlightPath;
	public static final Color HIGHLIGHT_COLOR = new Color(255, 255, 204);
	public static final Color NON_HIGHLIGHT_COLOR = UIManager.getColor("Tree.textBackground");
	private TreePath _lastHighlightPath;


	public Tree(final TreeModel treeModel) {
		super(treeModel);
	}


	//@Override
	//public native TreePath getNextMatch(String s, int i, javax.swing.text.Position.Bias bias);


	//public final native void setLineStyleAngled();

	/*@Override
	public TreePath getNextMatch(String prefix, int startingRow, Position.Bias bias) {
		return null;
	}*/


	@Override
	public void addNotify() {
		super.addNotify();
		ViewTooltips.register(this);
		TreeQuickSearch.install(this);
	}


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
			paintHighlightForPath(g, _highlightPath, HIGHLIGHT_COLOR);
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
}
