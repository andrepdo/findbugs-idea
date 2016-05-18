/*
 * Copyright 2008-2016 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.common.util;

import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

public final class New {

	private New() {
	}

	@NotNull
	public static <V> AtomicReference<V> atomicRef(@Nullable V initialValue) {
		return new AtomicReference<V>(initialValue);
	}

	@NotNull
	public static <K, V> Map<K, V> map() {
		return new HashMap<K, V>();
	}

	@NotNull
	public static <K, V> Map<K, V> map(final int initialCapacity) {
		return new HashMap<K, V>(initialCapacity);
	}

	@NotNull
	public static <K, V> WeakHashMap<K, V> weakHashMap() {
		return new WeakHashMap<K, V>();
	}

	@NotNull
	public static <V> WeakReference<V> weakRef(@NotNull final V referent) {
		return new WeakReference<V>(referent);
	}

	@NotNull
	public static <K, V> ConcurrentMap<K, V> concurrentMap() {
		return new ConcurrentHashMap<K, V>();
	}

	@NotNull
	public static <E> Set<E> set() {
		return new HashSet<E>();
	}

	@NotNull
	public static <T> Set<T> asSet(@Nullable final T... elements) {
		int cap = 4;
		if (elements != null) {
			cap = elements.length;
		}
		final HashSet<T> ret = new HashSet<T>(cap);
		if (elements != null) {
			ret.addAll(Arrays.asList(elements));
		}
		return ret;
	}

	@NotNull
	public static <E> Set<E> tSet() {
		return new THashSet<E>();
	}

	@NotNull
	public static <E> List<E> arrayList() {
		return new ArrayList<E>();
	}
}
