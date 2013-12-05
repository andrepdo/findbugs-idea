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
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.ProjectStats;
import edu.umd.cs.findbugs.SortedBugCollection;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.gui.common.ScrollPaneFacade;
import org.twodividedbyzero.idea.findbugs.gui.tree.GroupBy;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.GroupTreeModel;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.RootNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.view.BugTree;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

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


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.0.1
 */
@SuppressWarnings({"AnonymousInnerClass"})
public class BugTreePanel extends JPanel {

	private final transient Project _project;

	private boolean _scrollToSource;
	private final BugTree _bugTree;

	private final transient RootNode _visibleRootNode;
	private final GroupTreeModel _treeModel;
	private final FindBugsPreferences _preferences;
	private transient SortedBugCollection _bugCollection;
	private GroupBy[] _groupBy;
	private final ToolWindowPanel _parent;
	private double _splitPaneVerticalWeight = 1.0;
	private final double _splitPaneHorizontalWeight = 0.4;
	private boolean _bugPreviewEnabled;
	private final Object addNodeLock = new Object();


	public BugTreePanel(final ToolWindowPanel parent, final Project project) {
		setLayout(new BorderLayout());

		_parent = parent;
		_project = project;
		_preferences = IdeaUtilImpl.getPluginComponent(project).getPreferences();
		_groupBy = GroupBy.getSortOrderGroup(GroupBy.BugCategory); // default sort oder group

		_visibleRootNode = new RootNode(_project.getName());
		_treeModel = new GroupTreeModel(_visibleRootNode, _groupBy, _project);

		//noinspection ThisEscapedInObjectConstruction
		_bugTree = new BugTree(_treeModel, this, _project);

		final JScrollPane treeScrollPane = ScrollPaneFacade.createScrollPane();
		treeScrollPane.setViewportView(_bugTree);
		add(treeScrollPane, BorderLayout.CENTER);
	}


	void addNode(final BugInstance bugInstance) {
		synchronized(addNodeLock) {
			if (bugInstance == null) {
				return;
			}
			/*if(isHiddenBugGroup(bugInstance)) {
				return;
			}*/

			if (_treeModel.getGroupBy() != _groupBy) {
				_treeModel.setGroupBy(_groupBy);
			}

			_treeModel.addNode(bugInstance);
		}

	}


	void updateRootNode(@Nullable final ProjectStats projectStats) {
		int numClasses = 0;

		if (projectStats != null) {
			numClasses = projectStats.getNumClasses();
		}

		_visibleRootNode.setBugCount(_treeModel.getBugCount());
		_visibleRootNode.setClassesCount(numClasses);

		EventDispatchThreadHelper.invokeLater(new Runnable() {
			public void run() {
				_treeModel.nodeChanged(_visibleRootNode);
			}
		});
	}


