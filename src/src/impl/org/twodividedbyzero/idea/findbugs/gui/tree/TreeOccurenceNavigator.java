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
package org.twodividedbyzero.idea.findbugs.gui.tree;

import com.intellij.ide.OccurenceNavigator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
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

	private BugTree _tree;


	public TreeOccurenceNavigator(final BugTree tree) {
		super();
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
			final BugInstanceGroupNode node = (BugInstanceGroupNode) nodedescriptor;
			return node.getChildCount() > 0;
		} else if (nodedescriptor instanceof RootNode) {
			final RootNode node = (RootNode) nodedescriptor;
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
		return null;  //TODO: implement
	}


	public OccurenceInfo goPreviousOccurence() {
		_tree.getTreeHelper().selectPreviousNode();
		return null;  //TODO: implement
	}


	public String getNextOccurenceActionName() {
		return "FindBugs.TreeNextOccurence";  // NON-NLS
	}


	public String getPreviousOccurenceActionName() {
		return "FindBugs.TreePreviousOccurence";  // NON-NLS
	}


	private OccurenceInfo getNodeOccurenceInfo(final BugInstanceNode node) {
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
	}


	private BugInstanceNode selectPrevious() {
		return null;
	}


	private BugInstanceNode selectNext() {
		return null;
	}


	private boolean isLeaf(final TreeNode treenode) {
		final TreeNode treeNode = treenode.getParent();
		return treeNode == null || treeNode.getIndex(treenode) == 0 && isLeaf(treeNode);
	}
}
