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
package org.twodividedbyzero.idea.findbugs.gui;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.cloud.Cloud;
import edu.umd.cs.findbugs.cloud.Cloud.CloudListener;
import edu.umd.cs.findbugs.cloud.Cloud.CloudTask;
import edu.umd.cs.findbugs.cloud.Cloud.CloudTaskListener;
import edu.umd.cs.findbugs.gui2.AbstractSwingGuiCallback;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.ToolWindowPanel;

import java.awt.EventQueue;
import java.util.concurrent.CountDownLatch;

public class PluginGuiCallback extends AbstractSwingGuiCallback {

	@NotNull
	private final Project project;

	private Cloud _cloud;

	private final CloudListener _cloudListener = new CloudListener() {
		public void issueUpdated(final BugInstance bug) {
			final ToolWindowPanel toolWindowPanel = ToolWindowPanel.getInstance(project);
			if (toolWindowPanel != null) {
				toolWindowPanel.getBugDetailsComponents().issueUpdated(bug);
			}
		}

		public void statusUpdated() {
			final StatusBar sb = WindowManager.getInstance().getStatusBar(project);
			if (sb != null && _cloud != null)
				sb.setInfo(_cloud.getStatusMsg());
		}

		public void taskStarted(final CloudTask task) {
			task.setUseDefaultListener(false);
			final Task backgroundable = new Backgroundable(project, task.getName(), false) {

				@Override
				public void run(@NotNull final ProgressIndicator progressIndicator) {
					try {
						final CountDownLatch latch = new CountDownLatch(1);
						task.addListener(new CloudTaskListener() {
							public void taskStatusUpdated(final String statusLine, final double percentCompleted) {
								progressIndicator.setText(statusLine);
								progressIndicator.setFraction(percentCompleted / 100.0);
							}

							public void taskFinished() {
								latch.countDown();
							}

							public void taskFailed(final String message) {
								progressIndicator.setText(message);
								latch.countDown();
							}
						});
						if (!task.isFinished())
							latch.await();
					} catch (final InterruptedException e) {
						throw new IllegalStateException(e);
					}
				}
			};
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					backgroundable.queue();
				}
			});
		}
	};

	public PluginGuiCallback(@NotNull final Project project) {
		super(ToolWindowPanel.getInstance(project));
		this.project = project;
	}

	public void setErrorMessage(final String errorMsg) {
	}

	public void registerCloud(final edu.umd.cs.findbugs.Project project, final BugCollection collection, final Cloud cloud) {
		_cloud = cloud;
		cloud.addListener(_cloudListener);
	}

	public void unregisterCloud(final edu.umd.cs.findbugs.Project project, final BugCollection collection, final Cloud cloud) {
		//noinspection ObjectEquality
		if (cloud == _cloud) {
			//noinspection AssignmentToNull
			_cloud = null;
			cloud.removeListener(_cloudListener);
		}
	}
}
