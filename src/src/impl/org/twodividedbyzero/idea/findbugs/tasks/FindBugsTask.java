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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.IFindBugsEngine;
import edu.umd.cs.findbugs.SortedBugCollection;
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
	private final IFindBugsEngine _engine;
	private final boolean _startInBackground;
    private final SortedBugCollection _bugCollection;
	private final edu.umd.cs.findbugs.Project _findBugsProject;


	public FindBugsTask(@Nullable final Project project, SortedBugCollection bugCollection, @NotNull final String title, final boolean canBeCancelled, final IFindBugsEngine engine, final boolean startInBackground) {
		super(project, title, canBeCancelled);
        _bugCollection = bugCollection;
        setCancelText("Cancel");  // NON-NLS
		asBackgroundable();
		_startInBackground = startInBackground;
		_engine = engine;
		_findBugsProject = _engine.getProject();
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
				_indicator.setIndeterminate(true);
			}
		});

		runFindBugs(_engine);
	}


	@SuppressWarnings({"MethodMayBeStatic"})
	public void runFindBugs(final IFindBugsEngine findBugs) {
		// bug 1828973 was fixed by findbugs engine, so that workaround to start the
		// analysis in an extra thread is not more needed
		try {
			// Perform the analysis! (note: This is not thread-safe)
			//CommandProcessor.getInstance().executeCommand();
			findBugs.execute();

		} catch (InterruptedException e) {
			//LOGGER.error("FindBugsWorker interrupted.", e);
			//noinspection ThrowableResultOfMethodCallIgnored
			FindBugsPluginImpl.processError("FindBugsWorker interrupted.", e);
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			LOGGER.error("Error performing FindBugs analysis", e);
			//noinspection ThrowableResultOfMethodCallIgnored
			FindBugsPluginImpl.processError("Error performing FindBugs analysis", e);
		} finally {
			//final Reporter bugReporter = (Reporter) findBugs.getBugReporter();
			((FindBugs2) findBugs).dispose();
		}

        _bugCollection.setDoNotUseCloud(false);
        _bugCollection.setTimestamp(System.currentTimeMillis());
        _bugCollection.reinitializeCloud();
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


	public edu.umd.cs.findbugs.Project getFindBugsProject() {
		return _findBugsProject;
	}
}
