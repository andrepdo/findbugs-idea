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
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.IoUtil;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.gui.preferences.ConfigurationPanel;
import org.twodividedbyzero.idea.findbugs.gui.preferences.importer.SonarProfileImporter;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
import org.twodividedbyzero.idea.findbugs.preferences.PersistencePreferencesBean;

import javax.swing.event.HyperlinkEvent;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.List;

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


	static void importPreferences(@NotNull final FindBugsPlugin plugin, @NotNull final String filePath) {

		final File file = new File(filePath);
		if (!file.exists()) {
			showImportPreferencesWarning(plugin, String.format("Specified 'Import File' (%s) not exists", filePath));
			return;
		}
		if (!file.isFile()) {
			showImportPreferencesWarning(plugin, String.format("Specified 'Import File' (%s) is not a file", filePath));
			return;
		}
		if (!file.canRead()) {
			showImportPreferencesWarning(plugin, String.format("Specified 'Import File' (%s) is not readable", filePath));
			return;
		}

		try {
			final FileInputStream input = new FileInputStream(file);
			try {
				final Document document = JDOMUtil.loadDocument(input);
				importPreferencesImpl(plugin, filePath, document);
			} finally {
				IoUtil.safeClose(input);
			}
		} catch (final Exception e) {
			final String msg = String.format("Fatal error while import the specified 'Import File' (%s): %s" + "\n" +
					"See logs for more details/stack", filePath, e.getMessage());
			LOGGER.warn(msg);
			showImportPreferencesWarning(plugin, msg);
		}
	}


	/**
	 * LATER: Consolidate this with {@link ConfigurationPanel#importPreferences()}.
	 */
	private static void importPreferencesImpl(
			@NotNull final FindBugsPlugin plugin,
			@NotNull final String filePath,
			@NotNull final Document document
	) {

		final PersistencePreferencesBean prefs;
		if (SonarProfileImporter.isValid(document)) {
			prefs = SonarProfileImporter.doImportLegacy(plugin.getProject(), document);
			if (prefs == null) {
				return;
			}
		} else {
			if (!PersistencePreferencesBean.PERSISTENCE_ROOT_NAME.equals(document.getRootElement().getName())) {
				showImportPreferencesWarning(plugin, String.format("Specified 'Import File' (%s) is not supported", filePath));
				return;
			}
			prefs = XmlSerializer.deserialize(document, PersistencePreferencesBean.class);
			prefs.getBasePreferences().put(FindBugsPreferences.IMPORT_FILE_PATH, filePath); // restore current
		}
		if (!validatePreferences(plugin, prefs)) {
			return;
		}
		plugin.loadState(prefs);
	}


	private static boolean validatePreferences(
			@NotNull final FindBugsPlugin plugin,
			@Nullable final PersistencePreferencesBean prefs
	) {

		if (prefs == null) {
			showImportPreferencesWarning(plugin, "Malformed configuration (specified by 'Import File')");
			return false;
		} else if (prefs.isEmpty()) {
			showImportPreferencesWarning(plugin, "Empty configuration (specified by 'Import File')");
			return false;
		}
		final List<String> invalidPlugins = FindBugsPreferences.collectInvalidPlugins(prefs.getPlugins());
		if (!invalidPlugins.isEmpty()) {
			final StringBuilder sb = new StringBuilder("Configuration (specified by 'Import File') contains invalid plugins:");
			for (final String invalidPlugin : invalidPlugins) {
				sb.append("\n    - ").append(invalidPlugin);
			}
			showImportPreferencesWarning(plugin, sb.toString());
			return false;
		}
		return true;
	}


	private static void showImportPreferencesWarning(
			@NotNull final FindBugsPlugin plugin,
			@NotNull String message
	) {

		message = message + "<br><br><a href='" + A_HREF_OPEN_IMPORT_SETTINGS + "'>" + "Edit settings" + "</a>";
		NOTIFICATION_GROUP_ERROR.createNotification("Import Preferences", message, NotificationType.ERROR, new NotificationListener() {
			@Override
			public void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())) {
					final String description = event.getDescription();
					if (A_HREF_OPEN_IMPORT_SETTINGS.equals(description)) {
						openImportSettings(plugin);
					}
				}
			}
		}).notify(plugin.getProject());
	}


	private static void openImportSettings(@NotNull final FindBugsPlugin plugin) {
		ShowSettingsUtil.getInstance().editConfigurable(plugin.getProject(), plugin, new Runnable() {
			@SuppressWarnings("ConstantConditions")
			@Override
			public void run() {
				final ConfigurationPanel panel = ((ConfigurationPanel) plugin.createComponent());
				panel.showConfigPage(panel.getImportExportConfig());
				panel.getImportExportConfig().getImportFilePathTextField().requestFocus();
			}
		});
	}
}
