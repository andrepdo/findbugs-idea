/*
 * Copyright 2008-2015 Andre Pfeiler
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

package org.twodividedbyzero.idea.findbugs.gui.tree.model;

import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.lang.reflect.Array;


/**
 * An abstract implementation of the TreeModel interface. This implementation is
 * partly copied from the <code>DefaultTreeModel</code> implementation
 * (version 6) as supplied with the JVM. However, the type of nodes has been
 * left out, as well as a mechanism to insert, update or delete nodes. This is
 * up to subclasses.
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @param <N> the type of objects that can be used as nodes.
 * @see DefaultTreeModel
 */
public abstract class AbstractTreeModel<N extends TreeNode, R extends N> implements TreeModel {

	/** Root of the tree. */
	protected R _root;

	/** Listeners. */
	protected final EventListenerList _treeModelListeners = new EventListenerList();


	/**
	 * Creates an empty tree in which any node can have children. Initially, no
	 * root is set.
	 */
	public AbstractTreeModel() {
	}


	/**
	 * Adds a listener for the TreeModelEvent posted after the tree changes.
	 *
	 * @param l the listener to add
	 * @see #removeTreeModelListener(TreeModelListener)
	 */
	public final void addTreeModelListener(final TreeModelListener l) {
		_treeModelListeners.add(TreeModelListener.class, l);
	}


	/**
	 * Returns the value returned from <code>getChildNode(parent, index)</code>.
	 *
	 * @param parent a node in the tree, obtained from this data source
	 * @return the child of <I>parent</I> at index <I>index</I>
	 * @see #getChildNode(javax.swing.tree.TreeNode, int)
	 */
	@SuppressWarnings("unchecked")
	public final N getChild(final Object parent, final int index) {
		return getChildNode((N) parent, index);
	}


	/**
	 * Returns the value from <code>getChildNodeCount(parent)</code>.
	 *
	 * @param parent a node in the tree, obtained from this data source
	 * @return the number of children of the node <I>parent</I>
	 */
	@SuppressWarnings("unchecked")
	public final int getChildCount(final Object parent) {
		return getChildNodeCount((N) parent);
	}


	/**
	 * Returns the child of <I>parent</I> at index <I>index</I> in the
	 * parent's child array. <I>parent</I> must be a node previously obtained
	 * from this data source. This should not return null if <i>index</i> is a
	 * valid index for <i>parent</i> (that is <i>index</i> >= 0 && <i>index</i> <
	 * getChildCount(<i>parent</i>)).
	 *
	 * @param parent a node in the tree, obtained from this data source
	 * @param index ..
	 * @return the child of <I>parent</I> at index <I>index</I>
	 */
	public abstract N getChildNode(N parent, int index);


	/**
	 * Returns the number of children of <I>parent</I>. Returns 0 if the node
	 * is a leaf or if it has no children. <I>parent</I> must be a node
	 * previously obtained from this data source.
	 *
	 * @param parent a node in the tree, obtained from this data source
	 * @return the number of children of the node <I>parent</I>
	 */
	public abstract int getChildNodeCount(N parent);


	/**
	 * Returns the value returned from
	 * <code>getIndexOfChildNode(parent, child)</code>.
	 *
	 * @param parent a note in the tree, obtained from this data source
	 * @param child  the node we are interested in
	 * @return the index of the child in the parent, or -1 if either the parent
	 *         or the child is <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	public final int getIndexOfChild(final Object parent, final Object child) {
		return getIndexOfChildNode((N) parent, (N) child);
	}


	/**
	 * Returns the index of child in parent. If either the parent or child is
	 * <code>null</code>, returns -1.
	 *
	 * @param parent a note in the tree, obtained from this data source
	 * @param child  the node we are interested in
	 * @return the index of the child in the parent, or -1 if either the parent
	 *         or the child is <code>null</code>
	 */
	public abstract int getIndexOfChildNode(N parent, N child);


	/**
	 * Obtain the parent of a node.
	 *
	 * @param child a node
	 * @return the parent of the child (or null if child has no parent).
	 */
	public abstract N getParentNode(N child);


	/**
	 * Builds the parents of node up to and including the root node, where the
	 * original node is the last element in the returned array. The length of
	 * the returned array gives the node's depth in the tree.
	 *
	 * @param aNode the R to get the path for
	 * @return ..
	 */
	public final N[] getPathToRoot(final N aNode) {
		return getPathToRoot(aNode, 0);
	}


	/**
	 * Returns the root of the tree. Returns null only if the tree has no nodes.
	 *
	 * @return the root of the tree
	 */
	public final N getRoot() {
		return _root;
	}


	/**
	 * Returns whether the specified node is a leaf node.
	 *
	 * @param node the node to check
	 * @return true if the node has no child nodes
	 */
	@SuppressWarnings("unchecked")
	public final boolean isLeaf(final Object node) {
		return getChildCount(node) == 0;
	}


	/**
	 * Invoke this method after you've changed how node is to be represented in
	 * the tree.
	 *
	 * @param node ..
	 */
	public final void nodeChanged(final N node) {
		if (_treeModelListeners != null && node != null) {
			final N parent = getParentNode(node);

			if (parent != null) {
				final int anIndex = getIndexOfChildNode(parent, node);
				if (anIndex != -1) {
					final int[] cIndexs = new int[1];

					cIndexs[0] = anIndex;
					nodesChanged(parent, cIndexs);
				}
			} else if (node == getRoot()) {
				nodesChanged(node, null);
			}
		}
	}


