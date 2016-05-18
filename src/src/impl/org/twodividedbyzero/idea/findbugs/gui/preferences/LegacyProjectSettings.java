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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.core.PluginSettings;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.plugins.AbstractPluginLoaderLegacy;
import org.twodividedbyzero.idea.findbugs.plugins.Plugins;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
import org.twodividedbyzero.idea.findbugs.preferences.PersistencePreferencesBean;

import java.util.List;
import java.util.Map;

@State(
		name = FindBugsPluginConstants.PLUGIN_ID,
		storages = {
				@Storage(id = "other", file = "$PROJECT_FILE$", deprecated = true),
				@Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/findbugs-idea.xml", scheme = StorageScheme.DIRECTORY_BASED, deprecated = true)})
public final class LegacyProjectSettings implements PersistentStateComponent<PersistencePreferencesBean> {

	private static final Logger LOGGER = Logger.getInstance(LegacyProjectSettings.class);

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

	void applyTo(@NotNull final ProjectSettings settings) {
		if (state == null) {
			return;
		}
		LOGGER.info("Start convert legacy findbugs-idea settings");
		applyBasePreferencesTo(settings);
		applyBugCategoriesTo(settings);
		applyFileFiltersTo(settings);
		applyDetectorsAndPluginsTo(settings);
	}

	private void applyBasePreferencesTo(@NotNull final ProjectSettings settings) {
		final Map<String, String> basePreferences = state.getBasePreferences();
		if (basePreferences == null || basePreferences.isEmpty()) {
			return;
		}
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

	private void applyBugCategoriesTo(@NotNull final ProjectSettings settings) {
		final Map<String, String> bugCategories = state.getBugCategories();
		if (bugCategories == null || bugCategories.isEmpty()) {
			return;
		}
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

	private void applyFileFiltersTo(@NotNull final ProjectSettings settings) {
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
	}

	private void applyDetectorsAndPluginsTo(@NotNull final ProjectSettings settings) {
		final LegacyPluginLoaderImpl legacyPluginLoader = new LegacyPluginLoaderImpl();
		legacyPluginLoader.load(
				state.getPlugins(),
				state.getDisabledUserPluginIds(),
				state.getEnabledBundledPluginIds(),
				state.getDisabledBundledPluginIds()
		);

		final Map<String, Map<String, Boolean>> detectorDefaultEnabled = New.map();
		for (final DetectorFactory detector : DetectorFactoryCollection.instance().getFactories()) {
			Map<String, Boolean> byDetector = detectorDefaultEnabled.get(detector.getPlugin().getPluginId());
			if (byDetector == null) {
				byDetector = New.map();
				detectorDefaultEnabled.put(detector.getPlugin().getPluginId(), byDetector);
			}
			byDetector.put(detector.getShortName(), detector.isDefaultEnabled());
		}

		// core detectors
		apply(state.getDetectors(), settings.detectors, detectorDefaultEnabled.get(FindBugsPluginConstants.FINDBUGS_CORE_PLUGIN_ID));

		// bundled plugins and detectors
		for (final String bundledPluginId : new String[]{
				Plugins.AndroidFindbugs_0_5.id,
				Plugins.fb_contrib_6_2_1.id,
				Plugins.findsecbugs_plugin_1_4_1.id
		}) {
			final boolean enabled = state.getEnabledBundledPluginIds() != null && state.getEnabledBundledPluginIds().contains(bundledPluginId);
			final PluginSettings pluginSettings = new PluginSettings();
			pluginSettings.id = bundledPluginId;
			pluginSettings.enabled = enabled;
			pluginSettings.bundled = true;
			apply(state.getDetectors(), pluginSettings.detectors, detectorDefaultEnabled.get(bundledPluginId));
			if (enabled || !pluginSettings.detectors.isEmpty()) {
				settings.plugins.add(pluginSettings);
			}
		}

		// user plugins and detectors
		final List<String> plugins = state.getPlugins();
		if (plugins != null && !plugins.isEmpty()) {
			for (final String pluginUrl : plugins) {
				final String pluginId = legacyPluginLoader.userPluginIdByPluginUrl.get(pluginUrl);
				if (!StringUtil.isEmptyOrSpaces(pluginId)) {
					final PluginSettings pluginSettings = new PluginSettings();
					pluginSettings.id = pluginId;
					pluginSettings.enabled = state.getDisabledUserPluginIds() == null || !state.getDisabledUserPluginIds().contains(pluginId);
					pluginSettings.url = pluginUrl;
					apply(state.getDetectors(), pluginSettings.detectors, detectorDefaultEnabled.get(pluginId));
					settings.plugins.add(pluginSettings);
				}
			}
		}
	}

	private static void apply(
			@Nullable final Map<String, String> from,
			@NotNull final Map<String, Boolean> to,
			@Nullable final Map<String, Boolean> defaults
	) {
		if (from == null || defaults == null) {
			return;
		}
		for (final Map.Entry<String, String> fromEntry : from.entrySet()) {
			if (!StringUtil.isEmptyOrSpaces(fromEntry.getValue()) && !StringUtil.isEmptyOrSpaces(fromEntry.getKey())) {
				final boolean enabled = Boolean.valueOf(fromEntry.getValue());
				final Boolean defaultEnabled = defaults.get(fromEntry.getKey());
				if (defaultEnabled != null) { // otherwise detector does not belong to the plugin
					final boolean apply = defaultEnabled != enabled;
					if (apply) {
						to.put(fromEntry.getKey(), enabled);
					}
				}
			}
		}
	}

	private static class LegacyPluginLoaderImpl extends AbstractPluginLoaderLegacy {
		@NotNull
		private final Map<String, String> userPluginIdByPluginUrl;

		LegacyPluginLoaderImpl() {
			super(false);
			userPluginIdByPluginUrl = New.map();
		}

		@Override
		protected void seenUserPlugin(@NotNull final String pluginUrl, Plugin plugin) {
			userPluginIdByPluginUrl.put(pluginUrl, plugin.getPluginId());
		}
	}
}
