/*
 * Copyright 2010 Andre Pfeiler
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
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.ToolWindowPanel;
import org.twodividedbyzero.idea.findbugs.gui.tree.view.BugTree;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.82-dev
 */
public class TreeNextOccurence extends BaseAction {

	private static final Logger LOGGER = Logger.getInstance(TreeNextOccurence.class.getName());

	private boolean _enabled;
	private AnActionEvent _actionEvent;
	private DataContext _dataContext;


	@Override
	public void actionPerformed(final AnActionEvent e) {
		_actionEvent = e;
		_dataContext = e.getDataContext();

		final Project project = DataKeys.PROJECT.getData(_actionEvent.getDataContext());
		if (project == null) {
			return;
		}

		final FindBugsPlugin findBugsPlugin = project.getComponent(FindBugsPlugin.class);
		if (findBugsPlugin == null) {
			throw new IllegalStateException("Couldn't get findbugs plugin");
		}

		final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(getPluginInterface(project).getInternalToolWindowId());
		final Content content = toolWindow.getContentManager().getContent(0);

		if (content != null) {
			final ToolWindowPanel panel = (ToolWindowPanel) content.getComponent();
			panel.getBugTreePanel().getBugTree().goNextOccurence();
		}

	}


	@Override
	public void update(final AnActionEvent event) {
		try {
			_actionEvent = event;
			_dataContext = event.getDataContext();

			final Project project = DataKeys.PROJECT.getData(_dataContext);
			final Presentation presentation = event.getPresentation();

			// check a project is loaded
			if (isProjectLoaded(project, presentation)) {
				return;
			}

			isPluginAccessible(project);

			// check if tool window is registered
			final ToolWindow toolWindow = isToolWindowRegistred(project);
			if (toolWindow == null) {
				presentation.setEnabled(false);
				presentation.setVisible(false);

				return;
			}

			// enable ?
			final Content content = toolWindow.getContentManager().getContent(0);

			if (content != null) {
				final ToolWindowPanel panel = (ToolWindowPanel) content.getComponent();
				final BugTree tree = panel.getBugTreePanel().getBugTree();
				_enabled = tree.hasNextOccurence();
				presentation.setEnabled(toolWindow.isAvailable() && isEnabled());
				presentation.setVisible(true);
			}
		} catch (Throwable e) {
			final FindBugsPluginException processed = FindBugsPluginImpl.processError("Action update failed", e);
			if (processed != null) {
				LOGGER.error("Action update failed", processed);
			}
		}
	}


	@Override
	protected boolean isEnabled() {
		return _enabled;
	}


	@Override
	protected boolean setEnabled(final boolean enabled) {
		final boolean was = _enabled;
		if (_enabled != enabled) {
			_enabled = enabled;
		}
		return was;
	}
}
