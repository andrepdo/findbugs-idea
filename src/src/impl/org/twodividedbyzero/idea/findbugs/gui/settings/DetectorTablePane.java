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

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.TreeState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.ToggleActionButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.util.Map;

final class DetectorTablePane extends JPanel implements SettingsOwner<AbstractSettings> {
	private FilterHidden filterHidden;
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

		filterHidden = new FilterHidden();
		// TODO GroupBy
		final JPanel toolbar = ToolbarDecorator.createDecorator(table)
				.addExtraActions(filterHidden)
				.setAsUsualTopToolbar().createPanel();

		scrollPane = ScrollPaneFactory.createScrollPane(table);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		add(toolbar, BorderLayout.NORTH);
		add(scrollPane);
	}

	@NotNull
	DetectorTable getTable() {
		return table;
	}

	@NotNull
	private AbstractDetectorNode getRootNode() {
		return (AbstractDetectorNode) model.getRoot();
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		filterHidden.setEnabled(enabled);
		table.setEnabled(enabled);
		scrollPane.setEnabled(enabled);
	}

	@Override
	public boolean isModified(@NotNull final AbstractSettings settings) {
		final Map<String, Map<String, Boolean>> currentDetectors = New.map();
		AbstractDetectorNode.fillEnabledMap(getRootNode(), currentDetectors);
		final Map<String, Map<String, Boolean>> settingsDetectors = AbstractDetectorNode.createEnabledMap(settings);
		return !settingsDetectors.equals(currentDetectors);
	}

	@Override
	public void apply(@NotNull final AbstractSettings settings) throws ConfigurationException {
		final Map<String, Map<String, Boolean>> detectors = New.map();
		AbstractDetectorNode.fillEnabledMap(
				getRootNode(),
				detectors
		);
		AbstractDetectorNode.fillSettings(settings, detectors);
	}

	@Override
	public void reset(@NotNull final AbstractSettings settings) {
		final Map<String, Map<String, Boolean>> detectors = AbstractDetectorNode.createEnabledMap(settings);
		model.setRoot(DetectorNode.buildRoot(!filterHidden.selected, detectors));
		TreeUtil.expandAll(table.getTree());
	}

	private class FilterHidden extends ToggleActionButton {
		private boolean selected = true;

		private FilterHidden() {
			super(ResourcesLoader.getString("detector.filter.hidden"), AllIcons.General.Filter);
		}

		@Override
		public boolean isDumbAware() {
			return true;
		}

		@Override
		public boolean isSelected(final AnActionEvent e) {
			return selected;
		}

		@Override
		public void setSelected(final AnActionEvent e, final boolean state) {
			selected = state;
			final Map<String, Map<String, Boolean>> detectors = New.map();
			AbstractDetectorNode.fillEnabledMap(getRootNode(), detectors);
			final TreeState treeState = TreeState.createOn(table.getTree(), getRootNode());
			model.setRoot(DetectorNode.buildRoot(!state, detectors));
			TreeUtil.expandAll(table.getTree()); // because TreeState does not work
			treeState.applyTo(table.getTree());
		}
	}
}
