/*
 * Copyright 2008-2014 Andre Pfeiler
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

import org.twodividedbyzero.idea.findbugs.common.util.IoUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;


/**
 * $Date$
 *
 * $Id$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
@SuppressWarnings({"HardCodedStringLiteral", "UseOfSystemOutOrSystemErr", "StringConcatenation", "CallToPrintStackTrace", "CallToPrintStackTrace"})
public class VersionManager {

	private static final long _major = 1;
	private static final long _minor = 0;
	private static final long _build = 1;

	private static final String _branch = "trunk";


	private static final String NAME = FindBugsPluginConstants.PLUGIN_NAME;

	private static final String WEBSITE = "http://andrepdo.github.io/findbugs-idea";

	private static final String DOWNLOAD_WEBSITE = "http://plugins.intellij.net/plugin/?id=3847";

	private static final String SUPPORT_EMAIL = "andre.pfeiler@gmail.com";

	private static final long REVISION;

	private static final String FULL_VERSION;

	private static final String MAJOR_MINOR_BUILD = _major + "." + _minor + '.' + _build;


	static {
		final String revisionString = VersionManager.class.getPackage().getImplementationVersion();
		long parsedRevision = -1;
		if (revisionString != null) {
			try {
				parsedRevision = Long.parseLong(revisionString);
				System.out.println("Revision: " + revisionString);
				System.out.println("parsedRevision: " + parsedRevision);
			} catch (final RuntimeException ignore) {
			}
		}
		REVISION = parsedRevision;
		//noinspection StringEqualsEmptyString,SingleCharacterStringConcatenation
		FULL_VERSION = NAME + ' ' + MAJOR_MINOR_BUILD;
	}


	/** e.g. "0.9.21".
	 * @return*/
	public static String getVersion() {
		return MAJOR_MINOR_BUILD;
	}


	public static String getBranch() {
		return _branch;
	}


	public static String getFullVersion() {
		return FULL_VERSION;
	}


	public static long getRevision() {
		return REVISION;
	}


	public static String getName() {
		return NAME;
	}


	public static String getWebsite() {
		return WEBSITE;
	}


	public static String getDownloadWebsite() {
		return DOWNLOAD_WEBSITE;
	}


	public static String getSupportEmail() {
		return SUPPORT_EMAIL;
	}


	@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
	public static void main(final String[] args) {
		if (args.length == 1) {
			final File file = new File(args[0]);
			System.out.println("version string file: " + args[0]);
			OutputStreamWriter writer = null;
			try {
				//noinspection IOResourceOpenedButNotSafelyClosed
				writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8").newEncoder());
				writer.write(getVersion());
				writer.flush();
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				IoUtil.safeClose(writer);
			}

		}
		System.out.println(getVersion());
	}
}
