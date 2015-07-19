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

import com.intellij.ui.components.JBTextField;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
public class AaTextField extends JBTextField {

	private List<ActionListener> _textChangeListeners;
	private DocumentListener _internalDocumentListener;
	private boolean _ignoreDocumentEvents;


	public AaTextField() {
		super();
	}


	public AaTextField(final int i) {
		super(i);
	}


	public void setText(final String t, final boolean fireTextChangeListeners) {
		_ignoreDocumentEvents = !fireTextChangeListeners;
		try {
			setText(t);
		} finally {
			_ignoreDocumentEvents = false;
		}
	}


	public void addTextChangeListener(final ActionListener actionListener) {
		if (_textChangeListeners == null) {
			_textChangeListeners = new LinkedList<ActionListener>();
			installInternalDocumentListener();
		}
		_textChangeListeners.add(actionListener);
	}


	public boolean removeTextChangeListener(final ActionListener actionListener) {
		boolean ret = false;
		if (_textChangeListeners != null) {
			ret = _textChangeListeners.remove(actionListener);
			if (_textChangeListeners.isEmpty()) {
				_textChangeListeners = null;
				uninstallInternalDocumentListener();
			}
		}
		return ret;
	}


	private void installInternalDocumentListener() {
		_internalDocumentListener = new DocumentListener() {
			public void insertUpdate(final DocumentEvent e) {
				documentUpdated(e);
			}


			public void removeUpdate(final DocumentEvent e) {
				documentUpdated(e);
			}


			public void changedUpdate(final DocumentEvent e) {
				documentUpdated(e);
			}


			private void documentUpdated(final DocumentEvent e) {
				if (!_ignoreDocumentEvents) {
					fireSelectionChangeListeners(null);
				}
			}
		};
		getDocument().addDocumentListener(_internalDocumentListener);
	}


	private void uninstallInternalDocumentListener() {
		getDocument().removeDocumentListener(_internalDocumentListener);
		_internalDocumentListener = null;
	}


	protected void fireSelectionChangeListeners(final ActionEvent e) {
		if (_textChangeListeners != null) {
			for (ActionListener listener : _textChangeListeners) {
				listener.actionPerformed(e);
			}
		}
	}
}
