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

package org.twodividedbyzero.idea.findbugs.common.util;

import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.Plugin;
import edu.umd.cs.findbugs.PluginException;
import edu.umd.cs.findbugs.PluginLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


/**
 * $Date: 2013-06-02 00:44:00 +0100 (Mo, 02 June 2014) $
 *
 * @author Reto Merz<reto.merz@gmail.com>
 * @version $Revision: 308 $
 * @since 0.9.993
 */
public final class FindBugsCustomPluginUtil {


	private FindBugsCustomPluginUtil() {
	}


	public static boolean check(@NotNull final File plugin) {
		return plugin.exists() && plugin.canRead();
	}


	public static String getAsString(@NotNull final Plugin plugin) {
		return getAsURL(plugin).toExternalForm();
	}


	private static URL getAsURL(@NotNull final Plugin plugin) {
		return plugin.getPluginLoader().getURL();
	}


	public static String getAsString(@NotNull final File plugin) throws MalformedURLException {
		return getAsURL(plugin).toExternalForm();
	}


	private static URL getAsURL(@NotNull final File plugin) throws MalformedURLException {
		return plugin.toURI().toURL();
	}


	public static File getAsFile(@NotNull final String pluginUrl) throws MalformedURLException {
		return new File(new URL(pluginUrl).getFile());
	}


	public static Plugin loadTemporary(@NotNull final File file) throws MalformedURLException, PluginException {
		return loadTemporary(getAsURL(file));
	}


	public static Plugin loadTemporary(@NotNull final String plugin) throws MalformedURLException, PluginException {
		return loadTemporary(new URL(plugin));
	}


	private static Plugin loadTemporary(@NotNull URL plugin) throws MalformedURLException, PluginException {
		final PluginLoader pluginLoader = PluginLoader.getPluginLoader(plugin, PluginLoader.class.getClassLoader(), false, true);
		final Plugin ret = pluginLoader.loadPlugin();
		if (ret != null) {
			ret.setGloballyEnabled(true);
		}
		return ret;
	}


	public static Plugin loadPermanently(@NotNull final String pluginUrl) throws MalformedURLException, PluginException {
		return loadPermanently(new URL(pluginUrl));
	}


	public static Plugin loadPermanently(@NotNull final URL plugin) throws PluginException {
		final Plugin ret = Plugin.loadCustomPlugin(plugin, null);
		if (ret != null) {
			ret.setGloballyEnabled(true);
		}
		return ret;
	}


	public static void unload(@NotNull final Plugin plugin) {
		Plugin.removeCustomPlugin(plugin);
	}


	public static Plugin loadTemporaryPermanently(@NotNull final Plugin plugin) throws PluginException {
		// since DetectorFactoryCollection.instance().loadPlugin() is package protected we need to unload an load
		final URL url = getAsURL(plugin);
		unload(plugin);
		return loadPermanently(url);
	}


	public static Plugin getPlugin(@NotNull final String pluginUrl) {
		for (Plugin loaded : Plugin.getAllPlugins()) {
			if (pluginUrl.equals(getAsString(loaded))) {
				return loaded;
			}
		}
		return null;
	}


	private static void setDetectorEnabled(@NotNull final Map<String, String> detectors, @NotNull final DetectorFactory detector, @Nullable final Boolean enabled) {
		if (enabled == null) {
			detectors.remove(detector.getShortName());
		} else {
			detectors.put(detector.getShortName(), String.valueOf(enabled));
		}
	}


	public static void setDetectorEnabled(@NotNull final Plugin plugin, @NotNull final Map<String, String> detectors, @Nullable final Boolean enabled) {
		for (final DetectorFactory detector : plugin.getDetectorFactories()) {
			if (enabled != null && enabled) {
				setDetectorEnabled(detectors, detector, detector.isDefaultEnabled());
			} else {
				setDetectorEnabled(detectors, detector, enabled);
			}
		}
	}


	public static boolean isDetectorConfigured(@NotNull final Map<String, String> detectors, @NotNull final DetectorFactory detector) {
		return null != detectors.get(detector.getShortName());
	}


	public static boolean isConfigured(@NotNull final Plugin plugin, @NotNull final Map<String, String> detectors) {
		for (final DetectorFactory detector : plugin.getDetectorFactories()) {
			if (isDetectorConfigured(detectors, detector)) {
				return true;
			}
		}
		return false;
	}
}