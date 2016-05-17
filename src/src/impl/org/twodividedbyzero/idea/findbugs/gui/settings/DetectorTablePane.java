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
import com.intellij.ide.ui.search.SearchableOptionsRegistrar;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.FilterComponent;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.ToggleActionButton;
import com.intellij.util.Processor;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.gui.common.TreeState;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class DetectorTablePane extends JPanel implements SettingsOwner<AbstractSettings>, Disposable {

	private FilterComponentImpl filterComponent;
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

		filterComponent = new FilterComponentImpl();
		final ActionToolbar actionToolbar = createActionToolbar();

		final JPanel topPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		topPane.add(filterComponent);
		topPane.add(actionToolbar.getComponent());
		add(topPane, BorderLayout.NORTH);

		add(topPane, BorderLayout.NORTH);
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
	private DetectorRootNode getRootNode() {
		return (DetectorRootNode) model.getRoot();
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
		final Map<String, Map<String, Boolean>> currentDetectors = getRootNode().getEnabledMap();
		final Map<String, Map<String, Boolean>> settingsDetectors = AbstractDetectorNode.createEnabledMap(settings);
		return !settingsDetectors.equals(currentDetectors);
	}

	@Override
	public void apply(@NotNull final AbstractSettings settings) throws ConfigurationException {
		final Map<String, Map<String, Boolean>> detectors = getRootNode().getEnabledMap();
		AbstractDetectorNode.fillSettings(settings, detectors);
	}

	@Override
	public void reset(@NotNull final AbstractSettings settings) {
		final Map<String, Map<String, Boolean>> detectors = AbstractDetectorNode.createEnabledMap(settings);
		final TreeState treeState = TreeState.create(table.getTree());
		model.setRoot(DetectorNode.buildRoot(groupBy, new AcceptorImpl(), detectors));
		treeState.restore();
	}

	void reload() {
		final Map<String, Map<String, Boolean>> detectors = getRootNode().getEnabledMap();
		final TreeState treeState = TreeState.create(table.getTree());
		model.setRoot(DetectorNode.buildRoot(groupBy, new AcceptorImpl(), detectors));
		treeState.restore();
	}

	void setFilter(String filter) {
		if (filterComponent != null) {
			filterComponent.setFilter(filter);
			filterComponent.filter();
		}
	}

	@Override
	public void dispose() {
		if (filterComponent != null) {
			filterComponent.dispose();
			filterComponent = null;
		}
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

	private class FilterComponentImpl extends FilterComponent {
		private FilterComponentImpl() {
			super("FINDBUGS_SETTINGS_SEARCH_HISTORY", 10);
		}

		@Override
		public void filter() {
			reload();
		}
	}

	private class AcceptorImpl implements Processor<DetectorFactory> {

		@Nullable
		private final String filter;

		@Nullable
		private Set<String> search;

		@Nullable
		private SearchableOptionsRegistrar searchableOptionsRegistrar;

		AcceptorImpl() {
			this.filter = filterComponent.getFilter();
			if (!StringUtil.isEmptyOrSpaces(filter)) {
				searchableOptionsRegistrar = SearchableOptionsRegistrar.getInstance();
				search = searchableOptionsRegistrar.getProcessedWords(filter);
			}
		}

		@Override
		public boolean process(final DetectorFactory detector) {
			if (!detector.isHidden() || !filterHidden.selected) {
				if (search == null) {
					return true;
				}
				if (isAccepted(search, filter, detector.getShortName())) {
					return true;
				}
				if (isAccepted(search, filter, detector.getFullName())) {
					return true;
				}
				final Set<BugPattern> patterns = detector.getReportedBugPatterns();
				for (final BugPattern pattern : patterns) {
					if (isAccepted(search, filter, pattern.getType())) {
						return true;
					}
					if (isAccepted(search, filter, pattern.getShortDescription())) {
						return true;
					}
					if (isAccepted(search, filter, pattern.getLongDescription())) {
						return true;
					}
				}
			}
			return false;
		}

		private boolean isAccepted(@NotNull final Set<String> search, @NotNull final String filter, @Nullable final String description) {
			if (null == description) return false;
			if (StringUtil.containsIgnoreCase(description, filter)) return true;
			final HashSet<String> descriptionSet = new HashSet<String>(search);
			descriptionSet.removeAll(searchableOptionsRegistrar.getProcessedWords(description));
			return descriptionSet.isEmpty();
		}
	}
}
