/*
 * Copyright 2016 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.core;

import com.intellij.CommonBundle;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.gui.common.NotificationUtil;

import javax.swing.event.HyperlinkEvent;

public class AbstractSuggestionNotificationListener implements NotificationListener {
	public static final String A_HREF_DISABLE_ANCHOR = "#disable";

	@NotNull
	private final Project project;

	@NotNull
	private final String notificationGroupId;

	protected AbstractSuggestionNotificationListener(@NotNull final Project project, @NotNull final String notificationGroupId) {
		this.project = project;
		this.notificationGroupId = notificationGroupId;
	}

	@Override
	public final void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
		if (event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
			final String desc = event.getDescription();
			if (desc.equals(A_HREF_DISABLE_ANCHOR)) {
				final int result = Messages.showYesNoDialog(
						project,
						"Notification will be disabled for all projects.\n\n" +
								"Settings | Appearance & Behavior | Notifications | " +
								notificationGroupId +
								"\ncan be used to configure the notification.",
						"FindBugs Plugin Suggestion Notification",
						"Disable Notification", CommonBundle.getCancelButtonText(), Messages.getWarningIcon());
				if (result == Messages.YES) {
					NotificationUtil.getNotificationsConfigurationImpl().changeSettings(
							notificationGroupId,
							NotificationDisplayType.NONE, false, false);
					notification.expire();
				} else {
					notification.hideBalloon();
				}
			} else {
				linkClicked(notification, desc);
			}
		}
	}

	protected void linkClicked(@NotNull final Notification notification, String description) {
	}
}
