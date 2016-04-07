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

import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
			final boolean addHidden,
			@NotNull final Map<String, Boolean> enabled
	) {

		final Map<String, List<DetectorNode>> byProvider = buildByProvider(addHidden, enabled);

		final Comparator<DetectorNode> nodeComparator = new Comparator<DetectorNode>() {
			@Override
			public int compare(final DetectorNode o1, final DetectorNode o2) {
				return o1.toString().compareToIgnoreCase(o2.toString());
			}
		};

		final AbstractDetectorNode root = createGroup(ResourcesLoader.getString("settings.detector"));
		final ArrayList<String> providerSorted = new ArrayList<String>(byProvider.keySet());
		Collections.sort(providerSorted);
		for (final String provider : providerSorted) {
			final AbstractDetectorNode group = createGroup(provider);
			root.add(group);
			final List<DetectorNode> nodes = new ArrayList<DetectorNode>(byProvider.get(provider));
			Collections.sort(nodes, nodeComparator);
			for (final DetectorNode node : nodes) {
				group.add(node);
			}
		}
		return root;
	}

	@NotNull
	private static Map<String, List<DetectorNode>> buildByProvider(
			final boolean addHidden,
			@NotNull final Map<String, Boolean> enabled
	) {

		final Iterator<DetectorFactory> iterator = DetectorFactoryCollection.instance().factoryIterator();
		final Map<String, List<DetectorNode>> byProvider = New.map();
		while (iterator.hasNext()) {
			final DetectorFactory factory = iterator.next();
			if (!factory.isHidden() || addHidden) {
				String provider = factory.getPlugin().getProvider();
				if (provider == null) {
					provider = "Unknown";
				} else if (provider.endsWith(" project")) {
					provider = provider.substring(0, provider.length() - " project".length());
				}
				List<DetectorNode> detectors = byProvider.get(provider);
				if (detectors == null) {
					detectors = New.arrayList();
					byProvider.put(provider, detectors);
				}
				detectors.add(create(factory, enabled.get(factory.getShortName())));
			}
		}
		return byProvider;
	}

	static void fillEnabledMap(@NotNull final AbstractDetectorNode node, @NotNull final Map<String, Boolean> enabled) {
		for (int i = 0; i < node.getChildCount(); i++) {
			fillEnabledMap((AbstractDetectorNode) node.getChildAt(i), enabled);
		}
		if (!node.isGroup()) {
			final DetectorNode detectorNode = (DetectorNode) node;
			enabled.remove(detectorNode.getDetector().getShortName());
			if (detectorNode.isEnabledDefaultDifferent()) {
				enabled.put(detectorNode.getDetector().getShortName(), detectorNode.getEnabled());
			}
		}
	}
}
