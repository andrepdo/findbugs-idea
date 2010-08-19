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

	private ActionToolbar _childBar;
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
	public Component add(final Component comp) {
		return super.add(comp);
	}


	@Override
	public void setOrientation(final int o) {
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
