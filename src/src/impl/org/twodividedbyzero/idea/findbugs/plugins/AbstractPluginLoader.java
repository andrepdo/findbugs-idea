/*
 * Copyright 2016 Andre Pfeiler
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

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.util.ui.UIUtil;
import edu.umd.cs.findbugs.Plugin;
import edu.umd.cs.findbugs.PluginException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginUtil;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsCustomPluginUtil;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.core.PluginSettings;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractPluginLoader {

	private static final Logger LOGGER = Logger.getInstance(AbstractPluginLoader.class);

	private final boolean _treatErrorsAsWarnings;
	private final List<String> _errorMessages;

	protected AbstractPluginLoader(final boolean treatErrorsAsWarnings) {
		_treatErrorsAsWarnings = treatErrorsAsWarnings;
		_errorMessages = new ArrayList<String>();
	}

	public void load(@NotNull final Set<PluginSettings> settings) {

		// 1. unload plugins
		for (final Plugin plugin : Plugin.getAllPlugins()) {
			if (plugin.isCorePlugin()) {
				seenCorePlugin(plugin);
			} else {
				FindBugsCustomPluginUtil.unload(plugin);
			}
		}


		// 2. load bundled plugins and unload
		final File[] bundledPlugins = Plugins.getDirectory(FindBugsPluginUtil.getIdeaPluginDescriptor()).listFiles();
		final Set<String> enabledBundledPluginUrls = New.set();
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
						PluginSettings pluginSettings = PluginSettings.findBundledById(settings, plugin.getPluginId());
						if (pluginSettings == null) {
							pluginSettings = new PluginSettings();
							pluginSettings.id = plugin.getPluginId();
							pluginSettings.bundled = true;
							pluginSettings.enabled = false;
							pluginSettings.url = FindBugsCustomPluginUtil.getAsString(plugin);
						}
						if (pluginSettings.enabled) {
							enabledBundledPluginUrls.add(FindBugsCustomPluginUtil.getAsString(plugin));
						}
						seenPlugin(pluginSettings, plugin, true);
						FindBugsCustomPluginUtil.unload(plugin);
					} else {
						handleError("Plugin '" + pluginFile.getPath() + "' not loaded. Archive inaccessible.");
					}
				} catch (final Throwable e) {
					handleError("Could not load custom findbugs plugin: " + pluginFile.getPath(), e);
				}
			}
		}


		// 3. load user plugins and unload
		final Set<String> enabledUserPluginUrls = new HashSet<String>();
		for (final PluginSettings pluginSettings : settings) {
			if (pluginSettings.bundled) {
				continue;
			}
			final String pluginUrl = pluginSettings.url;
			try {
				final File pluginFile = FindBugsCustomPluginUtil.getAsFile(pluginUrl);
				if (FindBugsCustomPluginUtil.check(pluginFile)) {
					final Plugin plugin = FindBugsCustomPluginUtil.loadTemporary(pluginUrl);
					if (plugin == null) {
						handleError("Could not load plugin: " + pluginUrl);
						continue;
					}
					seenPlugin(pluginSettings, plugin, false);
					if (pluginSettings.enabled) {
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

	private void loadPluginsPermanently(@NotNull final Set<String> pluginUrls, final boolean userPlugins) {
		for (final String pluginUrl : pluginUrls) {
			try {
				final Plugin plugin = FindBugsCustomPluginUtil.loadPermanently(pluginUrl);
				if (plugin == null) {
					handleError("Could not load plugin: " + pluginUrl);
					continue;
				}
				pluginPermanentlyLoaded(plugin, userPlugins);
			} catch (final MalformedURLException e) {
				handleError("Could not load plugin: " + pluginUrl, e);
			} catch (final PluginException e) {
				handleError("Could not load plugin: " + pluginUrl, e);
			}
		}
	}

	protected void seenCorePlugin(@NotNull final Plugin plugin) {
	}

	protected void seenPlugin(@NotNull final PluginSettings settings, @NotNull final Plugin plugin, final boolean bundled) {
	}

	protected void pluginPermanentlyLoaded(@NotNull final Plugin plugin, final boolean userPlugin) {
	}

	protected void handleError(@NotNull final String message) {
		if (_treatErrorsAsWarnings) {
			LOGGER.warn(message);
		} else {
			LOGGER.error(message);
		}
		_errorMessages.add(message);
	}

	protected void handleError(@NotNull final String message, @Nullable final Throwable exception) {
		if (_treatErrorsAsWarnings) {
			LOGGER.warn(message, exception);
		} else {
			LOGGER.error(message, exception);
		}
		_errorMessages.add(message);
	}

	@Nullable
	public String makeErrorMessage() {
		if (!_errorMessages.isEmpty()) {
			final StringBuilder message = new StringBuilder();
			for (final String errorMessage : _errorMessages) {
				if (message.length() > 0) {
					message.append("<b>");
				}
				message.append(" - ").append(errorMessage);
			}
			return message.toString();
		}
		return null;
	}

	public void showErrorBalloonIfNecessary(@Nullable final Project project) {
		final String errorMessage = makeErrorMessage();
		if (errorMessage != null) {
			// do not use BalloonTipFactory here, at this point FindBugs tool window is not yet created
			// code adapted from com.intellij.openapi.components.impl.stores.StorageUtil#notifyUnknownMacros
			UIUtil.invokeLaterIfNeeded(new RunnableErrorNotification(project, errorMessage));
		}
	}

	private static class RunnableErrorNotification implements Runnable {
		private final Project _project;
		private final String _message;


		RunnableErrorNotification(@Nullable final Project project, @NotNull String message) {
			_project = project;
			_message = message;
		}


		@Override
		public void run() {
			Project currentProject = _project;
			if (_project == null) {
				currentProject = ProjectManager.getInstance().getDefaultProject();
			}
			new Notification("FindBugs Custom Plugin Load Error", "Error while loading custom FindBugs plugins", _message, NotificationType.ERROR).notify(currentProject);
		}
	}
}
