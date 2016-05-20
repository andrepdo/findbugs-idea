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
import edu.umd.cs.findbugs.cloud.Cloud;
import org.dom4j.Document;
import org.dom4j.io.DocumentSource;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.util.ErrorUtil;
import org.twodividedbyzero.idea.findbugs.common.util.FileUtilFb;
import org.twodividedbyzero.idea.findbugs.common.util.IoUtil;
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
			final BugCollection bugCollection = ToolWindowPanel.getInstance(project).getBugCollection();
			enable = bugCollection != null && bugCollection.iterator().hasNext();
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

		final BugCollection bugCollection = ToolWindowPanel.getInstance(project).getBugCollection();

		new Task.Backgroundable(project, ResourcesLoader.getString("export.progress.title"), false) {
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				final boolean withMessages = bugCollection.getWithMessages();
				try {

					FileUtilFb.mkdirs(exportDirPath);
					File finalExportDir = exportDirPath;
					final String currentTime = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.ENGLISH).format(new Date());
					final String uniqueName = "findbugs-result-" + project.getName() + "_" + currentTime;
					String filename = "result";
					if (createSubDir) {
						finalExportDir = new File(exportDirPath, uniqueName);
						FileUtilFb.mkdirs(finalExportDir);
					} else {
						filename = uniqueName;
					}

					bugCollection.setWithMessages(true);
					if (exportXml) {
						final File xml = new File(finalExportDir, filename + ".xml");
						exportXml(bugCollection, xml.getPath());
					}
					if (exportHtml) {
						final File html = new File(finalExportDir, filename + ".html");
						exportHtml(bugCollection, html);
						if (openInBrowser) {
							openInBrowser(html);
						}
					}

				} catch (final Exception e) {
					throw ErrorUtil.toUnchecked(e);
				} finally {
					bugCollection.setWithMessages(withMessages);
				}
			}
		}.queue();
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
