/*
 * Copyright 2016 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.gui.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.New;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.List;

/**
 * {@link com.intellij.ide.util.treeView.TreeState} can only be used if the TreeNode-instances are recycled.
 * Otherwise restore expand-state does not work.
 */
public final class TreeState {

	@NotNull
	private final JTree tree;

	@NotNull
	private final List<String[]> expanded;

	@Nullable
	private final List<String[]> selection;

	private TreeState(
			@NotNull final JTree tree,
			@NotNull final List<String[]> expanded,
			@Nullable final List<String[]> selection
	) {
		this.tree = tree;
		this.expanded = expanded;
		this.selection = selection;
	}

	public void restore() {
		for (final String[] strPath : expanded) {
			final List<TreeNode> path = createPath(strPath);
			if (!path.isEmpty()) {
				tree.expandPath(new TreePath(path.toArray()));
			}
		}
		if (selection != null) {
			final List<TreePath> paths = New.arrayList();
			for (final String[] strSelection : selection) {
				final List<TreeNode> path = createPath(strSelection);
				if (!path.isEmpty()) {
					paths.add(new TreePath(path.toArray()));
				}
			}
			if (!paths.isEmpty()) {
				tree.setSelectionPaths(paths.toArray(new TreePath[paths.size()]));
			}
		}
	}

	@NotNull
	private List<TreeNode> createPath(@NotNull final String[] path) {
		final List<TreeNode> ret = New.arrayList();
		createPathRecursive(path, 0, ret, (TreeNode) tree.getModel().getRoot());
		return ret;
	}

	private boolean createPathRecursive(
			@NotNull final String[] wantPath,
			int depth,
			@NotNull final List<TreeNode> havePath,
			@NotNull final TreeNode parent) {

		if (parent.toString().equals(wantPath[depth])) {
			havePath.add(parent);
			if (wantPath.length > (depth + 1)) {
				for (int i = 0; i < parent.getChildCount(); i++) {
					if (createPathRecursive(wantPath, depth + 1, havePath, parent.getChildAt(i))) {
						break;
					}
				}
			}
			return true;
		}
		return false;
	}

	@NotNull
	public static TreeState create(@NotNull final JTree tree) {
		final List<String[]> expanded = New.arrayList();
		final TreeModel model = tree.getModel();
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		addExpandedRecursive(expanded, tree, root);

		final TreePath[] selected = tree.getSelectionPaths();
		List<String[]> strSelection = null;
		if (selected != null) {
			strSelection = New.arrayList();
			for (final TreePath treePath : selected) {
				final Object[] path = treePath.getPath();
				final String[] strPath = new String[path.length];
				for (int i = 0; i < path.length; i++) {
					strPath[i] = String.valueOf(path[i]);
				}
				strSelection.add(strPath);
			}
		}

		return new TreeState(
				tree,
				expanded,
				strSelection
		);
	}

	private static void addExpandedRecursive(
			@NotNull final List<String[]> expanded,
			@NotNull final JTree tree,
			@NotNull final DefaultMutableTreeNode parent
	) {
		final TreeNode[] path = parent.getPath();
		final TreePath treePath = new TreePath(path);
		if (tree.isExpanded(treePath)) {
			final String[] strPath = new String[path.length];
			for (int i = 0; i < path.length; i++) {
				strPath[i] = path[i].toString();
			}
			expanded.add(strPath);
			for (int i = 0; i < parent.getChildCount(); i++) {
				addExpandedRecursive(expanded, tree, (DefaultMutableTreeNode) parent.getChildAt(i));
			}
		}
	}
}
