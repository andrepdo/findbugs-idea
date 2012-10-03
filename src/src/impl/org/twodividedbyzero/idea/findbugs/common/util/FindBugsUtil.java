/*
 * Copyright 2010 Andre Pfeiler
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
@SuppressWarnings("UnusedDeclaration")
public final class FindBugsUtil {

	public static final Map<String, String> ARCHIVE_EXTENSION_SET = new HashMap<String, String>();
	public static final Map<String, String> XML_EXTENSIONS_SET = new HashMap<String, String>();
	public static final Map<String, String> PLUGINS_EXTENSIONS_SET = new HashMap<String, String>();


	private FindBugsUtil() {
		throw new UnsupportedOperationException();
	}


	static {
		ARCHIVE_EXTENSION_SET.put(".jar", "");
		ARCHIVE_EXTENSION_SET.put(".zip", "");
		ARCHIVE_EXTENSION_SET.put(".ear", "");
		ARCHIVE_EXTENSION_SET.put(".war", "");
		ARCHIVE_EXTENSION_SET.put(".sar", "");

		XML_EXTENSIONS_SET.put(".xml", "");

		PLUGINS_EXTENSIONS_SET.put(".jar", "FindBugs Plugins");
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


