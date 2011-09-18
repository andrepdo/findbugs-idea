/*
 * Copyright 2011 Andre Pfeiler
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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import edu.umd.cs.findbugs.cloud.CloudPlugin;
import edu.umd.cs.findbugs.gui2.CloudCommentsPane;
import org.twodividedbyzero.idea.findbugs.common.util.GuiUtil;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import java.awt.Graphics;
import java.util.List;

public class CloudCommentsPaneIntellij extends CloudCommentsPane {

	private static final Logger LOGGER = Logger.getInstance(CloudCommentsPane.class.getName());

    private final ToolWindowPanel _toolWindowPanel;

    public CloudCommentsPaneIntellij(final ToolWindowPanel toolWindowPanel) {
        _toolWindowPanel = toolWindowPanel;
    }


	@Override
	protected void paintComponent(final Graphics g) {
		GuiUtil.configureGraphics(g);
		super.paintComponent(g);
	}


	@Override
	protected void paintChildren(final Graphics g) {
		GuiUtil.configureGraphics(g);
		super.paintChildren(g);
	}


	protected void setSignInOutText(String buttonText) {
        ((LinkLabel)_signInOutLink).setText(buttonText);
    }

    protected void setupLinksOrButtons() {
        _addCommentLink = new LinkLabel();
        ((LinkLabel)_addCommentLink).setText("add comment");
        ((LinkLabel)_addCommentLink).setListener(new LinkListener() {
            public void linkSelected(final LinkLabel linkLabel, final Object o) {
                addCommentClicked();
            }
        }, null);
        _cancelLink = new LinkLabel();
        ((LinkLabel)_cancelLink).setText("cancel");
        ((LinkLabel)_cancelLink).setListener(new LinkListener() {
            public void linkSelected(final LinkLabel linkLabel, final Object o) {
                cancelClicked();
            }
        }, null);
        _signInOutLink = new LinkLabel();
        ((LinkLabel)_signInOutLink).setText("sign in");
        ((LinkLabel)_signInOutLink).setListener(new LinkListener() {
            public void linkSelected(final LinkLabel linkLabel, final Object o) {
                signInOrOutClicked();
            }
        }, null);
        _changeLink = new LinkLabel();
        ((LinkLabel)_changeLink).setText("change");
        ((LinkLabel)_changeLink).setListener(new LinkListener() {
            public void linkSelected(final LinkLabel linkLabel, final Object o) {
                changeClicked();
            }
        }, null);

    }

    protected void showCloudChooser(final List<CloudPlugin> plugins, final List<String> descriptions) {
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
                        changeCloud(newCloudId);
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

    protected boolean isDisabled(CloudPlugin plugin) {
        final FindBugsPlugin findBugsPlugin = _toolWindowPanel.getProject().getComponent(FindBugsPlugin.class);
        final FindBugsPreferences prefs = findBugsPlugin.getPreferences();
        return prefs.isPluginDisabled(plugin.getFindbugsPluginId());
    }

    protected void issueUpdated() {
        _toolWindowPanel.getBugDetailsComponents().issueUpdated(_bugInstance);
    }
}
