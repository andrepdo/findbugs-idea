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

package org.twodividedbyzero.idea.findbugs.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.HTMLBugReporter;
import org.dom4j.Document;
import org.dom4j.io.DocumentSource;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.event.EventListener;
import org.twodividedbyzero.idea.findbugs.common.event.EventManagerImpl;
import org.twodividedbyzero.idea.findbugs.common.event.filters.BugReporterEventFilter;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.gui.common.ExportFileDialog;
import org.twodividedbyzero.idea.findbugs.tasks.BackgroundableTask;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.EventQueue;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @author Keith Lea <keithl@gmail.com>
 * @version $Revision$
 * @since 0.9.95
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class ExportBugCollection extends BaseAction implements EventListener<BugReporterEvent> {

	private static final Logger LOGGER = Logger.getInstance(ExportBugCollection.class.getName());

	private static String _exportDir = System.getProperty("user.home") + File.separatorChar + FindBugsPluginConstants.TOOL_WINDOW_ID;

	private static final String FINDBUGS_PLAIN_XSL = "plain.xsl";
	private static final String FINDBUGS_RESULT_PREFIX = "FindBugsResult_";
	private static final String FINDBUGS_RESULT_HTML_SUFFIX = ".html";
	private static final String FINDBUGS_RESULT_RAW_SUFFIX = ".xml";

	private boolean _enabled;
	private BugCollection _bugCollection;
	private boolean _running;


	@Override
	protected boolean isEnabled() {
		return _enabled;
	}


	@Override
	protected boolean setEnabled(final boolean enabled) {
		final boolean was = _enabled;
		if (_enabled != enabled) {
			_enabled = enabled;
		}
		return was;
	}


	@SuppressWarnings({"AssignmentToStaticFieldFromInstanceMethod"})
	@Override
	public void actionPerformed(final AnActionEvent e) {
		final Project project = IdeaUtilImpl.getProject(e.getDataContext());
		assert project != null;
		final Presentation presentation = e.getPresentation();

		// check a project is loaded
		if (isProjectLoaded(project, presentation)) {
			Messages.showWarningDialog("Project not loaded.", "FindBugs");  // NON-NLS
			return;
		}

		//Ask the user for a export directory
		final DialogBuilder dialogBuilder = new DialogBuilder(project);
		dialogBuilder.addOkAction();
		dialogBuilder.addCancelAction();
		dialogBuilder.setTitle("Select directory to save the exported file");
		final ExportFileDialog exportDialog = new ExportFileDialog(_exportDir, dialogBuilder);
		dialogBuilder.showModal(true);
		if (dialogBuilder.getDialogWrapper().getExitCode() == DialogWrapper.CANCEL_EXIT_CODE) {
			return;
		}
		final String path = exportDialog.getText();
		if (path == null || path.trim().length() == 0) {
			return;
		}
		final boolean exportXml = exportDialog.isXml();
		_exportDir = path.trim();
		//Create a unique file name by using time stamp
		final String timestamp = new SimpleDateFormat().format(new Date()).replaceAll("[/ :]", "_");
		final String fileName = _exportDir + File.separatorChar + FINDBUGS_RESULT_PREFIX + timestamp + (exportXml ? FINDBUGS_RESULT_RAW_SUFFIX : FINDBUGS_RESULT_HTML_SUFFIX);

		createExportDirIfAbsent();

		//Create a task to export the bug collection to html
		final AtomicReference<Task> exportTask = new AtomicReference<Task>(new BackgroundableTask(project, "Exporting Findbugs Result", false) {
			private ProgressIndicator _indicator;


			@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				indicator.setText2(fileName);
				setProgressIndicator(indicator);
				FileWriter writer = null;
				try {
					if (_bugCollection != null) {
						_bugCollection.setWithMessages(true);
						if (exportXml) {
							_bugCollection.writeXML(fileName);
						} else {
							final Document document = _bugCollection.toDocument();
							final Source xsl = new StreamSource(getStylesheetStream(FINDBUGS_PLAIN_XSL));
							xsl.setSystemId(FINDBUGS_PLAIN_XSL);

							// Create a transformer using the stylesheet
							final Transformer transformer = TransformerFactory.newInstance().newTransformer(xsl);

							// Source document is the XML generated from the BugCollection
							final Source source = new DocumentSource(document);

							// Write result to output stream
							writer = new FileWriter(fileName);
							final Result result = new StreamResult(writer);

							// Do the transformation
							transformer.transform(source, result);
						}
						_bugCollection.setWithMessages(false);

						showToolWindowNotifier("Exported bug collection to " + fileName + '.', MessageType.INFO);
						if(!exportXml) {
							BrowserUtil.launchBrowser(new File(fileName).getAbsolutePath());
						}
					}
				} catch (IOException e1) {
					final String message = "Export failed";
					showToolWindowNotifier(message, MessageType.ERROR);
					LOGGER.error(message, e1);
				} catch (TransformerConfigurationException e1) {
					final String message = "Transform to html failed due to configuration problems.";
					showToolWindowNotifier(message, MessageType.ERROR);
					LOGGER.error(message, e1);
				} catch (TransformerException e1) {
					final String message = "Transformation to exportXml failed.";
					showToolWindowNotifier(message, MessageType.ERROR);
					LOGGER.error(message, e1);
				} finally {
					if (writer != null) {
						try {
							writer.close();
						} catch (IOException ignored) {
						}
					}
				}
			}


			@Override
			public void setProgressIndicator(@NotNull final ProgressIndicator indicator) {
				_indicator = indicator;
			}


			@Override
			public ProgressIndicator getProgressIndicator() {
				return _indicator;
			}
		});

		final File file = new File(fileName);
		if (file.getParentFile() == null) {
			showToolWindowNotifier("Exporting bug collection failed. not a directory. " + fileName + '.', MessageType.ERROR);
		} else {
			exportTask.get().queue();
		}
	}


	private static void createExportDirIfAbsent() {
		final File exportDir = new File(_exportDir);
		if(!exportDir.exists()) {
			if(!exportDir.mkdir()) {
				final String message = "Creating the export directory '" + _exportDir + "' failed.";
				showToolWindowNotifier(message, MessageType.ERROR);
				LOGGER.error(message);
			}
		}
	}


	private static void showToolWindowNotifier(final String message, final MessageType type) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				FindBugsPluginImpl.showToolWindowNotifier(message, type);
			}
		});
	}


	private static InputStream getStylesheetStream(final String stylesheet) throws IOException {
		try {
			final URL url = new URL(stylesheet);
			return url.openStream();
		} catch (Exception e) {
			LOGGER.info("xls read failed.", e);
		}
		try {
			return new BufferedInputStream(new FileInputStream(stylesheet));
		} catch (Exception ignored) {
		}
		final InputStream xslInputStream = HTMLBugReporter.class.getResourceAsStream("/" + stylesheet);
		if (xslInputStream == null) {
			throw new IOException("Could not load HTML generation stylesheet " + stylesheet);
		}
		return xslInputStream;
	}


	public static void writeXmlFile(final Document doc, final OutputStream outputStream) {
		try {
			final Source source = new DocumentSource(doc);
			final Result result = new StreamResult(outputStream);
			final Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
			transformer.transform(source, result);
		} catch (IllegalArgumentException ignore) {
			LOGGER.warn("Setting output property to document failed. no problem. may be an empty document.");
		} catch (TransformerConfigurationException ignore) {
		} catch (TransformerException ignore) {
		}
	}


	public static String loadXmlFromDocument(final Document doc) {
		String xmlString = "";

		try {
			final Source source = new DocumentSource(doc);
			final StringWriter out = new StringWriter();
			final Result result = new StreamResult(out);

			final Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
			transformer.transform(source, result);
			xmlString = out.toString();
		} catch (IllegalArgumentException ignore) {
			LOGGER.warn("Setting output property to document failed. no problem. may be an empty document.");
		} catch (TransformerException e) {
			LOGGER.error("Loading xml string from org.w3c.dom.Document failed.", e);
		}

		return xmlString;
	}


	@Override
	public void update(final AnActionEvent event) {
		try {
			final com.intellij.openapi.project.Project project = DataKeys.PROJECT.getData(event.getDataContext());
			final Presentation presentation = event.getPresentation();

			// check a project is loaded
			if (isProjectLoaded(project, presentation)) {
				return;
			}

			isPluginAccessible(project);

			// check if tool window is registered
			final ToolWindow toolWindow = isToolWindowRegistred(project);
			if (toolWindow == null) {
				presentation.setEnabled(false);
				presentation.setVisible(false);

				return;
			}

			registerEventListener(project);

			if (!_running) {
				_enabled = _bugCollection != null && _bugCollection.iterator().hasNext();
			}

			presentation.setEnabled(toolWindow.isAvailable() && isEnabled());
			presentation.setVisible(true);

		} catch (Throwable e) {
			final FindBugsPluginException processed = FindBugsPluginImpl.processError("Action update failed", e);// NON-NLS
			if (processed != null) {
				LOGGER.error("Action update failed", processed);
			}
		}
	}


	@SuppressWarnings({"AssignmentToNull"})
	public void onEvent(@NotNull final BugReporterEvent event) {
		switch (event.getOperation()) {
			case ANALYSIS_STARTED:
				_bugCollection = null;
				setEnabled(false);
				setRunning(true);
				break;
			case ANALYSIS_ABORTED:
				_bugCollection = null;
				setEnabled(true);
				setRunning(false);
				break;
			case ANALYSIS_FINISHED:
				_bugCollection = event.getBugCollection();
				setEnabled(true);
				setRunning(false);
				break;
			case NEW_BUG_INSTANCE:
				break;
			default:
		}
	}


	private void registerEventListener(final Project project) {
		final String projectName = project.getName();
		if (!isRegistered(projectName)) {
			EventManagerImpl.getInstance().addEventListener(new BugReporterEventFilter(projectName), this);
			addRegisteredProject(projectName);
		}
	}


	protected boolean isRunning() {
		return _running;
	}


	protected boolean setRunning(final boolean running) {
		final boolean was = _running;
		if (_running != running) {
			_running = running;
		}
		return was;
	}
}
