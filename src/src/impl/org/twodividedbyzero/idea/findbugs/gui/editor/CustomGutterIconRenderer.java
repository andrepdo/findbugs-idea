/*
 * Copyright 2009 Andre Pfeiler
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

package org.twodividedbyzero.idea.findbugs.gui.editor;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.92
 */
class CustomGutterIconRenderer extends com.intellij.openapi.editor.markup.GutterIconRenderer {

	private boolean _fullySynchronized;
	private final Integer _lineStart;
	private final Map<ExtendedProblemDescriptor, RangeMarker> _issues;
	private final EditorHandler _editorHandler;


	public CustomGutterIconRenderer(final EditorHandler editorHandler, final Integer lineStart) {
		_editorHandler = editorHandler;
		_lineStart = lineStart;

		// Use IdentityHashMap to retrieve issues by reference equality instead of content equality
		_issues = new IdentityHashMap<ExtendedProblemDescriptor, RangeMarker>();
		_fullySynchronized = true;
	}


	public Integer getLineStart() {
		return _lineStart;
	}


	public Map<ExtendedProblemDescriptor, RangeMarker> getIssues() {
		return Collections.unmodifiableMap(_issues);
	}


	public void checkFullySynchronized() {
		boolean tmp = true;
		for (final Map.Entry<ExtendedProblemDescriptor, RangeMarker> entry : _issues.entrySet()) {
			if (!_editorHandler.isSynchronized(entry.getKey(), entry.getValue())) {
				tmp = false;
				break;
			}
		}

		_fullySynchronized = tmp;
	}


	public void addIssue(@NotNull final ExtendedProblemDescriptor issue, @NotNull final RangeMarker marker) {
		_issues.put(issue, marker);
		_fullySynchronized = ((_fullySynchronized) && (_editorHandler.isSynchronized(issue, marker)));
	}


	public boolean isEmpty() {
		return _issues.isEmpty();
	}


	public void removeIssue(final ExtendedProblemDescriptor issue) {
		_issues.remove(issue);

		if (!_fullySynchronized) {
			checkFullySynchronized();
		}
	}


	@NotNull
	@Override
	public Icon getIcon() {
		final int count = _issues.size();

		// Should not have to return an empty icon, but renderer is not removed when unset from RangeHighlighter !?
		if (count == 0) {
			return new ImageIcon();
		}

		return GuiResources.FINDBUGS_ICON;
		//return .getIcon((count == 1) ? (fullySynchronized ? RevuIconProvider.IconRef.GUTTER_ISSUE : RevuIconProvider.IconRef.GUTTER_ISSUE_DESYNCHRONIZED) : (fullySynchronized ? RevuIconProvider.IconRef.GUTTER_ISSUES : RevuIconProvider.IconRef.GUTTER_ISSUES_DESYNCHRONIZED));
	}


	@Override
	public String getTooltipText() {
		final StringBuilder buffer = new StringBuilder("<html><body>");
		for (Iterator<ExtendedProblemDescriptor> it = _issues.keySet().iterator(); it.hasNext();) {
			final ProblemDescriptor issue = it.next();
			//buffer.append("<b>").append(issue.getHistory().getCreatedBy().getDisplayName()).append("</b> - <i>").append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(issue.getHistory().getCreatedOn())).append("</i><br/>").append(issue.getSummary());
			if (it.hasNext()) {
				buffer.append("---<hr/>");
			}
		}

		buffer.append("</body><html");

		return buffer.toString();
	}


	@Override
	public ActionGroup getPopupMenuActions() {
		final ActionGroup templateGroup = (ActionGroup) ActionManager.getInstance().getAction("revu.issueGutter.popup");

		final DefaultActionGroup result = new DefaultActionGroup();
		if (_issues.size() == 1) {
			for (final AnAction templateAction : templateGroup.getChildren(null)) {
				result.add(buildActionProxy(templateAction, _issues.keySet().iterator().next()));
			}
		} else {
			for (final AnAction templateAction : templateGroup.getChildren(null)) {
				final DefaultActionGroup actionGroup = new DefaultActionGroup("", true);
				actionGroup.copyFrom(templateAction);
				for (final ExtendedProblemDescriptor issue : _issues.keySet()) {
					final AnAction action = buildActionProxy(templateAction, issue);
					action.getTemplatePresentation().setText("sdasf sd ");
					action.getTemplatePresentation().setIcon(null);
					actionGroup.add(action);
				}
				result.add(actionGroup);
			}
		}

		return result;
	}


	// Build a proxy on actions to inject issue
	// Want to use DataContext, but didn't find any way to inject data into EditorComponent from this renderer
	private AnAction buildActionProxy(final AnAction templateAction, final ExtendedProblemDescriptor issue) {
		final AnAction actionProxy = new AnAction() {
			@Override
			public void actionPerformed(final AnActionEvent e) {
				final DataContext dataContextProxy = new DataContext() {
					public Object getData(@NonNls final String dataId) {
						/*if (RevuDataKeys.ISSUE.getName().equals(dataId)) {
							return issue;
						}*/
						return e.getDataContext().getData(dataId);
					}
				};
				final AnActionEvent eventProxy = new AnActionEvent(e.getInputEvent(), dataContextProxy, e.getPlace(), e.getPresentation(), e.getActionManager(), e.getModifiers());
				templateAction.actionPerformed(eventProxy);
			}
		};
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
				}
			}
		};
	}
}

