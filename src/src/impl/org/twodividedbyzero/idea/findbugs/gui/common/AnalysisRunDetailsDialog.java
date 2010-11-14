/*
 * Copyright 2010 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.List;

/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
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

		html.append("<p><h3>Configured Output Files").append(" <font color='gray'>(").append(configuredOutputFiles.length).append(")</h3></p>");
		html.append("<ul>");
		for (final VirtualFile file : configuredOutputFiles) {
			html.append("<li>");
			html.append(file.getPresentableName());
			html.append("</li>");
		}
		html.append("</ul>");

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
		final JEditorPane jEditorPane = new JEditorPane();
		jEditorPane.setPreferredSize(new Dimension(550, 650));
		jEditorPane.setEditable(false);
		jEditorPane.setContentType("text/html");
		jEditorPane.setEditorKit(htmlEditorKit);


		jEditorPane.setText(html.toString());

		panel.add(new JScrollPane(jEditorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
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