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
package org.twodividedbyzero.idea.findbugs.gui.tree;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.core.Bug;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.AbstractTreeNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceGroupNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.GroupTreeModel;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.VisitableTreeNode;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BugTreeHelper {

	@NotNull
	private final JTree tree;

	public BugTreeHelper(@NotNull final JTree tree) {
		this.tree = tree;
	}

	@Nullable
	public BugInstanceNode getSelectedBugInstanceNode() {
		final TreePath treepath = tree.getSelectionPath();
		if (treepath == null) {
			return null;
		}
		final Object treeNode = treepath.getLastPathComponent();
		if (treeNode instanceof BugInstanceNode) {
			return (BugInstanceNode) treeNode;
		}
		return null;
	}

	@Nullable
	public PsiFile getSelectedPsiFile() {
		final BugInstanceNode node = getSelectedBugInstanceNode();
		return node != null ? node.getPsiFile() : null;
	}

	@Nullable
	BugInstanceNode selectPreviousNode() {
		final TreePath treepath = tree.getSelectionPath();
		if (treepath == null) {
			return null;
		}


		@SuppressWarnings({"unchecked"})
		final AbstractTreeNode<VisitableTreeNode> treeNode = (AbstractTreeNode<VisitableTreeNode>) treepath.getLastPathComponent();


		final BugInstanceNode bugInstanceNode = getPreviousBugInstanceLeafNode(treeNode);

		if (bugInstanceNode != null) {
			final TreePath path = getPath(bugInstanceNode);
			tree.expandPath(path);
			tree.setExpandsSelectedPaths(true);
			tree.setSelectionPath(path);
			scrollPathToVisible(path);
		}

		return bugInstanceNode;
	}

	@Nullable
	BugInstanceNode selectNextNode() {
		final TreePath treepath = tree.getSelectionPath();
		if (treepath == null) {
			return null;
		}

		final TreePath path = tree.getPathForRow(tree.getRowForPath(treepath) + 1);

		//noinspection unchecked
		final AbstractTreeNode<VisitableTreeNode> lastPathComponent = (AbstractTreeNode<VisitableTreeNode>) path.getLastPathComponent();
		final BugInstanceNode nextBugInstanceLeafNode = getNextBugInstanceLeafNode(lastPathComponent);
		if (nextBugInstanceLeafNode == null) {
			return null;
		}
		final TreePath treePath = getPath(nextBugInstanceLeafNode);


		tree.setExpandsSelectedPaths(true);
		tree.expandPath(treePath);
		tree.setSelectionPath(treePath);
		scrollPathToVisible(treePath);

		return nextBugInstanceLeafNode;
	}

	@Nullable
	private static BugInstanceNode getNextBugInstanceLeafNode(final AbstractTreeNode<VisitableTreeNode> node) {
		if (node instanceof BugInstanceNode) {
			return (BugInstanceNode) node;
		}
		final List<VisitableTreeNode> childList = node.getChildsList();
		for (final VisitableTreeNode childNode : childList) {
			//noinspection unchecked
			final BugInstanceNode result = childNode instanceof BugInstanceNode ? (BugInstanceNode) childNode : getNextBugInstanceLeafNode((AbstractTreeNode<VisitableTreeNode>) childNode);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	// FIXME: fix me
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"BC_UNCONFIRMED_CAST_OF_RETURN_VALUE"})
	@Nullable
	private BugInstanceNode getPreviousBugInstanceLeafNode(@SuppressWarnings("TypeMayBeWeakened") final AbstractTreeNode<VisitableTreeNode> node) {
		if (node instanceof BugInstanceNode) {
			return (BugInstanceNode) node;
		}
		if (tree.getModel().getRoot().equals(node)) {
			return null;
		}

		@SuppressWarnings("unchecked")
		final List<VisitableTreeNode> childList = (List<VisitableTreeNode>) node.getParent().getChildsList();
		Collections.reverse(childList);
		for (final VisitableTreeNode childNode : childList) {
			//noinspection ObjectEquality
			if (childNode instanceof BugInstanceNode && childNode != node) {
				return (BugInstanceNode) childNode;
			} else if (childNode instanceof BugInstanceGroupNode) {
				//noinspection TailRecursion,unchecked
				return getPreviousBugInstanceLeafNode((AbstractTreeNode<VisitableTreeNode>) childNode);
			}
		}

		return null;
	}

	private void scrollPathToVisible(final TreePath path) {
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			public void run() {
				tree.scrollPathToVisible(path);
				tree.setSelectionPath(path);
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
	private void expandNode(final TreeNode node, final TreePath path, final int level) {
		if (level <= 0) {
			return;
		}

		tree.expandPath(path);

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
		expandNode((TreeNode) tree.getModel().getRoot(), new TreePath(tree.getModel().getRoot()), level);
	}

	/**
	 * Collapse the tree so that only the root node is visible.
	 */
	public void collapseTree() {
		for (int i = 1; i < tree.getRowCount(); ++i) {
			tree.collapseRow(i);
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

	public void gotoNode(final Bug bug) {
		final AbstractTreeNode<VisitableTreeNode> node = findTreeNodeByBugInstance(bug);
		final TreePath path = getPath(node);
		scrollPathToVisible(path);
	}

	@Nullable
	private AbstractTreeNode<VisitableTreeNode> findTreeNodeByBugInstance(final Bug bug) {
		return ((GroupTreeModel) tree.getModel()).findNodeByBugInstance(bug);
	}
}
