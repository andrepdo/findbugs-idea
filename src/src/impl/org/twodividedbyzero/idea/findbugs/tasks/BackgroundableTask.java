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

			if ("".equals(text2) || text2 != null) {
				progressIndicator.setText(text2);
			}
		}
	}

}
