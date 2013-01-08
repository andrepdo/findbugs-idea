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

package org.twodividedbyzero.idea.findbugs.common.event.types;

import org.twodividedbyzero.idea.findbugs.common.event.EventImpl;


/**
 * $Date$
 *
 * @version $Revision$
 */
public class EventManagerEventImpl extends EventImpl implements EventManagerEvent {

	private static final long serialVersionUID = 0L;

	private final Operation _operation;


	public EventManagerEventImpl(final Operation operation) {
		super(EventType.EVENT_MANAGER);
		_operation = operation;
	}


	public Operation getOperation() {
		return _operation;
	}


	@Override
	public String toString() {
		return "EventManagerEventImpl{" + "_operation=" + _operation + '}';
	}
}
