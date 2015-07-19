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

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Reto Merz<reto.merz@gmail.com>
 * @version $Revision$
 * @since 0.9.992
 */
@SuppressFBWarnings("SE_BAD_FIELD")
public class AaComboBox<E> extends JComboBox {

	private List<ActionListener> _selectionChangeListeners;
	private ActionListener _internalActionListener;
	private boolean _ignoreActionEvents;


	public AaComboBox() {
		super();
	}


	public AaComboBox(final ComboBoxModel model) {
		super(model);
	}


	public AaComboBox(final E[] items) {
		super(items);
	}


	public void setSelectedItem(final Object anObject, final boolean fireSelectionChangeListeners) {
		_ignoreActionEvents = !fireSelectionChangeListeners;
		try {
			setSelectedItem(anObject);
		} finally {
			_ignoreActionEvents = false;
		}
	}


	public void addSelectionChangeListener(final ActionListener actionListener) {
		if (_selectionChangeListeners == null) {
			_selectionChangeListeners = new LinkedList<ActionListener>();
			installInternalActionListener();
		}
		_selectionChangeListeners.add(actionListener);
	}


	public boolean removeSelectionChangeListener(final ActionListener actionListener) {
		boolean ret = false;
		if (_selectionChangeListeners != null) {
			ret = _selectionChangeListeners.remove(actionListener);
			if (_selectionChangeListeners.isEmpty()) {
				_selectionChangeListeners = null;
				uninstallInternalActionListener();
			}
		}
		return ret;
	}


	private void installInternalActionListener() {
		_internalActionListener = new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (!_ignoreActionEvents) {
					fireSelectionChangeListeners(e);
				}
			}
		};
		addActionListener(_internalActionListener);
	}


	private void uninstallInternalActionListener() {
		removeActionListener(_internalActionListener);
		_internalActionListener = null;
	}


	protected void fireSelectionChangeListeners(final ActionEvent e) {
		if (_selectionChangeListeners != null) {
			for (ActionListener listener : _selectionChangeListeners) {
				listener.actionPerformed(e);
			}
		}
	}
}
