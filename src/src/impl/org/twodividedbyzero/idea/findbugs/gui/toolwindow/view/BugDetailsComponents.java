/**
 * Copyright 2008 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.twodividedbyzero.idea.findbugs.gui.toolwindow.view;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.MethodAnnotation;
import org.twodividedbyzero.idea.findbugs.common.util.BugInstanceUtil;
import org.twodividedbyzero.idea.findbugs.gui.preferences.DetectorConfiguration;
import org.twodividedbyzero.idea.findbugs.gui.tree.view.BugTree;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class BugDetailsComponents /*extends JPanel*/ {

	private static final Logger LOGGER = Logger.getInstance(BugDetailsComponents.class.getName());

	private HTMLEditorKit _htmlEditorKit;
	private JEditorPane _bugDetailsPane;
	private JEditorPane _explanationPane;
	private JPanel _bugDetailsPanel;
	private JPanel _explanationPanel;
	private Component _parent;
	private TreePath _currentTreePath;
	private double _splitPaneHorizontalWeight = 0.6;


	public BugDetailsComponents(final Component parent) {
		_parent = parent;
		init();
	}


	public void init() {
		_htmlEditorKit = new HTMLEditorKit();
		setStyleSheets();
	}


	public JPanel getBugDetailsPanel() {
		if (_bugDetailsPanel == null) {
			final JScrollPane detailTextScoller = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			detailTextScoller.setViewportView(getBugDetailsPane());

			_bugDetailsPanel = new JPanel();
			//_bugDetailsPanel.setPreferredSize(new Dimension((int) (_parent.getPreferredSize().width * 0.6), (int) (_parent.getPreferredSize().height * 0.6)));
			_bugDetailsPanel.setLayout(new BorderLayout());
			_bugDetailsPanel.add(detailTextScoller, BorderLayout.CENTER);
			_bugDetailsPane.setEditorKit(_htmlEditorKit);
		}

		return _bugDetailsPanel;
	}


	private JEditorPane getBugDetailsPane() {
		if (_bugDetailsPane == null) {
			_bugDetailsPane = new BugDetailsEditorPane();
			_bugDetailsPane.setBorder(new EmptyBorder(10, 10, 10, 10));
			_bugDetailsPane.setEditable(false);
			_bugDetailsPane.setBackground(Color.white);
			_bugDetailsPane.setContentType("text/html");
			_bugDetailsPane.addHyperlinkListener(new BugDetailsPaneHyperlinkListener());
		}

		return _bugDetailsPane;
	}


	public JPanel getBugExplanationPanel() {
		if (_explanationPanel == null) {
			final JScrollPane detailHtmlScoller = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			detailHtmlScoller.setViewportView(getExplanationPane());

			_explanationPanel = new JPanel();
			_explanationPanel.setLayout(new BorderLayout());
			//_explanationPanel.setMinimumSize(new Dimension((int) (_parent.getPreferredSize().width * 0.6), 100));
			//_explanationPanel.setPreferredSize(new Dimension((int) (_parent.getPreferredSize().width * 0.6), 150));
			_explanationPanel.add(detailHtmlScoller, BorderLayout.CENTER);
			_explanationPane.setEditorKit(_htmlEditorKit);
		}

		return _explanationPanel;
	}


	private JEditorPane getExplanationPane() {
		if (_explanationPane == null) {
			_explanationPane = new ExplanationEditorPane();
			_explanationPane.setBorder(new EmptyBorder(10, 10, 10, 10));
			//_explanationPane.setPreferredSize(new Dimension(_parent.getPreferredSize().width, 150));
			_explanationPane.setEditable(false);
			_explanationPane.setBackground(Color.white);
			_explanationPane.setContentType("text/html");

			_explanationPane.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(final HyperlinkEvent evt) {
					editorPaneHyperlinkUpdate(evt);
				}
			});
		}

		return _explanationPane;
	}


	private void setStyleSheets() {
		final StyleSheet styleSheet = new StyleSheet();
		styleSheet.addRule("body {font-size: 12pt}");
		styleSheet.addRule("H1 {color: #005555;  font-size: 120%; font-weight: bold;}");
		styleSheet.addRule("H2 {color: #005555;  font-size: 12pt; font-weight: bold;}");
		styleSheet.addRule("H3 {color: #005555;  font-size: 12pt; font-weight: bold;}");
		styleSheet.addRule("code {font-family: courier; font-size: 12pt}");
		styleSheet.addRule("pre {color: gray; font-family: courier; font-size: 12pt}");
		styleSheet.addRule("a {color: blue; font-decoration: underline}");
		styleSheet.addRule("li {margin-left: 10px; list-style-type: none}");
		styleSheet.addRule("#Low {background-color: green; width: 15px; height: 15px;}");
		styleSheet.addRule("#Medium {background-color: yellow; width: 15px; height: 15px;}");
		styleSheet.addRule("#High {background-color: red; width: 15px; height: 15px;}");
		styleSheet.addRule("#Exp {background-color: black; width: 15px; height: 15px;}");

		_htmlEditorKit.setStyleSheet(styleSheet);
	}


	private void scrollToError(final HyperlinkEvent evt) {
		if (evt.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
			if (_parent instanceof ToolWindowPanel) {
				final BugTreePanel bugTreePanel = ((ToolWindowPanel) _parent).getBugTreePanel();
				//bugTreePanel.scrollToSource(_currentTreePath);
				final BugTree tree = bugTreePanel.getBugTree();
				if (bugTreePanel.isScrollToSource()) {
					tree.getScrollToSourceHandler().scollToSelectionSource();
				} else {
					bugTreePanel.setScrollToSource(true);
					tree.getScrollToSourceHandler().scollToSelectionSource();
					bugTreePanel.setScrollToSource(false);
				}

			}
		}
	}


	private void editorPaneHyperlinkUpdate(final HyperlinkEvent evt) {
		try {
			if (evt.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
				final URL url = evt.getURL();
				//showInBrowser(url.toString());
				//LaunchBrowser.showDocument(url);
				BrowserUtil.launchBrowser(url.toExternalForm());
				_explanationPane.setPage(url);
			}
		} catch (Exception e) {
			LOGGER.debug(e);
		}
	}


	@SuppressWarnings({"HardCodedStringLiteral"})
	public void setBugsDetails(final BugInstance bugInstance, final TreePath treePath) {
		_currentTreePath = treePath;
		final int[] lines = BugInstanceUtil.getSourceLines(bugInstance);
		final MethodAnnotation methodAnnotation = BugInstanceUtil.getPrimaryMethod(bugInstance);
		final FieldAnnotation fieldAnnotation = BugInstanceUtil.getPrimaryField(bugInstance);

		final StringBuilder html = new StringBuilder();
		html.append("<html><body>");
		html.append("<h2>");
		html.append(bugInstance.getAbridgedMessage());
		html.append("</h2>");

		html.append("<p><h3>Class:</p>");
		html.append("<ul>");
		html.append("<li>");
		html.append("<a href=''><u>");
		html.append(BugInstanceUtil.getSimpleClassName(bugInstance));
		html.append("</u></a>");
		html.append(" <font color='gray'>(");
		html.append(BugInstanceUtil.getPackageName(bugInstance));
		html.append(")</font></li>");
		html.append("</ul>");

		if (lines[0] > -1) {
			html.append("<p><h3>Line:</p>");
			html.append("<ul>");
			html.append("<li>");
			html.append(lines[0]).append(" - ").append(lines[1]);
			html.append("</li>");
			html.append("</ul>");
		}

		if (methodAnnotation != null) {
			html.append("<p><h3>Method:</p>");
			html.append("<ul>");
			html.append("<li>");

			if ("<init>".equals(methodAnnotation.getMethodName())) {
				html.append(BugInstanceUtil.getJavaSourceMethodName(bugInstance)).append("&lt;init&gt; <font color='gray'>(").append(BugInstanceUtil.getFullMethod(bugInstance)).append(")</font>");
			} else {
				html.append(BugInstanceUtil.getMethodName(bugInstance)).append(" <font color='gray'>(").append(BugInstanceUtil.getFullMethod(bugInstance)).append(")</font>");
			}
			html.append("</li>");
			html.append("</ul>");
		}

		if (fieldAnnotation != null) {
			html.append("<p><h3>Field:</p>");
			html.append("<ul>");
			html.append("<li>");
			html.append(BugInstanceUtil.getFieldName(bugInstance));
			html.append("</li>");
			html.append("</ul>");
		}

		html.append("<p><h3>Priority:</p>");
		html.append("<ul>");
		html.append("<li>");
		html.append("<span width='15px' height='15px;' id='").append(BugInstanceUtil.getPriorityString(bugInstance)).append("'> &nbsp; &nbsp; </span>&nbsp;");
		html.append(BugInstanceUtil.getPriorityTypeString(bugInstance));
		html.append("</li>");
		html.append("</ul>");

		html.append("<p><h3>Problem classification:</p>");
		html.append("<ul>");
		html.append("<li>");
		html.append(BugInstanceUtil.getBugCategoryDescription(bugInstance));
		html.append(" <font color='gray'>(");
		html.append(BugInstanceUtil.getBugTypeDescription(bugInstance));
		html.append(")</font>");
		html.append("</li>");
		html.append("<li>");
		html.append(BugInstanceUtil.getBugType(bugInstance));
		html.append(" <font color='gray'>(");
		html.append(BugInstanceUtil.getBugPatternShortDescription(bugInstance));
		html.append(")</font>");
		html.append("</li>");
		html.append("<ul>");
		html.append("<li>");
		html.append(bugInstance.getDetectorFactory().getShortName());
		html.append(" <font color='gray'>(");
		html.append(DetectorConfiguration.createBugsAbbreviation(bugInstance.getDetectorFactory()));
		html.append(")</font>");
		html.append("</li>");
		html.append("</ul>");

		html.append("</body></html>");

		// todo: set Suppress actions hyperlink

		_bugDetailsPane.setText(html.toString());
		scrollRectToVisible(_bugDetailsPane);

	}


	public void setBugExplanation(final BugInstance bugInstance) {
		final String html = BugInstanceUtil.getDetailHtml(bugInstance);
		final StringReader reader = new StringReader(html); // no need for BufferedReader
		try {
			_explanationPane.setToolTipText(edu.umd.cs.findbugs.L10N.getLocalString("tooltip.longer_description", "This gives a longer description of the detected bug pattern"));
			_explanationPane.read(reader, "html bug description");
		} catch (IOException e) {
			_explanationPane.setText("Could not find bug description: " + e.getMessage());
			LOGGER.warn(e.getMessage(), e);
		} finally {
			reader.close(); // polite, but doesn't do much in StringReader
		}

		scrollRectToVisible(_bugDetailsPane);
	}


	private static void scrollRectToVisible(final JEditorPane pane) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				pane.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
				//_detailTextPane.setCaretPosition(0);
			}
		});
	}


	public void adaptSize(final int width, final int height) {
		//final int newWidth = (int) (width * _splitPaneHorizontalWeight);
		final int expHeight = (int) (height * 0.4);
		final int detailsHeight = (int) (height * 0.6);

		//if(_bugDetailsPanel.getPreferredSize().width != newWidth && _bugDetailsPanel.getPreferredSize().height != detailsHeight) {
		_bugDetailsPanel.setPreferredSize(new Dimension(width, detailsHeight));
		_bugDetailsPanel.setSize(new Dimension(width, detailsHeight));
		//_bugDetailsPanel.doLayout();
		_bugDetailsPanel.validate();
		//_parent.validate();
		//}

		//if(_explanationPanel.getPreferredSize().width != newWidth && _explanationPanel.getPreferredSize().height != expHeight) {
		_explanationPanel.setPreferredSize(new Dimension(width, expHeight));
		_explanationPanel.setSize(new Dimension(width, expHeight));
		//_explanationPanel.doLayout();
		_explanationPanel.validate();
		//_parent.validate();
		//}
	}


	public double getSplitPaneHorizontalWeight() {
		return _splitPaneHorizontalWeight;
	}


	public void setSplitPaneHorizontalWeight(final double splitPaneHorizontalWeight) {
		_splitPaneHorizontalWeight = splitPaneHorizontalWeight;
	}


	private static class BugDetailsEditorPane extends JEditorPane {

		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);
			final Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		}
	}

	private static class ExplanationEditorPane extends JEditorPane {

		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);
			final Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		}
	}

	private class BugDetailsPaneHyperlinkListener implements HyperlinkListener {

		public void hyperlinkUpdate(final HyperlinkEvent evt) {
			if (_parent instanceof ToolWindowPanel) {
				scrollToError(evt);
			}
		}
	}
}