	void clear(final boolean resetBugCount) {
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			public void run() {
				if (resetBugCount) {
					_visibleRootNode.setBugCount(0);
				}
				_treeModel.clear();
			}
		});
	}


	GroupTreeModel getTreeModel() {
		return _treeModel;
	}


	boolean isHiddenBugGroup(final BugInstance node) {
		final Map<String, String> categories = _preferences.getBugCategories();
		final String category = node.getBugPattern().getCategory();
		return categories.containsKey(category) && "false".equals(categories.get(category));
	}


	private static void scrollToPreviewSource(final BugInstanceNode bugInstanceNode, final Editor editor) {
		final int line = bugInstanceNode.getSourceLines()[0] - 1;
		if(line >= 0) {
			final LogicalPosition problemPos = new LogicalPosition(line, 0);
			editor.getCaretModel().moveToLogicalPosition(problemPos);
		} else { // anonymous classes
			final RangeHighlighter rangeHighlighter = editor.getMarkupModel().getAllHighlighters()[0];
			editor.getCaretModel().moveToOffset(rangeHighlighter.getStartOffset());
		}
		editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
	}


	public void setPreviewEnabled(final boolean enabled) {
		_bugPreviewEnabled = enabled;
		if(enabled) {
			setPreview(getBugTree().getSelectionPath());
		}
	}


	public boolean isPreviewEnabled() {
		return _bugPreviewEnabled;
	}


	public void setPreview(final TreePath treePath) {
		if (treePath != null && treePath.getLastPathComponent() instanceof BugInstanceNode) {
			final BugInstanceNode bugInstanceNode = (BugInstanceNode) getTreeNodeFromPath(treePath);
			if (bugInstanceNode == null) {
				return;
			}

			if (bugInstanceNode.getPsiFile() == null) {
				return; // no problem here
			}

			final PsiFile psiFile = bugInstanceNode.getPsiFile();
			if (psiFile != null) {
				final Document document = PsiDocumentManager.getInstance(_project).getDocument(psiFile);
				if (document != null) {
					final Editor editor = createEditor(bugInstanceNode, document);
					_parent.setPreviewEditor(editor, psiFile);
					scrollToPreviewSource(bugInstanceNode, editor);
				}
			}
		}
	}


	private Editor createEditor(final BugInstanceNode bugInstanceNode, final Document document) {
		final Editor editor = EditorFactory.getInstance().createEditor(document, _project, StdFileTypes.JAVA, false);
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
		
		final PsiFile psiFile = bugInstanceNode.getPsiFile();
		if(lineStart < 0 && lineEnd < 0) {   // find anonymous classes
			final PsiElement psiElement = IdeaUtilImpl.findAnonymousClassPsiElement(bugInstanceNode, _project);
			if (psiElement != null) {
				element = psiElement;
			}
		} else {
			if (psiFile != null) {
				element = IdeaUtilImpl.getElementAtLine(psiFile, lineStart);
			}
		}

		RangeMarker marker = null;
		if (element != null) {
			marker = document.createRangeMarker(element.getTextRange());
		} else if (lineStart >= 0 && lineEnd >= 0) {
			marker = document.createRangeMarker(document.getLineStartOffset(lineStart), document.getLineEndOffset(lineEnd));
		}

		if(marker != null) {
			editor.getMarkupModel().addRangeHighlighter(marker.getStartOffset(), marker.getEndOffset(), HighlighterLayer.FIRST - 1, new TextAttributes(null, null, JBColor.RED, EffectType.BOXED, Font.BOLD), HighlighterTargetArea.EXACT_RANGE);
		}

		return editor;
	}


	private static TreeNode getTreeNodeFromPath(final TreePath treePath) {
		return (TreeNode) treePath.getLastPathComponent();
	}


	public void setDetailHtml(final TreePath treePath) {
		final TreeNode treeNode = getTreeNodeFromPath(treePath);

		if (treeNode instanceof BugInstanceNode) {
			final BugInstanceNode bugNode = (BugInstanceNode) treeNode;
			final BugInstance bugInstance = bugNode.getBugInstance();

			if (_parent != null) {
				_parent.getBugDetailsComponents().setBugExplanation(_bugCollection, bugInstance);
			}
		}
	}


	public void setDetailText(final TreePath treePath) {
		final TreeNode treeNode = getTreeNodeFromPath(treePath);

		if (treeNode instanceof BugInstanceNode) {
			final BugInstanceNode bugNode = (BugInstanceNode) treeNode;

			if (_parent != null) {
				_parent.getBugDetailsComponents().setBugsDetails(bugNode, treePath);
			}
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


	/** Collapse the tree so that only the root node is visible. */
	public void collapseTree() {
		_bugTree.getTreeHelper().collapseTree();
	}


	/** Expand the error tree to the fullest. */
	public void expandTree() {
		_bugTree.getTreeHelper().expandTree(3);
	}


	public void setBugCollection(final BugCollection bugCollection) {
		_bugCollection = (SortedBugCollection) bugCollection;
	}


	public BugCollection getBugCollection() {
		return _bugCollection;
	}


	public Map<PsiFile, List<ExtendedProblemDescriptor>> getProblems() {
		return getTreeModel().getProblems();
	}


	public void setGroupBy(final GroupBy[] groupBy) {
		if (!Arrays.equals(getGroupBy(), groupBy)) {
			_groupBy = groupBy.clone();
			regroupTree();
		}
	}


	public GroupBy[] getGroupBy() {
		return _groupBy.clone();
	}


	private void regroupTree() {
		if (_bugCollection != null) {
			final Collection<BugInstance> instanceCollection = _bugCollection.getCollection();
			if (instanceCollection != null && !instanceCollection.isEmpty()) {
				clear(false);
				for (final BugInstance bugInstance : instanceCollection) {
					addNode(bugInstance);
				}
			}
		}
	}


	public void adaptSize(final int width, final int height) {
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


	public BugTree getBugTree() {
		return _bugTree;
	}


	public GroupTreeModel getGroupModel() {
		return (GroupTreeModel) _bugTree.getModel();
	}


}
