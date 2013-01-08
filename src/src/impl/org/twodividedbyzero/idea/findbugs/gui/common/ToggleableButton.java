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

import javax.swing.Action;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.7-dev
 */
public class ToggleableButton extends JButton {

	protected int _modifiers;


	ToggleableButton() {
	}


	public ToggleableButton(final String text) {
		super(text);
	}


	public ToggleableButton(final Icon icon) {
		super(icon);
	}


	public ToggleableButton(final Action action) {
		super(action);
	}


	public ToggleableButton(final String text, final Icon icon) {
		super(text, icon);
	}


	public ToggleableButton(final String text, final Icon icon, final Action action) {
		super(text, icon);
		setAction(action);
	}


	public void setToggleMode(final boolean value) {
		if (getToggleMode() != value) {
			if (value) {
				setModel(new JToggleButton.ToggleButtonModel());
			} else {
				setModel(new DefaultButtonModel());
			}
			updateUI();
		}
	}


	boolean getToggleMode() {
		return getModel() instanceof JToggleButton.ToggleButtonModel;
	}


	@Override
	public String getUIClassID() {
		if (getToggleMode()) {
			return "ToggleButtonUI";
		}
		return super.getUIClassID();
	}
}

