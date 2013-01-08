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
package org.twodividedbyzero.idea.findbugs.common.nevent;

import org.twodividedbyzero.idea.findbugs.common.nevent.annotation.EventFilter;


/**
 * Convenience class which is easily subclassed when using the EventFilter
 * annotation, but also provides a way for subclassers to not have to
 * provide annotations if they don't want to (they can override getEventTypes().
 * <p/>
 * For examples of how one annotates a listener to receive notifications of
 * only certain events see DeleteNodesListener, GraphEventListener, etc.
 * <p/>
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.7-dev
 */
public abstract class AbstractListener implements Listener {
	//private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	//private final static Log LOG = LogFactory.getLog(AbstractListener.class);

	//@SuppressWarnings("unchecked")
	private final Class<Event<?>>[] _eventTypes = initEventTypes();


	//@SuppressWarnings("unchecked")
	public Class<Event<?>>[] getEventTypes() {
		return _eventTypes.clone();
	}


	public abstract void receiveNotification(Notification notification);


	//@SuppressWarnings("unchecked")
	private Class<Event<?>>[] initEventTypes() {
		Class<Event<?>>[] eventTypes = new Class[0];
		if (getClass().getAnnotation(EventFilter.class) != null) {
			eventTypes = (Class<Event<?>>[]) getClass().getAnnotation(EventFilter.class).value();
		} else {
			// no filter
		}

		return eventTypes;
	}


}