	/**
	 * Invoke this method after you've changed how the children identified by
	 * childIndicies are to be represented in the tree.
	 *
	 * @param node ..
	 * @param childIndices ..
	 */
	public final void nodesChanged(final N node, final int[] childIndices) {
		if (node != null) {
			if (childIndices != null) {
				final int cCount = childIndices.length;

				if (cCount > 0) {
					final Object[] cChildren = new Object[cCount];

					for (int counter = 0; counter < cCount; counter++) {
						cChildren[counter] = getChildNode(node, childIndices[counter]);
					}
					fireTreeNodesChanged(this, getPathToRoot(node), childIndices, cChildren);
				}
			} else if (node == getRoot()) {
				fireTreeNodesChanged(this, getPathToRoot(node), null, null);
			}
		}
	}


	/**
	 * Invoke this method if you've totally changed the children of node and its
	 * childrens children... This will post a treeStructureChanged event.
	 *
	 * @param node ..
	 */
	public final void nodeStructureChanged(@Nullable final N node) {
		EventDispatchThreadHelper.checkEDT();
		if (node != null) {
			fireTreeStructureChanged(this, getPathToRoot(node), null, null);
		}
	}


	/**
	 * Invoke this method if you've modified the {@code R}s upon which this
	 * model depends. The model will notify all of its listeners that the model
	 * has changed.
	 */
	public final void reload() {
		reload(_root);
	}


	/**
	 * Invoke this method if you've modified the {@code R}s upon which this
	 * model depends. The model will notify all of its listeners that the model
	 * has changed below the given node.
	 *
	 * @param node the node below which the model has changed
	 */
	public final void reload(final N node) {
		if (node != null) {
			fireTreeStructureChanged(this, getPathToRoot(node), null, null);
		}
	}


	/**
	 * Removes a listener previously added with
	 * <code>addTreeModelListener</code>.
	 *
	 * @param l the listener to remove
	 * @see #addTreeModelListener
	 */
	@Override
	public final void removeTreeModelListener(final TreeModelListener l) {
		_treeModelListeners.remove(TreeModelListener.class, l);
	}


	@SuppressWarnings("unchecked")
	private N[] createEmptyArray(final int size) {
		return (N[]) Array.newInstance(getNodeClass(), size);
	}


	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 *
	 * @param source the node where the tree model has changed
	 * @param path   the path to the root node
	 * @see EventListenerList
	 */
	private void fireTreeStructureChanged(final Object source, final TreePath path) {
		// Guaranteed to return a non-null array
		final Object[] listeners = _treeModelListeners.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null) {
					e = new TreeModelEvent(source, path);
				}
				((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
			}
		}
	}


	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 *
	 * @param source	   the node being changed
	 * @param path		 the path to the root node
	 * @param childIndices the indices of the changed elements
	 * @param children	 the changed elements
	 * @see EventListenerList
	 */
	protected final void fireTreeNodesChanged(final Object source, final Object[] path, final int[] childIndices, final Object[] children) {
		// Guaranteed to return a non-null array
		final Object[] listeners = _treeModelListeners.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null) {
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
			}
		}
	}


	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 *
	 * @param source	   the node where the tree model has changed
	 * @param path		 the path to the root node
	 * @param childIndices the indices of the affected elements
	 * @param children	 the affected elements
	 * @see EventListenerList
	 */
	protected final void fireTreeStructureChanged(final Object source, final Object[] path, final int[] childIndices, final Object[] children) {
		// Guaranteed to return a non-null array
		final Object[] listeners = _treeModelListeners.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null) {
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
			}
		}
	}


	/**
	 * Subclasses must implement this method and return a <code>Class</code>
	 * object for the generic type.
	 *
	 * @return the <code>Class</code> for the generic type
	 */
	protected abstract Class<N> getNodeClass();


	/**
	 * Builds the parents of node up to and including the root node, where the
	 * original node is the last element in the returned array. The length of
	 * the returned array gives the node's depth in the tree.
	 *
	 * @param aNode the R to get the path for
	 * @param depth an integer giving the number of steps already taken
	 *              towards the root (on recursive calls), used to size the
	 *              returned array
	 * @return an array of TreeNodes giving the path from the root to the
	 *         specified node
	 */
	protected final N[] getPathToRoot(final N aNode, int depth) {
		// This method recurses, traversing towards the root in order
		// size the array. On the way back, it fills in the nodes,
		// starting from the root and working back to the original node.

		final N[] retNodes;

		if (aNode == null) {
			if (depth == 0) {
				return createEmptyArray(0);
			} else {
				retNodes = createEmptyArray(depth);
			}
		} else {
			//noinspection AssignmentToMethodParameter
			depth++;
			if (aNode == _root) {
				retNodes = createEmptyArray(depth);
			} else {
				retNodes = getPathToRoot(getParentNode(aNode), depth);
			}
			retNodes[retNodes.length - depth] = aNode;
		}
		return retNodes;
	}


	@Override
	public void valueForPathChanged(final TreePath path, final Object newValue) {
		if (_treeModelListeners.getListenerCount() > 0 && !path.getLastPathComponent().equals(newValue)) {
			final TreePath parentPath = path.getParentPath();
			final int index = getIndexOfChild(parentPath.getLastPathComponent(), newValue);
			if (index != -1) {
				fireTreeNodesChanged(this, path.getPath(), new int[] {index}, new Object[] {newValue});
				//fireTreeEvent(CHANGE, new TreeModelEvent(this, parentPath, new int[] {index}, new Object[] {newValue}));
			} else {
				fireTreeStructureChanged(this, parentPath);
				//fireTreeEvent(STRUCTURE, new TreeModelEvent(this, parentPath));
			}
		}
	}


}
