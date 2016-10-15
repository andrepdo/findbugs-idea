/*
 * Copyright 2008-2016 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.gui.toolwindow.view;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.Bug;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;
import org.twodividedbyzero.idea.findbugs.core.FindBugsResult;
import org.twodividedbyzero.idea.findbugs.gui.common.ScrollPaneFacade;
import org.twodividedbyzero.idea.findbugs.gui.tree.GroupBy;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.GroupTreeModel;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.RootNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.view.BugTree;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressFBWarnings("SE_BAD_FIELD")
@SuppressWarnings({"AnonymousInnerClass"})
public class BugTreePanel extends JPanel {

	@NotNull
	private final Project _project;

	private boolean _scrollToSource;

	@NotNull
	private final BugTree _bugTree;

	private final RootNode _visibleRootNode;
	private final GroupTreeModel _treeModel;
	private FindBugsResult result;
	private GroupBy[] _groupBy;
	private final ToolWindowPanel _parent;
	private double _splitPaneVerticalWeight = 1.0;
	private final double _splitPaneHorizontalWeight = 0.4;
	private boolean _bugPreviewEnabled;

	BugTreePanel(@NotNull final ToolWindowPanel parent, @NotNull final Project project) {
		setLayout(new BorderLayout());

		_parent = parent;
		_project = project;
		_groupBy = GroupBy.getSortOrderGroup(GroupBy.BugCategory); // default sort oder group

		_visibleRootNode = new RootNode(_project.getName());
		_treeModel = new GroupTreeModel(_visibleRootNode, _groupBy, _project);

		//noinspection ThisEscapedInObjectConstruction
		_bugTree = new BugTree(_treeModel, this, _project);

		final JScrollPane treeScrollPane = ScrollPaneFacade.createScrollPane();
		treeScrollPane.setViewportView(_bugTree);
		add(treeScrollPane, BorderLayout.CENTER);
	}

	void addNode(@NotNull final Bug bug) {
		/*if(isHiddenBugGroup(bugInstance)) {
			return;
		}*/

		if (_treeModel.getGroupBy() != _groupBy) {
			_treeModel.setGroupBy(_groupBy);
		}

		_treeModel.addNode(bug);
	}

	void updateRootNode(@Nullable final Integer classCount) {
		int numClasses = 0;
		if (classCount != null) {
			numClasses = classCount;
		}
		_visibleRootNode.setBugCount(classCount == null ? -1 : _treeModel.getBugCount());
		_visibleRootNode.setClassesCount(numClasses);
		_treeModel.nodeChanged(_visibleRootNode);
	}

	void clear() {
		result = null;
		_treeModel.clear();
	}

	private GroupTreeModel getTreeModel() {
		return _treeModel;
	}

	private static void scrollToPreviewSource(final BugInstanceNode bugInstanceNode, final Editor editor) {
		final int line = bugInstanceNode.getSourceLines()[0] - 1;
		if (line >= 0) {
			final LogicalPosition problemPos = new LogicalPosition(line, 0);
			editor.getCaretModel().moveToLogicalPosition(problemPos);
		} else { // anonymous classes
			final RangeHighlighter rangeHighlighter = editor.getMarkupModel().getAllHighlighters()[0];
			editor.getCaretModel().moveToOffset(rangeHighlighter.getStartOffset());
		}
		editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
	}

	void setPreviewEnabled(final boolean enabled) {
		_bugPreviewEnabled = enabled;
		if (enabled) {
			setPreview(getBugTree().getSelectionPath());
		}
	}

	public boolean isPreviewEnabled() {
		return _bugPreviewEnabled;
	}

	public void setPreview(@Nullable final TreePath treePath) {
		boolean clear = true;
		if (treePath != null && treePath.getLastPathComponent() instanceof BugInstanceNode) {
			final BugInstanceNode bugInstanceNode = (BugInstanceNode) getTreeNodeFromPath(treePath);
			if (bugInstanceNode != null) {
				final PsiFile psiFile = bugInstanceNode.getPsiFile();
				if (psiFile != null) {
					final Document document = PsiDocumentManager.getInstance(_project).getDocument(psiFile);
					if (document != null) {
						final Editor editor = createEditor(bugInstanceNode, psiFile, document);
						_parent.setPreviewEditor(editor, psiFile);
						scrollToPreviewSource(bugInstanceNode, editor);
						clear = false;
					}
				}
			}
		}
		if (clear) {
			_parent.setPreviewEditor(null, null);
		}
	}

	@NotNull
	private Editor createEditor(@NotNull final BugInstanceNode bugInstanceNode, @NotNull final PsiFile psiFile, @NotNull final Document document) {
		final Editor editor = EditorFactory.getInstance().createEditor(document, _project, psiFile.getFileType(), false);
		final EditorColorsScheme scheme = editor.getColorsScheme();
		scheme.setEditorFontSize(scheme.getEditorFontSize() - 1);

		final EditorSettings editorSettings = editor.getSettings();
		editorSettings.setLineMarkerAreaShown(true);
		editorSettings.setLineNumbersShown(true);
		editorSettings.setFoldingOutlineShown(true);
		editorSettings.setAnimatedScrolling(true);
		editorSettings.setWheelFontChangeEnabled(true);
		editorSettings.setVariableInplaceRenameEnabled(true);

		final int lineStart = bugInstanceNode.getSourceLines()[0] - 1;
		final int lineEnd = bugInstanceNode.getSourceLines()[1];
		PsiElement element = null;

		if (lineStart < 0 && lineEnd < 0) {   // find anonymous classes
			final PsiElement psiElement = IdeaUtilImpl.findAnonymousClassPsiElement(bugInstanceNode.getPsiFile(), bugInstanceNode.getBugInstance(), _project);
			if (psiElement != null) {
				element = psiElement;
			}
		} else {
			element = IdeaUtilImpl.getElementAtLine(psiFile, lineStart);
		}

		RangeMarker marker = null;
		if (element != null) {
			marker = document.createRangeMarker(element.getTextRange());
		} else if (lineStart >= 0 && lineEnd >= 0) {
			marker = document.createRangeMarker(document.getLineStartOffset(lineStart), document.getLineEndOffset(lineEnd));
		}

		if (marker != null) {
			editor.getMarkupModel().addRangeHighlighter(marker.getStartOffset(), marker.getEndOffset(), HighlighterLayer.FIRST - 1, new TextAttributes(null, null, JBColor.RED, EffectType.BOXED, Font.BOLD), HighlighterTargetArea.EXACT_RANGE);
		}

		return editor;
	}

	private static TreeNode getTreeNodeFromPath(final TreePath treePath) {
		return (TreeNode) treePath.getLastPathComponent();
	}

	public void setDetails(@Nullable final TreePath treePath) {
		boolean clear = true;
		if (treePath != null) {
			final TreeNode treeNode = getTreeNodeFromPath(treePath);
			if (treeNode instanceof BugInstanceNode) {
				final BugInstanceNode bugNode = (BugInstanceNode) treeNode;
				final BugInstance bugInstance = bugNode.getBugInstance();
				_parent.getBugDetailsComponents().setBugExplanation(bugNode.getBug().getBugCollection(), bugInstance);
				_parent.getBugDetailsComponents().setBugsDetails(bugInstance);
				clear = false;
			}
		}
		if (clear) {
			_parent.getBugDetailsComponents().clear();
		}
	}

	/**
	 * Should we scroll to the selected error in the editor automatically?
	 *
	 * @param scrollToSource true if the error should be scrolled to automatically.
	 */
	public void setScrollToSource(final boolean scrollToSource) {
		_scrollToSource = scrollToSource;
	}

	/**
	 * Should we scroll to the selected error in the editor automatically?
	 *
	 * @return true if the error should be scrolled to automatically.
	 */
	public boolean isScrollToSource() {
		return _scrollToSource;
	}

	/**
	 * Collapse the tree so that only the root node is visible.
	 */
	public void collapseTree() {
		_bugTree.getTreeHelper().collapseTree();
	}

	/**
	 * Expand the error tree to the fullest.
	 */
	public void expandTree() {
		_bugTree.getTreeHelper().expandTree(3);
	}

	public void setResult(final FindBugsResult result) {
		this.result = result;
	}

	public FindBugsResult getResult() {
		return result;
	}

	public Map<PsiFile, List<ExtendedProblemDescriptor>> getProblems() {
		return getTreeModel().getProblems();
	}

	public void setGroupBy(final GroupBy[] groupBy) {
		EventDispatchThreadHelper.checkEDT();
		if (!Arrays.equals(getGroupBy(), groupBy)) {
			_groupBy = groupBy.clone();
			regroupTree();
		}
	}

	public GroupBy[] getGroupBy() {
		return _groupBy.clone();
	}

	private void regroupTree() {
		EventDispatchThreadHelper.checkEDT();
		if (result != null) {
			boolean cleared = false;
			for (final Map.Entry<edu.umd.cs.findbugs.Project, SortedBugCollection> entry : result.getResults().entrySet()) {
				Module module = null;
				if (entry.getKey() instanceof FindBugsProject) {
					module = ((FindBugsProject) entry.getKey()).getModule();
				}
				final Collection<BugInstance> instanceCollection = entry.getValue().getCollection();
				if (instanceCollection != null && !instanceCollection.isEmpty()) {
					if (!cleared) {
						cleared = true;
						_treeModel.clear();
					}
					for (final BugInstance bugInstance : instanceCollection) {
						if (bugInstance != null) {
							addNode(new Bug(
									module,
									entry.getValue(),
									bugInstance
							));
						}
					}
				}
			}
		} else {
			// may be a analysis is running, we need to regroup existing nodes
			final Collection<Bug> existing = _treeModel.getBugs();
			_treeModel.clear();
			for (final Bug bug : existing) {
				addNode(bug);
			}
		}
	}

	void adaptSize(final int width, final int height) {
		//final int newWidth = (int) (width * _splitPaneHorizontalWeight);
		setPreferredSize(new Dimension(width, height));
		setSize(new Dimension(width, height));
		validate();
	}

	public double getSplitPaneVerticalWeight() {
		return _splitPaneVerticalWeight;
	}

	public void setSplitPaneVerticalWeight(final double splitPaneVerticalWeight) {
		_splitPaneVerticalWeight = splitPaneVerticalWeight;
	}

	@NotNull
	public BugTree getBugTree() {
		return _bugTree;
	}

	public GroupTreeModel getGroupModel() {
		return (GroupTreeModel) _bugTree.getModel();
	}
}
