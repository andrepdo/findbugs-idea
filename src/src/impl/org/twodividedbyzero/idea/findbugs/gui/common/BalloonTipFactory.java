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
package org.twodividedbyzero.idea.findbugs.gui.common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.Balloon.Position;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;

import javax.annotation.Nullable;
import javax.swing.event.HyperlinkListener;

public final class BalloonTipFactory {

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

	public static void showToolWindowInfoNotifier(@NotNull final Project project, final String html) {
		showToolWindowInfoNotifier(project, html, null);
	}

	public static void showToolWindowInfoNotifier(@NotNull final Project project, final String html, @Nullable final HyperlinkListener hyperlinkListener) {
		final ToolWindowManager manager = ToolWindowManager.getInstance(project);
		if (manager == null) { // this should never happen.
			return;
		}
		manager.notifyByBalloon(FindBugsPluginConstants.TOOL_WINDOW_ID, MessageType.INFO, html, null, hyperlinkListener);
	}

	public static void showToolWindowWarnNotifier(@NotNull final Project project, final String html) {
		showToolWindowWarnNotifier(project, html, null);
	}

	public static void showToolWindowWarnNotifier(@NotNull final Project project, final String html, @Nullable final HyperlinkListener hyperlinkListener) {
		final ToolWindowManager manager = ToolWindowManager.getInstance(project);
		if (manager == null) { // this should never happen.
			return;
		}
		manager.notifyByBalloon(FindBugsPluginConstants.TOOL_WINDOW_ID, MessageType.WARNING, html, null, hyperlinkListener);
	}

	public static void showToolWindowErrorNotifier(@NotNull final Project project, final String html) {
		showToolWindowErrorNotifier(project, html, null);
	}

	public static void showToolWindowErrorNotifier(@NotNull final Project project, final String html, @Nullable final HyperlinkListener hyperlinkListener) {
		final ToolWindowManager manager = ToolWindowManager.getInstance(project);
		if (manager == null) { // this should never happen.
			return;
		}
		manager.notifyByBalloon(FindBugsPluginConstants.TOOL_WINDOW_ID, MessageType.ERROR, html, null, hyperlinkListener);
	}

	private BalloonTipFactory() {
	}
}
