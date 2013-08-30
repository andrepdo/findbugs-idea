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
package org.twodividedbyzero.idea.findbugs.gui.tree;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import edu.umd.cs.findbugs.BugInstance;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.AbstractTreeNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.GroupTreeModel;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.VisitableTreeNode;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.8-dev
 */
public class BugTreeHelper {

	private static final Logger LOGGER = Logger.getInstance(BugTreeHelper.class.getName());

	private final JTree _tree;
	private final Project _project;


	private BugTreeHelper(final JTree tree, final Project project) {
		_tree = tree;
		_project = project;
	}


	public static BugTreeHelper create(final JTree tree, final Project project) {
		return new BugTreeHelper(tree, project);
	}


	@Nullable
	public PsiElement getSelectedElement() {
		final TreePath treepath = _tree.getSelectionPath();
		if (treepath == null) {
			return null;
		}

		@SuppressWarnings({"unchecked"})
		final AbstractTreeNode<VisitableTreeNode> treeNode = (AbstractTreeNode<VisitableTreeNode>) treepath.getLastPathComponent();
		PsiElement psiElement = null;

		if (treeNode == null) {
			return null;
		}
		if (treeNode instanceof BugInstanceNode) {
			final BugInstanceNode node = (BugInstanceNode) treeNode;
			if (node.getPsiFile() != null) {
				return node.getPsiFile();
			} else {
				final PsiClass psiClass = IdeaUtilImpl.findJavaPsiClass(_project, node.getSourcePath());
				LOGGER.debug("BugTreeHelper#getSeletedElement(" + _project + ", " + node.getSourcePath() + ")");
				if (psiClass != null) {
					LOGGER.debug("Found: psiClass (" + psiClass.getName() + ")");
					psiElement = IdeaUtilImpl.getPsiFile(psiClass);
					LOGGER.debug("BugTreeHelper - IdeaUtilImpl.getPsiFile(psiClass) - found - psiElement: [" + psiElement.getText() + "]");
					node.setPsiFile((PsiFile) psiElement);
				}
			}
		}

		if (psiElement != null) {
			return psiElement;
		} else {
			return getSelectedFile();
		}
	}


	@Nullable
	public PsiFile getSelectedFile() {
		final TreePath treepath = _tree.getSelectionPath();
		if (treepath == null) {
			return null;
		}

		@SuppressWarnings({"unchecked"})
		final AbstractTreeNode<VisitableTreeNode> treeNode = (AbstractTreeNode<VisitableTreeNode>) treepath.getLastPathComponent();

		if (treeNode == null) {
			return null;
		} else {
			if (treeNode instanceof BugInstanceNode) {
				final BugInstanceNode node = (BugInstanceNode) treeNode;
				if (node.getPsiFile() != null) {
					return node.getPsiFile();
				} else {
					final PsiClass psiClass = IdeaUtilImpl.findJavaPsiClass(_project, node.getSourcePath());
					LOGGER.debug("BugTreeHelper#getSelectedFile(" + _project + ", " + node.getSourcePath() + ")");
					if (psiClass != null) {
						LOGGER.debug("Found: psiClass (" + psiClass.getName() + ")");
						final PsiFile psiFile = IdeaUtilImpl.getPsiFile(psiClass);
						LOGGER.debug("BugTreeHelper - IdeaUtilImpl.getPsiFile(psiClass) - found - psiFile: " + psiFile.getName());
						node.setPsiFile(psiFile);
						return psiFile;
					} else {
						return null;
					}
				}
			}
			return null;
		}
	}


	@Nullable
	public BugInstanceNode selectPreviousNode() {
		final TreePath treepath = _tree.getSelectionPath();
		if (treepath == null) {
			return null;
		}

		//_tree.expandPath(treepath.getParentPath());
		//final TreePath path = _tree.getPathForRow(_tree.getRowForPath(treepath) - 1);
		//_tree.setSelectionPath(path);
		//scrollPathToVisible(path);

		//treepath.
		final TreePath treePath = _tree.getPathForRow(_tree.getRowForPath(treepath) - 1);
		@SuppressWarnings({"unchecked"})
		final AbstractTreeNode<VisitableTreeNode> treeNode = (AbstractTreeNode<VisitableTreeNode>) treePath.getLastPathComponent();


		final BugInstanceNode bugInstanceNode = findPreviousBugNode(treeNode, treepath, _tree.getModel());

		if (bugInstanceNode != null) {
			final TreePath path = getPath(bugInstanceNode);
			_tree.setExpandsSelectedPaths(true);
			_tree.setSelectionPath(path);
			scrollPathToVisible(path);
		}

		return null;
	}


