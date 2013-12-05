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
package org.twodividedbyzero.idea.findbugs.tasks;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * <pre>
 * final FindBugsTask findBugsTask = new FindBugsTask(_project, "FindBugs", true, engine);
 * _bugReporter.setFindBugsTask(findBugsTask);
 * findBugsTask.setCancelText("Cancel");
 * findBugsTask.asBackgroundable();
 * findBugsTask.queue();
 * </pre>
 * <p/>
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public abstract class BackgroundableTask extends Backgroundable {

	public BackgroundableTask(@org.jetbrains.annotations.Nullable final Project project, @NotNull final String title, final boolean canBeCancelled) {
		super(project, title, canBeCancelled);
	}


	@Override
	public boolean shouldStartInBackground() {
		return false;
	}


	@Override
	public void processSentToBackground() {
		super.processSentToBackground();
	}


	/**
	 * The progress indicator member needs to be set in side this method.
	 *
	 * @param indicator the progress indicator to use
	 * @see #setProgressIndicator(com.intellij.openapi.progress.ProgressIndicator)
	 */
	@Override
	public abstract void run(@NotNull final ProgressIndicator indicator);


	public abstract void setProgressIndicator(@NotNull final ProgressIndicator indicator);


	public abstract ProgressIndicator getProgressIndicator();


	public void setIndicatorText(final String text) {
		setIndicatorText(text, null);
	}


	public void setIndicatorText2(final String text2) {
		final ProgressIndicator progressIndicator = getProgressIndicator();
		if (progressIndicator != null) {
			progressIndicator.setText2(text2);
		}
	}


	void setIndicatorText(final String text, @Nullable final String text2) {
		final ProgressIndicator progressIndicator = getProgressIndicator();
		if (progressIndicator != null) {
			progressIndicator.setText(text);

			if (text2 != null && text2.isEmpty() || text2 != null) {
				progressIndicator.setText(text2);
			}
		}
	}

}
