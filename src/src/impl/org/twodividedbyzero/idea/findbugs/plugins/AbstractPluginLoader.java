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

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
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
import org.twodividedbyzero.idea.findbugs.gui.settings.ProjectConfigurableImpl;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.event.HyperlinkEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractPluginLoader {

	private static final Logger LOGGER = Logger.getInstance(AbstractPluginLoader.class);

	private final boolean addEditSettingsLinkToErrorMessage;
	private final List<String> errorMessages;

	protected AbstractPluginLoader(final boolean addEditSettingsLinkToErrorMessage) {
		this.addEditSettingsLinkToErrorMessage = addEditSettingsLinkToErrorMessage;
		errorMessages = new ArrayList<String>();
	}

	public void load(@NotNull final Set<PluginSettings> settings) {

		PluginLoader.invalidate();

		// 1. unload plugins
		for (final Plugin plugin : Plugin.getAllPlugins()) {
			if (plugin.isCorePlugin()) {
				seenCorePlugin(plugin);
			} else {
				FindBugsCustomPluginUtil.unload(plugin);
			}
		}


		// 2. load bundled plugins and unload
		final IdeaPluginDescriptor ideaPluginDescriptor = FindBugsPluginUtil.getIdeaPluginDescriptor();
		Plugins.deploy(ideaPluginDescriptor);
		final File[] bundledPlugins = Plugins.getDirectory(ideaPluginDescriptor).listFiles();
		final Set<String> enabledBundledPluginUrls = New.set();
		if (bundledPlugins != null) {
			for (final File pluginFile : bundledPlugins) {
				if (!pluginFile.getName().endsWith(".jar")) {
					continue;
				}
				if (!pluginFile.isFile()) {
					continue;
				}
				if (!pluginFile.canRead()) {
					continue;
				}
				try {
					Plugin plugin = FindBugsCustomPluginUtil.loadTemporary(pluginFile);
					PluginSettings pluginSettings = PluginSettings.findBundledById(settings, plugin.getPluginId());
					if (pluginSettings == null) {
						pluginSettings = new PluginSettings();
						pluginSettings.id = plugin.getPluginId();
						pluginSettings.bundled = true;
						pluginSettings.enabled = false;
						seenBundledPlugin(PluginInfo.create(pluginSettings, plugin));
					} else {
						seenConfiguredPlugin(PluginInfo.create(pluginSettings, plugin));
					}
					if (pluginSettings.enabled) {
						enabledBundledPluginUrls.add(FindBugsCustomPluginUtil.getAsString(plugin));
					}
					FindBugsCustomPluginUtil.unload(plugin);
				} catch (final Exception e) {
					LOGGER.warn("Could not load plugin: " + pluginFile, e);
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
				if (!pluginFile.exists()) {
					final String error = ResourcesLoader.getString("error.path.exists", pluginFile.getPath());
					seenConfiguredPlugin(PluginInfo.create(pluginSettings, error));
					handleError(error);
					continue;
				}
				if (!pluginFile.isFile()) {
					final String error = ResourcesLoader.getString("error.file.type", pluginFile.getPath());
					seenConfiguredPlugin(PluginInfo.create(pluginSettings, error));
					handleError(error);
					continue;
				}
				if (!pluginFile.canRead()) {
					final String error = ResourcesLoader.getString("error.file.readable", pluginFile.getPath());
					seenConfiguredPlugin(PluginInfo.create(pluginSettings, error));
					handleError(error);
					continue;
				}
				final Plugin plugin = FindBugsCustomPluginUtil.loadTemporary(pluginUrl);
				seenConfiguredPlugin(PluginInfo.create(pluginSettings, plugin));
				if (pluginSettings.enabled) {
					enabledUserPluginUrls.add(pluginUrl);
				}
				FindBugsCustomPluginUtil.unload(plugin);
			} catch (final Exception e) {
				final String error = ResourcesLoader.getString("plugins.load.error.text.path", pluginUrl);
				seenConfiguredPlugin(PluginInfo.create(pluginSettings, error));
				handleFatalError(error, e);
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
				if (plugin != null) {
					pluginPermanentlyLoaded(plugin, userPlugins);
				} else {
					handleFatalError("Could not load plugin: " + pluginUrl, null);
				}
			} catch (final MalformedURLException e) {
				handleFatalError("Could not load plugin: " + pluginUrl, e);
			} catch (final PluginException e) {
				handleFatalError("Could not load plugin: " + pluginUrl, e);
			}
		}
	}

	protected void seenCorePlugin(@NotNull final Plugin plugin) {
	}

	protected void seenBundledPlugin(@NotNull final PluginInfo plugin) {
	}

	protected void seenConfiguredPlugin(@NotNull final PluginInfo plugin) {
	}

	protected void pluginPermanentlyLoaded(@NotNull final Plugin plugin, final boolean userPlugin) {
	}

	protected void handleError(@NotNull final String message) {
		LOGGER.error(message, (Throwable) null);
		errorMessages.add(message);
	}

	protected void handleFatalError(@NotNull final String message, @Nullable final Throwable exception) {
		LOGGER.error(message, exception);
		errorMessages.add(message);
	}

	@Nullable
	private String makeHtmlErrorMessage() {
		if (!errorMessages.isEmpty()) {
			final StringBuilder message = new StringBuilder();
			for (final String errorMessage : errorMessages) {
				if (message.length() > 0) {
					message.append("<br>");
				}
				message.append(" - ").append(errorMessage);
			}
			if (addEditSettingsLinkToErrorMessage) {
				message.append("<br><br>").append("<a href=edit>").append(ResourcesLoader.getString("edit.settings")).append("</a>");
			}
			return message.toString();
		}
		return null;
	}

	public boolean showErrorNotificationIfNecessary(@Nullable final Project project) {
		final String errorMessage = makeHtmlErrorMessage();
		if (errorMessage != null) {
			// do not use BalloonTipFactory here, at this point FindBugs tool window is not yet created
			// code adapted from com.intellij.openapi.components.impl.stores.StorageUtil#notifyUnknownMacros
			UIUtil.invokeLaterIfNeeded(new RunnableErrorNotification(project, errorMessage));
			return false;
		}
		return true;
	}

	private static class RunnableErrorNotification implements Runnable {
		@Nullable
		private final Project project;

		@NotNull
		private final String message;

		RunnableErrorNotification(@Nullable final Project project, @NotNull String message) {
			this.project = project;
			this.message = message;
		}

		@Override
		public void run() {
			final Project currentProject;
			if (project != null) {
				currentProject = project;
			} else {
				currentProject = ProjectManager.getInstance().getDefaultProject();
			}
			new Notification(
					"FindBugs Custom Plugin Load Error",
					ResourcesLoader.getString("error.customPlugins.title"),
					message,
					NotificationType.ERROR,
					new NotificationListener() {
						@Override
						public void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
							if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())) {
								notification.hideBalloon();
								ProjectConfigurableImpl.show(currentProject);
							}
						}
					}
			).notify(currentProject);
		}
	}
}
