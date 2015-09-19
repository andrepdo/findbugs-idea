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

import java.io.IOException;


/**
 * $Date: 2015-02-13 20:45:02 +0100 (Fr, 13 Feb 2015) $
 *
 * @author Reto Merz<reto.merz@gmail.com>
 * @since 0.9.995
 */
public final class ErrorUtil {


	private ErrorUtil() {
	}


	@NotNull
	public static RuntimeException toUnchecked(@NotNull final Throwable e) {
		if (e instanceof RuntimeException) {
			return (RuntimeException)e;
		}
		return new RuntimeException(e);
	}


	@NotNull
	public static RuntimeException toUnchecked(@Nullable final String message, @NotNull final IOException e) {
		return new RuntimeException(message, e);
	}


	@NotNull
	public static Throwable getCause(@NotNull final Throwable e) {
		Throwable ret = e;
		while (ret.getCause() != null) {
			ret = ret.getCause();
		}
		return ret;
	}
}