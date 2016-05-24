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

import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.ide.ui.search.SearchUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.BrowserHyperlinkListener;
import com.intellij.ui.HintHint;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.ui.UIUtil;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.StringUtilFb;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Point;
import java.io.StringReader;
import java.util.Locale;
import java.util.Set;

final class DetectorDetailsPane extends JPanel {
	@NonNls
	private static final String EMPTY_HTML = "<html><body></body></html>";

	private JScrollPane scrollPane;
	private JEditorPane description;
	private DetectorTableHeaderPane headerPane;

	private String latest;

	DetectorDetailsPane() {
		super(new BorderLayout());
		setBorder(IdeBorderFactory.createTitledBorder(
				ResourcesLoader.getString("detector.description.title"), false, new Insets(2, 0, 0, 0)));

		description = new JEditorPane(UIUtil.HTML_MIME, EMPTY_HTML);
		description.setEditable(false);
		description.setBorder(IdeBorderFactory.createEmptyBorder(5, 5, 5, 5));
		description.addHyperlinkListener(BrowserHyperlinkListener.INSTANCE);

		scrollPane = ScrollPaneFactory.createScrollPane(description);
		add(scrollPane);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		scrollPane.setEnabled(enabled);
		description.setEnabled(enabled);
	}

	void setHeaderPane(@NotNull final DetectorTableHeaderPane headerPane) {
		this.headerPane = headerPane;
	}

	void reload() {
		try {
			String filter = headerPane.getFilter();
			String text = latest;
			if (!StringUtil.isEmptyOrSpaces(filter)) {
				filter = filter.toLowerCase(Locale.US);
				text = SearchUtil.markup(text, filter);
			}
			description.read(new StringReader(text), null);
		} catch (final Exception ignored) {
			Logger.getInstance(DetectorTab.class).error("Could not load HTML:\n" + latest, ignored);
		}
	}

	void load(@Nullable final DetectorFactory detector) {
		try {
			latest = null;
			setLatest(detector);
		} catch (final Exception ignored) {
			Logger.getInstance(DetectorTab.class).error("Could not load HTML:\n" + latest, ignored);
		}
		reload();
	}

	private void setLatest(@Nullable final DetectorFactory detector) throws Exception {
		final String html = detector != null ? detector.getDetailHTML() : EMPTY_HTML;
		String body = null;
		int pos = html.indexOf("<BODY>");
		if (-1 != pos) {
			body = html.substring(pos + "<BODY>".length());
			pos = body.indexOf("</BODY>");
			if (-1 != pos) {
				body = body.substring(0, pos);
				body = StringUtilFb.trim(body, ' ', '\n', '\r', '\t');
				if (body.startsWith("<p>")) {
					body = body.substring("<p>".length());
					body = StringUtilFb.trim(body, ' ', '\n', '\r', '\t');
					if (body.endsWith("<p></p>")) {
						body = body.substring(0, body.length() - "<p></p>".length());
					}
					body = StringUtilFb.trim(body, ' ', '\n', '\r', '\t');
					if (body.endsWith("</p>")) {
						body = body.substring(0, body.length() - "</p>".length());
					}
				}
				body = StringUtilFb.trim(body, ' ', '\n', '\r', '\t');
			} else {
				body = null;
			}
		}

		if (detector != null && body != null) {
			final StringBuilder s = new StringBuilder();
			s.append("\n\n<table border=1>");
			s.append("<tr><td>").append(ResourcesLoader.getString("detector.details.plugin")).append("</td><td>").append(detector.getPlugin().getShortDescription()).append("</td></tr>");
			s.append("<tr><td>").append(ResourcesLoader.getString("detector.details.pluginId")).append("</td><td>").append(detector.getPlugin().getPluginId()).append("</td></tr>");
			s.append("<tr><td>").append(ResourcesLoader.getString("detector.details.detectorName")).append("</td><td>").append(detector.getShortName()).append("</td></tr>");
			s.append("</table>");
			final Set<BugPattern> bugPatterns = detector.getReportedBugPatterns();
			if (bugPatterns.isEmpty()) {
				s.append("\n<b>").append(ResourcesLoader.getString("detector.details.noReportedPatterns")).append("</b>");
			} else {
				s.append("\n").append(ResourcesLoader.getString("detector.details.reportedPatterns")).append(":");
				s.append("<table border=1>");
				for (final BugPattern pattern : bugPatterns) {
					s.append("<tr>")
							.append("<td>").append(pattern.getCategory()).append("</td>")
							.append("<td>").append(pattern.getAbbrev()).append("</td>")
							.append("<td>").append(pattern.getType()).append("</td>")
							.append("<td>").append(pattern.getShortDescription()).append("</td>")
							.append("</tr>");

				}
				s.append("</table>");
			}
			body += s.toString();
		}

		final HintHint hintHint = new HintHint(description, new Point(0, 0));
		hintHint.setFont(UIUtil.getLabelFont());
		latest = HintUtil.prepareHintText(body != null ? body : html, hintHint);

	}
}
