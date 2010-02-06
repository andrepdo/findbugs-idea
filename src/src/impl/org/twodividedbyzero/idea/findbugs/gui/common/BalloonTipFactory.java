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

		private Balloon.Position _orientation;


		private Orientation(final Position orientation) {
			_orientation = orientation;
		}


		public Balloon.Position getOrientation() {
			return _orientation;
		}
	}


	public enum NotificationType {

		INFO, WARNING, ERROR
	}


	public static void showToolWindowInfoNotifier(final String html) {
		showToolWindowInfoNotifier(IdeaUtilImpl.getProject(), html);
	}


	public static void showToolWindowInfoNotifier(final Project project, final String html) {
		final ToolWindowManager manager = ToolWindowManager.getInstance(project);
		if (manager == null) { // this should never happen.
			return;
		}
		manager.notifyByBalloon(IdeaUtilImpl.getPluginComponent(project).getInternalToolWindowId(), MessageType.INFO, html);
	}


	public static void showToolWindowWarnNotifier(final String html) {
		showToolWindowWarnNotifier(IdeaUtilImpl.getProject(), html);
	}


	public static void showToolWindowWarnNotifier(final Project project, final String html) {
		final ToolWindowManager manager = ToolWindowManager.getInstance(project);
		if (manager == null) { // this should never happen.
			return;
		}
		manager.notifyByBalloon(IdeaUtilImpl.getPluginComponent(project).getInternalToolWindowId(), MessageType.WARNING, html);
	}


	public static void showToolWindowErrorNotifier(final String html) {
		showToolWindowErrorNotifier(IdeaUtilImpl.getProject(), html);
	}


	public static void showToolWindowErrorNotifier(final Project project, final String html) {
		final ToolWindowManager manager = ToolWindowManager.getInstance(project);
		if (manager == null) { // this should never happen.
			return;
		}
		manager.notifyByBalloon(IdeaUtilImpl.getPluginComponent(project).getInternalToolWindowId(), MessageType.ERROR, html);
	}


	public static void showPopup(final Component parent, final String html, final Orientation orientation, @Nullable final Icon icon, final Color bgColor) {
		final BalloonBuilder builder = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(html, icon, bgColor, null);
		_createBalloon(parent, orientation, builder);
	}


	public static void showInfoPopup(final Component parent, final String html, final Orientation orientation) {
		final MessageType type = MessageType.INFO;
		final BalloonBuilder builder = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(html, type.getDefaultIcon(), type.getPopupBackground(), null);
		_createBalloon(parent, orientation, builder);
	}


	public static void showWarnPopup(final Component parent, final String html, final Orientation orientation) {
		final MessageType type = MessageType.WARNING;
		final BalloonBuilder builder = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(html, type.getDefaultIcon(), type.getPopupBackground(), null);
		_createBalloon(parent, orientation, builder);
	}


	public static void showErrorPopup(final Component parent, final String html, final Orientation orientation) {
		final MessageType type = MessageType.ERROR;
		final BalloonBuilder builder = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(html, type.getDefaultIcon(), type.getPopupBackground(), null);
		_createBalloon(parent, orientation, builder);
	}


	private static void _createBalloon(final Component parent, final Orientation orientation, final BalloonBuilder builder) {
		builder.setHideOnClickOutside(true);
		builder.setHideOnKeyOutside(true);
		final Balloon balloon = builder.createBalloon();
		balloon.show(new RelativePoint(parent, new Point(parent.getWidth() / 2, parent.getHeight() / 2)), orientation.getOrientation());
	}


	private BalloonTipFactory() {
	}

}
