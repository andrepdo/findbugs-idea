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

import org.twodividedbyzero.idea.findbugs.gui.tree.NodeVisitor;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
public abstract class AbstractTreeNode<T extends VisitableTreeNode & TreeNode> extends AbstractNodeDescriptor<T> implements TreeNode {

	private T _parent;


	public abstract void accept(NodeVisitor<? extends VisitableTreeNode> visitor);


	public abstract List<T> getChildsList();


	public void addChild(final T node) {
		getChildsList().add(node);
	}


	public void removeChildAt(final int childIndex) {
		getChildsList().remove(childIndex);
	}


	public void removeChild(final T node) {
		getChildsList().remove(node);
	}


	public void removeAllChilds() {
		getChildsList().clear();
	}


	public void insertChild(final int index, final T node) {
		getChildsList().add(index, node);
	}


	public T getChildAt(final int childIndex) {
		return getChildsList().get(childIndex);
	}


	public int getChildCount() {
		return getChildsList().size();
	}


	public int getIndex(final TreeNode node) {
		return getChildsList().indexOf(node);
	}


	public Enumeration<T> children() {
		return Collections.enumeration(getChildsList());
	}


	public final T getParent() {
		return _parent;
	}


	public void setParent(final T parent) {
		_parent = parent;
	}


	public void setExpandedIcon(final Icon expandedIcon) {
		_expandedIcon = expandedIcon;
	}


	public void setCollapsedIcon(final Icon collapsedIcon) {
		_collapsedIcon = collapsedIcon;
	}


	public void setTooltip(final String tooltip) {
		_tooltip = tooltip;
	}


	@Override
	public abstract String toString();

}
