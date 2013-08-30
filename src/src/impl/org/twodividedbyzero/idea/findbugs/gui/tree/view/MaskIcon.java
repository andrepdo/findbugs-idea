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

import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.UIManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
public class MaskIcon implements Icon {

	private final Icon _delegate;
	private BufferedImage _mask;
	private boolean _colorPainted = true;


	public MaskIcon(@NotNull final Icon delegate) {
		this(delegate, UIManager.getColor("textHighlight"), 0.5F);
	}


	public MaskIcon(@NotNull final Icon delegate, final Color color) {
		this(delegate, color, 0.5F);
	}


	private MaskIcon(@NotNull final Icon delegate, final Color color, final float alpha) {
		_delegate = delegate;
		createMask(color, alpha);
	}


	private void createMask(final Color color, final float alpha) {
		_mask = UIUtil.createImage(_delegate.getIconWidth(), _delegate.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		final Graphics2D gbi = (Graphics2D) _mask.getGraphics();
		_delegate.paintIcon(new JLabel(), gbi, 0, 0);
		gbi.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, alpha));
		gbi.setColor(color);
		gbi.fillRect(0, 0, _mask.getWidth() - 1, _mask.getHeight() - 1);
	}


	public boolean isColorPainted() {
		return _colorPainted;
	}


	public void setColorPainted(final boolean colorPainted) {
		_colorPainted = colorPainted;
	}


	public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
		_delegate.paintIcon(c, g, x, y);
		if (_colorPainted) {
			g.drawImage(_mask, x, y, c);
		}
	}


	public int getIconWidth() {
		return _delegate.getIconWidth();
	}


	public int getIconHeight() {
		return _delegate.getIconHeight();
	}
}
