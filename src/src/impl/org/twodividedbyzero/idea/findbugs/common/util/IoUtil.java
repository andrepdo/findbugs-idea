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


import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;


/**
 * $Date$
 *
 * $Id$
 *
 * @author $Author$
 * @version $Revision$
 * @since 0.9.991
 */
public class IoUtil {

	private static final Logger LOGGER = Logger.getInstance(IoUtil.class.getName());


	private IoUtil() {
	}


	public static void safeClose(@Nullable final Closeable closable) {
		if (closable != null) {
			try {
				closable.close();
			} catch (final RuntimeException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exception occured when closing " + closable + " - " + e);
				}
			} catch (final IOException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("exception occured when closing " + closable + " - " + e);
				}
			}
		}
	}


	public static void saveClose(@Nullable final Closeable closable) {
		safeClose(closable);
	}


	public static void safeClose(@Nullable final Closeable... closeables) {
		if (closeables != null) {
			for (final Closeable closeable : closeables) {
				safeClose(closeable);
			}
		}
	}


	public static void saveClose(@Nullable final Closeable... closeables) {
		safeClose(closeables);
	}


	public static void safeClose(@Nullable final Iterable<Closeable> list) {
		if (list == null) {
			return;
		}
		for (final Closeable closeable : list) {
			safeClose(closeable);
		}
	}


	public static void saveClose(@Nullable final Iterable<Closeable> list) {
		safeClose(list);
	}

}
