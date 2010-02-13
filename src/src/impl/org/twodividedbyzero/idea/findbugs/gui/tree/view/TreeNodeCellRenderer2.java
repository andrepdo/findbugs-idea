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
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Map;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.7-dev
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class TreeNodeCellRenderer2 extends JPanel implements TreeCellRenderer {

	public static final BasicStroke _stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[] {1, 1}, 0);

	/** Is the value currently selected. */
	protected boolean _selected;

	/** True if has focus. */
	protected boolean _hasFocus;
	/** True if draws focus border around icon as well. */
	protected boolean _drawsFocusBorderAroundIcon;

	// Colors
	/** Color to use for the foreground for selected nodes. */
	protected Color _textSelectionColor;

	/** Color to use for the foreground for non-selected nodes. */
	protected Color _textNonSelectionColor;

	/** Color to use for the background when a node is selected. */
	protected Color _backgroundSelectionColor;

	/** Color to use for the background when the node is not selected. */
	protected Color _backgroundNonSelectionColor;

	/** Color to use for the background when the node is not selected. */
	protected Color _borderSelectionColor;

	protected Color _hitsForegroundColor;

	/** Map to use for rendering included images. */
	protected Map<?, ?> _map;

	protected int _hGap = 4;

	protected JLabel _icon;
	protected ValueLabel _title;
	protected ValueLabel _hits;


	/** Create a new cell renderer. */
	public TreeNodeCellRenderer2() {
		super();
		setOpaque(false);
		//_map = map;

		setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
		setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
		setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
		setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
		setBorderSelectionColor(UIManager.getColor("Tree.selectionBorderColor"));
		final Object value = UIManager.get("Tree.drawsFocusBorderAroundIcon");
		setHitsForegroundColor(Color.GRAY);
		_drawsFocusBorderAroundIcon = (value != null && (Boolean) value);

		//noinspection ThisEscapedInObjectConstruction
		//setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setLayout(new BorderLayout(_hGap, 0));
		_icon = new JLabel();
		//_icon.setHorizontalAlignment(JLabel.LEFT);

		_title = new ValueLabel();
		_title.setFont(getFont());
		//_title.setHorizontalAlignment(JLabel.LEFT);

		_hits = new ValueLabel();
		_hits.setForeground(Color.GRAY);
		_hits.setFont(getFont());
		//_hits.setHorizontalAlignment(JLabel.RIGHT);

		//add(Box.createRigidArea(new Dimension(4, 0)));
		add(_icon, BorderLayout.LINE_START);
		//add(_icon);
		//add(Box.createRigidArea(new Dimension(4, 0)));
		add(_title, BorderLayout.CENTER);
		//add(_title);
		//add(Box.createRigidArea(new Dimension(4, 0)));
		add(_hits, BorderLayout.LINE_END);
		//add(_hits);
	}


	/*@Override
	public String toString() {
		return "TreeNodeCellRenderer2{" + "_icon=" + _icon + ", _title=" + _title + ", _hits=" + _hits + '}';
	}*/


	public void setHitsForegroundColor(final Color color) {
		_hitsForegroundColor = color;
	}


	public Color getHitsForegroundColor() {
		return _hitsForegroundColor;
	}


	/** Sets the color the text is drawn with when the node is selected. */
	public void setTextSelectionColor(final Color newColor) {
		_textSelectionColor = newColor;
	}


	/** Returns the color the text is drawn with when the node is selected. */
	public Color getTextSelectionColor() {
		return _textSelectionColor;
	}


	/** Sets the color the text is drawn with when the node is not selected. */
	public void setTextNonSelectionColor(final Color newColor) {
		_textNonSelectionColor = newColor;
	}


	/** Returns the color the text is drawn with when the node is not selected. */
	public Color getTextNonSelectionColor() {
		return _textNonSelectionColor;
	}


	/** Sets the color to use for the background if the node is selected. */
	public void setBackgroundSelectionColor(final Color newColor) {
		_backgroundSelectionColor = newColor;
	}


	/** Returns the color to use for the background if the node is selected. */
	public Color getBackgroundSelectionColor() {
		return _backgroundSelectionColor;
	}


	/** Sets the background color to be used for unselected nodes. */
	public void setBackgroundNonSelectionColor(final Color newColor) {
		_backgroundNonSelectionColor = newColor;
	}


	/** Returns the background color to be used for unselected nodes. */
	public Color getBackgroundNonSelectionColor() {
		return _backgroundNonSelectionColor;
	}


	/** Sets the color to use for the border. */
	public void setBorderSelectionColor(final Color newColor) {
		_borderSelectionColor = newColor;
	}


	/** Returns the the border color. */
	public Color getBorderSelectionColor() {
		return _borderSelectionColor;
	}


	/** Subclassed to only accept the font if it is not a FontUIResource. */
	/*@Override
	public void setFont(Font font) {
		if (font instanceof FontUIResource) {
			//noinspection AssignmentToMethodParameter,AssignmentToNull
			font = null;
		}
		if (font != null) {
			if (_title != null) {
				_title.setFont(font);
			}

			if (_hits != null) {
				_hits.setFont(font);
			}
		}
		super.setFont(font);
	}*/


	/** Subclassed to only accept the color if it is not a ColorUIResource. */
	/*@Override
	public void setBackground(Color color) {
		if (color instanceof ColorUIResource) {
			//noinspection AssignmentToMethodParameter,AssignmentToNull
			color = null;
		}
		super.setBackground(color);
	}*/


	/** {@inheritDoc} */
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
			_hits.setForeground(getHitsForegroundColor());
			_hits.setBackground(bColor);
			_title.setForeground(getTextNonSelectionColor());
			_title.setBackground(bColor);
		}

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
				setHits(bugCount == -1 ? "" : " (found " + bugCount + " bug items in " + rootNode.getClassesCount() + " classes)");

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
				setHits(" (" + groupNode.getMemberCount() + " items)");

			} else {
				setIcon(null);
			}
		}

		_selected = selected;
		_hasFocus = hasFocus;

		updateBounds();

		return this;
	}


	/*@Override
	public void paintComponent(final Graphics g) {
		g.setColor(getBackground());

		int offset = 0;
		if (_title.getIcon() != null) {
			offset = _title.getIcon().getIconWidth() + _title.getIconTextGap();
		}

		g.fillRect(offset, 0, (getWidth() - 1 - offset), (getHeight() - 1));

		if (_selected) {
			g.setColor(UIManager.getColor("Tree.selectionBorderColor"));  // NON-NLS
			g.drawRect(offset, 0, (getWidth() - 1 - offset), (getHeight() - 1));
		}

		super.paintComponent(g);
	}*/


	/** Paints the value.  The background is filled based on selected color. */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
			value = "BC_UNCONFIRMED_CAST",
			justification = "")
	@Override
	public void paint(final Graphics g) {
		Color bColor;
		final Graphics2D g2d = (Graphics2D) g;

		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setStroke(_stroke);

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
		super.paintChildren(g);

	}


	/*@Override
	public Dimension getPreferredSize() {
		return updateBounds();
	}*/


	private void updateBounds() {
		/*invalidate();
		_icon.invalidate();
		_title.invalidate();
		_hits.invalidate();*/

		final Dimension size = _icon.getPreferredSize();
		size.width += _title.getPreferredSize().width;
		//size.width += _title.getFontMetrics(new Font(getFont().getName(), Font.BOLD, getFont().getSize())).stringWidth(_title.getText()) + 10;
		size.width += _hits.getPreferredSize().width;
		size.width += _hGap; // BorderLayout hGap
		size.height += 2;

		setSize(size);
		setPreferredSize(size);
		synchronized (getTreeLock()) {
			validateTree();
		}

		//eturn size;
	}


	private int getIconLabelStart() {
		return _icon.getWidth() + (_hGap - 2); // _icon.getX();
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


	private static class ValueLabel extends JLabel {

		private Graphics2D _g2D;


		@Override
		public void addNotify() {
			super.addNotify();
			_g2D = (Graphics2D) getGraphics().create();
		}


		/*@Override
		public void paintComponent(final Graphics g) {
			super.paintComponent(g);
			final Insets insets = getInsets();
			g.translate(insets.left, insets.top);
			final int baseline = 150;

			g.setFont(getFont());
			final FontMetrics fm = g.getFontMetrics();

			// Center line
			final int width = getWidth() - insets.right;
			final int stringWidth = fm.stringWidth(getText());
			final int x = (width - stringWidth) / 2;

			g.drawString(getText(), x, baseline);

			final Graphics2D g2d = (Graphics2D) g;
			final FontRenderContext frc = g2d.getFontRenderContext();
			final Shape shape = getFont().getStringBounds(getText(), frc);
			g.translate(x, baseline);
			g2d.draw(shape);
			g.translate(-x, -baseline);
			g.translate(-insets.left, -insets.top);
		}*/


		/*@Override
		public void setPreferredSize(final Dimension preferredSize) {
			super.setPreferredSize(preferredSize);
		}*/


		@Override
		public Dimension getPreferredSize() {
			final Dimension size = super.getPreferredSize();
			//size.width = SwingUtilities.computeStringWidth(getFontMetrics(getFont()), getText());
			final FontMetrics fm = getFontMetrics(getFont());
			//size.width = _g2D != null ? (int) fm.getStringBounds(getText() + 5, _g2D).getBounds2D().getWidth() : fm.stringWidth(getText()) + 5;
			size.width = fm.stringWidth(getText()) + getInsets().left + getInsets().right + 5;

			/*if (_g2D != null) {

				final Rectangle2D r = _g2D.getFontMetrics(getFont()).getStringBounds(getText(), _g2D);
				//size.width = getStringWidth(_g2D, getText());
				return new Dimension((int) r.getWidth(), (int) r.getHeight());
			}*/
			/*if (_g2D != null) {
				final TextLayout textLayout = new TextLayout(getText(), getFont(), _g2D.getFontRenderContext());
				final Rectangle2D bounds = textLayout.getBounds();
				final int width = (int) Math.ceil(bounds.getWidth()) + 5;
				//final int height = (int) Math.ceil(bounds.getHeight());
				size.width = width;
			}*/

			/*int w = 0;
			int c = 0;
			final StringTokenizer st = new StringTokenizer(getText(), " ");
            while (st.hasMoreTokens()) {
                final String s = st.nextToken();
                w += fm.stringWidth(s);
				c++;
            }

			size = new Dimension(w + (c * fm.stringWidth(" ")) + getInsets().left + getInsets().right + 5, size.height);*/


			//final Graphics2D g2d = (Graphics2D) g;
			//final FontMetrics fm = getFontMetrics(getFont());
			//final Rectangle2D rect = fm.getStringBounds(getText(), getGraphics());
			//final Rectangle2D rect = getFont().getStringBounds(getText(), g2d.getFontRenderContext());
			//final Dimension size = new Dimension((int) rect.getHeight(), (int) rect.getWidth());
			//setSize(size);
			//setPreferredSize(size);
			//setPreferredSize(size);
			return size;
		}
	}


	public static int getStringWidth(final Graphics g, final String s) {
		int maxWidth = 0;

		for (int i = 0, b = 0; i < s.length(); i++) {
			if (s.charAt(i) == ' ' || i == s.length() - 1) {
				final int w = g.getFontMetrics().stringWidth(s.substring(b, i + ((i == s.length() - 1) ? 1 : 0)));

				maxWidth = (maxWidth > w) ? maxWidth : w;

				b = i + 1;
			}
		}

		return maxWidth;
	}

	/** Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for more information. */
	/*@Override
	protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
		// Strings get interned...
		if ("text".equals(propertyName)) {  // NON-NLS
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