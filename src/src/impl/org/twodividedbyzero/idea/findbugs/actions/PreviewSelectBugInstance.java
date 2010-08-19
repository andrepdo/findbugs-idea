/*
 * Copyright 2010 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.twodividedbyzero.idea.findbugs.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.ToolWindowPanel;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.96
 */
public class PreviewSelectBugInstance extends BaseToggleAction {


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

		// toggle value
		final Content content = toolWindow.getContentManager().getContent(0);
		if (content != null) {
			final ToolWindowPanel panel = (ToolWindowPanel) content.getComponent();
			//return panel.isPreviewEnabled();
			final FindBugsPreferences preferences = getPluginInterface(project).getPreferences();
			final boolean enabled = preferences.getBooleanProperty(FindBugsPreferences.TOOLWINDOW_EDITOR_PREVIEW, panel.isPreviewEnabled());
			if(enabled != panel.isPreviewEnabled()) {
				panel.setPreviewEnabled(enabled);
			}
			return enabled;
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
			preferences.setProperty(FindBugsPreferences.TOOLWINDOW_EDITOR_PREVIEW, selected);
			panel.setPreviewEnabled(selected);
		}
	}

}