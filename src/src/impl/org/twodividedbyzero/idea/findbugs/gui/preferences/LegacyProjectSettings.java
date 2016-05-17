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
package org.twodividedbyzero.idea.findbugs.gui.preferences;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsCustomPluginUtil;
import org.twodividedbyzero.idea.findbugs.core.PluginSettings;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
import org.twodividedbyzero.idea.findbugs.preferences.PersistencePreferencesBean;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@State(
		name = FindBugsPluginConstants.PLUGIN_ID,
		storages = {
				@Storage(id = "other", file = "$PROJECT_FILE$", deprecated = true),
				@Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/findbugs-idea.xml", scheme = StorageScheme.DIRECTORY_BASED, deprecated = true)})
public final class LegacyProjectSettings implements PersistentStateComponent<PersistencePreferencesBean> {

	private PersistencePreferencesBean state;

	@Nullable
	@Override
	public PersistencePreferencesBean getState() {
		return state;
	}

	@Override
	public void loadState(final PersistencePreferencesBean state) {
		this.state = state;
	}

	public static LegacyProjectSettings getInstance(@NotNull final Project project) {
		return ServiceManager.getService(project, LegacyProjectSettings.class);
	}

	public void applyTo(@NotNull final ProjectSettings settings) {
		if (state == null) {
			return;
		}

		final Map<String, String> basePreferences = state.getBasePreferences();
		if (basePreferences != null && !basePreferences.isEmpty()) {
			final String compileBeforeAnalyze = state.getBasePreferences().get(FindBugsPreferences.COMPILE_BEFORE_ANALYZE);
			if (!StringUtil.isEmptyOrSpaces(compileBeforeAnalyze)) {
				settings.compileBeforeAnalyze = Boolean.valueOf(compileBeforeAnalyze);
			}
			final String analyzeAfterCompile = state.getBasePreferences().get(FindBugsPreferences.ANALYZE_AFTER_COMPILE);
			if (!StringUtil.isEmptyOrSpaces(analyzeAfterCompile)) {
				settings.analyzeAfterCompile = Boolean.valueOf(analyzeAfterCompile);
			}
			final String analyzeAfterAutoMake = state.getBasePreferences().get(FindBugsPreferences.ANALYZE_AFTER_AUTOMAKE);
			if (!StringUtil.isEmptyOrSpaces(analyzeAfterAutoMake)) {
				settings.analyzeAfterAutoMake = Boolean.valueOf(analyzeAfterAutoMake);
			}
			final String runInBackground = state.getBasePreferences().get(FindBugsPreferences.RUN_ANALYSIS_IN_BACKGROUND);
			if (!StringUtil.isEmptyOrSpaces(runInBackground)) {
				settings.runInBackground = Boolean.valueOf(runInBackground);
			}
			final String toolWindowToFront = state.getBasePreferences().get(FindBugsPreferences.TOOLWINDOW_TO_FRONT);
			if (!StringUtil.isEmptyOrSpaces(toolWindowToFront)) {
				settings.toolWindowToFront = Boolean.valueOf(toolWindowToFront);
			}
			final String analysisEffort = state.getBasePreferences().get(FindBugsPreferences.ANALYSIS_EFFORT_LEVEL);
			if (!StringUtil.isEmptyOrSpaces(analysisEffort)) {
				settings.analysisEffort = analysisEffort;
			}
			final String minPriority = state.getBasePreferences().get(FindBugsPreferences.MIN_PRIORITY_TO_REPORT);
			if (!StringUtil.isEmptyOrSpaces(minPriority)) {
				settings.minPriority = minPriority;
			}
		}

		final Map<String, String> bugCategories = state.getBugCategories();
		if (bugCategories != null && !bugCategories.isEmpty()) {
			for (final String category : new String[]{
					"BAD_PRACTICE",
					"MALICIOUS_CODE",
					"CORRECTNESS",
					"PERFORMANCE",
					"SECURITY",
					"STYLE",
					"EXPERIMENTAL",
					"MT_CORRECTNESS",
					"I18N"
			}) {
				if (bugCategories.containsKey(category)) {
					final String value = bugCategories.get(category);
					if (!StringUtil.isEmptyOrSpaces(value) && !Boolean.valueOf(value)) {
						settings.hiddenBugCategory.add(category);
					}
				}
			}
		}

		final List<String> includeFilters = state.getIncludeFilters();
		if (includeFilters != null && !includeFilters.isEmpty()) {
			for (final String includeFilter : includeFilters) {
				settings.includeFilterFiles.put(includeFilter, true);
			}
		}
		final List<String> excludeFilters = state.getExcludeFilters();
		if (excludeFilters != null && !excludeFilters.isEmpty()) {
			for (final String excludeFilter : excludeFilters) {
				settings.excludeFilterFiles.put(excludeFilter, true);
			}
		}
		final List<String> excludeBugsFiles = state.getExcludeBaselineBugs();
		if (excludeBugsFiles != null && !excludeBugsFiles.isEmpty()) {
			for (final String excludeBugsFile : excludeBugsFiles) {
				settings.excludeBugsFiles.put(excludeBugsFile, true);
			}
		}

		// TODO

		apply(state.getDetectors(), settings.detectors);
		final Collection<String> enabledBundledPluginIds = state.getEnabledBundledPluginIds();
		if (enabledBundledPluginIds != null && !enabledBundledPluginIds.isEmpty()) {
			for (final String enabledBundledPluginId : enabledBundledPluginIds) {
				final PluginSettings pluginSettings = new PluginSettings();
				pluginSettings.id = enabledBundledPluginId;
				pluginSettings.enabled = true;
				pluginSettings.bundled = true;
				apply(state.getDetectors(), pluginSettings.detectors);
				settings.plugins.add(pluginSettings);
			}
		}

		final List<String> plugins = state.getPlugins();
		if (plugins != null && !plugins.isEmpty()) {
			for (final String pluginUrl : plugins) {
				try {
					final File pluginJar = FindBugsCustomPluginUtil.getAsFile(pluginUrl);
					if (pluginJar.isFile() && pluginJar.canRead()) {
						final PluginSettings pluginSettings = new PluginSettings();
						pluginSettings.enabled = true;
						pluginSettings.url = pluginUrl;
						apply(state.getDetectors(), pluginSettings.detectors);
						settings.plugins.add(pluginSettings);
					}
				} catch (final MalformedURLException ignore) {
				}
			}
		}
	}

	private static void apply(@Nullable final Map<String, String> from, @NotNull final Map<String, Boolean> to) {
		if (from != null && !from.isEmpty()) {
			for (final Map.Entry<String, String> fromEntry : from.entrySet()) {
				to.put(fromEntry.getKey(), Boolean.valueOf(fromEntry.getValue()));
			}
		}
	}
}
