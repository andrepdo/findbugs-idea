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

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.gui.common.TreeState;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.util.Map;

final class DetectorTablePane extends JPanel {
	private DetectorTableHeaderPane headerPane;
	private DetectorModel model;
	private DetectorTable table;
	private JScrollPane scrollPane;

	DetectorTablePane() {
		super(new BorderLayout());
		model = new DetectorModel(DetectorNode.notLoaded());
		table = new DetectorTable(model);
		model.setTree(table.getTree());

		table.getTree().setShowsRootHandles(true);
		table.getTree().setRootVisible(true);
		table.getTableHeader().setVisible(false);
		UIUtil.setLineStyleAngled(table.getTree());
		TreeUtil.installActions(table.getTree());

		scrollPane = ScrollPaneFactory.createScrollPane(table);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		add(scrollPane);
	}

	void setHeaderPane(@NotNull final DetectorTableHeaderPane headerPane) {
		this.headerPane = headerPane;
		table.setHeaderPane(headerPane);
	}

	@NotNull
	DetectorTable getTable() {
		return table;
	}

	@NotNull
	private DetectorRootNode getRootNode() {
		return (DetectorRootNode) model.getRoot();
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		table.setEnabled(enabled);
		scrollPane.setEnabled(enabled);
	}

	boolean isModified(@NotNull final AbstractSettings settings) {
		final Map<String, Map<String, Boolean>> currentDetectors = getRootNode().getEnabledMap();
		final Map<String, Map<String, Boolean>> settingsDetectors = AbstractDetectorNode.createEnabledMap(settings);
		return !settingsDetectors.equals(currentDetectors);
	}

	void apply(@NotNull final AbstractSettings settings) throws ConfigurationException {
		final Map<String, Map<String, Boolean>> detectors = getRootNode().getEnabledMap();
		AbstractDetectorNode.fillSettings(settings, detectors);
	}

	void reset(@NotNull final AbstractSettings settings) {
		final Map<String, Map<String, Boolean>> detectors = AbstractDetectorNode.createEnabledMap(settings);
		final TreeState treeState = TreeState.create(table.getTree());
		model.setRoot(DetectorNode.buildRoot(headerPane.getGroupBy(), headerPane.createAcceptor(), detectors));
		treeState.restore();
	}

	void reload(final boolean expandAll) {
		final Map<String, Map<String, Boolean>> detectors = getRootNode().getEnabledMap();
		final TreeState treeState = TreeState.create(table.getTree());
		model.setRoot(DetectorNode.buildRoot(headerPane.getGroupBy(), headerPane.createAcceptor(), detectors));
		treeState.restore();
		if (expandAll) {
			TreeUtil.expandAll(table.getTree());
			final TreePath lastSelected = table.getTree().getSelectionPath();
			if (lastSelected == null || !(lastSelected.getLastPathComponent() instanceof DetectorNode)) {
				final TreePath path = TreeUtil.getFirstLeafNodePath(table.getTree());
				TreeUtil.selectPath(table.getTree(), path);
			}
		}
	}
}
