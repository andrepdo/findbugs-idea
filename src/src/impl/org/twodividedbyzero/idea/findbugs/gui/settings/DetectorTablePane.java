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
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.DefaultTreeExpander;
import com.intellij.ide.util.treeView.TreeState;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.ToggleActionButton;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JComponent;
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
	private GroupBy groupBy = GroupBy.Provider;

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

		add(createActionToolbar().getComponent(), BorderLayout.NORTH);
		add(scrollPane);
	}

	@NotNull
	private ActionToolbar createActionToolbar() {
		final DefaultTreeExpander treeExpander = new DefaultTreeExpander(table.getTree()) {
			@Override
			public boolean canCollapse() {
				return true;
			}

			@Override
			public boolean canExpand() {
				return true;
			}
		};

		final DefaultActionGroup actions = new DefaultActionGroup();

		final CommonActionsManager actionManager = CommonActionsManager.getInstance();
		filterHidden = new FilterHidden();
		actions.add(filterHidden);
		actions.add(actionManager.createExpandAllAction(treeExpander, table));
		actions.add(actionManager.createCollapseAllAction(treeExpander, table));
		actions.add(new GroupByAction());

		final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, true);
		actionToolbar.setTargetComponent(this);
		return actionToolbar;
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
		model.setRoot(DetectorNode.buildRoot(groupBy, !filterHidden.selected, detectors));
		TreeUtil.expandAll(table.getTree());
	}

	void reload() {
		final Map<String, Map<String, Boolean>> detectors = New.map();
		AbstractDetectorNode.fillEnabledMap(getRootNode(), detectors);
		final TreeState treeState = TreeState.createOn(table.getTree(), getRootNode());
		model.setRoot(DetectorNode.buildRoot(groupBy, !filterHidden.selected, detectors));
		TreeUtil.expandAll(table.getTree()); // TODO ; TreeState does not work
		treeState.applyTo(table.getTree());
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
			reload();
		}
	}

	enum GroupBy {
		Provider("detector.groupBy.provider"),
		Speed("detector.groupBy.speed"),
		BugCategory("detector.groupBy.bugCategory");

		@NotNull
		final String displayName;

		GroupBy(@NotNull @PropertyKey(resourceBundle = ResourcesLoader.BUNDLE) final String propertyKey) {
			displayName = ResourcesLoader.getString(propertyKey);
		}
	}

	private class GroupByAction extends ComboBoxAction {
		GroupByAction() {
		}

		@Override
		@NotNull
		protected DefaultActionGroup createPopupActionGroup(final JComponent button) {
			final DefaultActionGroup group = new DefaultActionGroup();
			for (final GroupBy groupBy : GroupBy.values()) {
				group.add(new AnAction(groupBy.displayName) {
					@Override
					public void actionPerformed(final AnActionEvent e) {
						DetectorTablePane.this.groupBy = groupBy;
						reload();
					}
				});
			}
			return group;
		}

		@Override
		public void update(final AnActionEvent e) {
			super.update(e);
			e.getPresentation().setText(groupBy.displayName);
		}
	}
}
