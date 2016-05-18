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

import com.intellij.ide.OccurenceNavigator;
import com.intellij.openapi.diagnostic.Logger;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.AbstractNodeDescriptor;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceGroupNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.RootNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.VisitableTreeNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.view.BugTree;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.82-dev
 */
public final class TreeOccurenceNavigator implements OccurenceNavigator {

	private static final Logger LOGGER = Logger.getInstance(TreeOccurenceNavigator.class.getName());

	private final BugTree _tree;


	public TreeOccurenceNavigator(final BugTree tree) {
		_tree = tree;
	}


	public boolean hasNextOccurence() {
		final TreePath treepath = _tree.getSelectionPath();
		if (treepath == null) {
			return false;
		}

		@SuppressWarnings({"unchecked"})
		final AbstractNodeDescriptor<VisitableTreeNode> nodedescriptor = (AbstractNodeDescriptor<VisitableTreeNode>) treepath.getLastPathComponent();

		if (nodedescriptor instanceof BugInstanceNode) {
			return _tree.getRowCount() != _tree.getRowForPath(treepath) + 1;
		} else if (nodedescriptor instanceof BugInstanceGroupNode) {
			final TreeNode node = (BugInstanceGroupNode) nodedescriptor;
			return node.getChildCount() > 0;
		} else if (nodedescriptor instanceof RootNode) {
			final TreeNode node = (RootNode) nodedescriptor;
			return node.getChildCount() > 0;
		}
		return false;
	}


	public boolean hasPreviousOccurence() {
		final TreePath treepath = _tree.getSelectionPath();
		if (treepath == null) {
			return false;
		} else {
			/*DefaultMutableTreeNode defaultmutabletreenode = (DefaultMutableTreeNode) treepath.getLastPathComponent();
			Object obj = defaultmutabletreenode.getUserObject();
			return (obj instanceof NodeDescriptor) && !a(defaultmutabletreenode);*/
			@SuppressWarnings({"unchecked"})
			final AbstractNodeDescriptor<VisitableTreeNode> nodedescriptor = (AbstractNodeDescriptor<VisitableTreeNode>) treepath.getLastPathComponent();
			return !(nodedescriptor instanceof RootNode);

		}
	}


	public OccurenceInfo goNextOccurence() {
		_tree.getTreeHelper().selectNextNode();
		return null;  //FIXME: implement
	}


	public OccurenceInfo goPreviousOccurence() {
		_tree.getTreeHelper().selectPreviousNode();
		return null;  //FIXME: implement
	}


	public String getNextOccurenceActionName() {
		return "FindBugs.TreeNextOccurence";
	}


	public String getPreviousOccurenceActionName() {
		return "FindBugs.TreePreviousOccurence";
	}


	/*private OccurenceInfo getNodeOccurenceInfo(final BugInstanceNode node) {
		if (node == null) {
			return null;
		}

		final PsiFile psiFile = _tree.getTreeHelper().getSelectedFile();
		if (psiFile != null) {
			final VirtualFile virtualFile = psiFile.getVirtualFile();
			LOGGER.debug("PsiFile: " + psiFile + " VirtualFile: " + virtualFile.getName() + " - Line: " + node.getSourceLines()[0]);
			//return new com.intellij.ide.OccurenceNavigator.OccurenceInfo(new OpenFileDescriptor(myProject, ((SmartTodoItemPointer)todoitemnode.getValue()).getTodoItem().getFile().getVirtualFile(), ((SmartTodoItemPointer)todoitemnode.getValue()).getRangeMarker().getStartOffset()), -1, -1);
			return new OccurenceInfo(new OpenFileDescriptor(_tree.getProject(), virtualFile, node.getSourceLines()[0] - 1, 0), -1, -1);
		} else {
			return null;
		}
	}*/


	/*private BugInstanceNode selectPrevious() {
		return null;
	}


	@SuppressWarnings({"UPM_UNCALLED_PRIVATE_METHOD"})
	private BugInstanceNode selectNext() {
		return null;
	}


	private boolean isLeaf(final TreeNode treenode) {
		final TreeNode treeNode = treenode.getParent();
		return treeNode == null || treeNode.getIndex(treenode) == 0 && isLeaf(treeNode);
	}*/
}
