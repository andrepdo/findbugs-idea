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
package org.twodividedbyzero.idea.findbugs.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.HTMLBugReporter;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.cloud.Cloud;
import org.dom4j.Document;
import org.dom4j.io.DocumentSource;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.util.ErrorUtil;
import org.twodividedbyzero.idea.findbugs.common.util.FileUtilFb;
import org.twodividedbyzero.idea.findbugs.common.util.IoUtil;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;
import org.twodividedbyzero.idea.findbugs.core.FindBugsResult;
import org.twodividedbyzero.idea.findbugs.core.FindBugsState;
import org.twodividedbyzero.idea.findbugs.core.WorkspaceSettings;
import org.twodividedbyzero.idea.findbugs.gui.export.ExportBugCollectionDialog;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.ToolWindowPanel;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public final class ExportBugCollection extends AbstractAction {

	private static final Logger LOGGER = Logger.getInstance(ExportBugCollection.class);
	private static final String FINDBUGS_PLAIN_XSL = "plain.xsl";

	@Override
	void updateImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		boolean enable = false;
		if (state.isIdle()) {
			final ToolWindowPanel panel = ToolWindowPanel.getInstance(toolWindow);
			if (panel != null) {
				final FindBugsResult result = panel.getResult();
				enable = result != null && !result.isBugCollectionEmpty();
			}
		}

		e.getPresentation().setEnabled(enable);
		e.getPresentation().setVisible(true);
	}

	@Override
	void actionPerformedImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		final ToolWindowPanel panel = ToolWindowPanel.getInstance(toolWindow);
		if (panel == null) {
			return;
		}

		final ExportBugCollectionDialog dialog = new ExportBugCollectionDialog(project);
		dialog.reset();
		if (!dialog.showAndGet()) {
			return;
		}
		dialog.apply();

		final WorkspaceSettings workspaceSettings = WorkspaceSettings.getInstance(project);
		final String exportDir = workspaceSettings.exportBugCollectionDirectory;
		final boolean exportXml = workspaceSettings.exportBugCollectionAsXml;
		final boolean exportHtml = workspaceSettings.exportBugCollectionAsHtml;
		final boolean createSubDir = workspaceSettings.exportBugCollectionCreateSubDirectory;
		final boolean openInBrowser = workspaceSettings.openExportedHtmlBugCollectionInBrowser;

		if (StringUtil.isEmptyOrSpaces(exportDir)) {
			showError(ResourcesLoader.getString("export.error.emptyPath"));
			return;
		}
		final File exportDirPath = new File(exportDir);
		if (exportDirPath.exists()) {
			if (!exportDirPath.isDirectory()) {
				showError(ResourcesLoader.getString("error.directory.type", exportDirPath));
				return;
			}
		}

		final FindBugsResult result = panel.getResult();

		new Task.Backgroundable(project, ResourcesLoader.getString("export.progress.title"), false) {
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				try {
					FileUtilFb.mkdirs(exportDirPath);
					File finalExportDir = exportDirPath;
					final String currentTime = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.ENGLISH).format(new Date());
					if (createSubDir) {
						final String dirName = "findbugs-result-" + project.getName() + "_" + currentTime;
						finalExportDir = new File(exportDirPath, dirName);
						FileUtilFb.mkdirs(finalExportDir);
					}
					final boolean multiModule = result.getResults().size() > 1;

					for (final Map.Entry<edu.umd.cs.findbugs.Project, SortedBugCollection> entry : result.getResults().entrySet()) {
						final String fileName;
						if (createSubDir) {
							if (multiModule && entry.getKey() instanceof FindBugsProject) {
								fileName = ((FindBugsProject) entry.getKey()).getModule().getName();
							} else {
								fileName = "result";
							}
						} else {
							fileName = "findbugs-result-" + entry.getKey().getProjectName() + "_" + currentTime;
						}
						exportImpl(
								entry.getValue(),
								finalExportDir,
								fileName,
								exportXml,
								exportHtml,
								openInBrowser
						);
					}
				} catch (final Exception e) {
					throw ErrorUtil.toUnchecked(e);
				}
			}
		}.queue();
	}

	private void exportImpl(
			@NotNull final SortedBugCollection bugCollection,
			@NotNull final File exportDir,
			@NotNull final String fileName,
			final boolean exportXml,
			final boolean exportHtml,
			final boolean openInBrowser
	) throws Exception {
		final boolean withMessages = bugCollection.getWithMessages();
		try {
			bugCollection.setWithMessages(true);
			if (exportXml) {
				final File xml = new File(exportDir, fileName + ".xml");
				exportXml(bugCollection, xml.getPath());
			}
			if (exportHtml) {
				final File html = new File(exportDir, fileName + ".html");
				exportHtml(bugCollection, html);
				if (openInBrowser) {
					openInBrowser(html);
				}
			}
		} finally {
			bugCollection.setWithMessages(withMessages);
		}
	}

	private void exportXml(@NotNull final BugCollection bugCollection, @NotNull final String fileName) throws IOException {
		// Issue 77: workaround internal FindBugs NPE
		// As of my point of view, the NPE is a FindBugs bug
		for (final BugInstance bugInstance : bugCollection) {
			if (bugInstance.hasXmlProps()) {
				if (null == bugInstance.getXmlProps().getConsensus()) {
					bugInstance.getXmlProps().setConsensus(Cloud.UserDesignation.UNCLASSIFIED.toString());
				}
			}
		}
		bugCollection.writeXML(fileName);
	}

	private void exportHtml(@NotNull final BugCollection bugCollection, @NotNull final File file) throws IOException, TransformerException {
		final Document document = bugCollection.toDocument();
		final InputStream stylesheet = getStylesheetStream(FINDBUGS_PLAIN_XSL);
		try {
			final Source xsl = new StreamSource(stylesheet);
			xsl.setSystemId(FINDBUGS_PLAIN_XSL);

			// Create a transformer using the stylesheet
			final Transformer transformer = TransformerFactory.newInstance().newTransformer(xsl);

			// Source document is the XML generated from the BugCollection
			final Source source = new DocumentSource(document);

			// Write result to output stream
			final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8").newEncoder());
			try {
				final Result result = new StreamResult(writer);
				// Do the transformation
				transformer.transform(source, result);
			} finally {
				IoUtil.safeClose(writer);
			}
		} finally {
			IoUtil.safeClose(stylesheet);
		}
	}

	private static void openInBrowser(@NotNull final File file) {
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			@Override
			public void run() {
				BrowserUtil.browse(file);
			}
		});
	}

	private static void showError(@NotNull final String message) {
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			@Override
			public void run() {
				Messages.showErrorDialog(message, StringUtil.capitalizeWords(ResourcesLoader.getString("export.title"), true));
			}
		});
	}

	@NotNull
	private static InputStream getStylesheetStream(@NotNull final String stylesheet) throws IOException {
		try {
			final URL url = new URL(stylesheet);
			return url.openStream();
		} catch (final Exception e) {
			LOGGER.info("xls read failed.", e);
		}
		try {
			return new BufferedInputStream(new FileInputStream(stylesheet));
		} catch (final Exception ignored) {
		}
		final InputStream xslInputStream = HTMLBugReporter.class.getResourceAsStream('/' + stylesheet);
		if (xslInputStream == null) {
			throw new IOException("Could not load HTML generation stylesheet " + stylesheet);
		}
		return xslInputStream;
	}
}
