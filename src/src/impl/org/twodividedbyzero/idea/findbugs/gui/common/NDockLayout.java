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

/**
 * Layout Manager to control positions of docked toolbars
 *
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */

import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import java.awt.AWTError;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;


public class NDockLayout extends BorderLayout {

	private static final long serialVersionUID = 0L;

	private final ArrayList<Component> _north = new ArrayList<Component>(1);
	private final ArrayList<Component> _south = new ArrayList<Component>(1);
	private final ArrayList<Component> _east = new ArrayList<Component>(1);
	private final ArrayList<Component> _west = new ArrayList<Component>(1);
	private Component _center;
	private int _northHeight;
	private int _southHeight;
	private int _eastWidth;
	private int _westWidth;

	private final Object[] _embeddedComponents = new Object[4];
	private static final int TOP = SwingConstants.TOP;
	private static final int BOTTOM = SwingConstants.BOTTOM;
	private static final int LEFT = SwingConstants.LEFT;
	private static final int RIGHT = SwingConstants.RIGHT;


	public NDockLayout() {
		_embeddedComponents[0] = _north;
		_embeddedComponents[1] = _south;
		_embeddedComponents[2] = _west;
		_embeddedComponents[3] = _east;
	}


	@Override
	public void addLayoutComponent(final Component component, final Object con) {
		synchronized (component.getTreeLock()) {
			if (con != null) {
				final String s = con.toString();
				component.setVisible(true);
				if (s.equals(NORTH)) {
					_north.add(component);
				} else if (s.equals(SOUTH)) {
					_south.add(component);
				} else if (s.equals(EAST)) {
					_east.add(component);
				} else if (s.equals(WEST)) {
					_west.add(component);
				} else if (s.equals(CENTER)) {
					_center = component;
				}
				//component.getParent().invalidate();
				component.getParent().validate();
			} else {
				throw new IllegalArgumentException("cannot add to layout: constraint must be a string.");
			}
		}
	}


	@Override
	public void removeLayoutComponent(final Component component) {
		_north.remove(component);
		_south.remove(component);
		_east.remove(component);
		_west.remove(component);

		if (component.equals(_center)) {
			//noinspection AssignmentToNull
			_center = null;
		}

		flipSeparators(component, SwingConstants.VERTICAL);
		//component.getParent().invalidate();
		component.getParent().validate();
	}


	@Override
	public void layoutContainer(final Container target) {
		synchronized (target.getTreeLock()) {
			final Insets insets = target.getInsets();
			int top = insets.top;
			int bottom = target.getHeight() - insets.bottom;
			int left = insets.left;
			int right = target.getWidth() - insets.right;

			_northHeight = getPreferredDimension(_north).height;
			_southHeight = getPreferredDimension(_south).height;
			_eastWidth = getPreferredDimension(_east).width;
			_westWidth = getPreferredDimension(_west).width;

			layoutComponents(target, _north, left, top, right - left, _northHeight, TOP);
			top += _northHeight + getVgap();

			layoutComponents(target, _south, left, bottom - _southHeight, right - left, _southHeight, BOTTOM);
			bottom -= _southHeight + getVgap();

			layoutComponents(target, _east, right - _eastWidth, top, _eastWidth, bottom - top, RIGHT);
			right -= _eastWidth + getHgap();

			layoutComponents(target, _west, left, top, _westWidth, bottom - top, LEFT);
			left += _westWidth + getHgap();

			if (_center != null) {
				_center.setBounds(left, top, right - left, bottom - top);
			}
		}
	}


	/**
	 * Returns the ideal width for a vertically oriented toolbar and the ideal height for a horizontally oriented tollbar.
	 *
	 * @param components the <code>Component<code>(s) to get the ideal <code>Dimension</code> for
	 * @return Returns the ideal width for a vertically oriented toolbar and the ideal height for a horizontally oriented tollbar.
	 */
	private static Dimension getPreferredDimension(final Iterable<Component> components) {
		@SuppressWarnings("MultipleVariablesInDeclaration")
		int w = 0, h = 0;

		for (final Component comp : components) {
			final Dimension d = comp.getPreferredSize();
			w = Math.max(w, d.width);
			h = Math.max(h, d.height);
		}

		return new Dimension(w, h);
	}


