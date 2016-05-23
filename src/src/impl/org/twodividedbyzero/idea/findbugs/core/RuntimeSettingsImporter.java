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
package org.twodividedbyzero.idea.findbugs.core;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.util.IoUtil;
import org.twodividedbyzero.idea.findbugs.gui.settings.ProjectConfigurableImpl;
import org.twodividedbyzero.idea.findbugs.gui.settings.SettingsImporter;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.event.HyperlinkEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

final class RuntimeSettingsImporter {

	private static final Logger LOGGER = Logger.getInstance(RuntimeSettingsImporter.class);
	private static final NotificationGroup NOTIFICATION_GROUP_ERROR = new NotificationGroup("FindBugs: Analyze Error", NotificationDisplayType.STICKY_BALLOON, true);
	private static final String A_HREF_OPEN_IMPORT_SETTINGS = "#openImportSettings";

	private RuntimeSettingsImporter() {
	}

	static boolean importSettings(
			@NotNull final Project project,
			@NotNull final AbstractSettings settings,
			@NotNull final String filePath,
			@Nullable final String moduleNameForImportFilePath
	) {

		final File file = new File(filePath);
		if (!file.exists()) {
			showImportPreferencesWarning(project, ResourcesLoader.getString("analysis.error.importSettings.notExists", filePath));
			return false;
		}
		if (!file.isFile()) {
			showImportPreferencesWarning(project, ResourcesLoader.getString("analysis.error.importSettings.noFile", filePath));
			return false;
		}
		if (!file.canRead()) {
			showImportPreferencesWarning(project, ResourcesLoader.getString("analysis.error.importSettings.notReadable", filePath));
			return false;
		}

		try {
			final FileInputStream input = new FileInputStream(file);
			try {
				final WorkspaceSettings workspaceSettings = WorkspaceSettings.getInstance(project);
				final Map<String, String> importFilePath = new HashMap<String, String>(workspaceSettings.importFilePath);
				final boolean success = new SettingsImporter(project) {
					@Override
					protected void handleError(@NotNull final String title, @NotNull final String message) {
						showImportPreferencesWarning(project, title, message);
					}
				}.doImport(input, settings, moduleNameForImportFilePath);
				workspaceSettings.importFilePath = importFilePath; // restore current
				return success;
			} finally {
				IoUtil.safeClose(input);
			}
		} catch (final Exception e) {
			final String msg = ResourcesLoader.getString("analysis.error.importSettings.fatal", filePath, e.getMessage());
			LOGGER.error(msg, e);
			showImportPreferencesWarning(project, msg);
			return false;
		}
	}

	private static void showImportPreferencesWarning(@NotNull final Project project, @NotNull String message) {
		showImportPreferencesWarning(project, null, message);
	}

	private static void showImportPreferencesWarning(
			@NotNull final Project project,
			@Nullable final String title,
			@NotNull final String message
	) {

		EventDispatchThreadHelper.invokeLater(new Runnable() {
			@Override
			public void run() {
				showImportPreferencesWarningImpl(project, title, message);
			}
		});
	}

	private static void showImportPreferencesWarningImpl(
			@NotNull final Project project,
			@Nullable String title,
			@NotNull String message
	) {

		if (StringUtil.isEmptyOrSpaces(title)) {
			title = ResourcesLoader.getString("analysis.error.importSettings.title");
		}
		message = message + "<br><br><a href='" + A_HREF_OPEN_IMPORT_SETTINGS + "'>" + ResourcesLoader.getString("edit.settings") + "</a>";
		NOTIFICATION_GROUP_ERROR.createNotification(
				title,
				message,
				NotificationType.ERROR,
				new NotificationListener() {
					@Override
					public void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
						if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())) {
							final String description = event.getDescription();
							if (A_HREF_OPEN_IMPORT_SETTINGS.equals(description)) {
								ProjectConfigurableImpl.showShare(project); // TODO open module config if necessary
							}
						}
					}
				}).notify(project);
	}
}
