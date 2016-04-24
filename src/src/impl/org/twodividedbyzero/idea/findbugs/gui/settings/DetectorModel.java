/*
 * Copyright 2016 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.gui.settings;

import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.gui.common.TreeState;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

final class DetectorModel extends DefaultTreeModel implements TreeTableModel {
	private final static int TREE_COLUMN = 0;
	final static int IS_ENABLED_COLUMN = 1;

	private JTree tree;

	DetectorModel(@NotNull final AbstractDetectorNode root) {
		super(root);
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(final int column) {
		return null;
	}

	@Override
	public Class getColumnClass(final int column) {
		switch (column) {
			case TREE_COLUMN:
				return TreeTableModel.class;
			case IS_ENABLED_COLUMN:
				return Boolean.class;
			default:
				throw new IllegalArgumentException("Column " + column);
		}
	}

	@Override
	public Object getValueAt(final Object aNode, final int column) {
		final AbstractDetectorNode node = (AbstractDetectorNode) aNode;
		switch (column) {
			case TREE_COLUMN:
				return null;
			case IS_ENABLED_COLUMN:
				return node.getEnabled();
			default:
				throw new IllegalArgumentException("Column " + column);
		}
	}

	@Override
	public boolean isCellEditable(final Object node, final int column) {
		return column == IS_ENABLED_COLUMN;
	}

	@Override
	public void setValueAt(final Object aValue, final Object aNode, final int column) {
		final AbstractDetectorNode node = (AbstractDetectorNode) aNode;
		if (column == IS_ENABLED_COLUMN) {
			node.setEnabled((Boolean) aValue);

			final TreeState treeState = TreeState.create(tree);
			nodeStructureChanged((AbstractDetectorNode) tree.getModel().getRoot());
			treeState.restore();
		}
	}

	@Override
	public void setTree(final JTree tree) {
		this.tree = tree;
	}
}
