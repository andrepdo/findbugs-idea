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
package org.twodividedbyzero.idea.findbugs.gui.tree;

import edu.umd.cs.findbugs.BugInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.Bug;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceGroupNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.VisitableTreeNode;

public class RecurseNodeVisitor<T extends VisitableTreeNode> implements NodeVisitor<BugInstanceGroupNode> { // FIXME: rename recurseGroupVisitor

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

	@Override
	public void visitGroupNode(@NotNull final BugInstanceGroupNode node) {
		if (Bug.equalsBugType(_recurseVisitCriteria.getBug(), node.getBug()) && _recurseVisitCriteria.getDepth() == node.getDepth() && _recurseVisitCriteria.getGroupName().equals(node.getGroupName())) {
			_resultNode = node;
		} else {
			_resultNode = node.findChildNode(_recurseVisitCriteria.getBug(), _recurseVisitCriteria.getDepth(), _recurseVisitCriteria.getGroupName());
		}
	}

	public static class RecurseVisitCriteria {

		private final Bug _bug;
		private final int _depth;
		private final String _groupName;

		public RecurseVisitCriteria(final Bug bug, final int depth, final String groupName) {
			_bug = bug;
			_depth = depth;
			_groupName = groupName;
		}

		public Bug getBug() {
			return _bug;
		}

		public BugInstance getBugInstance() {
			return _bug.getInstance();
		}

		public int getDepth() {
			return _depth;
		}

		String getGroupName() {
			return _groupName;
		}
	}
}
