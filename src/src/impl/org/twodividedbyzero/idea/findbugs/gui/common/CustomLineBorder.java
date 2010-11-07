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

	private Color _lineColor;
	private int _top;
	private int _left;
	private int _bottom;
	private int _right;


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
