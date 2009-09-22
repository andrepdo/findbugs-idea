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

/**
 * Lots of questions about thread safety here, but rather than just
 * synchronize with abandon, leave that for further consideration for now.
 * <p/>
 * Scheme filtering occurs before notification. This is because
 * a notification may contain many events, and that could mean many listeners
 * all working to do the same or similar filtering, like a series of sequential
 * for loops, and this seemed excessive. So here a listener can receive a
 * notification with many events attached to it, but still it will only be events
 * it has said it is interested in.
 * <p/>
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.7-dev
 */
public class EventManagerImpl {

}
/*@SuppressWarnings("unchecked")
public class EventManagerImpl implements EventManager {

	private static final long serialVersionUID = 1L;


	private Map<Class, List<Listener>> listenersByEventTypeMap_ = new HashMap<Class, List<Listener>>();


	// needs some kind of thread safety
	public void register(Listener... listeners) {
		for (Listener listener : listeners) {
			for (Class<Event> eventType : listener.getEventTypes()) {
				addToListenerByEventTypeMap(eventType, listener);
			}
		}
	}


	// needs some kind of thread safety
	public void unregister(Listener... listeners) {
		for (Listener listener : listeners) {
			for (Class<Event> eventType : listener.getEventTypes()) {
				listenersByEventTypeMap_.get(eventType).remove(listener);
			}
		}
	}


	public void noteEvent(Event event) {
		// only fetch the listenters interested in this event type
		List<Listener> list = listenersByEventTypeMap_.get(event.getClass());

		if (list != null) {
			for (Listener listener : list) {
				addToMap(getThisThreadsNotificationByListenerMap(), listener, event);
			}

			notifyListeners();
		} else {
			; // no one cares about this event
		}


	}


	// mark the end of a batch -- this is a per thread concept, batches
	// are not shared across threads
	public Batch popBatch(Batch aBatch) {
		Batch b = getThisThreadsBatchStack().pop();

		if (b.equals(aBatch)) {
			notifyListeners();
		} else {
			throw new RuntimeException("Asymetrical batch!");
		}

		return b;
	}


	// mark the start of a batch-- this is a per thread concept, batches
	// are not shared across threads
	public void pushBatch(Batch b) {
		getThisThreadsBatchStack().push(b);
	}


	@SuppressWarnings("unchecked")
	private void notifyListeners() {
		// only notify listeners if all batches are complete, or
		// in the case where no batch existed
		if (getThisThreadsBatchStack().isEmpty()) {
			Map<Listener, Notification> thisThreadsNotificationByListenerMap = getThisThreadsNotificationByListenerMap();
			Set<Listener> keySet = thisThreadsNotificationByListenerMap.keySet();
			for (Listener listener : keySet) {
				Notification listenersNotification = thisThreadsNotificationByListenerMap.get(listener);
				listener.receiveNotification(listenersNotification);
			}
			// all the notifications have been sent
			thisThreadsNotificationByListenerMap.clear();
		}

	}


	private <E extends Event> void addToMap(Map<Listener, Notification> map, Listener l, E e) {
		Notification n = map.get(l);
		if (n == null) {
			n = new NotificationImpl();
			map.put(l, n);
		} else {
			; // already have a notification
		}

		n.getEvents().add(e);
	}


	private void addToListenerByEventTypeMap(Class eventType, Listener listener) {
		List<Listener> list = listenersByEventTypeMap_.get(eventType);
		if (list == null) {
			list = new ArrayList<Listener>();
			listenersByEventTypeMap_.put(eventType, list);
		} else {
			; // already have a list started
		}

		list.add(listener);
	}


	// each thread has its own batch stack
	private final ThreadLocal<Stack<Batch>> batchStackRef = new ThreadLocal<Stack<Batch>>() {
		@Override
		protected Stack<Batch> initialValue() {
			return new Stack<Batch>();
		}
	};

	// each thread keeps a list of notifications and their events that only
	// this thread generated
	private final ThreadLocal<Map<Listener, Notification>> notificationByListenerMap_ = new ThreadLocal<Map<Listener, Notification>>() {
		@Override
		protected Map<Listener, Notification> initialValue() {
			return new HashMap<Listener, Notification>();
		}
	};


	private Stack<Batch> getThisThreadsBatchStack() {
		return batchStackRef.get();
	}


	private Map<Listener, Notification> getThisThreadsNotificationByListenerMap() {
		return notificationByListenerMap_.get();
	}

}*/
