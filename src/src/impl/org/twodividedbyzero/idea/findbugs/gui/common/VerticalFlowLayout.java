/*
 * Copyright 2008-2015 Andre Pfeiler
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


import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.io.Serializable;


/**
 * @author Reto Merz<reto.merz@gmail.com>
 * @since 0.9.998
 */
@SuppressWarnings("unused")
public class VerticalFlowLayout implements LayoutManager2, Serializable {

	private HAlignment _hAlignment;
	private VAlignment _vAlignment;
	private int _fillComponent = -1;
	private int _hGap;
	private int _vGap;
	private boolean _hFill;
	private boolean _vFill;
	private boolean _reverse;


	public VerticalFlowLayout() {
		this(HAlignment.Left, VAlignment.Top, 0, 0, true, false);
	}


	public VerticalFlowLayout(final boolean hFill, final boolean vFill) {
		this(HAlignment.Left, VAlignment.Top, 0, 0, hFill, vFill);
	}


	public VerticalFlowLayout(@NotNull final HAlignment hAlign, @NotNull final VAlignment vAlign) {
		this(hAlign, vAlign, 0, 0, true, false);
	}


	public VerticalFlowLayout(
			@NotNull final HAlignment hAlign,
			@NotNull final VAlignment vAlign,
			final int hGap,
			final int vGap,
			final boolean hFill,
			final boolean vFill
	) {

		_hAlignment = hAlign;
		_vAlignment = vAlign;
		setHGap(hGap);
		setVGap(vGap);
		_hFill = hFill;
		_vFill = vFill;
	}


	public int getFillComponent() {
		return _fillComponent;
	}


	public void setFillComponent(final int fillComponent) {
		_fillComponent = fillComponent;
	}


	public int getHGap() {
		return _hGap;
	}


	public void setHGap(final int hGap) {
		if (hGap < 0)
			throw new IllegalArgumentException("hGap must be positive: " + hGap);
		_hGap = hGap;
	}


	public int getVGap() {
		return _vGap;
	}


	public void setVGap(final int vGap) {
		if (vGap < 0)
			throw new IllegalArgumentException("vGap must be positive: " + vGap);
		_vGap = vGap;
	}


	public boolean isHFill() {
		return _hFill;
	}


	public void setHFill(final boolean hFill) {
		_hFill = hFill;
	}


	public boolean isVFill() {
		return _vFill;
	}


	public void setVFill(final boolean vFill) {
		_vFill = vFill;
	}


	public boolean isReverse() {
		return _reverse;
	}


	public void setReverse(final boolean reverse) {
		_reverse = reverse;
	}


	@NotNull
	public HAlignment getHAlignment() {
		return _hAlignment;
	}


	@NotNull
	public VAlignment getVAlignment() {
		return _vAlignment;
	}


	public void setHAlignment(@NotNull final HAlignment hAlignment) {
		_hAlignment = hAlignment;
	}


	public void setVAlignment(@NotNull final VAlignment vAlignment) {
		_vAlignment = vAlignment;
	}


	@Override
	public void addLayoutComponent(final Component comp, final Object constraints) {
	}


	@Override
	public void addLayoutComponent(final String name, final Component comp) {
	}


	@Override
	public void removeLayoutComponent(final Component comp) {
	}


	@Override
	public float getLayoutAlignmentX(final Container parent) {
		return 0.5f;
	}


	@Override
	public float getLayoutAlignmentY(final Container parent) {
		return 0.5f;
	}


	@Override
	public void invalidateLayout(final Container target) {
	}


	@NotNull
	@Override
	public Dimension maximumLayoutSize(@NotNull final Container target) {
		if (_hFill && _vFill)
			return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		else {
			synchronized (target.getTreeLock()) {
				final Insets insets = target.getInsets();
				int height = _vFill ? Integer.MAX_VALUE : 0;
				int width = _hFill ? Integer.MAX_VALUE : 0;
				for (int i = 0; i < target.getComponentCount(); i++) {
					Component c = target.getComponent(i);
					if (c.isVisible()) {
						Dimension d = c.getMaximumSize();
						height = _addInt(height, d.height, _vGap);
						if (width < d.width)
							width = d.width;
					}
				}
				height = _addInt(height, insets.top, insets.bottom, _vGap);
				width = _addInt(width, insets.left, insets.right, 2 * _hGap);
				return new Dimension(width, height);
			}
		}
	}


