/**
 * Copyright 2008 Andre Pfeiler
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

import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceGroupNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.RootNode;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;
import java.awt.Component;
import java.awt.Graphics;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class TreeNodeCellRenderer extends JLabel implements TreeCellRenderer {

	private boolean _selected;


	/** Create a new cell renderer. */
	public TreeNodeCellRenderer() {
		super();
		setOpaque(false);
	}


	/** {@inheritDoc} */
	@Override
	public void paintComponent(final Graphics g) {
		g.setColor(getBackground());

		int offset = 0;
		if (getIcon() != null) {
			offset = getIcon().getIconWidth() + getIconTextGap();
		}

		g.fillRect(offset, 0, (getWidth() - 1 - offset), (getHeight() - 1));

		if (_selected) {
			g.setColor(UIManager.getColor("Tree.selectionBorderColor"));
			g.drawRect(offset, 0, (getWidth() - 1 - offset), (getHeight() - 1));
		}

		super.paintComponent(g);
	}


	/** {@inheritDoc} */
	public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
		_selected = selected;

		if (value != null) {

			if (value instanceof BugInstanceNode) {
				final BugInstanceNode bugInstanceNode = (BugInstanceNode) value;

				if (expanded) {
					final Icon expandedIcon = bugInstanceNode.getExpandedIcon();
					((MaskIcon) expandedIcon).setColorPainted(selected);
					setIcon(expandedIcon);
				} else {
					final Icon collapsedIcon = bugInstanceNode.getCollapsedIcon();
					((MaskIcon) collapsedIcon).setColorPainted(selected);
					setIcon(collapsedIcon);
				}

				setToolTipText(bugInstanceNode.getTooltip());
				setText(bugInstanceNode.toString());
				//validate();

			} else if (value instanceof RootNode) {
				final RootNode rootNode = (RootNode) value;

				if (expanded) {
					final Icon expandedIcon = rootNode.getExpandedIcon();
					((MaskIcon) expandedIcon).setColorPainted(selected);
					setIcon(expandedIcon);
				} else {
					final Icon collapsedIcon = rootNode.getCollapsedIcon();
					((MaskIcon) collapsedIcon).setColorPainted(selected);
					setIcon(collapsedIcon);
				}

				setToolTipText(rootNode.getTooltip());
				setText(rootNode.toString());
				//validate();

			} else if (value instanceof BugInstanceGroupNode) {
				final BugInstanceGroupNode groupNode = (BugInstanceGroupNode) value;

				if (expanded) {
					final Icon expandedIcon = groupNode.getExpandedIcon();
					((MaskIcon) expandedIcon).setColorPainted(selected);
					setIcon(expandedIcon);
				} else {
					final Icon collapsedIcon = groupNode.getCollapsedIcon();
					((MaskIcon) collapsedIcon).setColorPainted(selected);
					setIcon(collapsedIcon);
				}

				setToolTipText(groupNode.getTooltip());
				setText(groupNode.toString());
				//validate();
				//invalidate();

			} else {
				setIcon(null);
			}
		}

		//setDoubleBuffered(true);
		setFont(tree.getFont());
		//repaint();
		validate();

		setForeground(UIManager.getColor(selected ? "Tree.selectionForeground" : "Tree.textForegound"));
		setBackground(UIManager.getColor(selected ? "Tree.selectionBackground" : "Tree.textBackground"));


		return this;
	}


	/** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. */
	@Override
	protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
		// Strings get interned...
		if ("text".equals(propertyName)) {
			super.firePropertyChange(propertyName, oldValue, newValue);
		}
	}


	/** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. */
	@Override
	public void firePropertyChange(final String propertyName, final byte oldValue, final byte newValue) {
	}


	/** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. */
	@Override
	public void firePropertyChange(final String propertyName, final char oldValue, final char newValue) {
	}


	/** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. */
	@Override
	public void firePropertyChange(final String propertyName, final short oldValue, final short newValue) {
	}


	/** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. */
	@Override
	public void firePropertyChange(final String propertyName, final int oldValue, final int newValue) {
	}


	/** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. */
	@Override
	public void firePropertyChange(final String propertyName, final long oldValue, final long newValue) {
	}


	/** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. */
	@Override
	public void firePropertyChange(final String propertyName, final float oldValue, final float newValue) {
	}


	/** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. */
	@Override
	public void firePropertyChange(final String propertyName, final double oldValue, final double newValue) {
	}


	/** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. */
	@Override
	public void firePropertyChange(final String propertyName, final boolean oldValue, final boolean newValue) {
	}
}
