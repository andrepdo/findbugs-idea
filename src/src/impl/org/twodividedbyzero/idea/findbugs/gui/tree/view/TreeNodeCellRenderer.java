/*
 * Copyright 2010 Andre Pfeiler
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

import info.clearthought.layout.TableLayout;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceGroupNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.RootNode;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.7-dev
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class TreeNodeCellRenderer extends JPanel implements TreeCellRenderer/*, TreeCellEditor*/ {

	private static final BasicStroke _stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[] {1, 1}, 0);

	private boolean _selected;
	private boolean _hasFocus;
	private final boolean _drawsFocusBorderAroundIcon;
	private Color _textSelectionColor;
	private Color _textNonSelectionColor;
	private Color _backgroundSelectionColor;
	private Color _backgroundNonSelectionColor;
	private Color _borderSelectionColor;
	private Color _hitsForegroundColor;
	private final int _hGap = 4;

	private final JLabel _icon;
	private final ValueLabel _title;
	private final ValueLabel _hits;
	private final ValueLabel _link;


	public TreeNodeCellRenderer() {
		setOpaque(false);

		setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
		setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
		setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
		setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
		setBorderSelectionColor(UIManager.getColor("Tree.selectionBorderColor"));
		final Object value = UIManager.get("Tree.drawsFocusBorderAroundIcon");
		setHitsForegroundColor(Color.GRAY);
		_drawsFocusBorderAroundIcon = value != null && (Boolean) value;

		final double border = 0;
		final double rowsGap = 0;
		final double colsGap = _hGap;
		final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.PREFERRED, colsGap, TableLayout.PREFERRED, colsGap, TableLayout.PREFERRED, border}, // Columns
								 {border, TableLayout.PREFERRED, border}};// Rows
		final LayoutManager tbl = new TableLayout(size);
		setLayout(tbl);

		//setLayout(new BorderLayout(_hGap, 0));
		_icon = new JLabel();

		_title = new ValueLabel();
		_title.setFont(getFont());

		_hits = new ValueLabel();
		_hits.setForeground(Color.GRAY);
		_hits.setFont(getFont());

		_link = new ValueLabel();
		_link.setForeground(Color.GRAY);
		_link.setFont(getFont());

		add(_icon, "1, 1, 1, 1");
		add(_title, "3, 1, 3, 1");
		add(_hits, "5, 1, 5, 1");
		add(_link, "7, 1, 7, 1");

		/*add(_icon, BorderLayout.LINE_START);
		add(_title, BorderLayout.CENTER);
		add(_hits, BorderLayout.LINE_END);*/
		//add(_link, BorderLayout.LINE_END);

	}


	final void setHitsForegroundColor(final Color color) {
		_hitsForegroundColor = color;
	}


	Color getHitsForegroundColor() {
		return _hitsForegroundColor;
	}


	final void setTextSelectionColor(final Color newColor) {
		_textSelectionColor = newColor;
	}


	Color getTextSelectionColor() {
		return _textSelectionColor;
	}


	final void setTextNonSelectionColor(final Color newColor) {
		_textNonSelectionColor = newColor;
	}


	Color getTextNonSelectionColor() {
		return _textNonSelectionColor;
	}


	final void setBackgroundSelectionColor(final Color newColor) {
		_backgroundSelectionColor = newColor;
	}


	Color getBackgroundSelectionColor() {
		return _backgroundSelectionColor;
	}


	final void setBackgroundNonSelectionColor(final Color newColor) {
		_backgroundNonSelectionColor = newColor;
	}


	Color getBackgroundNonSelectionColor() {
		return _backgroundNonSelectionColor;
	}


	final void setBorderSelectionColor(final Color newColor) {
		_borderSelectionColor = newColor;
	}


	public Color getBorderSelectionColor() {
		return _borderSelectionColor;
	}


	public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
		if (_title.getFont().getStyle() == Font.BOLD) {
			_title.setFont(getFont());
		}

		if (selected && hasFocus) {
			_hits.setForeground(getTextNonSelectionColor());
			_hits.setBackground(getBackgroundSelectionColor());
			_title.setForeground(getTextSelectionColor());
			_title.setBackground(getBackgroundSelectionColor());
		} else {
			Color bColor = getBackgroundNonSelectionColor();
			if (bColor == null) {
				bColor = getBackground();
			}

			// Path highlighting
			final TreePath path = tree.getPathForRow(row);
			final TreePath selectionPath = tree.getSelectionPath();
			if (selectionPath != null && selectionPath.getPathCount() > 1) {
				final TreePath leadParentPath = new TreePath(new Object[] {selectionPath.getPathComponent(0), selectionPath.getPathComponent(1)});

				if (tree.getRowForPath(leadParentPath) > 0 && leadParentPath.isDescendant(path)) {
					setBackgroundNonSelectionColor(Tree.HIGHLIGHT_COLOR);
				} else {
					setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
				}
			} else {
				setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
			}

			_hits.setForeground(getHitsForegroundColor());
			_hits.setBackground(bColor);
			_title.setForeground(getTextNonSelectionColor());
			_title.setBackground(bColor);
		}

		if (value != null) {
			setLinkHtml("");
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
				setTitle(bugInstanceNode.getSimpleName());
				setHits("");

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
				setTitle(rootNode.getSimpleName());
				final int bugCount = rootNode.getBugCount();
				final int classesCount = rootNode.getClassesCount();
				setHits(bugCount == -1 ? "" : "(found " + bugCount + " bug items in " + classesCount + (classesCount == 1 ? " class)" : " classes)"));
				setLinkHtml(rootNode.getLinkHtml());

			} else if (value instanceof BugInstanceGroupNode) {
				final BugInstanceGroupNode groupNode = (BugInstanceGroupNode) value;
				//final TreePath path = tree.getPathForRow(row);

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
				setTitle(groupNode.getSimpleName());
				final int memberCount = groupNode.getMemberCount();
				setHits("(" + memberCount + (memberCount == 1 ? " item" : " items") + ')');

			} else {
				setIcon(null);
			}
		}

		_selected = selected;
		_hasFocus = hasFocus;

		updateBounds();

		return this;
	}


	/** Paints the value.  The background is filled based on selected color. */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
			value = "BC_UNCONFIRMED_CAST",
			justification = "")
	@Override
	public void paint(final Graphics g) {
		final Graphics2D g2d = (Graphics2D) g;

		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setStroke(_stroke);

		Color bColor;
		if (_selected && _hasFocus) {
			bColor = getBackgroundSelectionColor();
		} else {
			bColor = getBackgroundNonSelectionColor();
			if (bColor == null) {
				bColor = getBackground();
			}
		}
		int imageOffset = -1;
		if (bColor != null) {
			imageOffset = getIconLabelStart();
			g2d.setColor(bColor);
			final Dimension size = getSize();
			g2d.fillRect(imageOffset, 0, size.width - 1 - imageOffset, size.height);
		}
		if (_selected && _hasFocus) {
			if (_drawsFocusBorderAroundIcon) {
				imageOffset = 0;
			} else if (imageOffset == -1) {
				imageOffset = getIconLabelStart();
			}
			g2d.setColor(Color.BLACK);
			g2d.drawRect(imageOffset, 0, getWidth() - 1 - imageOffset, getHeight() - 1);
		} else if (_selected) {
			g2d.setColor(Color.BLACK);
			g2d.drawRect(imageOffset, 0, getWidth() - 1 - imageOffset, getHeight() - 1);
		}
		// call paintChildren and not paint so we don't
		// erase everyting we've already done.
		//super.paintComponent(g);
		paintChildren(g);

	}


	private void updateBounds() {
		final Dimension size = _icon.getPreferredSize();
		size.width += _title.getPreferredSize().width;
		size.width += _hits.getPreferredSize().width;
		if (_link.getText().length() > 0) {
			size.width += 50; //_link
		}
		size.width += _hGap; // BorderLayout hGap
		size.height += 2;

		setSize(size);
		setPreferredSize(size);
		synchronized (getTreeLock()) {
			validateTree();
		}
	}


	private int getIconLabelStart() {
		return _icon.getWidth() + _hGap - 2; // _icon.getX();
	}


	private void setIcon(final Icon icon) {
		_icon.setIcon(icon);
	}


	private void setTitle(final String description) {
		_title.setText(description);
	}


	private void setHits(final String stats) {
		_hits.setText(stats);
	}


	private void setLinkHtml(final String html) {
		_link.setText(html);
	}


	private static class ValueLabel extends JLabel {

		private Graphics2D _g2D;


		@Override
		public void addNotify() {
			super.addNotify();
			_g2D = (Graphics2D) getGraphics().create();
		}


		@Override
		public Dimension getPreferredSize() {
			final Dimension size = super.getPreferredSize();
			final FontMetrics fm = getFontMetrics(getFont());
			size.width = fm.stringWidth(getText()) + getInsets().left + getInsets().right + 5;
			return size;
		}
	}

	/** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. */
	/*@Override
	protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
		// Strings get interned...
		if ("text".equals(propertyName)) {
			super.firePropertyChange(propertyName, oldValue, newValue);
		}
	}


	*//** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. *//*
	@Override
	public void firePropertyChange(final String propertyName, final byte oldValue, final byte newValue) {
	}


	*//** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. *//*
	@Override
	public void firePropertyChange(final String propertyName, final char oldValue, final char newValue) {
	}


	*//** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. *//*
	@Override
	public void firePropertyChange(final String propertyName, final short oldValue, final short newValue) {
	}


	*//** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. *//*
	@Override
	public void firePropertyChange(final String propertyName, final int oldValue, final int newValue) {
	}


	*//** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. *//*
	@Override
	public void firePropertyChange(final String propertyName, final long oldValue, final long newValue) {
	}


	*//** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. *//*
	@Override
	public void firePropertyChange(final String propertyName, final float oldValue, final float newValue) {
	}


	*//** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. *//*
	@Override
	public void firePropertyChange(final String propertyName, final double oldValue, final double newValue) {
	}


	*//** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. *//*
	@Override
	public void firePropertyChange(final String propertyName, final boolean oldValue, final boolean newValue) {
	}*/
}