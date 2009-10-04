package org.twodividedbyzero.idea.findbugs.gui.tree.model;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.EventListener;
import java.util.Vector;


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
public abstract class AbstractTreeModel<N> implements Serializable, TreeModel {

	/** Root of the tree. */
	protected N _root;

	/** Listeners. */
	protected EventListenerList _treeModelListeners = new EventListenerList();
	//protected ArrayList<TreeModelListener> _treeModelListener = new ArrayList<TreeModelListener>();


	/**
	 * Creates an empty tree in which any node can have children. Initially, no
	 * root is set.
	 */
	public AbstractTreeModel() {
		this(null);
	}


	/**
	 * Creates a tree in which any node can have children.
	 *
	 * @param root a R object that is the root of the tree
	 */
	public AbstractTreeModel(final N root) {
		super();
		setRoot(root);
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
	 * @see #getChildNode(Object, int)
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
	 * @param index
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
	 * Returns an array of all the objects currently registered as
	 * <code><em>Foo</em>Listener</code>s upon this model.
	 * <code><em>Foo</em>Listener</code>s are registered using the
	 * <code>add<em>Foo</em>Listener</code> method.
	 * <p/>
	 * <p/>
	 * <p/>
	 * You can specify the <code>listenerType</code> argument with a class
	 * literal, such as <code><em>Foo</em>Listener.class</code>. For
	 * example, you can query a
	 * <code>AbstractTreeModelFrom6</code> <code>m</code> for its tree model
	 * listeners with the following code:
	 * <p/>
	 * <pre>
	 * TreeModelListener[] tmls = (TreeModelListener[]) (m
	 * 	.getListeners(TreeModelListener.class));
	 * </pre>
	 * <p/>
	 * If no such listeners exist, this method returns an empty array.
	 *
	 * @param listenerType the type of listeners requested; this parameter should
	 *                     specify an interface that descends from
	 *                     <code>java.util.EventListener</code>
	 * @return an array of all objects registered as
	 *         <code><em>Foo</em>Listener</code>s on this component, or an
	 *         empty array if no such listeners have been added
	 * @throws ClassCastException if <code>listenerType</code> doesn't specify a class
	 *                            or interface that implements
	 *                            <code>java.util.EventListener</code>
	 * @see #getTreeModelListeners
	 * @since 1.3
	 */
	public final <T extends EventListener> T[] getListeners(final Class<T> listenerType) {
		return _treeModelListeners.getListeners(listenerType);
	}


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
	 * @return
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
	 * Returns an array of all the tree model listeners registered on this
	 * model.
	 *
	 * @return all of this model's <code>TreeModelListener</code>s or an
	 *         empty array if no tree model listeners are currently registered
	 * @see #addTreeModelListener
	 * @see #removeTreeModelListener
	 */
	public final TreeModelListener[] getTreeModelListeners() {
		return _treeModelListeners.getListeners(TreeModelListener.class);
	}


	/**
	 * Returns whether the specified node is a leaf node.
	 *
	 * @param node the node to check
	 * @return true if the node has no child nodes
	 * @see TreeModel#isLeaf
	 */
	@SuppressWarnings("unchecked")
	public final boolean isLeaf(final Object node) {
		return getChildCount(node) == 0;
	}


	/*public void valueForPathChanged(final TreePath path, final Object newValue) {
		final MutableTreeNode node = (MutableTreeNode) path.getLastPathComponent();

		node.setUserObject(newValue);
		nodeChanged(path.getLastPathComponent());
	}*/


	/**
	 * Invoke this method after you've changed how node is to be represented in
	 * the tree.
	 *
	 * @param node
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
	 * @param node
	 * @param childIndices
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
	 * Message this to remove node from its parent. This will message
	 * nodesWereRemoved to create the appropriate event. This is the
	 * preferred way to remove a node as it handles the event creation
	 * for you.
	 */
	/*public void removeNodeFromParent(final N node) {
		final N parent = getParentNode(node);

		if (parent == null) {
			throw new IllegalArgumentException("node does not have a parent.");
		}

		final int[] childIndex = new int[1];
		final Object[] removedArray = new Object[1];


		childIndex[0] = getIndexOfChildNode(parent, node);
		//parent.remove(childIndex[0]);

		removedArray[0] = node;
		nodesWereRemoved(parent, childIndex, removedArray);
	}*/


	/**
	 * Invoke this method if you've totally changed the children of node and its
	 * childrens children... This will post a treeStructureChanged event.
	 *
	 * @param node
	 */
	public final void nodeStructureChanged(final N node) {
		if (node != null) {
			if (EventQueue.isDispatchThread()) {
				fireTreeStructureChanged(this, getPathToRoot(node), null, null);
			} else {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						nodeStructureChanged(node);
					}
				});
			}
		}
	}


	/**
	 * Invoke this method after you've inserted some TreeNodes into node.
	 * childIndices should be the index of the new elements and must be sorted
	 * in ascending order.
	 *
	 * @param node
	 * @param childIndices
	 */
	public final void nodesWereInserted(final N node, final int[] childIndices) {
		if (_treeModelListeners != null && node != null && childIndices != null && childIndices.length > 0) {
			final int cCount = childIndices.length;
			final Object[] newChildren = new Object[cCount];

			for (int counter = 0; counter < cCount; counter++) {
				newChildren[counter] = getChildNode(node, childIndices[counter]);
			}
			fireTreeNodesInserted(this, getPathToRoot(node), childIndices, newChildren);
		}
	}


	/**
	 * Invoke this method after you've removed some TreeNodes from node.
	 * childIndices should be the index of the removed elements and must be
	 * sorted in ascending order. And removedChildren should be the array of the
	 * children objects that were removed.
	 *
	 * @param node
	 * @param childIndices
	 * @param removedChildren
	 */
	public final void nodesWereRemoved(final N node, final int[] childIndices, final Object[] removedChildren) {
		if (node != null && childIndices != null) {
			fireTreeNodesRemoved(this, getPathToRoot(node), childIndices, removedChildren);
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
	public final void removeTreeModelListener(final TreeModelListener l) {
		_treeModelListeners.remove(TreeModelListener.class, l);
	}


	/**
	 * Sets the root to <code>root</code>. A null <code>root</code> implies
	 * the tree is to display nothing, and is legal.
	 *
	 * @param newRoot
	 */
	public final void setRoot(final N newRoot) {
		final N oldRoot = _root;
		if (oldRoot != newRoot) {
			if (oldRoot != null) {
				this.deinstall(oldRoot);
			}
			_root = newRoot;
			if (newRoot != null) {
				this.install(newRoot);
			}
			if (newRoot == null && oldRoot != null) {
				fireTreeStructureChanged(this, null);
			} else {
				nodeStructureChanged(newRoot);
			}
		}
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


	@SuppressWarnings("unchecked")
	private void readObject(final ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();

		final Vector<?> values = (Vector<?>) s.readObject();
		int indexCounter = 0;
		final int maxCounter = values.size();

		if (indexCounter < maxCounter && values.elementAt(indexCounter).equals("root")) {
			_root = (N) values.elementAt(++indexCounter);
			//indexCounter++;
		}
	}


	@SuppressWarnings("unchecked")
	private void writeObject(final ObjectOutputStream s) throws IOException {
		final Vector values = new Vector();

		s.defaultWriteObject();
		// Save the root, if its Serializable.
		if (_root != null && _root instanceof Serializable) {
			values.addElement("root");
			values.addElement(_root);
		}
		s.writeObject(values);
	}


	/**
	 * Will be called prior to removal of the current root node. Sub classes
	 * must remove listeners that were added previously by <code>install</code>.
	 *
	 * @param root the root node that is about to be be removed.
	 * @see #install(Object)
	 */
	protected abstract void deinstall(N root);


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
	 * @param source	   the node where new elements are being inserted
	 * @param path		 the path to the root node
	 * @param childIndices the indices of the new elements
	 * @param children	 the new elements
	 * @see EventListenerList
	 */
	protected final void fireTreeNodesInserted(final Object source, final Object[] path, final int[] childIndices, final Object[] children) {
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
				((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
			}
		}
	}


	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 *
	 * @param source	   the node where elements are being removed
	 * @param path		 the path to the root node
	 * @param childIndices the indices of the removed elements
	 * @param children	 the removed elements
	 * @see EventListenerList
	 */
	protected final void fireTreeNodesRemoved(final Object source, final Object[] path, final int[] childIndices, final Object[] children) {
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
				((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
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


	/**
	 * Will be called immediately after setting of the root node. Sub classes
	 * may add listeners to the root that enable them to monitor changes to the
	 * tree and fire change events accordingly.
	 *
	 * @param root the root node that is just installed.
	 * @see #deinstall(Object)
	 */
	protected abstract void install(N root);


	public void valueForPathChanged(final TreePath path, final Object newValue) {
		if ((_treeModelListeners.getListenerCount() > 0) && !path.getLastPathComponent().equals(newValue)) {
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
