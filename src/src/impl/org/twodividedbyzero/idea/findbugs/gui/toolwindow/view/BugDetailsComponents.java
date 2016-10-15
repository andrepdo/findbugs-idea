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
package org.twodividedbyzero.idea.findbugs.gui.toolwindow.view;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.UIUtil;
import edu.umd.cs.findbugs.BugAnnotation;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.I18N;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.Plugin;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import edu.umd.cs.findbugs.cloud.Cloud;
import edu.umd.cs.findbugs.cloud.Cloud.UserDesignation;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.BugInstanceUtil;
import org.twodividedbyzero.idea.findbugs.gui.common.CustomLineBorder;
import org.twodividedbyzero.idea.findbugs.gui.common.MultiSplitLayout;
import org.twodividedbyzero.idea.findbugs.gui.common.MultiSplitPane;
import org.twodividedbyzero.idea.findbugs.gui.common.ScrollPaneFacade;
import org.twodividedbyzero.idea.findbugs.gui.common.VerticalTextIcon;
import org.twodividedbyzero.idea.findbugs.gui.tree.view.BugTree;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

@SuppressWarnings("MagicNumber")
public final class BugDetailsComponents {

	private static final Logger LOGGER = Logger.getInstance(BugDetailsComponents.class);
	private static final String EDU_UMD_CS_FINDBUGS_PLUGINS_WEB_CLOUD = "edu.umd.cs.findbugs.plugins.webCloud";

	private HTMLEditorKit _htmlEditorKit;
	private JEditorPane _bugDetailsPane;
	private JEditorPane _explanationPane;
	private CloudCommentsPaneIntellij _cloudCommentsPane;
	private JPanel _bugDetailsPanel;
	private JPanel _explanationPanel;
	private JPanel _cloudCommentsPanel;
	private final ToolWindowPanel _parent;
	private double _splitPaneHorizontalWeight = 0.6;
	private SortedBugCollection _lastBugCollection;
	private BugInstance _lastBugInstance;
	private JTabbedPane _jTabbedPane;
	private MultiSplitPane _bugDetailsSplitPane;


	BugDetailsComponents(final ToolWindowPanel toolWindowPanel) {
		_parent = toolWindowPanel;
		_htmlEditorKit = GuiResources.createHtmlEditorKit();
	}

	JTabbedPane getTabbedPane() {
		if (_jTabbedPane == null) {
			if (SystemInfo.isMac) {
				// use JTabbedPane because JBTabbedPane does not work with tabPlacement=RIGHT
				// on OS X (Aqua) at least with IDEA 14.1.4 (141.1532.4) and OS X 10.10 Yosemite.
				//noinspection UndesirableClassUsage
				_jTabbedPane = new JTabbedPane(SwingConstants.RIGHT);
			} else {
				_jTabbedPane = new JBTabbedPane(SwingConstants.RIGHT);
			}

			_jTabbedPane.setFocusable(false);
			_jTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

			if (SystemInfo.isMac) {
				// Aqua LF will rotate content
				_jTabbedPane.addTab("Bug Details", GuiResources.FINDBUGS_ICON, getBugDetailsSplitPane(), "Bug details concerning the current selected bug in the left tree");
			} else {
				_jTabbedPane.addTab(null, new VerticalTextIcon("Bug Details", true, GuiResources.FINDBUGS_ICON), getBugDetailsSplitPane(), "Bug details concerning the current selected bug in the left tree");
			}

			_jTabbedPane.setMnemonicAt(0, KeyEvent.VK_1);


			if (Plugin.getByPluginId(EDU_UMD_CS_FINDBUGS_PLUGINS_WEB_CLOUD) != null) {
				if (SystemInfo.isMac) {
					// Aqua LF will rotate content
					_jTabbedPane.addTab("Comments", GuiResources.FINDBUGS_CLOUD_ICON, getCloudCommentsPanel(), "Comments from the FindBugs Cloud");
				} else {
					_jTabbedPane.addTab(null, new VerticalTextIcon("Comments", true, GuiResources.FINDBUGS_CLOUD_ICON), getCloudCommentsPanel(), "Comments from the FindBugs Cloud");
				}
				_jTabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
			}
		}

		return _jTabbedPane;
	}

