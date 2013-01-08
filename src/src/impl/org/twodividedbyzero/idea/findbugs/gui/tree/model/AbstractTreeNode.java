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
package org.twodividedbyzero.idea.findbugs.gui.tree.model;

import org.jetbrains.annotations.Nullable;
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
@SuppressWarnings({"UnusedDeclaration"})
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


	void setParent(@Nullable final T parent) {
		_parent = parent;
	}


	void setExpandedIcon(final Icon expandedIcon) {
		_expandedIcon = expandedIcon;
	}


	void setCollapsedIcon(final Icon collapsedIcon) {
		_collapsedIcon = collapsedIcon;
	}


	void setTooltip(final String tooltip) {
		_tooltip = tooltip;
	}


	@Override
	public abstract String toString();

}
