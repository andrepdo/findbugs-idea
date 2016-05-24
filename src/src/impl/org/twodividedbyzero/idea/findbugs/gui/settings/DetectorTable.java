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

import com.intellij.profile.codeInspection.ui.inspectionsTree.InspectionsConfigTreeTable;
import com.intellij.profile.codeInspection.ui.table.ThreeStateCheckBoxRenderer;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.text.Matcher;
import com.intellij.util.ui.UIUtil;
import edu.umd.cs.findbugs.BugPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JTree;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.Color;
import java.awt.Component;

/**
 * Based on inspection code ({@link com.intellij.profile.codeInspection.ui.SingleInspectionProfilePanel}) and
 * plugin table ({@link com.intellij.ide.plugins.PluginsTableRenderer}).
 */
final class DetectorTable extends TreeTable {

	private BugCategoryPane bugCategory;
	private DetectorTableHeaderPane headerPane;

	DetectorTable(@NotNull final DetectorModel model) {
		super(model);

		final DefaultTreeCellRenderer treeColumnRenderer = new DefaultTreeCellRenderer();
		treeColumnRenderer.setTextNonSelectionColor(JBColor.gray);
		setTreeCellRenderer(new TreeCellRenderer() {
			private SimpleColoredComponent label = new SimpleColoredComponent();

			@Override
			public Component getTreeCellRendererComponent(
					final JTree tree,
					final Object value,
					final boolean selected,
					final boolean expanded,
					final boolean leaf,
					final int row,
					final boolean hasFocus
			) {

				final AbstractDetectorNode node = (AbstractDetectorNode) value;
				label.clear();
				final boolean reallyHasFocus = ((TreeTableTree) tree).getTreeTable().hasFocus();
				final Color background = selected ? (reallyHasFocus ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeUnfocusedSelectionBackground())
						: UIUtil.getTreeTextBackground();
				UIUtil.changeBackGround(label, background);

				boolean gray = false;
				if (!selected) {
					if (bugCategory != null && !node.isGroup()) {
						gray = true;
						final DetectorNode detectorNode = (DetectorNode) node;
						for (final BugPattern pattern : detectorNode.getDetector().getReportedBugPatterns()) {
							if (bugCategory.isEnabled(pattern.getCategory())) {
								gray = false;
								break;
							}
						}
					}
				}
				final String text = node.toString();
				final String filter = headerPane.getFilter();
				final SimpleTextAttributes attributes = gray ? SimpleTextAttributes.GRAYED_ATTRIBUTES : SimpleTextAttributes.REGULAR_ATTRIBUTES;
				final Matcher matcher = NameUtil.buildMatcher("*" + filter, NameUtil.MatchingCaseSensitivity.NONE);
				SpeedSearchUtil.appendColoredFragmentForMatcher(text, label, attributes, matcher, UIUtil.getTableBackground(selected), true);
				return label;
			}
		});

		final TableColumn isEnabledColumn = getColumnModel().getColumn(DetectorModel.IS_ENABLED_COLUMN);
		isEnabledColumn.setMaxWidth(20 + InspectionsConfigTreeTable.getAdditionalPadding());
		isEnabledColumn.setCellRenderer(new ThreeStateCheckBoxRenderer());
		isEnabledColumn.setCellEditor(new ThreeStateCheckBoxRenderer());
	}

	void setHeaderPane(@NotNull final DetectorTableHeaderPane headerPane) {
		this.headerPane = headerPane;
	}

	void setBugCategory(@Nullable final BugCategoryPane bugCategory) {
		this.bugCategory = bugCategory;
	}
}
