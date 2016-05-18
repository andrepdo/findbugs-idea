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

package org.twodividedbyzero.idea.findbugs.gui.toolwindow.view;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import info.clearthought.layout.TableLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.LayoutManager;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
@SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
public class CloudCommentsPane2  extends JPanel {

	private final ToolWindowPanel _toolWindowPanel;
	private JLabel _titleLabel;


	public CloudCommentsPane2(final ToolWindowPanel toolWindowPanel) {
		_toolWindowPanel = toolWindowPanel;

		final double border = 5;
		final double colsGap = 2;
		final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.PREFERRED, colsGap, TableLayout.PREFERRED, border}, // Columns
								 {border, TableLayout.PREFERRED, border}};// Rows
		final LayoutManager tbl = new TableLayout(size);
		setLayout(tbl);

		
	}


	private JLabel getTitleLabel() {
		if (_titleLabel == null) {
			
			_titleLabel = new JLabel("FindBugs Cloud");
		}
		return _titleLabel;
	}
}

/*
package org.twodividedbyzero.idea.findbugs.gui.toolwindow.view;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.I18N;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.cloud.Cloud;
import edu.umd.cs.findbugs.cloud.Cloud.UserDesignation;
import edu.umd.cs.findbugs.cloud.CloudPlugin;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
import sun.swing.SwingUtilities2;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


@edu.umd.cs.findbugs.annotations.SuppressWarnings({"SE_TRANSIENT_FIELD_NOT_RESTORED", "SE_BAD_FIELD", "SE_BAD_FIELD_STORE"})
public class CloudCommentsPane extends JPanel {

	private static final Logger LOGGER = Logger.getInstance(CloudCommentsPane.class.getName());

	private JEditorPane _cloudReportPane;
	private LinkLabel _addCommentLink;
	private JTextArea _commentBox;
	private JButton _submitCommentButton;
	private JPanel _commentEntryPanel;
	private JComboBox _classificationCombo;
	private JPanel _mainPanel;
	private LinkLabel _cancelLink;
	private JScrollPane _cloudReportScrollPane;
	private JLabel _titleLabel;
	private LinkLabel _signInOutLink;
	private LinkLabel _changeLink;
	private JTextArea _cloudDetailsLabel;

	private SortedBugCollection _bugCollection;
	private BugInstance _bugInstance;

	private final Cloud.CloudStatusListener _cloudStatusListener = new Cloud.CloudStatusListener() {
		public void handleIssueDataDownloadedEvent() {
		}


		public void handleStateChange(final Cloud.SigninState oldState, final Cloud.SigninState state) {
			updateBugCommentsView();
		}
	};
	private final ToolWindowPanel _toolWindowPanel;


	public CloudCommentsPane(final ToolWindowPanel toolWindowPanel) {
		_toolWindowPanel = toolWindowPanel;
		setLayout(new BorderLayout());
		add(_mainPanel, BorderLayout.CENTER);

		_classificationCombo.removeAllItems();
		for (final UserDesignation designation : UserDesignation.values()) {
			_classificationCombo.addItem(I18N.instance().getUserDesignation(designation.name()));
		}

		_commentEntryPanel.setVisible(false);
		_addCommentLink.setListener(new LinkListener() {
			public void linkSelected(final LinkLabel linkLabel, final Object o) {
				_commentEntryPanel.setVisible(true);
				_addCommentLink.setVisible(false);
				_commentBox.requestFocusInWindow();
				_commentBox.setSelectionStart(0);
				_commentBox.setSelectionEnd(_commentBox.getText().length());
				invalidate();
			}
		}, null);
		_cancelLink.setListener(new LinkListener() {
			public void linkSelected(final LinkLabel linkLabel, final Object o) {
				_commentEntryPanel.setVisible(false);
				_addCommentLink.setVisible(true);
				invalidate();
			}
		}, null);
		_submitCommentButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				final String comment = _commentBox.getText();
				final int index = _classificationCombo.getSelectedIndex();
				final UserDesignation choice;
				if (index == -1) {
					choice = UserDesignation.UNCLASSIFIED;
				} else {
					choice = UserDesignation.values()[index];
				}
				_bugInstance.setUserDesignationKey(choice.name(), _bugCollection);
				//FIXME: do in background
				_bugInstance.setAnnotationText(comment, _bugCollection);

				_commentBox.setText("My comment");

				updateBugCommentsView();

				_commentEntryPanel.setVisible(false);
				_addCommentLink.setVisible(true);
				invalidate();
			}
		});
		_signInOutLink.setListener(new LinkListener() {
			public void linkSelected(final LinkLabel linkLabel, final Object o) {
				if (_bugCollection != null) {
					final Cloud cloud = _bugCollection.getCloud();
					switch (cloud.getSigninState()) {
						case SIGNED_OUT:
						case SIGNIN_FAILED:
						case UNAUTHENTICATED:
							try {
								cloud.signIn();
							} catch (Exception e) {
								Messages.showErrorDialog("The FindBugs Cloud could not be contacted at this time.\n\n" + e.getMessage(), "Could not connect to FindBugs Cloud");
								LOGGER.warn(e);
							}
							break;
						case SIGNED_IN:
							cloud.signOut();
							break;
						default:
					}
				}
			}
		}, null);
		_changeLink.setListener(new LinkListener() {
			public void linkSelected(final LinkLabel linkLabel, final Object o) {
				final List<CloudPlugin> plugins = new ArrayList<CloudPlugin>();
				final List<String> descriptions = new ArrayList<String>();
				for (final CloudPlugin plugin : DetectorFactoryCollection.instance().getRegisteredClouds().values()) {
					final FindBugsPlugin findBugsPlugin = _toolWindowPanel.getProject().getComponent(FindBugsPlugin.class);
					final FindBugsPreferences prefs = findBugsPlugin.getPreferences();
					final boolean disabled = prefs.isPluginDisabled(plugin.getFindbugsPluginId());
					if (!disabled && !plugin.isHidden()) {
						descriptions.add(plugin.getDescription());
						plugins.add(plugin);
					}
				}
				final JBPopupFactory factory = JBPopupFactory.getInstance();
				final ListPopup popup = factory.createListPopup(new BaseListPopupStep<String>("Store comments in:", descriptions) {
					@Override
					public PopupStep<?> onChosen(final String selectedValue, final boolean finalChoice) {
						if (selectedValue != null) {
							final int index = descriptions.indexOf(selectedValue);
							if (index == -1) {
								LOGGER.error("Error - not found - '" + selectedValue + "' among " + descriptions);
							} else {
								final CloudPlugin newPlugin = plugins.get(index);
								final String newCloudId = newPlugin.getId();
								final String oldCloudId = _bugCollection.getCloud().getPlugin().getId();
								if (!oldCloudId.equals(newCloudId)) {
									_bugCollection.getProject().setCloudId(newCloudId);
									//FIXME: execute in background so signin doesn't stall UI
									_bugCollection.reinitializeCloud();
									_toolWindowPanel.getBugDetailsComponents().issueUpdated(_bugInstance);
								}
							}
						}
						return super.onChosen(selectedValue, finalChoice);
					}


					@Override
					public void canceled() {
						super.canceled();
					}
				});
				popup.showInCenterOf(_changeLink);
			}
		}, null);
		_cloudDetailsLabel.setBackground(null);
		_cloudDetailsLabel.setBorder(null);
		_titleLabel.putClientProperty(SwingUtilities2.AA_TEXT_PROPERTY_KEY, new SwingUtilities2.AATextInfo(RenderingHints.VALUE_TEXT_ANTIALIAS_ON, null));
		_cloudDetailsLabel.putClientProperty(SwingUtilities2.AA_TEXT_PROPERTY_KEY, new SwingUtilities2.AATextInfo(RenderingHints.VALUE_TEXT_ANTIALIAS_ON, null));
		updateBugCommentsView();
	}


	public SortedBugCollection getBugCollection() {
		return _bugCollection;
	}


	public void setBugInstance(final SortedBugCollection bugCollection, final BugInstance bugInstance) {
		updateCloudListeners(bugCollection);
		_bugCollection = bugCollection;
		_bugInstance = bugInstance;
		updateBugCommentsView();
	}


	private void updateCloudListeners(final SortedBugCollection newBugCollection) {
		boolean isNewCloud = false;
		final Cloud newCloud = newBugCollection.getCloud();
		if (_bugCollection != null) {
			final Cloud oldCloud = _bugCollection.getCloud();
			//noinspection ObjectEquality
			if (oldCloud != newCloud) {
				isNewCloud = true;
				if (oldCloud != null) {
					oldCloud.removeStatusListener(_cloudStatusListener);
				}
			}
		} else {
			isNewCloud = true;
		}
		if (isNewCloud && newCloud != null) {
			newCloud.addStatusListener(_cloudStatusListener);
		}
	}


	private void updateBugCommentsView() {
		if (_bugCollection == null) {
			_signInOutLink.setVisible(false);
			_changeLink.setVisible(false);
			_cloudDetailsLabel.setText("");
			_titleLabel.setText("<html>FindBugs Cloud");
			return;
		}
		_changeLink.setVisible(true);
		final HTMLDocument doc = (HTMLDocument) _cloudReportPane.getDocument();
		final Cloud cloud = _bugCollection.getCloud();
		try {
			doc.remove(0, doc.getLength());
			doc.insertString(0, cloud.getCloudReport(_bugInstance), null);
		} catch (BadLocationException e) {
			// probably won't happen
		}
		final CloudPlugin plugin = cloud.getPlugin();
		_cloudDetailsLabel.setText(plugin.getDetails());
		final Cloud.SigninState state = cloud.getSigninState();
		final String stateStr = state == Cloud.SigninState.NO_SIGNIN_REQUIRED ? "" : "" + state;
		final String userStr = cloud.getUser() == null ? "" : cloud.getUser();
		_titleLabel.setText("<html><b>Comments - " + cloud.getCloudName() + "</b>"
                + "<br><font style='font-size: x-small;color:darkgray'>" + stateStr
                + (userStr.length() > 0 ? " - " + userStr : ""));
        _addCommentLink.setVisible(cloud.canStoreUserAnnotation(_bugInstance));
		switch (state) {
			case NO_SIGNIN_REQUIRED:
			case SIGNING_IN:
				_signInOutLink.setVisible(false);
				break;
			case SIGNED_OUT:
			case SIGNIN_FAILED:
			case UNAUTHENTICATED:
				_signInOutLink.setText("sign in");
				_signInOutLink.setVisible(true);
				break;
			case SIGNED_IN:
				_signInOutLink.setText("sign out");
				_signInOutLink.setVisible(true);
				break;
			default:
		}
	}


	{
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}


	*//**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 *//*
	private void $$$setupUI$$$() {
		_mainPanel = new JPanel();
		_mainPanel.setLayout(new GridBagLayout());
		final JPanel spacer1 = new JPanel();
		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.fill = GridBagConstraints.BOTH;
		_mainPanel.add(spacer1, gbc);
		_addCommentLink = new LinkLabel();
		_addCommentLink.setText("Add Comment");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.ipadx = 5;
		gbc.ipady = 5;
		_mainPanel.add(_addCommentLink, gbc);
		_commentEntryPanel = new JPanel();
		_commentEntryPanel.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridheight = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 5, 5);
		_mainPanel.add(_commentEntryPanel, gbc);
		_commentEntryPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null));
		_submitCommentButton = new JButton();
		_submitCommentButton.setText("Submit");
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.insets = new Insets(5, 5, 5, 5);
		_commentEntryPanel.add(_submitCommentButton, gbc);
		_classificationCombo = new JComboBox();
		final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
		defaultComboBoxModel1.addElement("Unclassified");
		defaultComboBoxModel1.addElement("Not a bug");
		defaultComboBoxModel1.addElement("Etc");
		_classificationCombo.setModel(defaultComboBoxModel1);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		_commentEntryPanel.add(_classificationCombo, gbc);
		final JLabel label1 = new JLabel();
		label1.setText("Classification:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		_commentEntryPanel.add(label1, gbc);
		final JScrollPane scrollPane1 = new JScrollPane();
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 4;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		_commentEntryPanel.add(scrollPane1, gbc);
		scrollPane1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
		_commentBox = new JTextArea();
		_commentBox.setRows(5);
		_commentBox.setText("My comment");
		scrollPane1.setViewportView(_commentBox);
		_cancelLink = new LinkLabel();
		_cancelLink.setText("cancel");
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 2;
		_commentEntryPanel.add(_cancelLink, gbc);
		final JPanel spacer2 = new JPanel();
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		_commentEntryPanel.add(spacer2, gbc);
		_cloudReportScrollPane = new JScrollPane();
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 5, 5);
		_mainPanel.add(_cloudReportScrollPane, gbc);
		_cloudReportScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null));
		_cloudReportPane = new JEditorPane();
		_cloudReportPane.setContentType("text/html");
		_cloudReportPane.setEditable(false);
		_cloudReportPane.setText("<html>\r\n  <head>\r\n    \r\n  </head>\r\n  <body>\r\n  </body>\r\n</html>\r\n");
		_cloudReportScrollPane.setViewportView(_cloudReportPane);
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridBagLayout());
		panel1.setBackground(new Color(-3355444));
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		_mainPanel.add(panel1, gbc);
		panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16751002)), null));
		_titleLabel = new JLabel();
		_titleLabel.setFont(new Font(_titleLabel.getFont().getName(), Font.BOLD, 14));
		_titleLabel.setForeground(new Color(-16777216));
		_titleLabel.setText("FindBugs Cloud - signed in");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		panel1.add(_titleLabel, gbc);
		_signInOutLink = new LinkLabel();
		_signInOutLink.setText("sign out");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		panel1.add(_signInOutLink, gbc);
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 5, 5);
		_mainPanel.add(panel2, gbc);
		_changeLink = new LinkLabel();
		_changeLink.setText("change");
		_changeLink.setToolTipText("Choose where comments are stored");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		panel2.add(_changeLink, gbc);
		_cloudDetailsLabel = new JTextArea();
		_cloudDetailsLabel.setEditable(false);
		_cloudDetailsLabel.setFont(new Font(_cloudDetailsLabel.getFont().getName(), Font.ITALIC, 10));
		_cloudDetailsLabel.setForeground(new Color(-10066330));
		_cloudDetailsLabel.setLineWrap(true);
		_cloudDetailsLabel.setMaximumSize(new Dimension(100, 50));
		_cloudDetailsLabel.setMinimumSize(new Dimension(50, 16));
		_cloudDetailsLabel.setOpaque(false);
		_cloudDetailsLabel.setPreferredSize(new Dimension(100, 31));
		_cloudDetailsLabel.setText("Comments are stored on the FindBugs Cloud at http://findbugs-cloud.appspot.com");
		_cloudDetailsLabel.setWrapStyleWord(true);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		panel2.add(_cloudDetailsLabel, gbc);
		label1.setLabelFor(_classificationCombo);
	}


	*//** @noinspection ALL *//*
	public JComponent $$$getRootComponent$$$() {
		return _mainPanel;
	}
}*/

