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
package org.twodividedbyzero.idea.findbugs.common;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
class FindBugsPluginLoader {

	private static final Logger LOGGER = Logger.getInstance(FindBugsPluginLoader.class.getName());

	private static final URL[] URL = new URL[0];

	// DetectorFactoryCollection.rawInstance().setPluginList(new URL[0])


	private FindBugsPluginLoader() {
	}


	public static URL[] determinePlugins() {
		return determinePlugins(new String[] {FindBugsPluginConstants.FINDBUGS_PLUGIN_PATH});
	}


	/**
	 * Collect all findbugs detector plugins from a given dir.
	 *
	 * @param dirPaths a direcotry paths string array
	 * @return a URL[] List
	 */
	private static URL[] determinePlugins(@NotNull final String[] dirPaths) {
		dirPaths[dirPaths.length - 1] = FindBugsPluginConstants.FINDBUGS_PLUGIN_PATH;
		final ArrayList<URL> arr = new ArrayList<URL>();
		final URL[] pluginList;

		for (final String pathName : dirPaths) {
			LOGGER.debug("Searching for plugins: " + pathName);

			final File pluginDir = new File(pathName);
			final File[] contentList = pluginDir.listFiles();
			if (contentList == null) {
				pluginList = URL;
				return pluginList;
			}

			for (final File aContentList : contentList) {
				if (aContentList.getName().endsWith(".jar")) {

					try {
						arr.add(aContentList.toURI().toURL());
						LOGGER.info("Found plugin: " + aContentList.toString());
					} catch (MalformedURLException ignore) {
					}
				}
			}
		}
		pluginList = arr.toArray(new URL[arr.size()]);
		return pluginList;
	}
}
