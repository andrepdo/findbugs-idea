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

package org.twodividedbyzero.idea.findbugs.gui.common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import edu.umd.cs.findbugs.ProjectStats;
import org.twodividedbyzero.idea.findbugs.common.VersionManager;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsUtil;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.Serializable;
import java.util.List;

/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
@SuppressWarnings({"HardcodedFileSeparator"})
public class AnalysisRunDetailsDialog implements Serializable {

	private static final long serialVersionUID = 0L;


	private AnalysisRunDetailsDialog() {
	}


	public static DialogBuilder create(final Project project, final int bugCount, final ProjectStats projectStats, final FindBugsProject bugsProject) {
		final int numAnalysedClasses = projectStats.getNumClasses();

		final List<String> fileList = bugsProject.getFileList();
		final List<String> auxClasspathEntries = bugsProject.getAuxClasspathEntryList();
		final VirtualFile[] configuredOutputFiles = bugsProject.getConfiguredOutputFiles();
		//bugsProject.get

		final StringBuilder html = new StringBuilder();
		html.append("<html><body>");
		html.append("<p><h2>").append(VersionManager.getName()).append(": <b>found ").append(bugCount).append(" bugs in ").append(numAnalysedClasses).append(numAnalysedClasses > 1 ? " classes" : " class").append("</b>").append("</h2></p>");
		html.append("<p>").append("<font size='10px'>using ").append(VersionManager.getFullVersion()).append(" with Findbugs version ").append(FindBugsUtil.getFindBugsFullVersion()).append("</font>").append("</p>");

		if (configuredOutputFiles.length > 0) {
			html.append("<p><h3>Configured Output Files/Paths - the analysis entry point").append(" <font color='gray'>(").append(configuredOutputFiles.length).append(")</h3></p>");
			html.append("<ul>");
			for (final VirtualFile file : configuredOutputFiles) {
				html.append("<li>");
				html.append(file.getPath());
				html.append("</li>");
			}
			html.append("</ul>");
		}

		html.append("<p><h3>Compile Output Path").append(" <font size='9px' color='gray'>(").append("IDEA)</h3></p>");
		html.append("<ul>");
		for (final String resolved : bugsProject.getResolvedSourcePaths()) {
			html.append("<li>");
			html.append(resolved);
			html.append("</li>");
		}
		html.append("</ul>");


		html.append("<p><h3>Sources Dir List ").append(" <font size='9px' color='gray'>(").append(bugsProject.getNumSourceDirs()).append(")</h3></p>");
		html.append("<ul>");
		final List<String> sourceDirList = bugsProject.getSourceDirList();
		for (final String sourceDir : sourceDirList) {
			html.append("<li>");
			html.append(sourceDir);
			html.append("</li>");
		}
		html.append("</ul>");


		html.append("<p><h3>Analyzed Classes List ").append(" <font size='9px' color='gray'>(").append(bugsProject.getFileCount()).append(")</h3></p>");
		html.append("<ul>");
		for (final String file : fileList) {
			html.append("<li>");
			html.append(file);
			html.append("</li>");
		}
		html.append("</ul>");

		html.append("<p><h3>Aux Classpath Entries ").append(" <font size='9px' color='gray'>(").append(bugsProject.getNumAuxClasspathEntries()).append(")</h3></p>");
		html.append("<ul>");
		for (final String auxClasspathEntry : auxClasspathEntries) {
			html.append("<li>");
			html.append(auxClasspathEntry);
			html.append("</li>");
		}
		html.append("</ul>");

		html.append("</html></body>");


		final DialogBuilder dialogBuilder = new DialogBuilder(project);
		dialogBuilder.addCloseButton();
		dialogBuilder.setTitle("FindBugs analysis settings");
		final JComponent panel = new JPanel(new BorderLayout());
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		final HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
		htmlEditorKit.setStyleSheet(GuiResources.EDITORPANE_STYLESHEET);
		final JEditorPane jEditorPane = new JEditorPane() {

			@Override
			protected void paintComponent(final Graphics g) {
				super.paintComponent(g);
				final Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			}

		};
		jEditorPane.setPreferredSize(new Dimension(550, 650));
		jEditorPane.setEditable(false);
		jEditorPane.setContentType("text/html");
		jEditorPane.setEditorKit(htmlEditorKit);


		jEditorPane.setText(html.toString());

		panel.add(ScrollPaneFacade.createScrollPane(jEditorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		panel.setBorder(BorderFactory.createTitledBorder("FindBugs analysis run configuration"));
		dialogBuilder.setCenterPanel(panel);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jEditorPane.scrollRectToVisible(new Rectangle(0, 0));
			}
		});

		return dialogBuilder;
	}
}