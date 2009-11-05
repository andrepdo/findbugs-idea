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