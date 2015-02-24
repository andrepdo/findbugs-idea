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
package org.twodividedbyzero.idea.findbugs.gui.tree.view;

import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.AbstractTreeNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.VisitableTreeNode;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
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
final class TreeQuickSearch extends QuickSearch<AbstractTreeNode<VisitableTreeNode>> implements TreeModelListener {

	private JTree _tree;
	private List<TreePath> _elementsCache;


	private TreeQuickSearch() {
	}


	@NotNull
	static TreeQuickSearch install(@NotNull final JTree tree) {
		final TreeQuickSearch ret = new TreeQuickSearch();
		ret.installImpl(tree);
		ret.attachTo(tree);
		return ret;
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
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			public void run() {
				_tree.scrollPathToVisible(path);
			}
		});
	}


	private void clearElementsCache() {
		if (_elementsCache != null) {
			_elementsCache.clear();
			//noinspection AssignmentToNull
			_elementsCache = null;
		}
	}


	@Override
	public void treeNodesChanged(final TreeModelEvent e) {
		hidePopup();
		clearElementsCache();
	}


	@Override
	public void treeNodesInserted(final TreeModelEvent e) {
		hidePopup();
		clearElementsCache();
	}


	@Override
	public void treeNodesRemoved(final TreeModelEvent e) {
		hidePopup();
		clearElementsCache();
	}


	@Override
	public void treeStructureChanged(final TreeModelEvent e) {
		hidePopup();
		clearElementsCache();
	}
}