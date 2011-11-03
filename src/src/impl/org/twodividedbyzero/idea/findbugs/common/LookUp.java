/*
 * Copyright 2011 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.twodividedbyzero.idea.findbugs.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * $Date: 2010-10-11 23:47:29 +0200 (Mon, 11 Oct 2010) $
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision: 112 $
 * @since 0.9.96
 */
public final class LookUp {

	private static final Collection<Object> _instances = new ConcurrentLinkedQueue<Object>();


	private LookUp() {
		// no instances
	}


	@Nullable
	public static <T> T get(@NotNull final Class<T> type) {
		for (final Object instance : _instances) {
			if (type.isAssignableFrom(instance.getClass())) {
				return type.cast(instance);
			}
		}
		return null;
	}


	@edu.umd.cs.findbugs.annotations.SuppressWarnings({"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
	@NotNull
	public static <T> T getNotNull(@NotNull final Class<T> type) throws IllegalStateException {
		final T result = get(type);
		if (result == null) {
			throw new IllegalStateException("not registered: " + type.getName());
		}
		return result;
	}


	public static <T> void register(@NotNull final T instance) {
		_instances.add(instance);
	}


	public static <T> void unregister(final T instance) {
		if (instance != null) {
			for (final Iterator<Object> it = _instances.iterator(); it.hasNext(); ) {
				final Object o = it.next();
				if (o == instance) {
					it.remove();
				}
			}
		}
	}

}

