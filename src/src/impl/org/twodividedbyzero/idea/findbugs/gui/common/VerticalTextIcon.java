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

import com.intellij.ui.JBColor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;


@SuppressWarnings("MagicNumber")
public class VerticalTextIcon implements Icon, SwingConstants {

	private final Font _font = UIManager.getFont("Label.font");
	private final FontMetrics _fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(_font);

	private final String _text;
	@Nullable private final Icon _icon;
	private final int _width;
	private final int _height;
	private final boolean _clockwise;


	public VerticalTextIcon(final String text, final boolean clockwise, @Nullable final Icon icon) {
		_text = text;
		_icon = icon;
		_width = SwingUtilities.computeStringWidth(_fontMetrics, text) + (icon != null ? icon.getIconWidth() : 0) + 15;
		_height = _fontMetrics.getHeight();
		_clockwise = clockwise;
	}


	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	@Override
	public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		final Font oldFont = g.getFont();
		final Color oldColor = g.getColor();
		final AffineTransform oldTransform = g2.getTransform();

		g.setFont(_font);
		g.setColor(JBColor.BLACK);
		if (_clockwise) {
			g2.translate(x + getIconWidth(), y);
			g2.rotate(Math.PI / 2);
		} else {
			g2.translate(x, y + getIconHeight());
			g2.rotate(-Math.PI / 2);
		}
		int extraSpace = 0;
		if (_icon != null) {
			extraSpace = _icon.getIconHeight() + 10;
			_icon.paintIcon(c, g, x + 5, y);
		}
		g.drawString(_text, extraSpace, _fontMetrics.getLeading() + _fontMetrics.getAscent());

		g.setFont(oldFont);
		g.setColor(oldColor);
		g2.setTransform(oldTransform);
	}


	@Override
	public int getIconWidth() {
		return _height;
	}


	@Override
	public int getIconHeight() {
		return _width;
	}
}