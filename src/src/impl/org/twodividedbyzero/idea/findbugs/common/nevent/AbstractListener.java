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
			; // no filter
		}

		return eventTypes;
	}


}
