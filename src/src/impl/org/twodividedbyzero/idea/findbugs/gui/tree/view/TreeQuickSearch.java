/**
 * Copyright 2009 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.twodividedbyzero.idea.findbugs.gui.tree.view;

import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.AbstractTreeNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.VisitableTreeNode;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
public class TreeQuickSearch extends QuickSearch<AbstractTreeNode<VisitableTreeNode>> implements TreeModelListener {

	private static final TreeQuickSearch _instance = new TreeQuickSearch();
	private JTree _tree;
	private List<TreePath> _elementsCache;


	private TreeQuickSearch() {
	}


	public static void install(@NotNull final JTree tree) {
		_instance.install((JComponent) tree);
		_instance.attachTo(tree);
	}


	private void attachTo(final JTree tree) {
		_tree = tree;
		if (tree.getModel() != null) {
			tree.getModel().addTreeModelListener(this);
		}
	}


	@Override
	protected void uninstallListeners() {
		_tree.getModel().removeTreeModelListener(this);
	}


	@Override
	protected int getElementCount() {
		return getElementsCache().size();
	}


	@Override
	protected String convertElementToString(final AbstractTreeNode<VisitableTreeNode> element) {
		return element.getSimpleName();
	}


	@Override
	protected List<TreePath> getElementsCache() {
		if (_elementsCache == null) {
			buildElementsCache();
		}
		return Collections.unmodifiableList(_elementsCache);
	}


	@Override
	protected AbstractTreeNode<VisitableTreeNode> getElementAt(final int index) {
		if (index == -1) {
			return null;
		}

		//noinspection unchecked
		return (AbstractTreeNode<VisitableTreeNode>) getElementsCache().get(index).getLastPathComponent();
	}


	void buildElementsCache() {
		_elementsCache = new ArrayList<TreePath>();
		final Object root = _tree.getModel().getRoot();
		_buildElementsCache(root, new TreePath(root), _tree.getModel());
	}


	private void _buildElementsCache(final Object node, final TreePath path, final TreeModel model) {
		if (_tree.isRootVisible() || path.getLastPathComponent() != _tree.getModel().getRoot()) {
			_elementsCache.add(path);
		}
		for (int i = 0; i < model.getChildCount(node); i++) {
			final Object childNode = model.getChild(node, i);
			_buildElementsCache(childNode, path.pathByAddingChild(childNode), model);
		}
	}


	@Override
	protected void setSelectedElement(final int index) {
		final TreePath path = getElementsCache().get(index);
		_tree.setExpandsSelectedPaths(true);
		//_tree.addSelectionPath(path);
		_tree.setSelectionPath(path);
		scrollPathToVisible(path);
	}


	private void scrollPathToVisible(final TreePath path) {
		if (EventQueue.isDispatchThread()) {
			_tree.scrollPathToVisible(path);
		} else {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					scrollPathToVisible(path);
				}
			});
		}
	}


	private void clearElementsCache() {
		if (_elementsCache != null) {
			_elementsCache.clear();
			//noinspection AssignmentToNull
			_elementsCache = null;
		}
	}


	public void treeNodesChanged(final TreeModelEvent e) {
		hidePopup();
		clearElementsCache();
	}


	public void treeNodesInserted(final TreeModelEvent e) {
		hidePopup();
		clearElementsCache();
	}


	public void treeNodesRemoved(final TreeModelEvent e) {
		hidePopup();
		clearElementsCache();
	}


	public void treeStructureChanged(final TreeModelEvent e) {
		hidePopup();
		clearElementsCache();
	}
}