	@SuppressWarnings({"AssignmentToMethodParameter"})
	private void layoutComponents(final Container target, final ArrayList<Component> components, int x, int y, final int w, final int h, final int orientation) {
		int offset = 0;
		Component component = null;

		if (orientation == TOP || orientation == BOTTOM) {

			offset = x;

			int totalWidth = 0;
			int cwidth = 0;
			final int num = components.size();

			for (int i = 0; i < num; i++) {
				component = components.get(i);
				flipSeparators(component, SwingConstants.VERTICAL);
				final int widthSwap = totalWidth;
				final int cwidthSwap = cwidth;
				cwidth = component.getPreferredSize().width;
				totalWidth += cwidth;
				if (w < totalWidth && i != 0) {
					final Component c0 = components.get(i - 1);
					final Rectangle rec = c0.getBounds();
					c0.setBounds(rec.x, rec.y, w - widthSwap + cwidthSwap, rec.height);
					offset = x;
					if (orientation == TOP) {
						y += h;
						_northHeight += h;
					} else if (orientation == BOTTOM) {
						_southHeight += h;
						y -= h;
					}
					totalWidth = cwidth;
				}
				if (i + 1 == num) {
					component.setBounds(x + offset, y, w - totalWidth + cwidth, h);
				} else {
					component.setBounds(x + offset, y, cwidth, h);
					offset += cwidth;
				}
			}

			flipSeparators(component, SwingConstants.VERTICAL);

		} else {
			int totalHeight = 0;
			int cheight = 0;
			final int num = components.size();

			for (int i = 0; i < num; i++) {
				component = components.get(i);
				flipSeparators(component, SwingConstants.HORIZONTAL);
				final int heightSwap = totalHeight;
				final int cheightSwap = cheight;
				cheight = component.getPreferredSize().height;
				totalHeight += cheight;
				if (h < totalHeight && i != 0) {
					final Component c0 = components.get(i - 1);
					final Rectangle rec = c0.getBounds();
					c0.setBounds(rec.x, rec.y, rec.width, h - heightSwap + cheightSwap);
					//offset = y;
					if (orientation == LEFT) {
						x += w;
						_westWidth += w;
					} else if (orientation == RIGHT) {
						_eastWidth += w;
						x -= w;
					}
					totalHeight = cheight;
					offset = 0;
				}

				if (i + 1 == num) {
					component.setBounds(x, y + offset, w, h - totalHeight + cheight);
				} else {
					component.setBounds(x, y + offset, w, cheight);
					offset += cheight;
				}

			}

			flipSeparators(component, SwingConstants.HORIZONTAL);
		}
	}


	private static void flipSeparators(final Component c, final int orientation) {
		if (c != null && c instanceof JToolBar/* && UIManager.getLookAndFeel().getName().toLowerCase().contains("windows")*/) {
			final JToolBar jtb = (JToolBar) c;

			final Component[] comps = jtb.getComponents();
			if (comps != null && comps.length > 0) {
				for (int i = 0; i < comps.length; i++) {
					try {
						final Component component = comps[i];
						if (component != null) {
							if (component instanceof JSeparator) {
								final boolean isVisible = component.isVisible();
								jtb.remove(component);
								final JSeparator separ = new JSeparator();
								separ.setVisible(isVisible);
								if (orientation == SwingConstants.VERTICAL) {
									jtb.setOrientation(SwingConstants.VERTICAL);
									separ.setOrientation(SwingConstants.VERTICAL);
									separ.setMinimumSize(new Dimension(2, 6));
									separ.setPreferredSize(new Dimension(2, 6));
									separ.setMaximumSize(new Dimension(2, 100));
								} else {
									jtb.setOrientation(SwingConstants.HORIZONTAL);
									separ.setOrientation(SwingConstants.HORIZONTAL);
									separ.setMinimumSize(new Dimension(6, 20));
									separ.setPreferredSize(new Dimension(6, 20));
									separ.setMaximumSize(new Dimension(100, 20));
								}
								jtb.add(separ, i);
							}
						}
					} catch (final Exception e) {
						throw new AWTError(e.getMessage());
					}
				}
			}
		}
	}


	public boolean containsImbeddedComp(final Component component) {
		for (final Object curImbeddedTBR : _embeddedComponents) {
			//noinspection unchecked
			if (((Collection<Component>) curImbeddedTBR).contains(component)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Description:
	 * (SwingConstants top,left,bottom,right):
	 * top:1, left:2, bottom:3, right:4
	 *
	 * @param component
	 * @param inx
	 * @return
	 */
	public boolean containsEmbeddedComp(final Component component, final int inx) {
		//noinspection unchecked
		return inx > 0 && inx < 5 && ((Collection<Component>) _embeddedComponents[inx + 1]).contains(component);
	}

}
