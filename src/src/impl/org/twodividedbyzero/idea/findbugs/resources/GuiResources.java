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
package org.twodividedbyzero.idea.findbugs.resources;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jdesktop.swingx.color.ColorUtil;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.New;

import javax.swing.Icon;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.Color;
import java.util.Collections;
import java.util.Map;

@SuppressWarnings({"HardCodedStringLiteral", "HardcodedFileSeparator"})
public class GuiResources {

	public static final Icon FINDBUGS_ICON = ResourcesLoader.loadIcon("bug.png");
	public static final Icon FINDBUGS_ICON_13X13 = ResourcesLoader.loadIcon("bug13x13.png");
	public static final Icon FINDBUGS_CONFIGURATION_ICON = ResourcesLoader.loadIcon("smallBuggy.png");
	public static final Icon FINDBUGS_EXECUTE_ICON = ResourcesLoader.loadIcon("actions/bug18x18.png");
	public static final Icon FINDBUGS_CLOUD_ICON = ResourcesLoader.loadIcon("bug_cloud.png");

	public static final Icon CLOSE_EDITOR_ICON = ResourcesLoader.loadIcon("close.png");
	public static final Icon CLOSE_EDITOR_HOVER_ICON = ResourcesLoader.loadIcon("closeHovered.png");

	public static final Color HIGH_PRIORITY_COLOR = JBColor.RED;
	public static final Color MIDDLE_PRIORITY_COLOR = JBColor.YELLOW;
	public static final Color LOW_PRIORITY_COLOR = JBColor.GREEN;
	public static final Color EXP_PRIORITY_COLOR = JBColor.BLACK;

	public static final Icon PRIORITY_HIGH_ICON = ResourcesLoader.loadIcon("priority/bug_high.png");
	public static final Icon PRIORITY_NORMAL_ICON = ResourcesLoader.loadIcon("priority/bug_normal.png");
	public static final Icon PRIORITY_LOW_ICON = ResourcesLoader.loadIcon("priority/bug_low.png");
	public static final Icon PRIORITY_EXP_ICON = ResourcesLoader.loadIcon("priority/bug_exp.png");

	/**
	 * --------------------------------------------------------------------------------------------------
	 * Grouping icons
	 */
	public static final Icon GROUP_BY_CATEGORY_ICON = ResourcesLoader.loadIcon("actions/groupByBugCategory.png");
	public static final Icon GROUP_BY_CLASS_ICON = ResourcesLoader.loadIcon("actions/groupByClass.png");
	public static final Icon GROUP_BY_RANK_ICON = ResourcesLoader.loadIcon("actions/groupByRank.png");
	public static final Icon GROUP_BY_PRIORITY_ICON = ResourcesLoader.loadIcon("actions/groupByPriority.png");
	public static final Icon GROUP_BY_PACKAGE_ICON = ResourcesLoader.loadIcon("actions/groupByPackage.png");

	public static final Icon GROUP_BY_RANK_SCARIEST_ICON = ResourcesLoader.loadIcon("priority/rankScariest.png");
	public static final Icon GROUP_BY_RANK_SCARY_ICON = ResourcesLoader.loadIcon("priority/rankScary.png");
	public static final Icon GROUP_BY_RANK_TROUBLING_ICON = ResourcesLoader.loadIcon("priority/rankTroubling.png");
	public static final Icon GROUP_BY_RANK_OF_CONCERN_ICON = ResourcesLoader.loadIcon("priority/rankOfConcern.png");
	public static final Map<String, Icon> GROUP_BY_RANK_ICONS;

	static {
		final Map<String, Icon> groupByRankIcons = New.map(5);
		groupByRankIcons.put("SCARIEST", GROUP_BY_RANK_SCARIEST_ICON);
		groupByRankIcons.put("SCARY", GROUP_BY_RANK_SCARY_ICON);
		groupByRankIcons.put("TROUBLING", GROUP_BY_RANK_TROUBLING_ICON);
		groupByRankIcons.put("OF_CONCERN", GROUP_BY_RANK_OF_CONCERN_ICON);
		groupByRankIcons.put("OF CONCERN", GROUP_BY_RANK_OF_CONCERN_ICON);
		GROUP_BY_RANK_ICONS = Collections.unmodifiableMap(groupByRankIcons);
	}

	public static final Icon GROUP_BY_PRIORITY_HIGH_ICON = ResourcesLoader.loadIcon("priority/priorityHigh.png");
	public static final Icon GROUP_BY_PRIORITY_MEDIUM_ICON = ResourcesLoader.loadIcon("priority/priorityMedium.png");
	public static final Icon GROUP_BY_PRIORITY_LOW_ICON = ResourcesLoader.loadIcon("priority/priorityLow.png");
	public static final Icon GROUP_BY_PRIORITY_EXP_ICON = ResourcesLoader.loadIcon("priority/priorityExp.png");
	public static final Icon GROUP_BY_PRIORITY_IGNORE_ICON = ResourcesLoader.loadIcon("priority/priorityIgnore.png");
	public static final Map<String, Icon> GROUP_BY_PRIORITY_ICONS;


