/*
 * Copyright 2010 Andre Pfeiler
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

import com.intellij.openapi.project.Project;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugRankCategory;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.gui.tree.BugInstanceComparator;
import org.twodividedbyzero.idea.findbugs.gui.tree.GroupBy;
import org.twodividedbyzero.idea.findbugs.gui.tree.NodeVisitor;
import org.twodividedbyzero.idea.findbugs.gui.tree.RecurseNodeVisitor;
import org.twodividedbyzero.idea.findbugs.gui.tree.RecurseNodeVisitor.RecurseVisitCriteria;
import org.twodividedbyzero.idea.findbugs.gui.tree.view.MaskIcon;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.1.1
 */
public class BugInstanceGroupNode extends AbstractTreeNode<VisitableTreeNode> implements VisitableTreeNode {

	private final List<VisitableTreeNode> _childs;
	private final BugInstance _bugInstance;
	private final RecurseNodeVisitor<BugInstanceGroupNode> _recurseNodeVisitor = new RecurseNodeVisitor<BugInstanceGroupNode>(this);
	private final Project _project;

	//private static Icon _expandedIcon = new MaskIcon(ResourcesLoader.loadIconResource("/nodes/TreeOpen.png"));
	//private static Icon _collapsedIcon = new MaskIcon(ResourcesLoader.loadIconResource("/nodes/TreeClosed.png"));


	/**
	 * Creates a new instance of BugInstanceGroup.
	 *
	 * @param groupBy	 string indicating why the bug instances in the group
	 *                    are related
	 * @param groupName   name of the group (e.g., the class name if the group
	 * @param parent	  the parent tree node
	 * @param bugInstance the buginstance
	 * @param depth	   the category depth from root category
	 * @param project
	 */
	public BugInstanceGroupNode(final GroupBy groupBy, final String groupName, final VisitableTreeNode parent, final BugInstance bugInstance, final int depth, final Project project) {

		//_parent = parent;
		setParent(parent);
		_project = project;
		_bugInstance = bugInstance;
		_childs = new ArrayList<VisitableTreeNode>();
		_groupBy = groupBy;
		_groupName = groupName;
		_simpleName = groupName;
		_memberCount = 0;
		_depthFromRoot = depth;

		setTooltip(_groupName);
		setCollapsedIcon(new MaskIcon(getGroupByCollapsedIcon(groupBy), Color.BLACK));
		setExpandedIcon(new MaskIcon(getGroupByExpandedIcon(groupBy), Color.BLACK));
	}


	Project getProject() {
		return _project;
	}


	@Override
	public void addChild(final VisitableTreeNode node) {
		_childs.add(node);

		if (node instanceof BugInstanceNode && node.isLeaf()) {

			Collections.sort(_childs, new ChildComparator());

			incrementMemberCount();
			TreeNode treeNode = getParent();
			while (treeNode != null && treeNode instanceof BugInstanceGroupNode) {
				((BugInstanceGroupNode) treeNode).incrementMemberCount();
				treeNode = treeNode.getParent();
			}
		}
	}


	/**
	 * Perfomrs a deep search. Get child BugInstanceGroupNode by BugInstance group name.
	 *
	 * @param groupName the group name to search for
	 * @param depth
	 * @return the BugInstanceGroupNode
	 * @deprecated use {@link BugInstanceGroupNode#findChildNode(edu.umd.cs.findbugs.BugInstance, int, String)}
	 */
	@Deprecated
	@Nullable
	public BugInstanceGroupNode getChildByGroupName(final String groupName, final int depth) {

		if (groupName.equals(_groupName) && depth == _depthFromRoot) {
			return this;
		}

		BugInstanceGroupNode resultNode = null;
		for (final TreeNode node : _childs) {
			if (node instanceof BugInstanceGroupNode) {
				final BugInstanceGroupNode groupNode = (BugInstanceGroupNode) node;
				if (groupName.equals(groupNode.getGroupName()) && depth == groupNode.getDepth()) {
					resultNode = groupNode;
				} else {
					resultNode = groupNode.getChildByGroupName(groupName, depth);
				}

				if (resultNode != null) {
					break;
				}
			}
		}

		return resultNode;
	}


