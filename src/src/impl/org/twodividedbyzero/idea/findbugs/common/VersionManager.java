/**
 * Copyright 2008 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.twodividedbyzero.idea.findbugs.common;

/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class VersionManager {

	public static final long _major = 0;
	public static final long _minor = 9;
	public static final long _build = 9;

	public static final String _branch = "";// NON-NLS


	private static final String NAME = "FindBugs-IDEA";  // NON-NLS

	private static final String WEBSITE = "http://findbugs-idea.dev.java.net";  // NON-NLS

	private static final String DOWNLOAD_WEBSITE = "http://plugins.intellij.net/plugin/?id=3847";  // NON-NLS

	private static final String SUPPORT_EMAIL = "andrepdo@dev.java.net";  // NON-NLS

	private static final long REVISION;

	private static final String FULL_VERSION_INTERNAL;

	private static final String MAJOR_MINOR_BUILD = _major + "." + _minor + "." + _build;

	private static final String MAJOR_MINOR_BUILD_REVISION;


	static {
		final String revisionString = VersionManager.class.getPackage().getImplementationVersion();
		long parsedRevision = -1;
		if (revisionString != null) {
			try {
				parsedRevision = Long.parseLong(revisionString);
			} catch (final RuntimeException ignore) {
			}
		}
		REVISION = parsedRevision;
		MAJOR_MINOR_BUILD_REVISION = MAJOR_MINOR_BUILD + ((REVISION == -1) ? "" : "." + REVISION);
		FULL_VERSION_INTERNAL = NAME + " " + MAJOR_MINOR_BUILD_REVISION + ("".equals(_branch) ? "" : "-" + _branch);
	}


	/** e.g. "0.9.21". */
	public static String getVersion() {
		return MAJOR_MINOR_BUILD;
	}


	/** e.g. "0.9.21.26427" if revision is available, else "0.9.21". */
	public static String getVersionWithRevision() {
		return MAJOR_MINOR_BUILD_REVISION;
	}


	public static String getBranch() {
		return _branch;
	}


	/* e.g. "FindBugs-IDEA 0.9.21.26427". */
	public static String getFullVersion() {
		return FULL_VERSION_INTERNAL;
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
		System.out.println(getFullVersion());
		System.out.println("$Id: VersionManager.java 26607 2008-10-17 10:10:00Z andrep $");
	}
}
