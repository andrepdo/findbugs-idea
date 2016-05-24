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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.FilterComponent;
import com.intellij.ui.ToggleActionButton;
import com.intellij.util.Processor;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.util.HashSet;
import java.util.Set;

final class DetectorTableHeaderPane extends JPanel implements Disposable {

	@NotNull
	private final DetectorTablePane tablePane;

	@NotNull
	private final DetectorDetailsPane detailsPane;

	private FilterComponentImpl filterComponent;
	private FilterHidden filterHidden;
	private DetectorGroupBy groupBy = DetectorGroupBy.Provider;

	DetectorTableHeaderPane(@NotNull final DetectorTablePane tablePane, @NotNull final DetectorDetailsPane detailsPane) {
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		this.tablePane = tablePane;
		this.detailsPane = detailsPane;

		filterComponent = new FilterComponentImpl();
		final ActionToolbar actionToolbar = createActionToolbar();

		add(filterComponent);
		add(actionToolbar.getComponent());
	}

	@NotNull
	private ActionToolbar createActionToolbar() {
		final DefaultTreeExpander treeExpander = new DefaultTreeExpander(tablePane.getTable().getTree()) {
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
		actions.add(actionManager.createExpandAllAction(treeExpander, tablePane.getTable()));
		actions.add(actionManager.createCollapseAllAction(treeExpander, tablePane.getTable()));
		actions.add(new GroupByAction());

		final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, true);
		actionToolbar.setTargetComponent(this);
		return actionToolbar;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		filterComponent.setEnabled(enabled);

	}

	@NotNull
	DetectorGroupBy getGroupBy() {
		return groupBy;
	}

	@NotNull
	Processor<DetectorFactory> createAcceptor() {
		return new AcceptorImpl();
	}

	void setFilter(String filter) {
		if (filterComponent != null) {
			filterComponent.setFilter(filter);
			filterComponent.filter();
		}
	}

	@Nullable
	String getFilter() {
		if (filterComponent != null) {
			return filterComponent.getFilter();
		}
		return null;
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
			tablePane.reload(false);
		}
	}

	private class GroupByAction extends ComboBoxAction {
		GroupByAction() {
		}

		@Override
		@NotNull
		protected DefaultActionGroup createPopupActionGroup(final JComponent button) {
			final DefaultActionGroup group = new DefaultActionGroup();
			for (final DetectorGroupBy groupBy : DetectorGroupBy.values()) {
				group.add(new AnAction(groupBy.displayName) {
					@Override
					public void actionPerformed(final AnActionEvent e) {
						DetectorTableHeaderPane.this.groupBy = groupBy;
						tablePane.reload(false);
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
			tablePane.reload(true);
			detailsPane.reload();
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
				if (isAccepted(search, filter, detector.getPlugin().getShortDescription())) {
					return true;
				}
				if (isAccepted(search, filter, StringUtil.removeHtmlTags(detector.getDetailHTML()))) {
					return true;
				}
				final Set<BugPattern> patterns = detector.getReportedBugPatterns();
				for (final BugPattern pattern : patterns) {
					if (isAccepted(search, filter, pattern.getCategory())) {
						return true;
					}
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
