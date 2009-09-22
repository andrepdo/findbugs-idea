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
package org.twodividedbyzero.idea.findbugs.common.ui;

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
		super();
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
	public void setIcon(final Icon icon) {
		_icon = icon;
		_selectedIcon = icon;

		_buttonBackground = ResourcesLoader.loadIcon("actions/button-empty.png");
		_buttonPressedBackground = ResourcesLoader.loadIcon("actions/button-empty_pressed.png");
		setIconSize(_buttonBackground.getIconWidth(), _buttonBackground.getIconHeight());

		super.setIcon(null);
		super.setRolloverIcon(ResourcesLoader.loadIcon("actions/button-empty_hover.png"));
		super.setRolloverSelectedIcon(ResourcesLoader.loadIcon("actions/button-empty_hover.png"));
		super.setPressedIcon(ResourcesLoader.loadIcon("actions/button-empty_pressed.png"));
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
		final Icon icon;
		icon = ResourcesLoader.loadIcon("actions/button-empty.png");

		return setIconSize(icon.getIconWidth(), icon.getIconHeight());
	}


	@Override
	public Icon getIcon() {
		return new ButtonIcon(_buttonBackground, super.getIcon(), _icon);
	}


	@Override
	public Icon getDisabledIcon() {
		return new ButtonIcon(true, _buttonBackground, super.getIcon(), _icon);
	}


	@Override
	public Icon getRolloverIcon() {
		return new ButtonIcon(_buttonBackground, super.getRolloverIcon(), _icon);
	}


	@Override
	public Icon getPressedIcon() {
		return new ButtonIcon(_buttonPressedBackground, super.getPressedIcon(), _icon);
	}


	@Override
	public Icon getSelectedIcon() {
		return new ButtonIcon(_buttonPressedBackground, super.getPressedIcon(), _selectedIcon);
	}


	@Override
	public Icon getRolloverSelectedIcon() {
		return getRolloverIcon();
	}


	private class ButtonIcon implements Icon {

		private static final float DISABLED_OPACITY = 0.5f;
		private Icon[] _icons;
		private int _maxWidth;
		private int _maxHeight;
		private boolean _disabledOpacity;


		public ButtonIcon(final Icon... icons) {
			this(false, icons);
		}


		public ButtonIcon(final boolean disabledOpacity, final Icon... icons) {
			_disabledOpacity = disabledOpacity;
			_icons = icons;
			final Dimension dimension = getIconSize();
			_maxWidth = (int) dimension.getWidth();
			_maxHeight = (int) dimension.getHeight();
		}


		public int getIconHeight() {
			return _maxHeight;
		}


		public int getIconWidth() {
			return _maxWidth;
		}


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
