/*
 * Copyright 2010 Andre Pfeiler
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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import edu.umd.cs.findbugs.Version;
import org.jetbrains.annotations.NonNls;
import org.twodividedbyzero.idea.findbugs.common.VersionManager;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsUtil;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
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
		HTML_BODY.append("<p>");
		HTML_BODY.append("<h3>Findbugs ").append(FindBugsUtil.getFindBugsFullVersion()).append("</h3>");
		HTML_BODY.append("Website: <a href='").append(Version.WEBSITE).append("'>").append(Version.WEBSITE).append("</a>");
		HTML_BODY.append("<br>");
		HTML_BODY.append("Download: <a href='").append(Version.DOWNLOADS_WEBSITE).append("'>").append(Version.DOWNLOADS_WEBSITE).append("</a>");
		HTML_BODY.append("<br>");
		HTML_BODY.append("Email: ").append(Version.SUPPORT_EMAIL);
		HTML_BODY.append("</p>");
		HTML_BODY.append("<p><br/>");
		HTML_BODY.append("The name FindBugs™ and the FindBugs logo are trademarked by The University of Maryland.");
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

		BalloonTipFactory.showPopup(e.getInputEvent().getComponent(), HTML_BODY.toString(), BalloonTipFactory.Orientation.RIGHT, GuiResources.FINDBUGS_ICON, MessageType.INFO.getPopupBackground());
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
