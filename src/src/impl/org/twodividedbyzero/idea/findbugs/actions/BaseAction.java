/**
 * Copyright 2008 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.twodividedbyzero.idea.findbugs.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NonNls;
import org.twodividedbyzero.idea.findbugs.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;

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
public abstract class BaseAction extends AnAction {

	/** Logger for this class. */
	private static final Logger LOGGER = Logger.getInstance(BaseAction.class.getName());

	private Set<String> _registeredProjects = new HashSet<String>();


	/** {@inheritDoc} */
	@Override
	public void update(final AnActionEvent event) {
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

			// enable ?
			presentation.setEnabled(toolWindow.isAvailable() && isEnabled());
			presentation.setVisible(true);

		} catch (Throwable e) {
			final FindBugsPluginException processed = FindBugsPluginImpl.processError("Action update failed", e);// NON-NLS
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
			Messages.showWarningDialog("Couldn't get findbugs plugin", "FindBugs");  // NON-NLS
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
		return IdeaUtilImpl.getPluginComponent(project);
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


	protected abstract boolean isEnabled();


	protected abstract boolean setEnabled(final boolean enabled);


}