	/**
	 * Perfomrs a deep search. Get child BugInstanceGroupNode by BugInstance object.
	 *
	 * @param bugInstance the findbugs buginstance to search for
	 * @param depth	   the machting depth to search for
	 * @return the BugInstanceGroupNode
	 * @deprecated use {@link BugInstanceGroupNode#findChildNode(edu.umd.cs.findbugs.BugInstance, int, String)}
	 */
	@Deprecated
	@Nullable
	public BugInstanceGroupNode getChildByBugInstance(final BugInstance bugInstance, final int depth) {
		if (bugInstance.equals(_bugInstance) && depth == _depthFromRoot) {
			return this;
		}

		BugInstanceGroupNode resultNode = null;

		for (final TreeNode node : _childs) {
			if (node instanceof BugInstanceGroupNode) {
				final BugInstanceGroupNode groupNode = (BugInstanceGroupNode) node;
				if (bugInstance.equals(groupNode.getBugInstance()) && depth == groupNode.getDepth()) {
					resultNode = groupNode;
					//break;
				} else {
					resultNode = groupNode.getChildByBugInstance(bugInstance, depth);
				}

				if (resultNode != null) {
					break;
				}
			}
		}

		return resultNode;
	}


	@Nullable
	public BugInstanceGroupNode findChildNode(final BugInstance bugInstance, final int depth, final String groupName) {
		if (bugInstance.equals(_bugInstance) && depth == _depthFromRoot && groupName.equals(_groupName)) {
			return this;
		}

		final RecurseVisitCriteria criteria = new RecurseVisitCriteria(bugInstance, depth, groupName);
		return _recurseNodeVisitor.findChildNode(criteria);
	}


	@Override
	public void accept(final NodeVisitor visitor) {
		visitor.visitGroupNode(this);
	}


	public List<BugInstance> getChildBugInstances() {
		final List<BugInstance> list = new ArrayList<BugInstance>();

		for (final TreeNode child : _childs) {
			if (child instanceof BugInstanceGroupNode) {
				final BugInstance bugInstance = ((BugInstanceGroupNode) child).getBugInstance();
				list.add(bugInstance);
			}
		}

		return list;
	}


	public List<BugInstance> getAllChildBugInstances() {
		final List<BugInstance> list = new ArrayList<BugInstance>();

		for (final TreeNode child : _childs) {
			if (child instanceof BugInstanceGroupNode) {
				final BugInstanceGroupNode node = (BugInstanceGroupNode) child;
				list.add(node.getBugInstance());
				final List<BugInstance> bugInstances = ((BugInstanceGroupNode) child).getAllChildBugInstances();
				list.addAll(list.size(), bugInstances);
			}
		}

		return list;
	}


	public static TreePath getPath(TreeNode node) {
		final List<TreeNode> list = new ArrayList<TreeNode>();

		while (node != null) {
			list.add(node);
			//noinspection AssignmentToMethodParameter
			node = node.getParent();
		}
		Collections.reverse(list);

		return new TreePath(list.toArray());
	}


	public String[] getPath() {
		final List<String> path = _getPath();
		return getPath(path.size());
	}


	String[] getPath(final int depth) {
		final List<String> path = _getPath();
		final String[] result = new String[depth];

		for (int i = 0; i < depth; i++) {
			final String str = path.get(i);
			result[i] = str;
		}

		return result;
	}


	List<String> _getPath() {
		final List<String> list = new ArrayList<String>();

		TreeNode node = this;
		while (node != null) {
			if (node instanceof BugInstanceGroupNode) {
				list.add(((BugInstanceGroupNode) node).getGroupName());
				node = node.getParent();
			}
		}
		//Collections.reverse(list);

		return list;
	}