	static {
		final Map<String, Icon> groupByPriorityIcons = New.map(5);
		groupByPriorityIcons.put("Low", GROUP_BY_PRIORITY_LOW_ICON);
		groupByPriorityIcons.put("Medium", GROUP_BY_PRIORITY_MEDIUM_ICON);
		groupByPriorityIcons.put("High", GROUP_BY_PRIORITY_HIGH_ICON);
		groupByPriorityIcons.put("Exp", GROUP_BY_PRIORITY_EXP_ICON);
		groupByPriorityIcons.put("Ignore", GROUP_BY_PRIORITY_IGNORE_ICON); // this should never happen
		GROUP_BY_PRIORITY_ICONS = Collections.unmodifiableMap(groupByPriorityIcons);
	}


	/**
	 * --------------------------------------------------------------------------------------------------
	 * Tree icons
	 */
	public static final Icon TREENODE_OPEN_ICON = AllIcons.Nodes.TreeOpen;
	public static final Icon TREENODE_CLOSED_ICON = AllIcons.Nodes.TreeClosed;


	/**
	 * --------------------------------------------------------------------------------------------------
	 * Navigation icons
	 */
	public static final Icon NAVIGATION_MOVEUP_ICON = AllIcons.Actions.MoveUp;
	public static final Icon NAVIGATION_MOVEDOWN_ICON = AllIcons.Actions.MoveDown;

	private static final StyleSheet EDITORPANE_STYLESHEET;

	static {

		final String black = ColorUtil.toHexString(JBColor.black);
		final String gray = ColorUtil.toHexString(JBColor.gray);
		final String blue = ColorUtil.toHexString(JBColor.blue);
		final String green = ColorUtil.toHexString(JBColor.green);
		final String yellow = ColorUtil.toHexString(JBColor.yellow);
		final String red = ColorUtil.toHexString(JBColor.red);
		final String cremeWhite = ColorUtil.toHexString(new JBColor(new Color(0x005555), JBColor.green));
		final String fontColor = UIUtil.isUnderDarcula() ? "#bbbbbb" : ColorUtil.toHexString(JBColor.black);
		final int fontSize = JBUI.isHiDPI() ? 24 : 12;
		final int h1FontSize = JBUI.isHiDPI() ? 32 : 16;
		final int h2FontSize = JBUI.isHiDPI() ? 28 : 14;

		EDITORPANE_STYLESHEET = new StyleSheet();
		EDITORPANE_STYLESHEET.addRule("body {font-size: " + fontSize + "pt; color: " + fontColor + "}");
		EDITORPANE_STYLESHEET.addRule("p {margin-top:4px;margin-bottom:8px;}");
		EDITORPANE_STYLESHEET.addRule("code {font-family: courier; font-size: " + fontSize + "pt; background-color:#f5f5f5;padding:4px;}");
		EDITORPANE_STYLESHEET.addRule("pre {color: " + gray + "; font-family: courier; font-size: " + fontSize + "pt; padding:9px; background-color:#f5f5f5; border:1px solid #cccccc;color:#333333;}");
		EDITORPANE_STYLESHEET.addRule("blockquote {padding: 10px 20px; margin: 0 0 20px; border-left: 5px solid #bbbbbb;");
		EDITORPANE_STYLESHEET.addRule("a {color: " + blue + "; font-decoration: underline}");
		EDITORPANE_STYLESHEET.addRule("li {margin-left: 10px; list-style-type: none}");
		EDITORPANE_STYLESHEET.addRule("#Low {background-color: " + green + "; width: 15px; height: 15px;}");
		EDITORPANE_STYLESHEET.addRule("#Medium {background-color: " + yellow + "; width: 15px; height: 15px;}");
		EDITORPANE_STYLESHEET.addRule("#High {background-color: " + red + "; width: 15px; height: 15px;}");
		EDITORPANE_STYLESHEET.addRule("#Exp {background-color: " + black + "; width: 15px; height: 15px;}");
		EDITORPANE_STYLESHEET.addRule("H1 {color: " + cremeWhite + ";  font-size: " + h1FontSize + "pt; font-weight: bold;}");
		EDITORPANE_STYLESHEET.addRule("H1 a {color: " + cremeWhite + ";  font-size: " + h1FontSize + "pt; font-weight: bold;}");
		EDITORPANE_STYLESHEET.addRule("H2, .fakeH2 {color: " + cremeWhite + ";  font-size: " + h2FontSize + "pt; font-weight: bold;}");
		EDITORPANE_STYLESHEET.addRule("H3 {color: " + cremeWhite + ";  font-size: " + fontSize + "pt; font-weight: bold;}");
	}

	@NotNull
	public static HTMLEditorKit createHtmlEditorKit() {
		return new HTMLEditorKit() {
			@Override
			public StyleSheet getStyleSheet() {
				return GuiResources.EDITORPANE_STYLESHEET;
			}
		};
	}

	public static final Color HIGHLIGHT_COLOR = new JBColor(new Color(219, 219, 137), new Color(189, 189, 120));
	public static final Color HIGHLIGHT_COLOR_DARKER = new JBColor(new Color(135, 135, 69, 254), new Color(112, 112, 56, 254));
	public static final Color HIGHLIGHT_COLOR_LIGHTER = new JBColor(new Color(255, 255, 204), new Color(86, 86, 43, 254));


	private GuiResources() {
	}
}
