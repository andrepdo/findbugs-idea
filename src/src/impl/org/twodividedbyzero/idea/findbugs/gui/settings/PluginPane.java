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

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.StringUtilFb;
import org.twodividedbyzero.idea.findbugs.plugins.PluginInfo;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

final class PluginPane extends JPanel {

	private JLabel title;
	private JLabel id;
	private JLabel text;
	private JLabel website;
	private JLabel url;
	private JPanel bottom;

	PluginPane() {
		super(new BorderLayout());
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));

		title = new JLabel();
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		title.setForeground(UIUtil.getTableForeground());

		id = new JLabel();
		id.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));
		id.setForeground(UIUtil.getTableForeground());

		text = new JLabel();
		text.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));
		text.setForeground(UIUtil.getTableForeground());

		// LATER: HyperlinkLabel does not work in cell
		website = new JLabel();
		website.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));
		website.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 5));
		website.setForeground(UIUtil.getTableForeground());

		url = new JLabel();
		url.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 5));
		url.setFont(UIUtil.getLabelFont(UIUtil.FontSize.MINI));
		url.setForeground(UIUtil.getTableForeground());

		final JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		top.setOpaque(false);
		top.add(title);
		top.add(id);

		bottom = new JPanel(new BorderLayout());
		bottom.setOpaque(false);
		bottom.add(website, BorderLayout.NORTH);
		bottom.add(url, BorderLayout.SOUTH);

		add(top, BorderLayout.NORTH);
		add(text);
		add(bottom, BorderLayout.SOUTH);
	}

	void load(@NotNull final PluginInfo plugin) {
		title.setText(plugin.shortDescription);
		id.setText("(" + plugin.settings.id + ")");
		text.setText(normalize(plugin.detailedDescription));
		bottom.remove(website);
		if (!StringUtil.isEmptyOrSpaces(plugin.website)) {
			bottom.add(website, BorderLayout.NORTH);
			website.setVisible(true);
			website.setText(plugin.website);
		}
		url.setText(plugin.settings.url);
	}

	@NotNull
	private static String normalize(@Nullable String text) {
		if (text == null) {
			return "No Text";
		}
		if (text.startsWith("<p>") && text.endsWith("</p>")) {
			text = text.substring("<p>".length());
			text = text.substring(0, text.length() - "</p>".length());
		}
		return StringUtilFb.trim(text, '\r', '\n', '\t');
	}
}