	@Override
	public List<VisitableTreeNode> getChildsList() {
		return _childs;
	}


	public BugInstanceGroupNode getTreeNode() {
		return this;
	}


	public boolean getAllowsChildren() {
		return true;
	}


	public boolean isLeaf() {
		return _childs.isEmpty();
	}


	void incrementMemberCount() {
		++_memberCount;
	}


	public BugInstance getBugInstance() {
		return _bugInstance;
	}


	private Icon getGroupByCollapsedIcon(final GroupBy groupBy) {

		switch (groupBy) {

			case BugCategory:
				return GuiResources.GROUP_BY_CATEGORY_ICON;
			case BugShortDescription:
			case BugType:
				return _collapsedIcon;
			case Class:
				return GuiResources.GROUP_BY_CLASS_ICON;
			case Package:
				return GuiResources.GROUP_BY_PACKAGE_ICON;
			case Priority:
				final String priorityString = getBugInstance().getPriorityString();
				if (GuiResources.GROUP_BY_PRIORITY_ICONS.containsKey(priorityString)) {
					return GuiResources.GROUP_BY_PRIORITY_ICONS.get(priorityString);
				} else {
					return GuiResources.GROUP_BY_PRIORITY_EXP_ICON;
				}
			case BugRank:
				final String rankString = BugRankCategory.getRank(getBugInstance().getBugRank()).toString();
				if (GuiResources.GROUP_BY_RANK_ICONS.containsKey(rankString)) {
					return GuiResources.GROUP_BY_RANK_ICONS.get(rankString);
				} else {
					return GuiResources.GROUP_BY_PRIORITY_ICON;
				}
			default:
				return _collapsedIcon;
		}
	}


	private Icon getGroupByExpandedIcon(final GroupBy groupBy) {

		switch (groupBy) {

			case BugCategory:
				return GuiResources.GROUP_BY_CATEGORY_ICON;
			case BugShortDescription:
			case BugType:
				return _expandedIcon;
			case Class:
				return GuiResources.GROUP_BY_CLASS_ICON;
			case Package:
				return GuiResources.GROUP_BY_PACKAGE_ICON;
			case Priority:
				final String priorityString = getBugInstance().getPriorityString();
				if (GuiResources.GROUP_BY_PRIORITY_ICONS.containsKey(priorityString)) {
					return GuiResources.GROUP_BY_PRIORITY_ICONS.get(priorityString);
				} else {
					return GuiResources.GROUP_BY_PRIORITY_EXP_ICON;
				}
			case BugRank:
				final String rankString = BugRankCategory.getRank(getBugInstance().getBugRank()).name();
				if (GuiResources.GROUP_BY_RANK_ICONS.containsKey(rankString)) {
					return GuiResources.GROUP_BY_RANK_ICONS.get(rankString);
				} else {
					return GuiResources.GROUP_BY_PRIORITY_ICON;
				}
			default:
				return _expandedIcon;
		}
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("BugInstanceGroupNode");
		sb.append("{_childs=").append(_childs);
		sb.append(", _bugInstance=").append(_bugInstance);
		sb.append(", _recurseNodeVisitor=").append(_recurseNodeVisitor);
		sb.append('}');
		return sb.toString();
	}


	private static class ChildComparator implements Comparator<TreeNode>, Serializable {

		private static final long serialVersionUID = 0L;


		@edu.umd.cs.findbugs.annotations.SuppressWarnings(
				value = "BC_UNCONFIRMED_CAST",
				justification = "")
		public int compare(final TreeNode a, final TreeNode b) {
			if (!(a instanceof BugInstanceNode) || !(b instanceof BugInstanceNode)) {
				throw new IllegalArgumentException("argument not instance of BugInstanceNode.");
			}
			return BugInstanceComparator.getBugInstanceClassComparator().compare(((BugInstanceNode) a).getBugInstance(), ((BugInstanceNode) b).getBugInstance());
		}
	}
}
