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
package org.twodividedbyzero.idea.findbugs.common.util;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;


/**
 * @author Reto Merz<reto.merz@gmail.com>
 * @version $Revision: 358 $
 * @since 0.9.995
 */
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
	public static <K, V> ConcurrentMap<K, V> concurrentMap() {
		return new ConcurrentHashMap<K, V>();
	}


	@NotNull
	public static <E> Set<E> set() {
		return new HashSet<E>();
	}
}