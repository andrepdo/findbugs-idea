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
package org.twodividedbyzero.idea.findbugs.common.util;

import java.util.Arrays;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
class HashCodeBuilder {

	private HashCodeBuilder() {
	}


	/**
	 * Returns true if given two objects are same.
	 * <p/>
	 * null values are handled as follows:
	 * null != non-null
	 * null == null
	 * <p/>
	 * Arrays are handled using Arrays.equals(...)
	 *
	 * @param obj1 object one to compare
	 * @param obj2 object two to compare
	 * @return true if and only if the given object are the same
	 */
	public static boolean equals(final Object obj1, final Object obj2) {
		if (obj1 == obj2) {
			return true;
		} else if (obj1 == null || obj2 == null) {
			return false;
		} else if (obj1.getClass().isArray()) {
			if (obj2.getClass().isArray()) {
				if (obj1 instanceof Object[] && obj2 instanceof Object[]) {
					return Arrays.deepEquals((Object[]) obj1, (Object[]) obj2);
				} else if (obj1 instanceof boolean[] && obj2 instanceof boolean[]) {
					return Arrays.equals((boolean[]) obj1, (boolean[]) obj2);
				} else if (obj1 instanceof char[] && obj2 instanceof char[]) {
					return Arrays.equals((char[]) obj1, (char[]) obj2);
				} else if (obj1 instanceof byte[] && obj2 instanceof byte[]) {
					return Arrays.equals((byte[]) obj1, (byte[]) obj2);
				} else if (obj1 instanceof short[] && obj2 instanceof short[]) {
					return Arrays.equals((short[]) obj1, (short[]) obj2);
				} else if (obj1 instanceof int[] && obj2 instanceof int[]) {
					return Arrays.equals((int[]) obj1, (int[]) obj2);
				} else if (obj1 instanceof long[] && obj2 instanceof long[]) {
					return Arrays.equals((long[]) obj1, (long[]) obj2);
				} else if (obj1 instanceof float[] && obj2 instanceof float[]) {
					return Arrays.equals((float[]) obj1, (float[]) obj2);
				} else if (obj1 instanceof double[] && obj2 instanceof double[]) {
					return Arrays.equals((double[]) obj1, (double[]) obj2);
				} else {
					throw new IllegalArgumentException("couldn't do equals for " + obj1.getClass().getComponentType().getSimpleName() + "[]");
				}
			} else {
				return false;
			}
		} else {
			return obj1.equals(obj2);
		}
	}


	/**
	 * returns hashCode of given argument.
	 *
	 * @param obj the Object ot build a hashcode from
	 * @return return 0 if argument is null
	 */
	private static int hashCode(final Object obj) {
		if (obj == null) {
			return 0;
		} else if (obj.getClass().isArray()) {
			if (obj instanceof Object[]) {
				return Arrays.deepHashCode((Object[]) obj);
			} else if (obj instanceof boolean[]) {
				return Arrays.hashCode((boolean[]) obj);
			} else if (obj instanceof char[]) {
				return Arrays.hashCode((char[]) obj);
			} else if (obj instanceof byte[]) {
				return Arrays.hashCode((byte[]) obj);
			} else if (obj instanceof short[]) {
				return Arrays.hashCode((short[]) obj);
			} else if (obj instanceof int[]) {
				return Arrays.hashCode((int[]) obj);
			} else if (obj instanceof long[]) {
				return Arrays.hashCode((long[]) obj);
			} else if (obj instanceof float[]) {
				return Arrays.hashCode((float[]) obj);
			} else if (obj instanceof double[]) {
				return Arrays.hashCode((double[]) obj);
			} else {
				throw new IllegalArgumentException("couldn't find hashcode for" + obj.getClass().getComponentType().getSimpleName() + "[]");
			}
		} else {
			return obj.hashCode();
		}
	}


	/**
	 * returns hashCode of given arguments.
	 *
	 * @param objects build a hashcode from given arguments
	 * @return if any argument is null, then it considers its hashCode as zero
	 */
	public static int hashCode(final Object... objects) {
		int hashCode = 0;
		for (final Object obj : objects) {
			hashCode += hashCode(obj);
		}
		return hashCode;
	}
}
