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


	public ToggleableButton() {
		super();
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


	public boolean getToggleMode() {
		return getModel() instanceof JToggleButton.ToggleButtonModel;
	}


	@Override
	public String getUIClassID() {
		if (getToggleMode()) {
			return "ToggleButtonUI";  // NON-NLS
		}
		return super.getUIClassID();
	}
}

