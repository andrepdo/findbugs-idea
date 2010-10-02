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
 * @since 0.0.1
 */
public class ScrollToSource extends BaseToggleAction {


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
			final FindBugsPreferences preferences = getPluginInterface(project).getPreferences();
			final boolean isEnabled = panel.getBugTreePanel().isScrollToSource();
			final boolean enabled = preferences.getBooleanProperty(FindBugsPreferences.TOOLWINDOW_SCROLL_TO_SOURCE, isEnabled);
			if(enabled != isEnabled) {
				panel.getBugTreePanel().setScrollToSource(enabled);
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
			preferences.setProperty(FindBugsPreferences.TOOLWINDOW_SCROLL_TO_SOURCE, selected);
			panel.getBugTreePanel().setScrollToSource(selected);
		}
	}

}
