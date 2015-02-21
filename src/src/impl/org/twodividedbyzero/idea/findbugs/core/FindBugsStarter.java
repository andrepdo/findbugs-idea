/*
 * Copyright 2008-2015 Andre Pfeiler
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


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.IFindBugsEngine;
import edu.umd.cs.findbugs.Plugin;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.config.ProjectFilterSettings;
import edu.umd.cs.findbugs.config.UserPreferences;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.event.EventManagerImpl;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEventFactory;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.gui.PluginGuiCallback;
import org.twodividedbyzero.idea.findbugs.messages.AnalysisAbortedListener;
import org.twodividedbyzero.idea.findbugs.messages.MessageBusManager;
import org.twodividedbyzero.idea.findbugs.preferences.AnalysisEffort;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author Reto Merz<reto.merz@gmail.com>
 * @version $Revision: 369 $
 * @since 0.9.995
 */
public abstract class FindBugsStarter implements AnalysisAbortedListener {

	private static final Logger LOGGER = Logger.getInstance(FindBugsStarter.class.getName());

	private final Project _project;
	private final String _title;
	private final FindBugsPreferences _preferences;
	private final Map<String, String> _bugCategories;
	private final boolean _startInBackground;
	private final FindBugsPlugin _findBugsPlugin;
	private final AtomicBoolean _cancellingByUser;


	public FindBugsStarter(@NotNull final Project project, @NotNull final String title, @NotNull final FindBugsPreferences preferences) {
		_project = project;
		_title = title;
		_preferences = preferences;
		_bugCategories = new HashMap<String, String>(preferences.getBugCategories());
		_startInBackground = preferences.getBooleanProperty(FindBugsPreferences.RUN_ANALYSIS_IN_BACKGROUND, false);
		_findBugsPlugin = IdeaUtilImpl.getPluginComponent(_project);
		_cancellingByUser = new AtomicBoolean();
		MessageBusManager.subscribe(project, this, AnalysisAbortedListener.TOPIC, this);
	}


	public void start() {
		EventDispatchThreadHelper.checkEDT();
		MessageBusManager.publishAnalysisStarted();
		EventManagerImpl.getInstance().fireEvent(BugReporterEventFactory.newStarted(_project));

		if (Boolean.valueOf(_preferences.getProperty(FindBugsPreferences.TOOLWINDOW_TO_FRONT))) {
			final String toolWindowId = _findBugsPlugin.getInternalToolWindowId();
			final ToolWindow toolWindow = ToolWindowManager.getInstance(_project).getToolWindow(toolWindowId);
			IdeaUtilImpl.activateToolWindow(toolWindow);
		}

		new Task.Backgroundable(_project, _title, true) {
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				asyncStart(indicator);
			}
			@Override
			public boolean shouldStartInBackground() {
				return _startInBackground;
			}
		}.queue();
	}


	private void asyncStart(@NotNull final ProgressIndicator indicator) {

		final UserPreferences userPrefs = _preferences.getUserPreferences();
		{
			userPrefs.setEffort(_preferences.getProperty(FindBugsPreferences.ANALYSIS_EFFORT_LEVEL, AnalysisEffort.DEFAULT.getEffortLevel()));
			final ProjectFilterSettings projectFilterSettings = userPrefs.getFilterSettings();
			projectFilterSettings.setMinPriority(_preferences.getProperty(FindBugsPreferences.MIN_PRIORITY_TO_REPORT));
			FindBugsWorker.configureSelectedCategories(_preferences, projectFilterSettings);
			userPrefs.setIncludeFilterFiles(_preferences.getIncludeFiltersMap());
			userPrefs.setExcludeBugsFiles(_preferences.getExcludeBaselineBugsMap());
			userPrefs.setExcludeFilterFiles(_preferences.getExcludeFiltersMap());
		}

		final FindBugsProject findBugsProject = new FindBugsProject();
		{
			findBugsProject.setProjectName(_project.getName());
			for (final Plugin plugin : Plugin.getAllPlugins()) {
				findBugsProject.setPluginStatusTrinary(plugin.getPluginId(), !_preferences.isPluginDisabled(plugin.getPluginId()));
			}
			findBugsProject.setGuiCallback(new PluginGuiCallback(_findBugsPlugin));
		}

		final SortedBugCollection bugCollection = new SortedBugCollection(findBugsProject);
		bugCollection.setDoNotUseCloud(true);

		ApplicationManager.getApplication().runReadAction(new Runnable() {
			@Override
			public void run() {
				configure(indicator, findBugsProject);
			}
		});

		final Reporter reporter = new Reporter(_project, bugCollection, findBugsProject, _bugCategories, indicator, _cancellingByUser);
		reporter.setPriorityThreshold(userPrefs.getUserDetectorThreshold());

		final IFindBugsEngine engine = new FindBugs2();
		{
			engine.setNoClassOk(true);
			engine.setMergeSimilarWarnings(false);
			engine.setBugReporter(reporter);
			engine.setProject(findBugsProject);
			engine.setProgressCallback(reporter);
			FindBugsWorker.configureFilter(engine, _project, userPrefs);

			final DetectorFactoryCollection factoryCollection = FindBugsPreferences.getDetectorFactorCollection();
			engine.setDetectorFactoryCollection(factoryCollection);
			engine.setUserPreferences(userPrefs);
		}

		try {
			engine.execute();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (final IOException e) {
			LOGGER.error("Error performing FindBugs analysis", e);
		} finally {
			((FindBugs2)engine).dispose();
		}

		bugCollection.setDoNotUseCloud(false);
		bugCollection.setTimestamp(System.currentTimeMillis());
		bugCollection.reinitializeCloud();
	}


	protected abstract void configure(@NotNull final ProgressIndicator indicator, @NotNull final FindBugsProject findBugsProject);


	@Override
	public void analysisAborted() {
		_cancellingByUser.set(true);
	}
}