	public BugInstanceNode selectNextNode() {
		final TreePath treepath = _tree.getSelectionPath();
		if (treepath == null) {
			return null;
		}

		final TreePath path = _tree.getPathForRow(_tree.getRowForPath(treepath) + 1);
		_tree.setSelectionPath(path);
		scrollPathToVisible(path);

		return null;
	}


	public void scrollPathToVisible(final TreePath path) {
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			public void run() {
				_tree.scrollPathToVisible(path);
				_tree.setSelectionPath(path);
			}
		});
	}


	/**
	 * Expand the given tree to the given level, starting from the given node
	 * and path.
	 *
	 * @param node  The node to start from
	 * @param path  The path to start from
	 * @param level The number of levels to expand to
	 */
	public void expandNode(final TreeNode node, final TreePath path, final int level) {
		if (level <= 0) {
			return;
		}

		_tree.expandPath(path);

		for (int i = 0; i < node.getChildCount(); ++i) {
			final TreeNode childNode = node.getChildAt(i);
			expandNode(childNode, path.pathByAddingChild(childNode), level - 1);
		}
	}


	/**
	 * Expand the error tree to the given level.
	 *
	 * @param level The level to expand to
	 */
	public void expandTree(final int level) {
		expandNode((TreeNode) _tree.getModel().getRoot(), new TreePath(_tree.getModel().getRoot()), level);
	}


	/** Collapse the tree so that only the root node is visible. */
	public void collapseTree() {
		for (int i = 1; i < _tree.getRowCount(); ++i) {
			_tree.collapseRow(i);
		}
	}


	public static TreePath getPath(@Nullable TreeNode node) {
		final List<TreeNode> list = new ArrayList<TreeNode>();

		while (node != null) {
			list.add(node);
			//noinspection AssignmentToMethodParameter
			node = node.getParent();
		}
		Collections.reverse(list);

		return new TreePath(list.toArray());
	}


	// todo:
	private void _buildElementsCache(final Object node, final TreePath path, final TreeModel model) {
		/*if (_tree.isRootVisible() || path.getLastPathComponent() != _tree.getModel().getRoot()) {
			//_elementsCache.add(path);
		}*/
		for (int i = 0; i < model.getChildCount(node); i++) {
			final Object childNode = model.getChild(node, i);
			_buildElementsCache(childNode, path.pathByAddingChild(childNode), model);
		}
	}


	@Nullable
	public BugInstanceNode findPreviousBugNode(final Object node, final TreePath path, final TreeModel model) {

		final Object root = _tree.getModel().getRoot();
		BugInstanceNode resultNode = null;
		if (!root.equals(node)) {

			//final TreePath treePath = _tree.getPathForRow(_tree.getRowForPath(path) - 1);
			//if (treePath != null) {


			//@SuppressWarnings({"unchecked"})
			//final AbstractTreeNode<VisitableTreeNode> treeNode = (AbstractTreeNode<VisitableTreeNode>) treePath.getLastPathComponent();


			@SuppressWarnings({"unchecked"})
			final List<VisitableTreeNode> childList = ((AbstractTreeNode<VisitableTreeNode>) node).getChildsList();
			Collections.reverse(childList);
			for (final VisitableTreeNode childNode : childList) {
				if (childNode instanceof BugInstanceNode) {
					resultNode = (BugInstanceNode) childNode;
				} else {
					final TreePath path1 = getPath(childNode);
					resultNode = findPreviousBugNode(childNode, path1, model);
				}

				if (resultNode != null) {
					break;
				}
			}
		}

		return resultNode;
	}


	public void gotoNode(final BugInstanceNode node) {
		gotoNode(node.getBugInstance());
	}


	public void gotoNode(final BugInstance bugInstance) {
		final AbstractTreeNode<VisitableTreeNode> node = findTreeNodeByBugInstance(bugInstance);
		final TreePath path = getPath(node);
		scrollPathToVisible(path);
	}


	@Nullable
	private AbstractTreeNode<VisitableTreeNode> findTreeNodeByBugInstance(final BugInstance bugInstance) {
		return ((GroupTreeModel) _tree.getModel()).findNodeByBugInstance(bugInstance);
	}

}
