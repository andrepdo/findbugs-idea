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

import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.util.ui.SwingHelper;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.plugins.PluginInfo;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import java.awt.BorderLayout;
import java.awt.Font;

final class PluginErrorPane extends JPanel {

	private final JEditorPane htmlViewer;

	PluginErrorPane() {
		super(new BorderLayout());
		setOpaque(true);
		setBackground(MessageType.ERROR.getPopupBackground()); // like com.intellij.webcore.packaging.PackagesNotificationPanel#showError
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final JLabel title = new JLabel();
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		title.setForeground(UIUtil.getTableForeground());
		title.setText(StringUtil.capitalizeWords(ResourcesLoader.getString("plugins.load.error.title"), true));
		add(title, BorderLayout.NORTH);

		htmlViewer = SwingHelper.createHtmlViewer(true, null, null, null);
		htmlViewer.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			protected void hyperlinkActivated(@NotNull final HyperlinkEvent e) {
				final String description = e.getDescription();
				System.out.println(description);
			}
		});
		add(htmlViewer);
	}

	void load(@NotNull final PluginInfo plugin) {
		htmlViewer.setText(UIUtil.toHtml(plugin.errorMessage));
	}
}
