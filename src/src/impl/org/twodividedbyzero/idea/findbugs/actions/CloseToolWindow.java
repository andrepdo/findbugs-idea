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
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;


/**
 * $Date$
 *
 * @version $Revision$
 */
public class CloseToolWindow extends BaseAction {

	@Override
	public void actionPerformed(final AnActionEvent e) {
		final Project project = DataKeys.PROJECT.getData(e.getDataContext());
		if (project == null) {
			return;
		}

		final FindBugsPlugin findBugsPlugin = project.getComponent(FindBugsPlugin.class);
		if (findBugsPlugin == null) {
			throw new IllegalStateException("Couldn't get findbugs plugin");
		}

		final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(getPluginInterface(project).getInternalToolWindowId());
		toolWindow.hide(null);

	}


	@Override
	protected boolean isEnabled() {
		return true;
	}


	@Override
	protected boolean setEnabled(final boolean enabled) {
		return true;
	}
}
