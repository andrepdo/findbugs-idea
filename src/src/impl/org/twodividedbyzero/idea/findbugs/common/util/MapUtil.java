/*
 * Copyright 2016 Andre Pfeiler
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

import java.util.Iterator;
import java.util.Map;

public final class MapUtil {
	private MapUtil() {
	}

	public static <K extends Comparable<K>, V extends Comparable<V>> int compare(
			@NotNull final Map<K, V> x,
			@NotNull final Map<K, V> y
	) {
		final Iterator<Map.Entry<K, V>> xIt = x.entrySet().iterator();
		final Iterator<Map.Entry<K, V>> yIt = y.entrySet().iterator();
		while (true) {
			final boolean xHasNext = xIt.hasNext();
			final boolean yHasNext = yIt.hasNext();
			if (!xHasNext && !yHasNext) {
				return 0;
			} else if (xHasNext && yHasNext) {
				final Map.Entry<K, V> xNext = xIt.next();
				final Map.Entry<K, V> yNext = yIt.next();
				int ret = xNext.getKey().compareTo(yNext.getKey());
				if (ret != 0) {
					return ret;
				}
				ret = xNext.getValue().compareTo(yNext.getValue());
				if (ret != 0) {
					return ret;
				}
			} else if (xHasNext) {
				return 1;
			} else {
				return -1;
			}
		}
	}
}
