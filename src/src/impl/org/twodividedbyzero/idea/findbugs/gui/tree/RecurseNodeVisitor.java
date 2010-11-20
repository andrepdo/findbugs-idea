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
package org.twodividedbyzero.idea.findbugs.gui.tree;

import edu.umd.cs.findbugs.BugInstance;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceGroupNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.VisitableTreeNode;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
public class RecurseNodeVisitor<T extends VisitableTreeNode> implements NodeVisitor<BugInstanceGroupNode> { // todo: rename recurseGroupVisitor

	private final T _startNode;
	private BugInstanceGroupNode _resultNode;
	private RecurseVisitCriteria _recurseVisitCriteria;


	public RecurseNodeVisitor(final T startNode) {
		_startNode = startNode;
	}


	@Nullable
	public BugInstanceGroupNode findChildNode(final RecurseVisitCriteria recurseVisitCriteria) {
		_recurseVisitCriteria = recurseVisitCriteria;

		for (final VisitableTreeNode node : _startNode.getChildsList()) {
			//if (node instanceof BugInstanceGroupNode) {
			//((BugInstanceGroupNode) node).accept(this);
			//((VisitableTreeNode) node).accept(this);
			node.accept(this);

			if (_resultNode != null) {
				break;
			}
			//}
		}

		return _resultNode;
	}


	public void visitGroupNode(final BugInstanceGroupNode node) {
		if (_recurseVisitCriteria.getBugInstance().equals(node.getBugInstance()) && _recurseVisitCriteria.getDepth() == node.getDepth() && _recurseVisitCriteria.getGroupName().equals(node.getGroupName())) {
			_resultNode = node;
		} else {
			_resultNode = node.findChildNode(_recurseVisitCriteria.getBugInstance(), _recurseVisitCriteria.getDepth(), _recurseVisitCriteria.getGroupName());
		}
	}


	public static class RecurseVisitCriteria {

		private final BugInstance _bugInstance;
		private final int _depth;
		private final String _groupName;


		public RecurseVisitCriteria(final BugInstance bugInstance, final int depth, final String groupName) {
			_bugInstance = bugInstance;
			_depth = depth;
			_groupName = groupName;
		}


		public BugInstance getBugInstance() {
			return _bugInstance;
		}


		public int getDepth() {
			return _depth;
		}


		public String getGroupName() {
			return _groupName;
		}
	}
}