/*
 * Copyright 2008-2015 Andre Pfeiler
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

package org.twodividedbyzero.idea.findbugs.core;


import com.intellij.CommonBundle;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetTypeId;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.gui.common.NotificationUtil;
import org.twodividedbyzero.idea.findbugs.gui.preferences.ConfigurationPanel;
import org.twodividedbyzero.idea.findbugs.plugins.Plugins;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import javax.swing.event.HyperlinkEvent;
import java.util.Set;


/**
 * @author Reto Merz<reto.merz@gmail.com>
 * @since 0.9.998
 */
final class PluginSuggestion {

	private static final String A_HREF_DISABLE_ANCHOR = "#disable";


	private PluginSuggestion() {
	}


	static void suggest(
			@NotNull final Project project,
			@NotNull final FindBugsPlugin findBugsPlugin,
			@Nullable final FindBugsPreferences preferences
	) {

		if (preferences == null) {
			return;
		}
		if (!NotificationUtil.isGroupEnabled(FindBugsPluginImpl.NOTIFICATION_GROUP_ID_PLUGIN_SUGGESTION)) {
			return;
		}
		if (isPluginEnabled(Plugins.PLUGIN_ID_ANDROID, preferences)) {
			return;
		}
		final Set<Suggestion> suggestions = collectSuggestions(project, preferences);
		if (!suggestions.isEmpty()) {
			showSuggestions(project, findBugsPlugin, preferences, suggestions);
		}
	}


	private static boolean isPluginEnabled(@NotNull final String pluginId, @NotNull final FindBugsPreferences preferences) {
		return preferences.isPluginEnabled(pluginId, true) ||
				preferences.isPluginEnabled(pluginId, false);
	}


	@SuppressWarnings("UnusedParameters") // LATER: enabled plugin - think of bundled and user plugins
	private static void enablePlugin(
			@NotNull final Project project,
			@NotNull final FindBugsPlugin findBugsPlugin,
			@NotNull final String pluginId,
			@NotNull final FindBugsPreferences preferences
	) {

		ShowSettingsUtil.getInstance().editConfigurable(project, findBugsPlugin, new Runnable() {
			@SuppressWarnings("ConstantConditions")
			@Override
			public void run() {
				final ConfigurationPanel panel = ((ConfigurationPanel)findBugsPlugin.createComponent());
				panel.showConfigPage(panel.getPluginConfig());
			}
		});
	}


	private static void showSuggestions(
			@NotNull final Project project,
			@NotNull final FindBugsPlugin findBugsPlugin,
			@NotNull final FindBugsPreferences preferences,
			@NotNull final Set<Suggestion> suggestions
	) {

		final StringBuilder sb = new StringBuilder();
		for (final Suggestion suggestion : suggestions) {
			sb.append("&nbsp;&nbsp;- <a href='").append(suggestion._pluginId).append("'>").append("Enable '").append(suggestion._name).append("'</a><br>");
		}
		sb.append("<br><a href='").append(A_HREF_DISABLE_ANCHOR).append("'>Disable Suggestion</a>");

		FindBugsPluginImpl.NOTIFICATION_GROUP_PLUGIN_SUGGESTION.createNotification(
				"FindBugs Plugin Suggestion",
				sb.toString(),
				NotificationType.INFORMATION,
				new NotificationListener() {
					@Override
					public void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
						if (event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
							final String desc = event.getDescription();
							if (desc.equals(A_HREF_DISABLE_ANCHOR)) {
								final int result = Messages.showYesNoDialog(
										project,
										"Notification will be disabled for all projects.\n\n" +
												"Settings | Appearance & Behavior | Notifications | " +
												FindBugsPluginImpl.NOTIFICATION_GROUP_ID_PLUGIN_SUGGESTION +
												"\ncan be used to configure the notification.",
										"FindBugs Plugin Suggestion Notification",
										"Disable Notification", CommonBundle.getCancelButtonText(), Messages.getWarningIcon());
								if (result == Messages.YES) {
									NotificationUtil.getNotificationsConfigurationImpl().changeSettings(
											FindBugsPluginImpl.NOTIFICATION_GROUP_ID_PLUGIN_SUGGESTION,
											NotificationDisplayType.NONE, false, false);
									notification.expire();
								} else {
									notification.hideBalloon();
								}
							} else {
								enablePlugin(project, findBugsPlugin, desc, preferences);
								notification.hideBalloon();
							}
						}
					}
				}
		).setImportant(false).notify(project);
	}


	@NotNull
	private static Set<Suggestion> collectSuggestions(@NotNull final Project project, @NotNull final FindBugsPreferences preferences) {
		final Set<Suggestion> ret = New.set();
		collectSuggestionsByModules(project, preferences, ret);
		return ret;
	}


	private static void collectSuggestionsByModules(
			@NotNull final Project project,
			@NotNull final FindBugsPreferences preferences,
			@NotNull final Set<Suggestion> suggestions
	) {

		final ModuleManager moduleManager = ModuleManager.getInstance(project);
		if (moduleManager == null) {
			return;
		}
		final Module[] modules = moduleManager.getModules();
		for (final Module module : modules) {
			collectSuggestionsByFacets(preferences, module, suggestions);
		}
	}


	private static void collectSuggestionsByFacets(
			@NotNull final FindBugsPreferences preferences,
			@NotNull final Module module,
			@NotNull final Set<Suggestion> suggestions
	) {

		final FacetManager facetManager = FacetManager.getInstance(module);
		if (facetManager == null) {
			return;
		}
		final Facet[] facets = facetManager.getAllFacets();
		FacetTypeId facetTypeId;
		for (final Facet facet : facets) {
			facetTypeId = facet.getTypeId();
			if (facetTypeId != null) {
				if (!isPluginEnabled(Plugins.PLUGIN_ID_ANDROID, preferences)) {
					if ("AndroidFacetType".equals(facetTypeId.getClass().getSimpleName()) ||
							"android".equalsIgnoreCase(facetTypeId.toString())) {
						suggestions.add(new Suggestion(Plugins.PLUGIN_ID_ANDROID, "Android FindBugs"));
					}
				}
			}
		}
	}


	private static class Suggestion {

		private final String _pluginId;
		private final String _name;


		Suggestion(@NotNull final String pluginId, @NotNull final String name) {
			_pluginId = pluginId;
			_name = name;
		}


		@Override
		public boolean equals(@Nullable final Object o) {
			return this == o || !(o == null || getClass() != o.getClass()) && _pluginId.equals(((Suggestion) o)._pluginId);
		}


		@Override
		public int hashCode() {
			return _pluginId.hashCode();
		}
	}
}
