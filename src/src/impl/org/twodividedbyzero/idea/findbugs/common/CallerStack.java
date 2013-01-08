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

/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.96-dev
 */
public class CallerStack extends Throwable {

	private static final long serialVersionUID = 1L;


	public CallerStack() {
		super("called from");
	}


	public CallerStack(final CallerStack cause) {
		super("called from", cause);
	}


	public static void initCallerStack(final Throwable throwable, final CallerStack callerStack) {
		Throwable lastCause = throwable;
		while (lastCause.getCause() != null) {
			lastCause = lastCause.getCause();
		}
		try {
			lastCause.initCause(callerStack);
		} catch (final IllegalStateException ignored) {
			// some exceptions may override getCause(), but not initCause()
			// => getCause() can be null, but cause is alreay set
		}
	}


}
