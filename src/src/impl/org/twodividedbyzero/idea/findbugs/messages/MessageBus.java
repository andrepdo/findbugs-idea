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

package org.twodividedbyzero.idea.findbugs.messages;


import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.New;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;


/**
 * @author Reto Merz<reto.merz@gmail.com>
 * @version $Revision: 374 $
 * @since 0.9.995
 */
final class MessageBus {

	private static final Object EMPTY = new Object();


	private final Project _project;
	private final Map<Object/*subscriber*/, Map<Topic<?>, Object/*handler*/>> _subscribers;
	private final Map<Topic<?>, Object/*handler*/> _publisher;


	MessageBus(@NotNull final Project project) {
		_project = project;
		_subscribers = New.map();
		_publisher = New.map();
	}


	public <L> void subscribe(@NotNull final Object subscriber, @NotNull final Topic<L> topic, @NotNull final L handler) {
		Map<Topic<?>, Object/*handler*/> handlerByTopic = _subscribers.get(subscriber);
		if (handlerByTopic == null) {
			handlerByTopic = New.map();
			_subscribers.put(subscriber, handlerByTopic);
		}
		if (!handlerByTopic.containsKey(topic)) {
			handlerByTopic.put(topic, handler);
		} // else do nothing ; subscriber has already subscribed this topic
	}


	@SuppressWarnings("unchecked")
	@NotNull
	public <L> L publisher(@NotNull final Topic<L> topic) {
		L ret = (L)_publisher.get(topic);
		if (ret == null) {
			final Class<L> listenerClass = topic.getListenerClass();
			final InvocationHandler handler = new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					for (Map<Topic<?>, Object/*handler*/> handlerByTopic : _subscribers.values()) {
						final Object handler = handlerByTopic.get(topic);
						if (null != handler) {
							method.invoke(handler, args);
						}
					}
					return EMPTY;
				}
			};
			ret = (L)Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class[]{listenerClass}, handler);
			_publisher.put(topic, ret);
		}
		return ret;
	}


	@Override
	public String toString() {
		return "MessageBus{" +
				"project=" + _project +
				'}';
	}
}