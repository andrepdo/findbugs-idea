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

package org.twodividedbyzero.idea.findbugs.plugins;

import com.intellij.openapi.diagnostic.Logger;
import edu.umd.cs.findbugs.Plugin;
import edu.umd.cs.findbugs.PluginException;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginUtil;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsCustomPluginUtil;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * $Date: 2014-06-19 00:29:00 +0100 (Do, 19 June 2014) $
 *
 * @author Reto Merz<reto.merz@gmail.com>
 * @version $Revision: 311 $
 * @since 0.9.993
 */
public abstract class AbstractPluginLoader {

	private static final Logger LOGGER = Logger.getInstance(AbstractPluginLoader.class.getName());


	protected AbstractPluginLoader() {
	}


	public void load(final Collection<String> userPluginsUrls, final Collection<String> disabledUserPluginIds, final Collection<String> enabledBundledPluginIds, final Collection<String> disabledBundledPluginIds) {

		// 1. unload plugins
		for (Plugin plugin : Plugin.getAllPlugins()) {
			if (plugin.isCorePlugin()) {
				seenCorePlugin(plugin);
			} else {
				FindBugsCustomPluginUtil.unload(plugin);
			}
		}


		// 2. load bundled plugins and unload
		final File[] bundledPlugins = Plugins.getDirectory(FindBugsPluginUtil.getIdeaPluginDescriptor()).listFiles();
		final Set<String> enabledBundledPluginUrls = new HashSet<String>();
		if (bundledPlugins != null) {
			for (final File pluginFile : bundledPlugins) {
				if (!pluginFile.getName().endsWith(".jar")) {
					continue;
				}
				try {
					if (FindBugsCustomPluginUtil.check(pluginFile)) {
						Plugin plugin = FindBugsCustomPluginUtil.loadTemporary(pluginFile);
						if (plugin == null) {
							handleError("Could not load plugin: " + pluginFile.getPath());
							continue;
						}
						seenBundledPlugin(plugin);
						if (!disabledBundledPluginIds.contains(plugin.getPluginId()) && enabledBundledPluginIds.contains(plugin.getPluginId())) {
							enabledBundledPluginUrls.add(FindBugsCustomPluginUtil.getAsString(plugin));
						}
						FindBugsCustomPluginUtil.unload(plugin);
					} else {
						handleError("Plugin '" + pluginFile.getPath() + "' not loaded. Archive inaccessible.");
					}
				} catch (final Exception e) {
					handleError("Could not load custom findbugs plugin: " + pluginFile.getPath(), e);
				}
			}
		}


		// 3. load user plugins and unload
		final Set<String> enabledUserPluginUrls = new HashSet<String>();
		for (final String pluginUrl : userPluginsUrls) {
			try {
				final File pluginFile = FindBugsCustomPluginUtil.getAsFile(pluginUrl);
				if (FindBugsCustomPluginUtil.check(pluginFile)) {
					final Plugin plugin = FindBugsCustomPluginUtil.loadTemporary(pluginUrl);
					if (plugin == null) {
						handleError("Could not load plugin: " + pluginUrl);
						continue;
					}
					seenUserPlugin(plugin);
					if (!disabledUserPluginIds.contains(plugin.getPluginId())) {
						enabledUserPluginUrls.add(pluginUrl);
					}
					FindBugsCustomPluginUtil.unload(plugin);
				} else {
					handleError("Plugin '" + pluginUrl + "' not loaded. Archive '" + pluginFile.getPath() + "' inaccessible or not exists.");
				}
			} catch (MalformedURLException e) {
				handleError("Could not load plugin: " + pluginUrl, e);
			} catch (PluginException e) {
				handleError("Could not load plugin: " + pluginUrl, e);
			}
		}


		// 4. load enabled plugins
		loadPluginsPermanently(enabledBundledPluginUrls, false);
		loadPluginsPermanently(enabledUserPluginUrls, true);
	}


	private void loadPluginsPermanently(final Set<String> pluginUrls, final boolean userPlugins) {
		for (final String pluginUrl : pluginUrls) {
			try {
				final Plugin plugin = FindBugsCustomPluginUtil.loadPermanently(pluginUrl);
				if (plugin == null) {
					handleError("Could not load plugin: " + pluginUrl);
					continue;
				}
				pluginPermanentlyLoaded(plugin, userPlugins);
			} catch (MalformedURLException e) {
				handleError("Could not load plugin: " + pluginUrl, e);
			} catch (PluginException e) {
				handleError("Could not load plugin: " + pluginUrl, e);
			}
		}
	}


	protected void seenCorePlugin(final Plugin plugin) {
	}


	protected void seenBundledPlugin(final Plugin plugin) {
	}


	protected void seenUserPlugin(final Plugin plugin) {
	}


	protected void pluginPermanentlyLoaded(final Plugin plugin, final boolean userPlugin) {
	}


	protected void handleError(final String message) {
		LOGGER.error(message);
	}


	protected void handleError(final String message, final Exception exception) {
		LOGGER.error(message, exception);
	}
}