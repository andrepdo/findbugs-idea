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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.IFindBugsEngine2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;

import java.io.IOException;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class FindBugsTask extends BackgroundableTask {

	private static final Logger LOGGER = Logger.getInstance(FindBugsTask.class.getName());

	private ProgressIndicator _indicator;
	private IFindBugsEngine2 _engine;
	private boolean _startInBackground;


	public FindBugsTask(@Nullable final Project project, @NotNull final String title, final boolean canBeCancelled, final IFindBugsEngine2 engine, final boolean startInBackground) {
		super(project, title, canBeCancelled);
		setCancelText("Cancel");  // NON-NLS
		asBackgroundable();
		_startInBackground = startInBackground;
		_engine = engine;
	}


	@Override
	public boolean shouldStartInBackground() {
		return _startInBackground;
	}


	/*public boolean execute(final CompileContext context) {
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				((FindBugsProject) _engine.getProject()).getRecursiveClassCollector().execute(context);
				queue();
			}
		});

		return true;
	}*/


	@Override
	public void run(@NotNull final ProgressIndicator indicator) {
		setProgressIndicator(indicator);

		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				_indicator.setText("FindBugs analysis...");
				//performing some operations
				//indicator.setFraction(0.2); // Activity percentage
				_indicator.setIndeterminate(true);
				//indicator.setFraction(0);
				//_indicator.setText2("Bug count: 0");

			}
		});

		runFindBugs(_engine);
	}


	@SuppressWarnings({"MethodMayBeStatic"})
	public void runFindBugs(final IFindBugsEngine2 findBugs) {
		// bug 1828973 was fixed by findbugs engine, so that workaround to start the
		// analysis in an extra thread is not more needed
		try {
			// Perform the analysis! (note: This is not thread-safe)
			//CommandProcessor.getInstance().executeCommand();
			findBugs.execute();

		} catch (InterruptedException e) {
			//LOGGER.error("FindBugsWorker interrupted.", e);
			//noinspection ThrowableResultOfMethodCallIgnored
			FindBugsPluginImpl.processError("FindBugsWorker interrupted.", e);// NON-NLS
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			LOGGER.error("Error performing FindBugs analysis", e);
			//noinspection ThrowableResultOfMethodCallIgnored
			FindBugsPluginImpl.processError("Error performing FindBugs analysis", e);// NON-NLS
		} finally {
			//final Reporter bugReporter = (Reporter) findBugs.getBugReporter();
			((FindBugs2) findBugs).dispose();
		}
	}


	@Override
	public void setProgressIndicator(@NotNull final ProgressIndicator indicator) {
		_indicator = indicator;
	}


	@Nullable
	@Override
	public ProgressIndicator getProgressIndicator() {
		return _indicator;
	}
}
