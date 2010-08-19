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

	private Icon _delegate;
	private BufferedImage _mask;
	private boolean _colorPainted = true;


	public MaskIcon(@NotNull final Icon delegate) {
		this(delegate, UIManager.getColor("textHighlight"), 0.5F);
	}


	public MaskIcon(@NotNull final Icon delegate, final Color color) {
		this(delegate, color, 0.5F);
	}


	public MaskIcon(@NotNull final Icon delegate, final Color color, final float alpha) {
		_delegate = delegate;
		createMask(color, alpha);
	}


	private void createMask(final Color color, final float alpha) {
		_mask = new BufferedImage(_delegate.getIconWidth(), _delegate.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
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
