/*
 * Copyright 2008-2015 Andre Pfeiler
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
import edu.umd.cs.findbugs.BugCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;
import org.twodividedbyzero.idea.findbugs.messages.AnalysisStateListener;
import org.twodividedbyzero.idea.findbugs.messages.ClearListener;
import org.twodividedbyzero.idea.findbugs.messages.MessageBusManager;


/**
 * $Date$
 *
 * @version $Revision$
 */
public final class CloseToolWindow extends BaseAction implements AnalysisStateListener {

	private boolean _enabled;


	@Override
	protected void updateImpl(final @NotNull Project project) {
		MessageBusManager.subscribeAnalysisState(project, this, this);
	}


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

		MessageBusManager.publish(ClearListener.TOPIC).clear();
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


	@Override
	public void analysisAborted() {
		setEnabled(true);
	}


	@Override
	public void analysisFinished( @NotNull BugCollection bugCollection, @Nullable FindBugsProject findBugsProject ) {
		setEnabled(true);
	}


	@Override
	public void analysisStarted() {
		setEnabled(false);
	}
}