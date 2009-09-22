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
package org.twodividedbyzero.idea.findbugs.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import edu.umd.cs.findbugs.Version;
import org.jetbrains.annotations.NonNls;
import org.twodividedbyzero.idea.findbugs.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.common.VersionManager;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsUtil;
import org.twodividedbyzero.idea.findbugs.gui.common.BalloonTipFactory;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;


/**
 * $Date$
 *
 * @version $Revision$
 */
public class HelpAction extends BaseAction {

	@NonNls
	@SuppressWarnings({"StringBufferField"})
	private static final StringBuilder HTML_BODY;


	static {
		HTML_BODY = new StringBuilder();
		HTML_BODY.append("<p>");
		HTML_BODY.append("<h2>").append(VersionManager.getFullVersion()).append("</h2>");
		HTML_BODY.append("Website: <a href='").append(VersionManager.getWebsite()).append("'>").append(VersionManager.getWebsite()).append("</a>");
		HTML_BODY.append("<br>");
		HTML_BODY.append("Download: <a href='").append(VersionManager.getDownloadWebsite()).append("'>").append(VersionManager.getDownloadWebsite()).append("</a>");
		HTML_BODY.append("<br>");
		HTML_BODY.append("Email: ").append(VersionManager.getSupportEmail());
		HTML_BODY.append("</p>");
		//HTML_BODY.append("<a href=\"http://www.jetbrains.com/idea/\" style=\"position: relative;display:block; width:88px; height:31px; border:0; margin:0;padding:0;text-decoration:none;text-indent:0;\"><span style=\"margin: 0;padding: 0;position: absolute;top: -1px;left: 4px;font-size: 10px;cursor:pointer;  background-image:none;border:0;color: #acc4f9; font-family: trebuchet ms,arial,sans-serif;font-weight: normal;\">Hooked on</span><img src=\"http://www.jetbrains.com/idea/opensource/img/all/banners/idea88x31_blue.gif\" alt=\"The best Java IDE\" border=\"0\"/></a>");
		HTML_BODY.append("<p>");
		HTML_BODY.append("<h3>Findbugs ").append(FindBugsUtil.getFindBugsFullVersion()).append("</h3>");
		HTML_BODY.append("Website: <a href='").append(Version.WEBSITE).append("'>").append(Version.WEBSITE).append("</a>");
		HTML_BODY.append("<br>");
		HTML_BODY.append("Download: <a href='").append(Version.DOWNLOADS_WEBSITE).append("'>").append(Version.DOWNLOADS_WEBSITE).append("</a>");
		HTML_BODY.append("<br>");
		HTML_BODY.append("Email: ").append(Version.SUPPORT_EMAIL);
		HTML_BODY.append("</p>");

	}


	@Override
	public void actionPerformed(final AnActionEvent e) {
		final Project project = DataKeys.PROJECT.getData(e.getDataContext());
		if (project == null) {
			return;
		}

		final FindBugsPlugin findBugsPlugin = project.getComponent(FindBugsPlugin.class);
		if (findBugsPlugin == null) {
			throw new IllegalStateException("Couldn't get findbugs plugin");
		}

		BalloonTipFactory.showPopup(e.getInputEvent().getComponent(), HTML_BODY.toString(), BalloonTipFactory.Orientation.RIGHT, GuiResources.FINDBUGS_ICON, MessageType.INFO.getPopupBackground()); // NON-NLS
	}


	@Override
	protected boolean isEnabled() {
		return true;
	}


	@Override
	protected boolean setEnabled(final boolean enabled) {
		return true;
	}
}
