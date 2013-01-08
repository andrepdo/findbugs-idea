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
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NonNls;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
@SuppressWarnings({"MethodMayBeStatic"})
public abstract class BaseToggleAction extends ToggleAction {

	/** Logger for this class. */
	private static final Logger LOGGER = Logger.getInstance(BaseAction.class.getName());

	protected boolean _enabled;
	protected boolean _running;
	private final Set<String> _registeredProjects = new HashSet<String>();


	@Override
	public final void update(final AnActionEvent event) {
		try {
			final DataContext dataContext = event.getDataContext();
			final Project project = DataKeys.PROJECT.getData(dataContext);
			final Presentation presentation = event.getPresentation();

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

			// selected ?
			final Boolean selected = isSelected(event) ? Boolean.TRUE : Boolean.FALSE;
			presentation.putClientProperty(SELECTED_PROPERTY, selected);
			//setSelected(event, isSelected(event));

			// enable ?
			if (isRunning() && selected || !isRunning()) {
				_enabled = Boolean.TRUE;
			}

			//_enabled = isRunning() && selected ? ;
			presentation.setEnabled(toolWindow.isAvailable() && isEnabled());
			presentation.setVisible(true);

		} catch (Throwable e) {
			final FindBugsPluginException processed = FindBugsPluginImpl.processError("Action update failed", e);
			if (processed != null) {
				LOGGER.error("Action update failed", processed);
			}
		}
	}


	protected ToolWindow isToolWindowRegistred(final Project project) {
		return ToolWindowManager.getInstance(project).getToolWindow(getPluginInterface(project).getInternalToolWindowId());
	}


	protected void isPluginAccessible(final Project project) {
		final FindBugsPlugin findBugsPlugin = project.getComponent(FindBugsPlugin.class);
		if (findBugsPlugin == null) {
			Messages.showWarningDialog("Couldn't get findbugs plugin", "FindBugs");
			throw new IllegalStateException("Couldn't get findbugs plugin");
		}
	}


	protected boolean isProjectLoaded(final Project project, final Presentation presentation) {
		// check a project is loaded
		if (project == null) {
			presentation.setEnabled(false);
			presentation.setVisible(false);

			return true;
		}
		return false;
	}


	protected FindBugsPlugin getPluginInterface(final Project project) {
		return project.getComponent(FindBugsPlugin.class);
	}


	public Set<String> getRegisteredProjects() {
		return Collections.unmodifiableSet(_registeredProjects);
	}


	public void addRegisteredProject(@NonNls final String projectName) {
		_registeredProjects.add(projectName);
	}


	public boolean isRegistered(@NonNls final String projectName) {
		return _registeredProjects.contains(projectName);
	}


	public boolean isRegistered(final Project project) {
		return isRegistered(project.getName());
	}


	public boolean isProjectEvent(final String projectName1, final String projectName2) {
		return projectName1.equals(projectName2);
	}


	@Override
	public abstract boolean isSelected(final AnActionEvent event);


	@Override
	public abstract void setSelected(final AnActionEvent event, final boolean selected);


	protected boolean isEnabled() {
		return _enabled;
	}


	public boolean isRunning() {
		return _running;
	}
}
