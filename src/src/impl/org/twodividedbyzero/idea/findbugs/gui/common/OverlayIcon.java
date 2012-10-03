/*
 * Copyright 2012 Andre Pfeiler
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

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;

/**
 * $Date$
 *
 * @author Andre Pfeiler<andre.pfeiler@gmail.com>
 * @version $Revision$
 * @since 0.9.97
 */
public class OverlayIcon implements Icon {

	protected final Icon[] _icons;
	protected int _maxWidth;
	protected int _maxHeight;


	public OverlayIcon(final Icon... icons) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_icons = icons;
		for (final Icon icon : icons) {
			if (icon != null && icon.getIconWidth() > _maxWidth) {
				_maxWidth = icon.getIconWidth();
			}
			if (icon != null && icon.getIconHeight() > _maxHeight) {
				_maxHeight = icon.getIconHeight();
			}
		}
	}


	public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
		for (final Icon icon : _icons) {
			icon.paintIcon(c, g, x, y);
		}
	}


	public int getIconWidth() {
		return _maxWidth;
	}


	public int getIconHeight() {
		return _maxHeight;
	}
}

