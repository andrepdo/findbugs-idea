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

import com.intellij.openapi.actionSystem.ActionToolbar;

import java.awt.Component;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
public class ActionToolbarContainer extends AbstractBar {

	private final ActionToolbar _childBar;
	private int _orientation;


	public ActionToolbarContainer(final String floatedName, final int orientation, final ActionToolbar childBar, final boolean floatable) {
		super(orientation, floatable);
		setName(floatedName);
		_childBar = childBar;
		add(_childBar.getComponent());
		//setPreferredSize(new Dimension(_childBar.getComponent().getPreferredSize().width, _childBar.getComponent().getPreferredSize().height));
		setOrientation(orientation);
		_childBar.setOrientation(orientation);
	}


	@Override
	public final Component add(final Component comp) {
		return super.add(comp);
	}


	@Override
	public final void setOrientation(final int o) {
		super.setOrientation(o);

		if (_orientation != o) {
			final int old = _orientation;
			_orientation = o;

			if (_childBar != null) {
				_childBar.setOrientation(o);
				_childBar.getComponent().firePropertyChange("orientation", old, o);
				_childBar.getComponent().repaint();
				_childBar.getComponent().revalidate();
			}

			firePropertyChange("orientation", old, o);
			revalidate();
			repaint();
		}
	}
}
