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

package org.twodividedbyzero.idea.findbugs.gui.editor;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.util.BugInstanceUtil;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.ToolWindowPanel;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * $Date: 2010-10-10 16:30:08 +0200 (Sun, 10 Oct 2010) $
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision: 107 $
 * @since 0.9.92
 */
class CustomGutterIconRenderer extends com.intellij.openapi.editor.markup.GutterIconRenderer {

	private boolean _fullySynchronized;
	private final Integer _lineStart;
	private final Map<ExtendedProblemDescriptor, RangeMarker> _problems;
	private final EditorHandler _editorHandler;


	CustomGutterIconRenderer(final EditorHandler editorHandler, final Integer lineStart) {
		_editorHandler = editorHandler;
		_lineStart = lineStart;

		// Use IdentityHashMap to retrieve issues by reference equality instead of content equality
		_problems = new IdentityHashMap<ExtendedProblemDescriptor, RangeMarker>();
		_fullySynchronized = true;
	}


	public Integer getLineStart() {
		return _lineStart;
	}


	public Map<ExtendedProblemDescriptor, RangeMarker> getProblems() {
		return Collections.unmodifiableMap(_problems);
	}


	public void checkFullySynchronized() {
		boolean tmp = true;
		for (final Map.Entry<ExtendedProblemDescriptor, RangeMarker> entry : _problems.entrySet()) {
			if (!_editorHandler.isSynchronized(entry.getKey(), entry.getValue())) {
				tmp = false;
				break;
			}
		}

		_fullySynchronized = tmp;
	}


	public void addProblem(@NotNull final ExtendedProblemDescriptor problem, @NotNull final RangeMarker marker) {
		_problems.put(problem, marker);
		_fullySynchronized = _fullySynchronized && _editorHandler.isSynchronized(problem, marker);
	}


	public boolean isEmpty() {
		return _problems.isEmpty();
	}


	public void removeProblem(final ExtendedProblemDescriptor problemDescriptor) {
		_problems.remove(problemDescriptor);

		if (!_fullySynchronized) {
			checkFullySynchronized();
		}
	}


	@NotNull
	@Override
	public Icon getIcon() {
		final int count = _problems.size();

		// Should not have to return an empty icon, but renderer is not removed when unset from RangeHighlighter !?
		if (count == 0) {
			return new ImageIcon();
		}

		return GuiResources.FINDBUGS_ICON;
	}


	@Override
	public String getTooltipText() {
		final StringBuilder buffer = new StringBuilder("<html><body>");

		for (final Entry<ExtendedProblemDescriptor, RangeMarker> entry : _problems.entrySet()) {
			buffer.append(BugInstanceUtil.getDetailHtml(entry.getKey().getBugInstance())).append("<hr/>");
		}

		buffer.append("</body><html>");

		return buffer.toString();
	}


	@Override
	public ActionGroup getPopupMenuActions() {
		final ActionGroup templateGroup = (ActionGroup) ActionManager.getInstance().getAction(FindBugsPluginConstants.ACTION_GROUP_GUTTER);

		final DefaultActionGroup result = new DefaultActionGroup();
		if (_problems.size() == 1) {
			for (final AnAction templateAction : templateGroup.getChildren(null)) {
				result.add(buildActionProxy(templateAction, _problems.keySet().iterator().next()));
			}
		} else {
			for (final AnAction templateAction : templateGroup.getChildren(null)) {
				final DefaultActionGroup actionGroup = new DefaultActionGroup("", true);
				actionGroup.copyFrom(templateAction);

				for (final Entry<ExtendedProblemDescriptor, RangeMarker> entry : _problems.entrySet()) {
					final AnAction action = buildActionProxy(templateAction, entry.getKey());
					action.getTemplatePresentation().setText("sdasf sd ");
					action.getTemplatePresentation().setIcon(null);
					actionGroup.add(action);
				}
				result.add(actionGroup);
			}
		}

		return result;
	}


	// Build a proxy on actions
	// Want to use DataContext, but didn't find any way to inject data into EditorComponent from this renderer
	private AnAction buildActionProxy(final AnAction templateAction, final ExtendedProblemDescriptor problem) {
		final AnAction actionProxy = new ActionProxy(templateAction);
		actionProxy.copyFrom(templateAction);

		return actionProxy;
	}


	@Override
	public boolean isNavigateAction() {
		return true;
	}


	@Override
	public AnAction getClickAction() {
		return new AnAction() {
			@Override
			public void actionPerformed(final AnActionEvent e) {
				final Editor editor = e.getData(DataKeys.EDITOR);
				if (editor != null) {
					editor.getCaretModel().moveToOffset(editor.getDocument().getLineStartOffset(_lineStart));

					final Project project = DataKeys.PROJECT.getData(e.getDataContext());
					if (project == null) {
						return;
					}
					final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(IdeaUtilImpl.getPluginComponent(project).getInternalToolWindowId());
					final Content content = toolWindow.getContentManager().getContent(0);

					if (content != null) {
						final ToolWindowPanel panel = (ToolWindowPanel) content.getComponent();
						if (_problems.size() == 1) {
							panel.getBugTreePanel().getBugTree().gotoNode(_problems.entrySet().iterator().next().getKey().getBugInstance());
						} else {
							getPopupMenuActions().actionPerformed(e);
							// todo: popup list of problems
						}
					}
				}
			}
		};
	}


	private static class MyDataContext implements DataContext {

		private final AnActionEvent _e;


		private MyDataContext(final AnActionEvent e) {
			_e = e;
		}


		public Object getData(@NonNls final String dataId) {
			return _e.getDataContext().getData(dataId);
		}
	}

	private static class ActionProxy extends AnAction {

		private final AnAction _templateAction;


		private ActionProxy(final AnAction templateAction) {
			_templateAction = templateAction;
		}


		@Override
		public void actionPerformed(final AnActionEvent e) {
			final DataContext dataContextProxy = new MyDataContext(e);
			final AnActionEvent eventProxy = new AnActionEvent(e.getInputEvent(), dataContextProxy, e.getPlace(), e.getPresentation(), e.getActionManager(), e.getModifiers());
			_templateAction.actionPerformed(eventProxy);
		}
	}
}

