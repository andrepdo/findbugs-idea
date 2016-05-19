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
package org.twodividedbyzero.idea.findbugs.gui.preferences.importer;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.core.PluginSettings;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
import org.twodividedbyzero.idea.findbugs.preferences.PersistencePreferencesBean;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class SonarProfileImporter {

	private static final Logger LOGGER = Logger.getInstance(SonarProfileImporter.class.getName());
	private static final String ROOT_NAME = "profile";

	public static boolean isValid(@NotNull final Element root) {
		return ROOT_NAME.equals(root.getName());
	}

	public static boolean isValid(@NotNull final Document document) {
		return isValid(document.getRootElement());
	}

	public boolean doImport(@NotNull final Element profile, @NotNull final ProjectSettings settings) {
		final Element rules = profile.getChild("rules");
		if (rules == null) {
			handleError(ResourcesLoader.getString("sonar.import.error.noRules"));
			return false;
		}

		// disable all detectors (like legacy sonar importer logic)
		settings.detectors.clear();
		final Map<String, PluginSettings> settingsByPluginId = New.map();
		for (final PluginSettings pluginSettings : settings.plugins) {
			pluginSettings.detectors.clear();
			settingsByPluginId.put(pluginSettings.id, pluginSettings);
		}

		for (final DetectorFactory detector : DetectorFactoryCollection.instance().getFactories()) {
			Map<String, Boolean> detectors = settings.detectors;
			final PluginSettings pluginSettings = settingsByPluginId.get(detector.getPlugin().getPluginId());
			if (pluginSettings != null) {
				detectors = pluginSettings.detectors;
			}
			detectors.put(detector.getShortName(), false);
		}

		final Map<String, String> pluginIdByShortName = createIndexDetectorsPluginIdByShortName();
		final Map<String, Set<String>> shortNameByBugPatternType = createIndexShortNameByBugPatternType();

		final List ruleList = rules.getChildren("rule");
		for (final Object child : ruleList) {
			if (child instanceof Element) {
				final Element rule = (Element) child;
				final Element repositoryKey = rule.getChild("repositoryKey");
				final Element key = rule.getChild("key");
				if (repositoryKey != null && "findbugs".equals(repositoryKey.getValue()) && key != null) {
					final String bugPatternType = key.getValue();
					final Set<String> shortNames = shortNameByBugPatternType.get(bugPatternType);
					if (shortNames != null) {
						for (final String shortName : shortNames) {
							final String pluginId = pluginIdByShortName.get(shortName);
							final PluginSettings pluginSettings = settingsByPluginId.get(pluginId);
							Map<String, Boolean> detectors = settings.detectors;
							if (pluginSettings != null) {
								detectors = pluginSettings.detectors;
							}
							detectors.put(shortName, true);
						}
					} else {
						LOGGER.warn("Unknown bug pattern type: " + bugPatternType);
					}
				}
			}
		}
		return true;

	}

	protected abstract void handleError(@NotNull final String message);

	@NotNull
	private static Map<String, String> createIndexDetectorsPluginIdByShortName() {
		final Map<String, String> ret = New.map();
		for (final DetectorFactory detector : DetectorFactoryCollection.instance().getFactories()) {
			ret.put(detector.getShortName(), detector.getPlugin().getPluginId());
		}
		return ret;
	}

	@NotNull
	private static Map<String, Set<String>> createIndexShortNameByBugPatternType() {
		final Map<String, Set<String>> ret = New.map();
		for (final DetectorFactory detector : DetectorFactoryCollection.instance().getFactories()) {
			for (final BugPattern bugPattern : detector.getReportedBugPatterns()) {
				Set<String> detectorsShortName = ret.get(bugPattern.getType());
				if (detectorsShortName == null) {
					detectorsShortName = new HashSet<String>();
					ret.put(bugPattern.getType(), detectorsShortName);
				}
				detectorsShortName.add(detector.getShortName());
			}
		}
		return ret;
	}


	@Nullable
	public static PersistencePreferencesBean doImportLegacy(Project project, final Document document) {
		final Element profile = document.getRootElement();
		final Element rules = profile.getChild("rules");
		if (rules == null) {
			FindBugsPluginImpl.showToolWindowNotifier(project, "The file format is invalid. No rules element found.",
					MessageType.ERROR);
			return null;
		}

		final Map<String, Set<String>> detectorsShortNameByBugPatternType = createIndexDetectorsShortNameByBugPatternTypeLegacy();

		final PersistencePreferencesBean ret = new PersistencePreferencesBean();
		for (final Set<String> detectorsShortName : detectorsShortNameByBugPatternType.values()) {
			for (final String detectorShortName : detectorsShortName) {
				ret.getDetectors().put(detectorShortName, "false");
			}
		}

		final List ruleList = rules.getChildren("rule");
		for (final Object child : ruleList) {
			if (child instanceof Element) {
				final Element rule = (Element) child;
				final Element repositoryKey = rule.getChild("repositoryKey");
				final Element key = rule.getChild("key");
				if (repositoryKey != null && "findbugs".equals(repositoryKey.getValue()) && key != null) {
					final String bugPatternType = key.getValue();
					final Set<String> detectorsShortName = detectorsShortNameByBugPatternType.get(bugPatternType);
					if (detectorsShortName != null) {
						for (final String detectorShortName : detectorsShortName) {
							ret.getDetectors().put(detectorShortName, "true");
						}
					} else {
						LOGGER.warn("Unknown bug pattern type: " + bugPatternType);
					}
				}
			}
		}
		return ret;
	}


	private static Map<String, Set<String>> createIndexDetectorsShortNameByBugPatternTypeLegacy() {
		final Map<String, Set<String>> ret = new HashMap<String, Set<String>>();
		for (final DetectorFactory detector : FindBugsPreferences.getDetectorFactorCollection().getFactories()) {
			for (final BugPattern bugPattern : detector.getReportedBugPatterns()) {
				Set<String> detectorsShortName = ret.get(bugPattern.getType());
				if (detectorsShortName == null) {
					detectorsShortName = new HashSet<String>();
					ret.put(bugPattern.getType(), detectorsShortName);
				}
				detectorsShortName.add(detector.getShortName());
			}
		}
		return ret;
	}
}
