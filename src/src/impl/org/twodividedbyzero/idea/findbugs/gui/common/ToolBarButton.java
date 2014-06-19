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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.Action;
import javax.swing.Icon;
import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.7-dev
 */
public class ToolBarButton extends ToggleableButton {

	protected static final Insets _margin = new Insets(0, 0, 0, 0);

	public static final String ROLLOVER_ICON = "icon.rollover";
	public static final String PRESSED_ICON = "icon.pressed";
	public static final String DISABLED_ICON = "icon.disabled";

	private Icon _buttonBackground;
	private Icon _buttonPressedBackground;

	private Icon _icon;
	private Icon _selectedIcon;
	private Dimension _imageSize;
	private boolean _decorateEmpty;


	public ToolBarButton(@NotNull final Icon icon) {
		this(icon, true);
	}


	public ToolBarButton(@NotNull final Icon icon, final boolean decorateEmpty) {
		this(null, icon, decorateEmpty);

	}


	public ToolBarButton(@Nullable final Action action, @NotNull final Icon icon) {
		this(action, icon, true);
	}


	public ToolBarButton(@Nullable final Action action, final Icon icon, final boolean decorateEmpty) {
		if (action != null) {
			setAction(action);
		}
		setText("");
		init();
		setIcon(icon);
		setDecorateEmpty(decorateEmpty);
	}


	private void setDecorateEmpty(final boolean decorateEmpty) {
		_decorateEmpty = decorateEmpty;
	}


	public boolean isDecorateEmpty() {
		return _decorateEmpty;
	}


	private void init() {
		setMargin(_margin);
		setFocusable(false);
		setFocusPainted(false);
		setBorderPainted(false);
		setContentAreaFilled(false);
		setRequestFocusEnabled(false);
	}


	@Override
	public boolean isFocusable() {
		return false;
	}


	@Override
	public final void setIcon(final Icon icon) {
		_icon = icon;
		_selectedIcon = icon;

		_buttonBackground = ResourcesLoader.loadIcon("actions/button-empty.png");
		_buttonPressedBackground = ResourcesLoader.loadIcon("actions/button-empty_pressed.png");
		setIconSize(_buttonBackground.getIconWidth(), _buttonBackground.getIconHeight());

		super.setIcon(null);
		setRolloverIcon(ResourcesLoader.loadIcon("actions/button-empty_hover.png"));
		setRolloverSelectedIcon(ResourcesLoader.loadIcon("actions/button-empty_hover.png"));
		setPressedIcon(ResourcesLoader.loadIcon("actions/button-empty_pressed.png"));
		setBorder(null);
		setMargin(null);

	}

	/*@Override
	public void paint(final Graphics g) {
		
		*//*final int x = getInsets().top;
		final int y = getInsets().left;
		getRolloverIcon().paintIcon(this, g, x, y);
		final Icon icon = getIcon();
		if (icon != null) {
			icon.paintIcon(this, g, x, y);
		}*//*
		super.paint(g);
	}*/


	@Override
	protected void configurePropertiesFromAction(final Action a) {
		super.configurePropertiesFromAction(a);
		if (a != null) {
			if (a.getValue(ToolBarButton.ROLLOVER_ICON) != null) {
				setRolloverIcon((Icon) a.getValue(ROLLOVER_ICON));
			}
			if (a.getValue(ToolBarButton.PRESSED_ICON) != null) {
				setPressedIcon((Icon) a.getValue(PRESSED_ICON));
			}
			if (a.getValue(ToolBarButton.DISABLED_ICON) != null) {
				setPressedIcon((Icon) a.getValue(DISABLED_ICON));
			}
		}
	}


	private Dimension setIconSize(final int iconWidth, final int iconHeight) {
		_imageSize = new Dimension(iconWidth, iconHeight);
		setPreferredSize(new Dimension(iconWidth, iconHeight));

		return _imageSize;
	}


	private Dimension getIconSize() {
		if (_imageSize != null) {
			return _imageSize;
		}
		final Icon icon = ResourcesLoader.loadIcon("actions/button-empty.png");

		return setIconSize(icon.getIconWidth(), icon.getIconHeight());
	}


	@Override
	public Icon getIcon() {
		return new ButtonIcon(getIconSize(), _buttonBackground, super.getIcon(), _icon);
	}


	@Override
	public Icon getDisabledIcon() {
		return new ButtonIcon(true, getIconSize(), _buttonBackground, super.getIcon(), _icon);
	}


	@Override
	public Icon getRolloverIcon() {
		return new ButtonIcon(getIconSize(), _buttonBackground, super.getRolloverIcon(), _icon);
	}


	@Override
	public Icon getPressedIcon() {
		return new ButtonIcon(getIconSize(), _buttonPressedBackground, super.getPressedIcon(), _icon);
	}


	@Override
	public Icon getSelectedIcon() {
		return new ButtonIcon(getIconSize(), _buttonPressedBackground, super.getPressedIcon(), _selectedIcon);
	}


	@Override
	public Icon getRolloverSelectedIcon() {
		return getRolloverIcon();
	}


	private static class ButtonIcon implements Icon {

		private static final float DISABLED_OPACITY = 0.5f;
		private final Icon[] _icons;
		private final int _maxWidth;
		private final int _maxHeight;
		private final boolean _disabledOpacity;


		private ButtonIcon(final Dimension dimension, final Icon... icons) {
			this(false, dimension, icons);
		}


		private ButtonIcon(final boolean disabledOpacity, final Dimension dimension, final Icon... icons) {
			_disabledOpacity = disabledOpacity;
			_icons = icons;
			_maxWidth = (int) dimension.getWidth();
			_maxHeight = (int) dimension.getHeight();
		}


		public int getIconHeight() {
			return _maxHeight;
		}


		public int getIconWidth() {
			return _maxWidth;
		}


		@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
				value = "BC_UNCONFIRMED_CAST",
				justification = "")
		@SuppressWarnings({"AssignmentToMethodParameter"})
		public void paintIcon(final Component c, final Graphics g, int x, int y) {
			for (final Icon icon : _icons) {
				if (icon == null) {
					continue;
				}
				if (_disabledOpacity) {
					final Graphics2D g2d = (Graphics2D) g;
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, DISABLED_OPACITY));
				}
				if (icon.getIconWidth() < getIconWidth()) {
					x += (getIconWidth() - icon.getIconWidth()) / 2;
				}
				if (icon.getIconHeight() < getIconHeight()) {
					y += (getIconHeight() - icon.getIconHeight()) / 2;

				}
				icon.paintIcon(c, g, x, y);
			}
		}
	}
}