	private Component getBugDetailsSplitPane() {
		if (_bugDetailsSplitPane == null) {
			_bugDetailsSplitPane = new MultiSplitPane();
			_bugDetailsSplitPane.setContinuousLayout(true);
			final String layoutDef = "(ROW weight=1.0 (COLUMN weight=1.0 top bottom))";
			final MultiSplitLayout.Node modelRoot = MultiSplitLayout.parseModel(layoutDef);
			final MultiSplitLayout multiSplitLayout = _bugDetailsSplitPane.getMultiSplitLayout();
			multiSplitLayout.setDividerSize(6);
			multiSplitLayout.setModel(modelRoot);
			multiSplitLayout.setFloatingDividers(true);
			_bugDetailsSplitPane.add(getBugDetailsPanel(), "top");
			_bugDetailsSplitPane.add(getBugExplanationPanel(), "bottom");


		}
		return _bugDetailsSplitPane;
	}

	@SuppressWarnings("MagicNumber")
	private JPanel getBugDetailsPanel() {
		if (_bugDetailsPanel == null) {
			final JScrollPane scrollPane = ScrollPaneFacade.createScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setViewportView(getBugDetailsPane());
			//scrollPane.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0, 0, 0, 3), new CustomLineBorder(new Color(98, 95, 89), 0, 0, 1, 1)));
			scrollPane.setBorder(new CustomLineBorder(new JBColor(new Color(98, 95, 89), new Color(53, 51, 48)), 0, 0, 1, 0));
			//scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

			_bugDetailsPanel = new JPanel();
			_bugDetailsPanel.setBorder(new EmptyBorder(3, 2, 0, 3));
			_bugDetailsPanel.setLayout(new BorderLayout());
			_bugDetailsPanel.add(scrollPane, BorderLayout.CENTER);
		}

		return _bugDetailsPanel;
	}

