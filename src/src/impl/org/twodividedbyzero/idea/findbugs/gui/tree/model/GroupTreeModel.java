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
package org.twodividedbyzero.idea.findbugs.gui.tree.model;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.util.BugInstanceUtil;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.core.Bug;
import org.twodividedbyzero.idea.findbugs.core.ProblemCache;
import org.twodividedbyzero.idea.findbugs.gui.tree.BugInstanceComparator;
import org.twodividedbyzero.idea.findbugs.gui.tree.GroupBy;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.Grouper.GrouperCallback;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GroupTreeModel extends AbstractTreeModel<VisitableTreeNode, RootNode> implements GrouperCallback<Bug> {

	private static final Logger LOGGER = Logger.getInstance(GroupTreeModel.class.getName());

	private GroupBy[] _groupBy;
	private final transient Map<String, Map<Integer, List<BugInstanceGroupNode>>> _groups;
	private transient Grouper<Bug> _grouper;
	private int _bugCount;
	private final transient Map<PsiFile, List<ExtendedProblemDescriptor>> _problems;

	@NotNull
	private final transient Project _project;


	public GroupTreeModel(@NotNull final RootNode root, final GroupBy[] groupBy, @NotNull final Project project) {
		_root = root;
		_project = project;
		_groupBy = groupBy.clone();
		_groups = new HashMap<String, Map<Integer, List<BugInstanceGroupNode>>>();
		_problems = project.getComponent(ProblemCache.class).getProblems();
	}

	Project getProject() {
		return _project;
	}

	private void addGroupIfAbsent(final String groupNameKey, final int depth, final BugInstanceGroupNode groupNode) {
		if (!_groups.containsKey(groupNameKey)) {
			final Map<Integer, List<BugInstanceGroupNode>> map = new HashMap<Integer, List<BugInstanceGroupNode>>();
			_groups.put(groupNameKey, map);
			final List<BugInstanceGroupNode> groupNodes = addGroupIfAbsentImpl(groupNameKey, depth, groupNode);
			map.put(depth, groupNodes);
		} else {
			addGroupIfAbsentImpl(groupNameKey, depth, groupNode);
		}
	}

	@NotNull
	private List<BugInstanceGroupNode> addGroupIfAbsentImpl(final String groupNameKey, final int depth, final BugInstanceGroupNode groupNode) {
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

	@SuppressWarnings({"ReturnOfCollectionOrArrayField"})
	public Map<PsiFile, List<ExtendedProblemDescriptor>> getProblems() {
		return _problems;
	}

	@SuppressWarnings({"MethodMayBeStatic", "AnonymousInnerClass"})
	private void addProblem(final BugInstanceNode leaf) {
		final PsiFile psiFile = leaf.getPsiFile();
		_addProblem(psiFile, leaf);
	}

	private void _addProblem(@Nullable final PsiFile value, final BugInstanceNode leaf) {
		if (value != null) {
			final ExtendedProblemDescriptor element = new ExtendedProblemDescriptor(value, leaf.getBug());
			if (_problems.containsKey(value)) {
				_problems.get(value).add(element);
			} else {
				final List<ExtendedProblemDescriptor> list = new ArrayList<ExtendedProblemDescriptor>();
				list.add(element);
				_problems.put(value, list);
			}
		}
	}

	public int getBugCount() {
		EventDispatchThreadHelper.checkEDT();
		return _bugCount;
	}

	@SuppressWarnings({"LockAcquiredButNotSafelyReleased"})
	public void addNode(@NotNull final Bug bug) {
		/*if(isHiddenBugGroup(bugInstance)) {
			return;
		}*/
		_bugCount++;
		group(bug);
	}

	private void group(@NotNull final Bug bug) {
		if (_grouper == null) {
			_grouper = new Grouper<Bug>(this);
		}
		final List<Comparator<Bug>> groupComparators = BugInstanceComparator.getGroupComparators(_groupBy);
		_grouper.group(bug, groupComparators);
	}

	@Override
	public void startGroup(final Bug member, final int depth) {
		EventDispatchThreadHelper.assertInEDTorADT();

		final GroupBy groupBy = _groupBy[depth];
		final String groupName = GroupBy.getGroupName(groupBy, member);
		final BugInstanceGroupNode groupNode = new BugInstanceGroupNode(groupBy, groupName, _root, member, depth, _project);

		addGroupIfAbsent(Arrays.toString(BugInstanceUtil.getGroupPath(member, depth, _groupBy)), depth, groupNode);

		_root.addChild(groupNode);
		nodeStructureChanged(_root);

		startSubGroup(depth + 1, member, member);
	}

	@Override
	public void startSubGroup(final int depth, final Bug member, final Bug parent) {
		EventDispatchThreadHelper.assertInEDTorADT();

		String groupName = GroupBy.getGroupName(_groupBy[depth - 1], member);
		final BugInstanceGroupNode parentGroup = _root.findChildNode(parent, depth - 1, groupName);

		//final BugInstanceGroupNode parentGroup = ((RootNode) _root).getChildByBugInstance(parent, depth - 1);
		//final BugInstanceGroupNode parentGroup = ((RootNode) _root).getChildByGroupName(GroupBy.getGroupName(_groupBy[depth - 1], parent), depth - 1);
		//final BugInstanceGroupNode parentGroup = _groups.get(depth-1 + Arrays.toString(getGroupPath(member)));

		if (parentGroup != null) {
			final GroupBy groupBy = _groupBy[depth];
			groupName = GroupBy.getGroupName(groupBy, member);
			final BugInstanceGroupNode childGroup = new BugInstanceGroupNode(groupBy, groupName, parentGroup, member, depth, _project);

			addGroupIfAbsent(Arrays.toString(BugInstanceUtil.getGroupPath(parent, depth, _groupBy)), depth, childGroup);
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

	@Override
	public void addToGroup(final int depth, final Bug member, final Bug parent) {
		EventDispatchThreadHelper.checkEDT();

		final String groupName = GroupBy.getGroupName(_groupBy[depth], member);
		final BugInstanceGroupNode parentGroup = _root.findChildNode(parent, depth, groupName);

		if (parentGroup != null) {
			final BugInstanceNode childNode = new BugInstanceNode(member, parentGroup, _project);
			parentGroup.addChild(childNode);
			addProblem(childNode);
			nodeStructureChanged(parentGroup);
		} else {
			//noinspection ThrowableInstanceNeverThrown
			LOGGER.error("parentSubGroup can not be null. ", new NullPointerException());
		}
	}

	@Override
	public Comparator<Bug> currentGroupComparatorChain(final int depth) {
		return BugInstanceComparator.getComparatorChain(depth, getGroupBy());
	}

	@Override
	public List<Bug> availableGroups(final int depth, @NotNull final Bug bug) {
		final List<Bug> result = New.arrayList();

		//final GroupBy groupBy = _groupBy[0];
		//final String groupName = GroupBy.getGroupName(groupBy, bug);
		final String groupName = Arrays.toString(BugInstanceUtil.getGroupPath(bug, depth, _groupBy));

		if (_groups.containsKey(groupName)) {
			final Map<Integer, List<BugInstanceGroupNode>> map = _groups.get(groupName);

			if (map.containsKey(depth)) {
				final List<BugInstanceGroupNode> groupNodes = map.get(depth);

				for (final BugInstanceGroupNode node : groupNodes) {
					result.add(node.getBug());
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

	public void clear() {
		EventDispatchThreadHelper.checkEDT();

		//_sortedCollection.clear();
		_bugCount = 0;
		_groups.clear();
		_problems.clear();
		_root.removeAllChilds();
		nodeStructureChanged(_root);
		reload();

	}

	@Nullable
	public BugInstanceNode findNodeByBugInstance(final Bug bug) {
		final String[] fullGroupPath = BugInstanceUtil.getFullGroupPath(bug, _groupBy);
		final String[] groupNameKey = BugInstanceUtil.getGroupPath(bug, fullGroupPath.length - 1, _groupBy);

		final Map<Integer, List<BugInstanceGroupNode>> map = _groups.get(Arrays.toString(groupNameKey));
		if (map != null) {
			for (final Entry<Integer, List<BugInstanceGroupNode>> entry : map.entrySet()) {

				final List<BugInstanceGroupNode> groupNodes = entry.getValue();
				for (final BugInstanceGroupNode groupNode : groupNodes) {

					final List<VisitableTreeNode> bugInstanceNodes = groupNode.getChildsList();
					for (final VisitableTreeNode node : bugInstanceNodes) {
						final Bug otherBug = ((BugInstanceNode) node).getBug();
						if (otherBug.equals(bug)) {
							return (BugInstanceNode) node;
						}
					}
				}
			}
		}
		return null;
	}

	@NotNull
	public Collection<Bug> getBugs() {
		return _root.getAllChildBugs();
	}

	/**
	 * Returns the child of <I>parent</I> at index <I>index</I> in the
	 * parent's child array. <I>parent</I> must be a node previously obtained
	 * from this data source. This should not return null if <i>index</i> is a
	 * valid index for <i>parent</i> (that is <i>index</i> >= 0 && <i>index</i> <
	 * getChildCount(<i>parent</i>)).
	 *
	 * @param parent a node in the tree, obtained from this data source
	 * @param index  ..
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
	 * or the child is <code>null</code>
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
	 * Subclasses must implement this method and return a <code>Class</code>
	 * object for the generic type.
	 *
	 * @return the <code>Class</code> for the generic type
	 */
	@Override
	protected Class<VisitableTreeNode> getNodeClass() {
		return VisitableTreeNode.class;
	}
}
