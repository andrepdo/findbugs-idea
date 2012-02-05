/*
 * Copyright 2009-2010 Markus KARG
 *
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * For a copy of the GNU Lesser General Public License
 * see <http://www.gnu.org/licenses/>.
 */

package org.twodividedbyzero.idea.findbugs.common;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * This class allows Enumerations to be iterated.
 * 
 * @author Markus KARG (markus@headcrashing.eu)
 */
public final class Enumerations {

	private Enumerations() {
	}


	/**
	 * Allows using of {@link Enumeration} with the for-each statement. The
	 * implementation is not using any heap space and such is able to serve
	 * virtually endless Enumerations, while {@link Collections#list} is limited
	 * by available RAM. As a result, this implementation is much faster than
	 * Collections.list.
	 * 
	 * @param enumeration
	 *            The original enumeration.
	 * @return An {@link Iterable} directly calling the original Enumeration.
	 */
	public static final <T> Iterable<T> iterable(final Enumeration<T> enumeration) {
		return new Iterable<T>() {
			public final Iterator<T> iterator() {
				return new Iterator<T>() {
					public final boolean hasNext() {
						return enumeration.hasMoreElements();
					}

					public final T next() {
						return enumeration.nextElement();
					}

					/**
					 * This method is not implemeted as it is impossible to
					 * remove something from an Enumeration.
					 * 
					 * @throws UnsupportedOperationException
					 *             always.
					 */
					public final void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

}
