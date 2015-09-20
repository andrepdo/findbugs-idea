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

import edu.umd.cs.findbugs.Version;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.Map;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @since 0.0.1
 */
@SuppressWarnings("UnusedDeclaration")
public final class FindBugsUtil {

	public static final Map<String, String> ARCHIVE_EXTENSION_SET;
	public static final Map<String, String> XML_EXTENSIONS_SET = Collections.singletonMap(".xml", "");
	public static final Map<String, String> PLUGINS_EXTENSIONS_SET = Collections.singletonMap(".jar", "FindBugs Plugins");


	private FindBugsUtil() {
		throw new UnsupportedOperationException();
	}


	static {
		final Map<String, String> archiveExtensions = New.map(5);
		archiveExtensions.put(".jar", "");
		archiveExtensions.put(".zip", "");
		archiveExtensions.put(".ear", "");
		archiveExtensions.put(".war", "");
		archiveExtensions.put(".sar", "");
		ARCHIVE_EXTENSION_SET = Collections.unmodifiableMap(archiveExtensions);
	}


	public static int getFindBugsMajorVersion() {
		return Version.MAJOR;
	}


	public static int getFindBugsMinorVersion() {
		return Version.MINOR;
	}


	public static int getFindBugsPatchLevel() {
		return Version.PATCHLEVEL;
	}


	public static String getFindBugsFullVersion() {
		return Version.COMPUTED_RELEASE;
	}


	public static boolean isFindBugsError(@NotNull final Throwable error) {
		@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
		final Throwable cause = ErrorUtil.getCause(error);
		final StackTraceElement[] stack = error.getStackTrace();
		if (stack != null && stack.length > 0) { // just in case - think of -XX:-OmitStackTraceInFastThrow
			final String className = stack[0].getClassName();
			if (className != null) {
				return className.startsWith("edu.umd.cs.findbugs.") || className.startsWith("org.apache.bcel.");
			}
		}
		return false;
	}


	/** File filter for choosing archives and directories. */
	private static class ArchiveAndDirectoryFilter implements java.io.FileFilter {

		public boolean accept(final File file) {
			if (file.isDirectory()) {
				return true;
			}

			final String fileName = file.getName();
			final int dot = fileName.lastIndexOf('.');
			if (dot < 0) {
				return false;
			}
			final String extension = fileName.substring(dot);
			return ARCHIVE_EXTENSION_SET.containsKey(extension);
		}
	}
}