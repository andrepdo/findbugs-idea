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
package org.twodividedbyzero.idea.findbugs.gui.settings;

import com.intellij.openapi.diagnostic.Logger;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.core.PluginSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class SonarProfileImporter {

	private static final Logger LOGGER = Logger.getInstance(SonarProfileImporter.class.getName());
	private static final String ROOT_NAME = "profile";

	static boolean isValid(@NotNull final Element root) {
		return ROOT_NAME.equals(root.getName());
	}

	boolean doImport(@NotNull final Element profile, @NotNull final AbstractSettings settings) {
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

	abstract void handleError(@NotNull final String message);

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
}
