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

