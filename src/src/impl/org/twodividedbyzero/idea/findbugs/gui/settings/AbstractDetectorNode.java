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
package org.twodividedbyzero.idea.findbugs.gui.settings;

import com.intellij.openapi.util.text.StringUtil;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.I18N;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.core.PluginSettings;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class AbstractDetectorNode extends DefaultMutableTreeNode {
	AbstractDetectorNode(@NotNull final String text) {
		super(text);
	}

	abstract boolean isGroup();

	@Nullable
	abstract Boolean getEnabled();

	abstract void setEnabled(@Nullable Boolean enabled);

	@NotNull
	private static DetectorNode create(@NotNull final DetectorFactory detector, @Nullable final Boolean enabled) {
		return new DetectorNode(detector, enabled == null ? detector.isDefaultEnabled() : enabled);
	}

	@NotNull
	private static AbstractDetectorNode createGroup(@NotNull final String text) {
		return new DetectorGroupNode(text);
	}

	@NotNull
	static AbstractDetectorNode notLoaded() {
		return createGroup("not loaded");
	}

	@NotNull
	static AbstractDetectorNode buildRoot(
			@NotNull final DetectorTablePane.GroupBy groupBy,
			final boolean addHidden,
			@NotNull final Map<String, Map<String, Boolean>> detectors
	) {

		final Map<String, List<DetectorNode>> byGroup = New.map();
		final Iterator<DetectorFactory> detectorFactoryIterator = DetectorFactoryCollection.instance().factoryIterator();
		fillByGroup(groupBy, addHidden, detectorFactoryIterator, byGroup, detectors);

		final Comparator<DetectorNode> nodeComparator = new Comparator<DetectorNode>() {
			@Override
			public int compare(final DetectorNode o1, final DetectorNode o2) {
				return o1.toString().compareToIgnoreCase(o2.toString());
			}
		};

		final AbstractDetectorNode root = createGroup(groupBy.displayName);
		final ArrayList<String> groupSorted = new ArrayList<String>(byGroup.keySet());
		Collections.sort(groupSorted);
		for (final String group : groupSorted) {
			final AbstractDetectorNode groupNode = createGroup(group);
			root.add(groupNode);
			final List<DetectorNode> nodes = new ArrayList<DetectorNode>(byGroup.get(group));
			Collections.sort(nodes, nodeComparator);
			for (final DetectorNode node : nodes) {
				groupNode.add(node);
			}
		}
		return root;
	}

	@NotNull
	private static Map<String, List<DetectorNode>> fillByGroup(
			@NotNull final DetectorTablePane.GroupBy groupBy,
			final boolean addHidden,
			@NotNull final Iterator<DetectorFactory> iterator,
			@NotNull final Map<String, List<DetectorNode>> byGroup,
			@NotNull final Map<String, Map<String, Boolean>> enabledMap
	) {

		while (iterator.hasNext()) {
			final DetectorFactory factory = iterator.next();
			if (!factory.isHidden() || addHidden) {

				String group;
				switch (groupBy) {
					case Provider:
						group = factory.getPlugin().getProvider();
						if (group == null) {
							group = "Unknown";
						} else if (group.endsWith(" project")) {
							group = group.substring(0, group.length() - " project".length());
						}
						addNode(factory, group, byGroup, enabledMap);
						break;
					case Speed:
						group = factory.getSpeed();
						if (group == null) {
							group = "Unknown";
						}
						addNode(factory, group, byGroup, enabledMap);
						break;
					case BugCategory:
						final Collection<BugPattern> patterns = factory.getReportedBugPatterns();
						final Set<String> categories = New.set();
						for (final BugPattern bugPattern : patterns) {
							final String category = bugPattern.getCategory();
							if (!StringUtil.isEmptyOrSpaces(category)) {
								categories.add(category);
							}
						}
						if (categories.isEmpty()) {
							group = "Unknown";
							addNode(factory, group, byGroup, enabledMap);
						} else {
							for (final String category : categories) {
								group = I18N.instance().getBugCategoryDescription(category);
								if (StringUtil.isEmptyOrSpaces(group)) {
									group = category;
								}
								addNode(factory, group, byGroup, enabledMap);
							}
						}
						break;
					default:
						throw new UnsupportedOperationException("Unsupported " + groupBy);
				}

			}
		}
		return byGroup;
	}

	private static void addNode(
			@NotNull final DetectorFactory factory,
			@NotNull final String group,
			@NotNull final Map<String, List<DetectorNode>> byGroup,
			@NotNull final Map<String, Map<String, Boolean>> enabledMap
	) {
		List<DetectorNode> detectorNodes = byGroup.get(group);
		if (detectorNodes == null) {
			detectorNodes = New.arrayList();
			byGroup.put(group, detectorNodes);
		}
		final Boolean enabled = isEnabled(enabledMap, factory);
		detectorNodes.add(create(factory, enabled));
	}

	@NotNull
	static Map<String, Map<String, Boolean>> createEnabledMap(
			@NotNull final AbstractSettings settings
	) {
		final Map<String, Map<String, Boolean>> ret = New.map();
		if (!settings.detectors.isEmpty()) {
			ret.put(FindBugsPluginConstants.FINDBUGS_CORE_PLUGIN_ID, new HashMap<String, Boolean>(settings.detectors));
		}
		for (final PluginSettings pluginSettings : settings.plugins) {
			if (!pluginSettings.detectors.isEmpty()) {
				ret.put(pluginSettings.id, new HashMap<String, Boolean>(pluginSettings.detectors));
			}
		}
		return ret;
	}

	static void fillSettings(
			@NotNull final AbstractSettings settings,
			@NotNull final Map<String, Map<String, Boolean>> detectors
	) {
		apply(detectors.get(FindBugsPluginConstants.FINDBUGS_CORE_PLUGIN_ID), settings.detectors);
		for (final PluginSettings pluginSettings : settings.plugins) {
			apply(detectors.get(pluginSettings.id), pluginSettings.detectors);
		}
	}

	static void apply(
			@Nullable final Map<String, Boolean> from,
			@NotNull final Map<String, Boolean> to
	) {
		to.clear();
		if (from != null) {
			for (final Map.Entry<String, Boolean> entry : from.entrySet()) {
				to.put(entry.getKey(), entry.getValue());
			}
		}
	}

	static void fillEnabledMap(
			@NotNull final AbstractDetectorNode node,
			@NotNull final Map<String, Map<String, Boolean>> detectors
	) {
		for (int i = 0; i < node.getChildCount(); i++) {
			fillEnabledMap((AbstractDetectorNode) node.getChildAt(i), detectors);
		}
		if (!node.isGroup()) {
			final DetectorNode detectorNode = (DetectorNode) node;
			remove(detectors, detectorNode.getDetector());
			if (detectorNode.isEnabledDefaultDifferent()) {
				add(detectors, detectorNode.getDetector(), detectorNode.getEnabled());
			}
		}
	}

	@Nullable
	private static Boolean isEnabled(
			@NotNull final Map<String, Map<String, Boolean>> detectors,
			@NotNull final DetectorFactory detector
	) {
		final Map<String, Boolean> settings = detectors.get(detector.getPlugin().getPluginId());
		if (settings != null) {
			return settings.get(detector.getShortName());
		}
		return null;
	}

	private static void remove(
			@NotNull final Map<String, Map<String, Boolean>> detectors,
			@NotNull final DetectorFactory detector
	) {
		final String pluginId = detector.getPlugin().getPluginId();
		final Map<String, Boolean> settings = detectors.get(pluginId);
		if (settings != null) {
			settings.remove(detector.getShortName());
			if (settings.isEmpty()) {
				detectors.remove(pluginId);
			}
		}
	}

	private static void add(
			@NotNull final Map<String, Map<String, Boolean>> detectors,
			@NotNull final DetectorFactory detector,
			final boolean enabled
	) {
		final String pluginId = detector.getPlugin().getPluginId();
		Map<String, Boolean> settings = detectors.get(pluginId);
		if (settings == null) {
			settings = New.map();
			detectors.put(pluginId, settings);
		}
		settings.put(detector.getShortName(), enabled);
	}
}