	private JEditorPane getBugDetailsPane() {
		if (_bugDetailsPane == null) {
			_bugDetailsPane = new BugDetailsEditorPane();
			_bugDetailsPane.setBorder(new EmptyBorder(10, 10, 10, 10));
			_bugDetailsPane.setEditable(false);
			_bugDetailsPane.setContentType(UIUtil.HTML_MIME);
			_bugDetailsPane.setEditorKit(_htmlEditorKit);
			_bugDetailsPane.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(final HyperlinkEvent evt) {
					if (_parent != null) {
						handleDetailsClick(evt);
					}
				}
			});
		}

		return _bugDetailsPane;
	}

	private JPanel getBugExplanationPanel() {
		if (_explanationPanel == null) {
			final JScrollPane scrollPane = ScrollPaneFacade.createScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setViewportView(getExplanationPane());
			scrollPane.setBorder(BorderFactory.createCompoundBorder(new CustomLineBorder(new JBColor(new Color(208, 206, 203), new Color(170, 168, 165)), 1, 0, 0, 0), new CustomLineBorder(new JBColor(new Color(98, 95, 89), new Color(71, 68, 62)), 1, 0, 0, 0)));

			_explanationPanel = new JPanel();
			_explanationPanel.setBorder(new EmptyBorder(0, 2, 0, 3));
			_explanationPanel.setLayout(new BorderLayout());
			_explanationPanel.add(scrollPane, BorderLayout.CENTER);
		}

		return _explanationPanel;
	}

	@SuppressWarnings({"AnonymousInnerClass"})
	private JEditorPane getExplanationPane() {
		if (_explanationPane == null) {
			_explanationPane = new ExplanationEditorPane();
			_explanationPane.setBorder(new EmptyBorder(10, 10, 10, 10));
			_explanationPane.setEditable(false);
			_explanationPane.setContentType("text/html");
			_explanationPane.setEditorKit(_htmlEditorKit);
			_explanationPane.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(final HyperlinkEvent evt) {
					editorPaneHyperlinkUpdate(evt);
				}
			});
		}

		return _explanationPane;
	}

	private JPanel getCloudCommentsPanel() {
		if (_cloudCommentsPanel == null) {
			final JScrollPane scrollPane = ScrollPaneFacade.createScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setBorder(null);
			scrollPane.setViewportView(getCloudCommentsPane());
			_cloudCommentsPanel = new JPanel();
			_cloudCommentsPanel.setLayout(new BorderLayout());
			_cloudCommentsPanel.add(scrollPane, BorderLayout.CENTER);
		}

		return _cloudCommentsPanel;
	}

	private CloudCommentsPaneIntellij getCloudCommentsPane() {
		if (_cloudCommentsPane == null) {
			_cloudCommentsPane = new CloudCommentsPaneIntellij(_parent);
			_cloudCommentsPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		}

		return _cloudCommentsPane;
	}

	private void handleDetailsClick(final HyperlinkEvent evt) {
		if (evt.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
			if (_parent != null) {
				final String desc = evt.getDescription();
				if ("#class".equals(desc)) {
					final BugTreePanel bugTreePanel = _parent.getBugTreePanel();
					final BugTree tree = bugTreePanel.getBugTree();
					if (bugTreePanel.isScrollToSource()) {
						tree.getScrollToSourceHandler().scollToSelectionSource();
					} else {
						bugTreePanel.setScrollToSource(true);
						tree.getScrollToSourceHandler().scollToSelectionSource();
						bugTreePanel.setScrollToSource(false);
					}
				} else if ("#comments".equals(desc)) {
					getTabbedPane().setSelectedComponent(getCloudCommentsPanel());
				}
			}
		}
	}

	private void editorPaneHyperlinkUpdate(final HyperlinkEvent evt) {
		try {
			if (evt.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
				final URL url = evt.getURL();
				BrowserUtil.browse(url);
				_explanationPane.setPage(url);
			}
		} catch (final Exception e) {
			LOGGER.debug(e);
		}
	}

	@SuppressFBWarnings({"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
	@SuppressWarnings({"HardCodedStringLiteral"})
	void setBugsDetails(@NotNull final BugInstance bugInstance) {
		final int[] lines = BugInstanceUtil.getSourceLines(bugInstance);
		final MethodAnnotation methodAnnotation = BugInstanceUtil.getPrimaryMethod(bugInstance);
		final FieldAnnotation fieldAnnotation = BugInstanceUtil.getPrimaryField(bugInstance);

		final StringBuilder html = new StringBuilder();
		html.append("<html><body>");
		html.append("<h2>");
		html.append(bugInstance.getAbridgedMessage());
		html.append("</h2><p/>");
		final SortedBugCollection bc = _lastBugCollection;
		if (bc != null) {
			final Cloud cloud = bc.getCloud();
			//noinspection ConstantConditions
			if (cloud != null) {
				final int reviewers = cloud.getReviewers(bugInstance).size();
				if (reviewers > 0) {
					html.append(" - <a href='#comments'><u>");
					html.append(reviewers);
					html.append(" comment");
					html.append(reviewers != 1 ? "s" : "");
					html.append("</u></a>");
				}
				final UserDesignation designation = cloud.getConsensusDesignation(bugInstance);
				if (designation != UserDesignation.UNCLASSIFIED) {
					//List<String> userDesignationKeys = I18N.instance().getUserDesignationKeys(true);
					html.append(" - \"");
					html.append(I18N.instance().getUserDesignation(designation.name()));
					html.append('\"');
				}
				final int ageInDays = (int) ((System.currentTimeMillis() - cloud.getFirstSeen(bugInstance)) / (1000 * 60 * 60 * 24));
				if (cloud.isInCloud(bugInstance) && ageInDays > 0) {
					html.append(" - first seen ");
					html.append(ageInDays);
					html.append(" day");
					html.append(ageInDays != 1 ? "s" : "");
					html.append(" ago");
				}
			}
		}

		html.append("<table border=0><tr valign=top><td valign=top>");
		html.append("<h3>Class:</h3>");
		html.append("<ul>");
		html.append("<li>");
		html.append("<a href='#class'><u>");
		html.append(BugInstanceUtil.getSimpleClassName(bugInstance));
		html.append("</u></a>");
		html.append(" <font color='gray'>(");
		final String packageName = BugInstanceUtil.getPackageName(bugInstance);
		html.append(packageName);
		html.append(")</font>");

		if (lines[0] > -1) {
			final boolean singleLine = lines[1] == lines[0];
			if (singleLine) {
				html.append(" line ");
			} else {
				html.append(" lines ");
			}
			html.append(lines[0]);
			if (!singleLine) {
				html.append('-').append(lines[1]);
			}
		}
		html.append("</ul>");

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
		html.append("</td><td width='20px'>&nbsp;</td><td valign=top>");

		html.append("<h3>Problem classification:</h3>");
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

		final Iterable<BugAnnotation> annotations = bugInstance.getAnnotationsForMessage(false);
		if (annotations.iterator().hasNext()) {
			html.append("<p><h3>Notes:</p>");
			html.append("<ul>");
			for (final BugAnnotation annotation : annotations) {
				html.append("<li>").append(annotation.toString(bugInstance.getPrimaryClass())).append("</li>");
			}
			html.append("</ul>");
		}

		final DetectorFactory detectorFactory = bugInstance.getDetectorFactory();
		if (detectorFactory != null) {
			html.append("<li>");
			html.append(detectorFactory.getShortName());
			html.append(" <font color='gray'>(");
			html.append(createBugsAbbreviation(detectorFactory));
			html.append(")</font>");
			html.append("</li>");
		}
		html.append("</ul>");
		html.append("</tr></table>");
		html.append("</body></html>");

		// FIXME: set Suppress actions hyperlink

		_bugDetailsPane.setText(html.toString());
		scrollRectToVisible(_bugDetailsPane);

	}

	void setBugExplanation(final SortedBugCollection bugCollection, final BugInstance bugInstance) {
		_lastBugCollection = bugCollection;
		_lastBugInstance = bugInstance;
		refreshDetailsShown();
	}

	private void refreshDetailsShown() {
		final String html = BugInstanceUtil.getDetailHtml(_lastBugInstance);
		final StringReader reader = new StringReader(html); // no need for BufferedReader
		try {
			_explanationPane.setToolTipText(edu.umd.cs.findbugs.L10N.getLocalString("tooltip.longer_description", "This gives a longer description of the detected bug pattern"));
			_explanationPane.read(reader, "html bug description");
		} catch (final IOException e) {
			_explanationPane.setText("Could not find bug description: " + e.getMessage());
			LOGGER.warn(e.getMessage(), e);
		} finally {
			reader.close(); // polite, but doesn't do much in StringReader
		}
		getCloudCommentsPane().setBugInstance(_lastBugInstance);
		scrollRectToVisible(_bugDetailsPane);
	}

	@SuppressWarnings({"AnonymousInnerClass"})
	private static void scrollRectToVisible(final JEditorPane pane) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				pane.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
			}
		});
	}

	void adaptSize(final int width, final int height) {
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
		getBugDetailsSplitPane().validate();
		//_parent.validate();
		//}
	}

	public void issueUpdated(final BugInstance bug) {
		//noinspection ObjectEquality
		if (bug == _lastBugInstance) {
			refreshDetailsShown();
		}
	}

	public double getSplitPaneHorizontalWeight() {
		return _splitPaneHorizontalWeight;
	}

	public void setSplitPaneHorizontalWeight(final double splitPaneHorizontalWeight) {
		_splitPaneHorizontalWeight = splitPaneHorizontalWeight;
	}

	public void clear() {
		if (_bugDetailsPane != null) {
			_bugDetailsPane.setText(null);
		}
		if (_explanationPane != null) {
			_explanationPane.setText(null);
		}
	}

	public static String createBugsAbbreviation(final DetectorFactory factory) {
		final StringBuilder sb = new StringBuilder();
		final Collection<BugPattern> patterns = factory.getReportedBugPatterns();
		final HashSet<String> abbrs = new LinkedHashSet<String>();
		for (final BugPattern pattern : patterns) {
			final String abbr = pattern.getAbbrev();
			abbrs.add(abbr);
		}
		//noinspection ForLoopWithMissingComponent
		for (final Iterator<String> iter = abbrs.iterator(); iter.hasNext(); ) {
			final String element = iter.next();
			sb.append(element);
			if (iter.hasNext()) {
				sb.append('|');
			}
		}
		return sb.toString();
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
}
