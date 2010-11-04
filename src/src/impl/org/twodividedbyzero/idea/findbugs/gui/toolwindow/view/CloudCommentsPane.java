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

package org.twodividedbyzero.idea.findbugs.gui.toolwindow.view;

import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.I18N;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.cloud.Cloud.UserDesignation;

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
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CloudCommentsPane extends JPanel {

    private JEditorPane _cloudReportPane;
    private LinkLabel _addCommentLink;
    private JTextArea _commentBox;
    private JButton _submitCommentButton;
    private JPanel _commentEntryPanel;
    private JComboBox _classificationCombo;
    private JPanel _mainPanel;
    private LinkLabel _cancelLink;
    private JScrollPane _cloudReportScrollPane;
    private SortedBugCollection _bugCollection;
    private BugInstance _bugInstance;

    public CloudCommentsPane() {
        setLayout(new BorderLayout());
        this.add(_mainPanel, BorderLayout.CENTER);

        _classificationCombo.removeAllItems();
        for (UserDesignation designation : UserDesignation.values()) {
            _classificationCombo.addItem(I18N.instance().getUserDesignation(designation.name()));
        }

        _commentEntryPanel.setVisible(false);
        _addCommentLink.setListener(new LinkListener() {
            public void linkSelected(LinkLabel linkLabel, Object o) {
                _commentEntryPanel.setVisible(true);
                _addCommentLink.setVisible(false);
                _commentBox.requestFocus();
                _commentBox.setSelectionStart(0);
                _commentBox.setSelectionEnd(_commentBox.getText().length());
                CloudCommentsPane.this.invalidate();
            }
        }, null);
        _cancelLink.setListener(new LinkListener() {
            public void linkSelected(LinkLabel linkLabel, Object o) {
                _commentEntryPanel.setVisible(false);
                _addCommentLink.setVisible(true);
                CloudCommentsPane.this.invalidate();
            }
        }, null);
        _submitCommentButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String comment = _commentBox.getText();
                int index = _classificationCombo.getSelectedIndex();
                UserDesignation choice;
                if (index == -1)
                    choice = UserDesignation.UNCLASSIFIED;
                else
                    choice = UserDesignation.values()[index];
                _bugInstance.setUserDesignationKey(choice.name(), _bugCollection);
                _bugInstance.setAnnotationText(comment, _bugCollection);

                _commentBox.setText("My comment");

                updateBugCommentsView();

                _commentEntryPanel.setVisible(false);
                _addCommentLink.setVisible(true);
                CloudCommentsPane.this.invalidate();
            }
        });
    }

    public void setBugInstance(SortedBugCollection bugCollection, BugInstance bugInstance) {
        this._bugCollection = bugCollection;
        this._bugInstance = bugInstance;
        updateBugCommentsView();
    }

    private void updateBugCommentsView() {
        HTMLDocument doc = (HTMLDocument) _cloudReportPane.getDocument();
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, _bugCollection.getCloud().getCloudReport(_bugInstance), null);
        } catch (BadLocationException e) {
            // probably won't happen
        }
        TitledBorder border1 = (TitledBorder) _cloudReportScrollPane.getBorder();
        border1.setTitle(_bugCollection.getCloud().getPlugin().getDescription());
        _cloudReportScrollPane.invalidate();
        _cloudReportScrollPane.repaint();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        _mainPanel = new JPanel();
        _mainPanel.setLayout(new GridBagLayout());
        final JPanel spacer1 = new JPanel();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        _mainPanel.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        _mainPanel.add(spacer2, gbc);
        _addCommentLink = new LinkLabel();
        _addCommentLink.setText("Add Comment");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.ipadx = 5;
        gbc.ipady = 5;
        _mainPanel.add(_addCommentLink, gbc);
        _commentEntryPanel = new JPanel();
        _commentEntryPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
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
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        _commentEntryPanel.add(spacer3, gbc);
        _cloudReportScrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        _mainPanel.add(_cloudReportScrollPane, gbc);
        _cloudReportScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Comments - FindBugs Cloud"));
        _cloudReportPane = new JEditorPane();
        _cloudReportPane.setContentType("text/html");
        _cloudReportPane.setEditable(false);
        _cloudReportPane.setText("<html>\r\n  <head>\r\n\r\n  </head>\r\n  <body>\r\n    <p style=\"margin-top: 0\">\r\n      \r\n    </p>\r\n  </body>\r\n</html>\r\n");
        _cloudReportScrollPane.setViewportView(_cloudReportPane);
        label1.setLabelFor(_classificationCombo);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return _mainPanel;
    }
}
