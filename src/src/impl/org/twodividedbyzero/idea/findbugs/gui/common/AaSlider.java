/*
 * Copyright 2008-2015 Andre Pfeiler
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Reto Merz<reto.merz@gmail.com>
 * @version $Revision$
 * @since 0.9.992
 */
@SuppressFBWarnings("SE_BAD_FIELD")
public class AaSlider extends JSlider {

	private List<ChangeListener> _valueChangeListeners;
	private ChangeListener _internalChangeListener;
	private boolean _ignoreChangeEvents;


	public AaSlider() {
		super();
	}


	public AaSlider(final int orientation, final int min, final int max, final int value) {
		super(orientation, min, max, value);
	}


	public void setValue(final int n, final boolean fireValueChangeListeners) {
		_ignoreChangeEvents = !fireValueChangeListeners;
		try {
			setValue(n);
		} finally {
			_ignoreChangeEvents = false;
		}
	}


	public void addValueChangeListener(final ChangeListener valueChangeListener) {
		if (_valueChangeListeners == null) {
			_valueChangeListeners = new LinkedList<ChangeListener>();
			installInternalChangeListener();
		}
		_valueChangeListeners.add(valueChangeListener);
	}


	public boolean removeValueChangeListener(final ChangeListener valueChangeListener) {
		boolean ret = false;
		if (_valueChangeListeners != null) {
			ret = _valueChangeListeners.remove(valueChangeListener);
			if (_valueChangeListeners.isEmpty()) {
				_valueChangeListeners = null;
				uninstallInternalChangeListener();
			}
		}
		return ret;
	}


	private void installInternalChangeListener() {
		_internalChangeListener = new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				if (!_ignoreChangeEvents) {
					fireValueChangeListeners(e);
				}
			}
		};
		addChangeListener(_internalChangeListener);
	}


	private void uninstallInternalChangeListener() {
		removeChangeListener(_internalChangeListener);
		_internalChangeListener = null;
	}


	protected void fireValueChangeListeners(final ChangeEvent e) {
		if (_valueChangeListeners != null) {
			for (ChangeListener listener : _valueChangeListeners) {
				listener.stateChanged(e);
			}
		}
	}
}