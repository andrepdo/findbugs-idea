/*
 * Copyright 2008-2014 Andre Pfeiler
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
import com.intellij.openapi.ui.Messages;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
import org.twodividedbyzero.idea.findbugs.preferences.PersistencePreferencesBean;

import java.awt.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * $Date: 2013-01-23 15:00:00 +0100 (So, 23 Feb 2014) $
 *
 * @author Reto Merz<reto.merz@gmail.com>
 * @version $Revision:  $
 * @since 0.9.992
 */
public final class SonarProfileImporter {

	private static final Logger LOGGER = Logger.getInstance(SonarProfileImporter.class.getName());
	private static final String ROOT_NAME = "profile";


	private SonarProfileImporter() {
	}


	public static boolean isValid(final Document document) {
		return ROOT_NAME.equals(document.getRootElement().getName());
	}


	@Nullable
	public static PersistencePreferencesBean doImport(final Component owner, final Document document) {
		final Element profile = document.getRootElement();
		final Element rules = profile.getChild("rules");
		if (rules == null) {
			Messages.showErrorDialog(owner, "The file format is invalid. No rules element found.", "Invalid File");
			return null;
		}

		final Map<String, Set<String>> detectorsShortNameByBugPatternType = createIndexDetectorsShortNameByBugPatternType();

		final PersistencePreferencesBean ret = new PersistencePreferencesBean();
		for (final Set<String> detectorsShortName : detectorsShortNameByBugPatternType.values()) {
			for (final String detectorShortName : detectorsShortName) {
				ret.getDetectors().put(detectorShortName, "false");
			}
		}

		final List ruleList = rules.getChildren("rule");
		for (final Object child : ruleList) {
			if (child instanceof Element) {
				final Element rule = (Element)child;
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


	private static Map<String, Set<String>> createIndexDetectorsShortNameByBugPatternType() {
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
