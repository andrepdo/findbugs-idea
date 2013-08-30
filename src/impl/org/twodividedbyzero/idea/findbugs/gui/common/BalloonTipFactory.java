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
package org.twodividedbyzero.idea.findbugs.gui.common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.Balloon.Position;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;

import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.event.HyperlinkListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
public class BalloonTipFactory {

	public enum Orientation {

		LEFT(Balloon.Position.atLeft),
		RIGHT(Balloon.Position.atRight),
		ABOVE(Balloon.Position.above),
		BELOW(Balloon.Position.below);

		private final Balloon.Position _orientation;


		Orientation(final Position orientation) {
			_orientation = orientation;
		}


		public Balloon.Position getOrientation() {
			return _orientation;
		}
	}


	public enum NotificationType {

		INFO, WARNING, ERROR
	}


	public static void showToolWindowInfoNotifier(final Project project, final String html) {
		showToolWindowInfoNotifier(project, html, null);
	}


	public static void showToolWindowInfoNotifier(final Project project, final String html, @Nullable final HyperlinkListener hyperlinkListener) {
		final ToolWindowManager manager = ToolWindowManager.getInstance(project);
		if (manager == null) { // this should never happen.
			return;
		}
		manager.notifyByBalloon(IdeaUtilImpl.getPluginComponent(project).getInternalToolWindowId(), MessageType.INFO, html, null, hyperlinkListener);
	}


	public static void showToolWindowWarnNotifier(final Project project, final String html) {
		showToolWindowWarnNotifier(project, html, null);
	}


	public static void showToolWindowWarnNotifier(final Project project, final String html, @Nullable final HyperlinkListener hyperlinkListener) {
		final ToolWindowManager manager = ToolWindowManager.getInstance(project);
		if (manager == null) { // this should never happen.
			return;
		}
		manager.notifyByBalloon(IdeaUtilImpl.getPluginComponent(project).getInternalToolWindowId(), MessageType.WARNING, html, null, hyperlinkListener);
	}


	public static void showToolWindowErrorNotifier(final Project project, final String html) {
		showToolWindowErrorNotifier(project, html, null);
	}


	public static void showToolWindowErrorNotifier(final Project project, final String html, @Nullable final HyperlinkListener hyperlinkListener) {
		final ToolWindowManager manager = ToolWindowManager.getInstance(project);
		if (manager == null) { // this should never happen.
			return;
		}
		manager.notifyByBalloon(IdeaUtilImpl.getPluginComponent(project).getInternalToolWindowId(), MessageType.ERROR, html, null, hyperlinkListener);
	}


	public static void showPopup(final Project project, final Component parent, final String html, final Orientation orientation, @Nullable final Icon icon, final Color bgColor) {
		final BalloonBuilder builder = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(html, icon, bgColor, null);
		_createBalloon(project, parent, orientation, builder);
	}


	public static void showInfoPopup(final Project project, final Component parent, final String html, final Orientation orientation) {
		final MessageType type = MessageType.INFO;
		final BalloonBuilder builder = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(html, type.getDefaultIcon(), type.getPopupBackground(), null);
		_createBalloon(project, parent, orientation, builder);
	}


	public static void showWarnPopup(final Project project, final Component parent, final String html, final Orientation orientation) {
		final MessageType type = MessageType.WARNING;
		final BalloonBuilder builder = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(html, type.getDefaultIcon(), type.getPopupBackground(), null);
		_createBalloon(project, parent, orientation, builder);
	}


	public static void showErrorPopup(final Project project, final Component parent, final String html, final Orientation orientation) {
		final MessageType type = MessageType.ERROR;
		final BalloonBuilder builder = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(html, type.getDefaultIcon(), type.getPopupBackground(), null);
		_createBalloon(project, parent, orientation, builder);
	}


	private static void _createBalloon(final Project project, final Component parent, final Orientation orientation, final BalloonBuilder builder) {
		builder.setHideOnClickOutside(true);
		builder.setHideOnKeyOutside(true);
		final Balloon balloon = builder.createBalloon();
		balloon.show(new RelativePoint(parent, new Point(parent.getWidth() / 2, parent.getHeight() / 2)), orientation.getOrientation());
	}


	private BalloonTipFactory() {
	}

}