	@Override
	public void layoutContainer(@NotNull final Container target) {
		synchronized (target.getTreeLock()) {
			final Insets insets = target.getInsets();

			int fillComp = -1;
			int fillHeight = target.getHeight() - insets.top - insets.bottom - _vGap;
			final int width = target.getWidth() - insets.left - insets.right - 2 * _hGap;

			for (int i = 0; i < target.getComponentCount(); i++) {
				final Component c = target.getComponent(i);
				if (c.isVisible()) {
					final Dimension d = c.getPreferredSize();
					if ((i == _fillComponent) || ((i == (target.getComponentCount() - 1)) && fillComp == -1) && _vFill) {
						fillComp = i;
						fillHeight -= _vGap;
					} else
						fillHeight -= d.height + _vGap;
				}
			}
			if (fillComp == -1 && _vFill)
				fillComp = target.getComponentCount();

			int top = insets.top + _vGap;
			final int left = insets.left + _hGap;
			if (!_vFill) {
				switch (_vAlignment) {
					case Middle:
						top = insets.top + _vGap + fillHeight / 2;
						break;
					case Bottom:
						top = insets.top + _vGap + fillHeight;
						break;
				}
			}
			for (int i = _reverse ? target.getComponentCount() - 1 : 0; _reverse ? i >= 0 : i < target.getComponentCount(); i += _reverse ? -1 : 1) {
				final Component c = target.getComponent(i);
				if (c.isVisible()) {
					final Dimension d = c.getPreferredSize();
					final int h = i == fillComp ? fillHeight : d.height;
					final int w = _hFill ? width : d.width;
					int l = left;
					if (!_hFill) {
						switch (_hAlignment) {
							case Center:
								l = left + (width - d.width) / 2;
								break;
							case Right:
								l = left + width - d.width;
								break;
						}
					}
					c.setBounds(l, top, w, h);
					top += h;
					top += _vGap;
				}
			}
		}
	}


	@NotNull
	@Override
	public Dimension minimumLayoutSize(@NotNull final Container target) {
		synchronized (target.getTreeLock()) {
			final Insets insets = target.getInsets();
			int height = 0;
			int width = 0;
			for (int i = 0; i < target.getComponentCount(); i++) {
				final Component c = target.getComponent(i);
				if (c.isVisible()) {
					final Dimension d = c.getMinimumSize();
					height = _addInt(height, d.height, _vGap);
					if (width < d.width)
						width = d.width;
				}
			}
			height = _addInt(height, insets.top, insets.bottom, _vGap);
			width = _addInt(width, insets.left, insets.right, 2 * _hGap);
			return new Dimension(width < 0 ? 0 : width, height < 0 ? 0 : height);
		}
	}


	@NotNull
	@Override
	public Dimension preferredLayoutSize(@NotNull final Container target) {
		synchronized (target.getTreeLock()) {
			final Insets insets = target.getInsets();
			int height = 0;
			int width = 0;
			for (int i = 0; i < target.getComponentCount(); i++) {
				final Component c = target.getComponent(i);
				if (c.isVisible()) {
					final Dimension d = c.getPreferredSize();
					height = _addInt(height, d.height, _vGap);
					if (width < d.width)
						width = d.width;
				}
			}
			height = _addInt(height, insets.top, insets.bottom, _vGap);
			width = _addInt(width, insets.left, insets.right, 2 * _hGap);
			return new Dimension(width < 0 ? 0 : width, height < 0 ? 0 : height);
		}
	}


	private static int _addInt(final int i1, final int... args) {
		long result = i1;
		if(args != null) {
			for(int arg : args)
				result += arg;
		}
		return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : result < Integer.MIN_VALUE ? Integer.MIN_VALUE : (int) result;
	}
}