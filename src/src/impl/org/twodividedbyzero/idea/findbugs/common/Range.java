/*
 * Copyright 2010 Markus KARG
 *
 * This file is part of Range Class.
 *
 * Rangle Class is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Range Class is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Range Class.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.twodividedbyzero.idea.findbugs.common;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;

/**
 * A range is defined by a begin and an end. It allows checking whether a value
 * is within the range or outside.
 * 
 * @author Markus KARG (markus@headcrashing.eu)
 */
public final class Range<T extends Comparable<T>> {

	private final T _lowerBound;

	private final T _upperBound;

	/**
	 * Creates a range with the specified bounds. The bounds will be included,
	 * i. e. are part of the range.
	 * 
	 * @param lowerBound
	 *            The lowest possible value of the range.
	 * @param upperBound
	 *            The greatest possible value of the range.
	 * @throws IllegalArgumentException
	 *             if lower bound is greater than upper bound
	 */
	public Range(final T lowerBound, final T upperBound) throws IllegalArgumentException {
		if (lowerBound != null && upperBound != null && lowerBound.compareTo(upperBound) == 1)
			throw new IllegalArgumentException("lowerBound is greater than upperBound");

		_lowerBound = lowerBound;
		_upperBound = upperBound;
	}

	/**
	 * Checks whether the specified object is within the range, including
	 * bounds.
	 * 
	 * @param object
	 *            The object to be checked. Must not be <code>null</code>.
	 * @return <code>false<code> if <code>object</code> is lower than the lower
	 *         bound or greater than the upper bound; otherwise
	 *         <code>true</code>.
	 * @throws IllegalArgumentException
	 *             if the specified object is <code>null</code>.
	 */
	public final boolean contains(final T object) throws IllegalArgumentException {
		if (object == null)
			throw new IllegalArgumentException("object is null");

		if (_lowerBound != null && object.compareTo(_lowerBound) == -1)
			return false;

		if (_upperBound != null && object.compareTo(_upperBound) == 1)
			return false;

		return true;
	}

	@Override
	public final int hashCode() {
		// MAX_value is used for open LOWER bound as it will be most far away
		// from any existing LOWER bound.
		final int lowerBoundHashCode = _lowerBound == null ? MAX_VALUE : _lowerBound.hashCode();

		// MIN_value is used for open UPPER bound as it will be most far away
		// from any existing UPPER bound.
		final int upperBoundHashCode = _upperBound == null ? MIN_VALUE : _upperBound.hashCode();

		return lowerBoundHashCode << 16 ^ upperBoundHashCode;
	}

	@Override
	public final boolean equals(final Object other) {
		if (!(other instanceof Range))
			return false;

		final Range<?> that = (Range<?>) other;

		return equals(_lowerBound, that._lowerBound) && equals(_upperBound, that._upperBound);
	}

	private static boolean equals(final Object a, final Object b) {
		if (a == null && b == null)
			return true;

		if (a != null && b != null)
			return a.equals(b);

		return false;
	}

	@Override
	public final String toString() {
		return String.format("Range[%s, %s]", _lowerBound, _upperBound);
	}

}
