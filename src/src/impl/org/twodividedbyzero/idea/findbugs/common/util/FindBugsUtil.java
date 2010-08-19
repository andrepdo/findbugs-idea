package org.twodividedbyzero.idea.findbugs.common.util;

import edu.umd.cs.findbugs.Version;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class FindBugsUtil {

	public static final Set<String> ARCHIVE_EXTENSION_SET = new HashSet<String>();
	public static final Set<String> XML_EXTESIONS_SET = new HashSet<String>();


	private FindBugsUtil() {
	}


	static {
		ARCHIVE_EXTENSION_SET.add(".jar");
		ARCHIVE_EXTENSION_SET.add(".zip");
		ARCHIVE_EXTENSION_SET.add(".ear");
		ARCHIVE_EXTENSION_SET.add(".war");
		ARCHIVE_EXTENSION_SET.add(".sar");

		XML_EXTESIONS_SET.add(".xml");
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
			return ARCHIVE_EXTENSION_SET.contains(extension);
		}
	}
}


