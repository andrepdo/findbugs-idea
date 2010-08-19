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
public class FindBugsPluginLoader {

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
	public static URL[] determinePlugins(@NotNull final String[] dirPaths) {
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
