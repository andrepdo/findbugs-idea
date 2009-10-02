/**
 * Copyright 2008 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.gui.tree.model;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import edu.umd.cs.findbugs.BugInstance;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.gui.tree.BugInstanceComparator;
import org.twodividedbyzero.idea.findbugs.gui.tree.GroupBy;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.Grouper.GrouperCallback;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class GroupTreeModel extends AbstractTreeModel<VisitableTreeNode> implements GrouperCallback<BugInstance> {

	private static final long serialVersionUID = 0L;
	private static final Logger LOGGER = Logger.getInstance(GroupTreeModel.class.getName());

	private GroupBy[] _groupBy;
	private volatile Map<String, Map<Integer, List<BugInstanceGroupNode>>> _groups;
	private transient Grouper<BugInstance> _grouper;

	private FindBugsPreferences _preferences;
	private int _bugCount;


	public GroupTreeModel(final VisitableTreeNode root, final GroupBy[] groupBy, final Project project) {
		_root = root;
		_groupBy = groupBy.clone();
		_groups = new ConcurrentHashMap<String, Map<Integer, List<BugInstanceGroupNode>>>();
		_preferences = IdeaUtilImpl.getPluginComponent(project).getPreferences();
	}


	private void addGroupIfAbsent(final String groupNameKey, final int depth, final BugInstanceGroupNode groupNode) {
		if (!_groups.containsKey(groupNameKey)) {
			final Map<Integer, List<BugInstanceGroupNode>> map = new ConcurrentHashMap<Integer, List<BugInstanceGroupNode>>();
			_groups.put(groupNameKey, map);
			final List<BugInstanceGroupNode> groupNodes = _addGroupIfAbsent(groupNameKey, depth, groupNode);
			map.put(depth, groupNodes);
		} else {
			_addGroupIfAbsent(groupNameKey, depth, groupNode);
		}
	}


	private List<BugInstanceGroupNode> _addGroupIfAbsent(final String groupNameKey, final int depth, final BugInstanceGroupNode groupNode) {
		final Map<Integer, List<BugInstanceGroupNode>> map = _groups.get(groupNameKey);

		if (map.containsKey(depth)) {
			final List<BugInstanceGroupNode> list = map.get(depth);
			list.add(groupNode);

			return list;
		} else {
			final List<BugInstanceGroupNode> list = new ArrayList<BugInstanceGroupNode>();
			list.add(groupNode);
			map.put(depth, list);

			return list;
		}
	}


	public int getBugCount() {
		return _bugCount;
	}


	public void addNode(final BugInstance bugInstance) {
		/*if(isHiddenBugGroup(bugInstance)) {
			return;
		}*/
		_bugCount++;
		group(bugInstance);
	}


	private void group(final BugInstance bugInstance) {
		if (_grouper == null) {
			_grouper = new Grouper<BugInstance>(this);
		}
		final List<Comparator<BugInstance>> groupComparators = BugInstanceComparator.getGroupComparators(_groupBy);
		_grouper.group(bugInstance, groupComparators);
	}


	public void startGroup(final BugInstance member, final int depth) {
		final BugInstanceGroupNode groupNode;
		final GroupBy groupBy = _groupBy[depth];
		final String groupName = GroupBy.getGroupName(groupBy, member);
		groupNode = new BugInstanceGroupNode(groupBy, groupName, _root, member, depth);

		//addGroupIfAbsent(groupName, depth, groupNode);
		addGroupIfAbsent(Arrays.toString(getGroupPath(member, depth)), depth, groupNode);

		((RootNode) _root).addChild(groupNode);
		nodeStructureChanged(_root);

		startSubGroup(depth + 1, member, member);
	}


	public void startSubGroup(final int depth, final BugInstance member, final BugInstance parent) {

		String groupName = GroupBy.getGroupName(_groupBy[depth - 1], member);
		final BugInstanceGroupNode parentGroup = ((RootNode) _root).findChildNode(parent, depth - 1, groupName);

		//final BugInstanceGroupNode parentGroup = ((RootNode) _root).getChildByBugInstance(parent, depth - 1);
		//final BugInstanceGroupNode parentGroup = ((RootNode) _root).getChildByGroupName(GroupBy.getGroupName(_groupBy[depth - 1], parent), depth - 1);
		//final BugInstanceGroupNode parentGroup = _groups.get(depth-1 + Arrays.toString(getGroupPath(member)));

		if (parentGroup != null) {
			final GroupBy groupBy = _groupBy[depth];
			groupName = GroupBy.getGroupName(groupBy, member);
			final BugInstanceGroupNode childGroup = new BugInstanceGroupNode(groupBy, groupName, parentGroup, member, depth);

			addGroupIfAbsent(Arrays.toString(getGroupPath(parent, depth)), depth, childGroup);
			//addGroupIfAbsent(GroupBy.getGroupName(_groupBy[0], parent), depth, childGroup);

			parentGroup.addChild(childGroup);
			nodeStructureChanged(parentGroup);

			if (depth < _groupBy.length - 1) {
				startSubGroup(depth + 1, member, member);
			} else {
				addToGroup(depth, member, member);
			}
		} else {
			//noinspection ThrowableInstanceNeverThrown
			LOGGER.error(new NullPointerException("parentGroup can not be null."));
		}


	}


	public void addToGroup(final int depth, final BugInstance member, final BugInstance parent) {
		final String groupName = GroupBy.getGroupName(_groupBy[depth], member);
		final BugInstanceGroupNode parentGroup = ((RootNode) _root).findChildNode(parent, depth, groupName);


		if (parentGroup != null) {
			final BugInstanceNode childNode = new BugInstanceNode(member, parentGroup);
			parentGroup.addChild(childNode);
			nodeStructureChanged(parentGroup);
		} else {
			//noinspection ThrowableInstanceNeverThrown
			LOGGER.error("parentSubGroup can not be null. ", new NullPointerException());
		}
	}


	public List<String> getBugInstanceGroupPath(final BugInstance bugInstance) {
		final List<String> result = new ArrayList<String>();
		for (final GroupBy groupBy : _groupBy) {
			result.add(GroupBy.getGroupName(groupBy, bugInstance));
		}

		//Collections.reverse(result);
		return result;
	}


	public String[] getGroupPath(final BugInstance bugInstance, final int depth) {
		final List<String> path = getBugInstanceGroupPath(bugInstance);
		final String[] result = new String[depth];

		for (int i = 0; i < depth; i++) {
			final String str = path.get(i);
			result[i] = str;
		}

		return result;
	}


	public String[] getGroupPath(final BugInstance bugInstance) {
		final List<String> path = getBugInstanceGroupPath(bugInstance);
		return getGroupPath(bugInstance, path.size());
	}


	public Comparator<BugInstance> currentGroupComparatorChain(final int depth) {
		return BugInstanceComparator.getComparatorChain(depth, getGroupBy());
	}


	public List<BugInstance> availableGroups(final int depth, final BugInstance bugInstance) {
		final List<BugInstance> result = new ArrayList<BugInstance>();

		//final GroupBy groupBy = _groupBy[0];
		//final String groupName = GroupBy.getGroupName(groupBy, bugInstance);
		final String groupName = Arrays.toString(getGroupPath(bugInstance, depth));

		if (_groups.containsKey(groupName)) {
			final Map<Integer, List<BugInstanceGroupNode>> map = _groups.get(groupName);

			if (map.containsKey(depth)) {
				final List<BugInstanceGroupNode> groupNodes = map.get(depth);

				for (final BugInstanceGroupNode node : groupNodes) {
					result.add(node.getBugInstance());
				}
			}
		}

		return result;
	}


	public void setGroupBy(final GroupBy[] groupBy) {
		_groupBy = groupBy.clone();
	}


	public GroupBy[] getGroupBy() {
		return _groupBy.clone();
	}


	public synchronized void clear() {
		//_sortedCollection.clear();
		_bugCount = 0;
		_groups.clear();
		((RootNode) _root).removeAllChilds();
		nodeStructureChanged(_root);
		reload();
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
	@Override
	public VisitableTreeNode getChildNode(final VisitableTreeNode parent, final int index) {
		return (VisitableTreeNode) parent.getChildAt(index);
	}


	/**
	 * Returns the number of children of <I>parent</I>. Returns 0 if the node
	 * is a leaf or if it has no children. <I>parent</I> must be a node
	 * previously obtained from this data source.
	 *
	 * @param parent a node in the tree, obtained from this data source
	 * @return the number of children of the node <I>parent</I>
	 */
	@Override
	public int getChildNodeCount(final VisitableTreeNode parent) {
		return parent.getChildCount();
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
	@Override
	public int getIndexOfChildNode(final VisitableTreeNode parent, final VisitableTreeNode child) {
		return parent.getIndex(child);
	}


	/**
	 * Obtain the parent of a node.
	 *
	 * @param child a node
	 * @return the parent of the child (or null if child has no parent).
	 */
	@Override
	public VisitableTreeNode getParentNode(final VisitableTreeNode child) {
		return (VisitableTreeNode) child.getParent();
	}


	/**
	 * Will be called prior to removal of the current root node. Sub classes
	 * must remove listeners that were added previously by <code>install</code>.
	 *
	 * @param root the root node that is about to be be removed.
	 * @see #install(Object)
	 */
	@Override
	protected void deinstall(final VisitableTreeNode root) {
		//TODO: implement
	}


	/**
	 * Subclasses must implement this method and return a <code>Class</code>
	 * object for the generic type.
	 *
	 * @return the <code>Class</code> for the generic type
	 */
	@Override
	protected Class<VisitableTreeNode> getNodeClass() {
		return VisitableTreeNode.class;
	}


	/**
	 * Will be called immediately after setting of the root node. Sub classes
	 * may add listeners to the root that enable them to monitor changes to the
	 * tree and fire change events accordingly.
	 *
	 * @param root the root node that is just installed.
	 * @see #deinstall(Object)
	 */
	@Override
	protected void install(final VisitableTreeNode root) {
		//TODO: implement
	}

}
