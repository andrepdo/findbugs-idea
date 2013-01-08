/*
 * Copyright 2008-2013 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.event.EventListener;
import org.twodividedbyzero.idea.findbugs.common.event.EventManagerImpl;
import org.twodividedbyzero.idea.findbugs.common.event.filters.BugReporterEventFilter;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.ToolWindowPanel;
import org.twodividedbyzero.idea.findbugs.gui.tree.GroupBy;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import java.util.Arrays;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.9
 */
public class GroupByRank extends BaseToggleAction implements EventListener<BugReporterEvent> {


	@Override
	public boolean isSelected(final AnActionEvent event) {
		final Project project = DataKeys.PROJECT.getData(event.getDataContext());
		if (project == null) {
			return false;
		}

		final FindBugsPlugin findBugsPlugin = project.getComponent(FindBugsPlugin.class);
		if (findBugsPlugin == null) {
			throw new IllegalStateException("Couldn't get findbugs plugin");
		}

		final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(getPluginInterface(project).getInternalToolWindowId());
		registerEventListener(project);

		// toggle value
		final Content content = toolWindow.getContentManager().getContent(0);
		if (content != null) {
			final FindBugsPreferences preferences = getPluginInterface(project).getPreferences();
			final String groupByProperty = preferences.getProperty(FindBugsPreferences.TOOLWINDOW_GROUP_BY, GroupBy.BugCategory.name());

			final boolean equals = GroupBy.BugRank.name().equals(groupByProperty);
			final ToolWindowPanel panel = (ToolWindowPanel) content.getComponent();
			final GroupBy[] sortOrderGroup = GroupBy.getSortOrderGroup(GroupBy.BugRank);
			if(equals && !Arrays.equals(panel.getBugTreePanel().getGroupBy(), sortOrderGroup)) {
				panel.getBugTreePanel().setGroupBy(sortOrderGroup);
			}
			return equals;
		}

		return false;
	}


	@Override
	public void setSelected(final AnActionEvent event, final boolean selected) {
		final Project project = DataKeys.PROJECT.getData(event.getDataContext());
		if (project == null) {
			return;
		}

		final FindBugsPlugin findBugsPlugin = project.getComponent(FindBugsPlugin.class);
		if (findBugsPlugin == null) {
			throw new IllegalStateException("Couldn't get findbugs plugin");
		}

		final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(getPluginInterface(project).getInternalToolWindowId());

		// toggle value
		final Content content = toolWindow.getContentManager().getContent(0);
		if (content != null) {
			final ToolWindowPanel panel = (ToolWindowPanel) content.getComponent();
			final FindBugsPreferences preferences = getPluginInterface(project).getPreferences();
			if (selected) {
				preferences.setProperty(FindBugsPreferences.TOOLWINDOW_GROUP_BY, GroupBy.BugRank.name());
				panel.getBugTreePanel().setGroupBy(GroupBy.getSortOrderGroup(GroupBy.BugRank));
			}
		}
	}


	private void registerEventListener(final Project project) {
		final String projectName = project.getName();
		if (!isRegistered(projectName)) {
			EventManagerImpl.getInstance().addEventListener(new BugReporterEventFilter(projectName), this);
			addRegisteredProject(projectName);
		}
	}


	protected boolean setEnabled(final boolean enabled) {
		final boolean was = _enabled;
		if (_enabled != enabled) {
			_enabled = enabled;
		}
		return was;
	}


	protected boolean setRunning(final boolean running) {
		final boolean was = _running;
		if (_running != running) {
			_running = running;
		}
		return was;
	}


	public void onEvent(@NotNull final BugReporterEvent event) {
		switch (event.getOperation()) {
			case ANALYSIS_STARTED:
				setEnabled(false);
				setRunning(true);
				break;
			case ANALYSIS_ABORTED:
			case ANALYSIS_FINISHED:
				setEnabled(true);
				setRunning(false);
				break;
			case NEW_BUG_INSTANCE:
				break;
		}
	}

}