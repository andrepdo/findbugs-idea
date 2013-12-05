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

import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
public class CustomLineBorder implements Border {

	private final Color _lineColor;
	private final int _top;
	private final int _left;
	private final int _bottom;
	private final int _right;


	public CustomLineBorder(final int top, final int left, final int bottom, final int right) {
		this(UIManager.getColor("activeCaptionBorder"), top, left, bottom, right);
	}


	public CustomLineBorder(final Color lineColor, final int top, final int left, final int bottom, final int right) {
		_lineColor = lineColor;
		_top = top;
		_left = left;
		_bottom = bottom;
		_right = right;
	}


	public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width, final int height) {
		final Color oldColor = g.getColor();
		g.setColor(_lineColor);
		int i;
		for (i = 0; i < _top; i++) {
			g.drawLine(x, y + i, width, y + i);
		}
		for (i = 0; i < _left; i++) {
			g.drawLine(x + i, y, x + i, height);
		}
		for (i = 0; i < _bottom; i++) {
			g.drawLine(x, height - i - 1, width, height - i - 1);
		}
		for (i = 0; i < _right; i++) {
			g.drawLine(width - i - 1, y, width - i - 1, height);
		}
		g.setColor(oldColor);
	}


	public Insets getBorderInsets(final Component c) {
		return new Insets(_top, _left, _bottom, _right);
	}


	public boolean isBorderOpaque() {
		return true;
	}
}
