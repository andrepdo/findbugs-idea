/*
 * Copyright 2008-2016 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.actions;

import com.intellij.analysis.AnalysisScope;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.IoUtil;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.core.WorkspaceSettings;
import org.twodividedbyzero.idea.findbugs.gui.settings.ProjectConfigurableImpl;
import org.twodividedbyzero.idea.findbugs.gui.settings.SettingsImporter;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.event.HyperlinkEvent;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;

final class AnalyzeUtil {

	static final DataKey<AnalysisScope> KEY;
	private static final Logger LOGGER = Logger.getInstance(AnalyzeUtil.class);
	private static final NotificationGroup NOTIFICATION_GROUP_ERROR = new NotificationGroup("FindBugs: Analyze Error", NotificationDisplayType.STICKY_BALLOON, true);
	private static final String A_HREF_OPEN_IMPORT_SETTINGS = "#openImportSettings";

	static {
		DataKey<AnalysisScope> _key = null;

		try {
			_key = getStaticDataKey(Class.forName("com.intellij.analysis.AnalysisScopeUtil"));
		} catch (final ClassNotFoundException ignored) { // ignore e;
		} catch (final NoSuchFieldException ignored) { // ignore e;
		} catch (final IllegalAccessException e) {
			throw new Error(e); // we are expecting a public field
		}

		if (_key == null) {
			try {
				_key = getStaticDataKey(AnalysisScope.class);
			} catch (final NoSuchFieldException e) {
				throw new Error(e);
			} catch (final IllegalAccessException e) {
				throw new Error(e); // we are expecting a public field
			}
		}

		if (_key == null) {
			throw new Error("No data key");
		}

		KEY = _key;
	}

	private AnalyzeUtil() {
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private static DataKey<AnalysisScope> getStaticDataKey(@Nullable final Class<?> clazz) throws NoSuchFieldException, IllegalAccessException {
		if (clazz != null) {
			final Field key = clazz.getDeclaredField("KEY");
			return (DataKey<AnalysisScope>) key.get(null);
		}
		return null;
	}

	static boolean importSettings(
			@NotNull final Project project,
			@NotNull final ProjectSettings projectSettings,
			@NotNull final String filePath
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
				final String importFilePath = workspaceSettings.importFilePath;
				final boolean success = new SettingsImporter(project) {
					@Override
					protected void handleError(@NotNull final String title, @NotNull final String message) {
						showImportPreferencesWarning(project, title, message);
					}
				}.doImport(input, projectSettings);
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
								ProjectConfigurableImpl.showShare(project);
							}
						}
					}
				}).notify(project);
	}
}
