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
package org.twodividedbyzero.idea.findbugs.core;

import com.intellij.compiler.impl.CompositeScope;
import com.intellij.compiler.impl.OneProjectItemCompileScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.impl.ToolWindowImpl;
import com.intellij.util.Consumer;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.config.ProjectFilterSettings;
import edu.umd.cs.findbugs.config.UserPreferences;
import org.dom4j.DocumentException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.gui.common.BalloonTipFactory;
import org.twodividedbyzero.idea.findbugs.messages.AnalysisAbortingListener;
import org.twodividedbyzero.idea.findbugs.messages.MessageBusManager;
import org.twodividedbyzero.idea.findbugs.plugins.PluginLoader;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class FindBugsStarter implements AnalysisAbortingListener {

	private static final Logger LOGGER = Logger.getInstance(FindBugsStarter.class);

	@NotNull
	private final Project _project;

	@NotNull
	private final String _title;

	@NotNull
	private final ProjectSettings projectSettings;

	@NotNull
	private final WorkspaceSettings workspaceSettings;

	@NotNull
	private final AbstractSettings settings;

	private final boolean _startInBackground;
	private final AtomicBoolean _cancellingByUser;


	public FindBugsStarter(
			@NotNull final Project project,
			@NotNull final String title
	) {
		this(project, title, false);
	}


	public FindBugsStarter(
			@NotNull final Project project,
			@NotNull final String title,
			final boolean forceStartInBackground
	) {
		_project = project;
		_title = title;
		this.projectSettings = ProjectSettings.getInstance(project);
		this.workspaceSettings = WorkspaceSettings.getInstance(project);
		this.settings = projectSettings; // TODO
		_startInBackground = workspaceSettings.runInBackground || forceStartInBackground;
		_cancellingByUser = new AtomicBoolean();
		MessageBusManager.subscribe(project, this, AnalysisAbortingListener.TOPIC, this);
	}

	protected boolean isCompileBeforeAnalyze() {
		return workspaceSettings.compileBeforeAnalyze;
	}

	public final void start() {
		EventDispatchThreadHelper.checkEDT();
		if (isCompileBeforeAnalyze()) {
			final boolean isAnalyzeAfterCompile = workspaceSettings.analyzeAfterCompile;
			final CompilerManager compilerManager = CompilerManager.getInstance(_project);
			createCompileScope(compilerManager, new Consumer<CompileScope>() {
				@Override
				public void consume(@Nullable final CompileScope compileScope) {
					if (compileScope != null) {
						compilerManager.make(compileScope, new CompileStatusNotification() {
							@Override
							public void finished(final boolean aborted, final int errors, final int warnings, final CompileContext compileContext) {
								if (!aborted && errors == 0 && !isAnalyzeAfterCompile) {
									EventDispatchThreadHelper.checkEDT(); // see javadoc of CompileStatusNotification
									startImpl(true);
								}
							}
						});
					}
				}
			});
		} else {
			startImpl(false);
		}
	}

	private void startImpl(final boolean justCompiled) {
		MessageBusManager.publishAnalysisStarted(_project);

		final ToolWindow toolWindow = ToolWindowManager.getInstance(_project).getToolWindow(FindBugsPluginConstants.TOOL_WINDOW_ID);
		if (workspaceSettings.toolWindowToFront) {
			IdeaUtilImpl.activateToolWindow(toolWindow);
		} else {
			// Important: Make sure the tool window is initialized.
			((ToolWindowImpl) toolWindow).ensureContentInitialized();
		}

		new Task.Backgroundable(_project, _title, true) {
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				indicator.setIndeterminate(true);
				indicator.setText("Configure FindBugs...");
				try {
					asyncStart(indicator, justCompiled);
				} catch (final ProcessCanceledException ignore) {
					MessageBusManager.publishAnalysisAbortedToEDT(_project);
				}
			}

			@Override
			public boolean shouldStartInBackground() {
				return _startInBackground;
			}
		}.queue();
	}


	private void asyncStart(@NotNull final ProgressIndicator indicator, final boolean justCompiled) {

		final FindBugsProjects projects = new FindBugsProjects(_project);

		boolean canceled = !ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
			@Override
			public Boolean compute() {
				return configure(indicator, projects, justCompiled);
			}
		});

		final FindBugsResult result = new FindBugsResult();
		Throwable error = null;

		if (!canceled) {
			try {
				int numClassesOffset = 0;
				for (final Map.Entry<Module, FindBugsProject> entry : projects.getProjects().entrySet()) {
					final FindBugsProject findBugsProject = entry.getValue();
					final Module module = entry.getKey();
					indicator.setText("Start FindBugs analysis of " + findBugsProject.getProjectName());
					final Pair<SortedBugCollection, Reporter> data = executeImpl(indicator, module, findBugsProject, numClassesOffset);
					final int numClasses = data.getSecond().getProjectStats().getNumClasses();
					numClassesOffset += numClasses;
					result.put(findBugsProject, data.getFirst(), numClasses);
					if (data.getSecond().isCanceled()) {
						canceled = true;
						break;
					}
				}
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (final Throwable e) {
				error = e;
			}
		}

		if (canceled) {
			MessageBusManager.publishAnalysisAbortedToEDT(_project);
		} else {
			MessageBusManager.publishAnalysisFinishedToEDT(_project, result, error);
		}
	}

	private Pair<SortedBugCollection, Reporter> executeImpl(
			@NotNull final ProgressIndicator indicator,
			@NotNull final Module module,
			@NotNull final FindBugsProject findBugsProject,
			final int numClassesOffset
	) throws IOException, InterruptedException {

		if (!PluginLoader.load(_project, projectSettings, true)) {
			throw new ProcessCanceledException();
		}

		final DetectorFactoryCollection detectorFactoryCollection = DetectorFactoryCollection.instance();

		final UserPreferences userPrefs = UserPreferences.createDefaultUserPreferences();
		{
			userPrefs.setEffort(settings.analysisEffort);
			final ProjectFilterSettings projectFilterSettings = userPrefs.getFilterSettings();
			projectFilterSettings.setMinRank(settings.minRank);
			projectFilterSettings.setMinPriority(settings.minPriority);

			for (final String category : DetectorFactoryCollection.instance().getBugCategories()) {
				projectFilterSettings.removeCategory(category);
				projectFilterSettings.addCategory(category);
			}
			for (final String category : settings.hiddenBugCategory) {
				projectFilterSettings.removeCategory(category);
			}

			userPrefs.setIncludeFilterFiles(new HashMap<String, Boolean>(settings.includeFilterFiles));
			userPrefs.setExcludeBugsFiles(new HashMap<String, Boolean>(settings.excludeBugsFiles));
			userPrefs.setExcludeFilterFiles(new HashMap<String, Boolean>(settings.excludeFilterFiles));

			configureDetectors(projectSettings.detectors, detectorFactoryCollection, userPrefs);
			for (final PluginSettings pluginSettings : projectSettings.plugins) {
				configureDetectors(pluginSettings.detectors, detectorFactoryCollection, userPrefs);
			}
		}

		final SortedBugCollection bugCollection = new SortedBugCollection(findBugsProject);
		bugCollection.setDoNotUseCloud(true);

		final Reporter reporter = new Reporter(
				_project,
				module,
				bugCollection,
				new HashSet<String>(settings.hiddenBugCategory),
				indicator,
				_cancellingByUser,
				numClassesOffset
		);

		reporter.setPriorityThreshold(userPrefs.getUserDetectorThreshold());

		final FindBugs2 engine = new FindBugs2();
		{
			engine.setNoClassOk(true);
			engine.setMergeSimilarWarnings(false);
			engine.setBugReporter(reporter);
			engine.setProject(findBugsProject);
			engine.setProgressCallback(reporter);
			configureFilter(engine, userPrefs);
			engine.setDetectorFactoryCollection(detectorFactoryCollection);
			engine.setUserPreferences(userPrefs);
		}

		try {
			engine.execute();
		} finally {
			engine.dispose();
		}

		bugCollection.setDoNotUseCloud(false);
		bugCollection.setTimestamp(System.currentTimeMillis());
		bugCollection.reinitializeCloud();

		return Pair.create(bugCollection, reporter);
	}

	protected abstract void createCompileScope(@NotNull final CompilerManager compilerManager, @NotNull final Consumer<CompileScope> consumer);

	// like CompilerManagerImpl#createFilesCompileScope but Collection based
	@NotNull
	protected final CompileScope createFilesCompileScope(@NotNull final Collection<VirtualFile> files) {
		final CompileScope[] scopes = new CompileScope[files.size()];
		int i = 0;
		for (final VirtualFile file : files) {
			scopes[i++] = new OneProjectItemCompileScope(_project, file);
		}
		return new CompositeScope(scopes);
	}

	protected abstract boolean configure(
			@NotNull final ProgressIndicator indicator,
			@NotNull final FindBugsProjects projects,
			final boolean justCompiled
	);

	@Override
	public final void analysisAborting() {
		_cancellingByUser.set(true);
	}

	private static void configureDetectors(
			@NotNull final Map<String, Boolean> detectors,
			@NotNull final DetectorFactoryCollection detectorFactoryCollection,
			@NotNull final UserPreferences userPreferences
	) {
		for (final Map.Entry<String, Boolean> enabled : detectors.entrySet()) {
			final DetectorFactory detectorFactory = detectorFactoryCollection.getFactory(enabled.getKey());
			if (detectorFactory != null) {
				userPreferences.enableDetector(detectorFactory, enabled.getValue());
			}
		}
	}

	private static void configureFilter(
			@NotNull final FindBugs2 engine,
			@NotNull final UserPreferences userPrefs
	) {

		final Map<String, Boolean> excludeFilterFiles = userPrefs.getExcludeFilterFiles();
		for (final Map.Entry<String, Boolean> excludeFileName : excludeFilterFiles.entrySet()) {
			if (excludeFileName.getValue()) {
				final String filePath = excludeFileName.getKey();
				try {
					engine.addFilter(filePath, false);
				} catch (final IOException e) {
					LOGGER.error("ExcludeFilter configuration failed.", e);
				}
			}
		}
		final Map<String, Boolean> includeFilterFiles = userPrefs.getIncludeFilterFiles();
		for (final Map.Entry<String, Boolean> includeFileName : includeFilterFiles.entrySet()) {
			if (includeFileName.getValue()) {
				final String filePath = includeFileName.getKey();
				try {
					engine.addFilter(filePath, true);
				} catch (final IOException e) {
					LOGGER.error("IncludeFilter configuration failed.", e);
				}
			}
		}
		final Map<String, Boolean> excludeBugFiles = userPrefs.getExcludeBugsFiles();
		for (final Map.Entry<String, Boolean> excludeBugFile : excludeBugFiles.entrySet()) {
			if (excludeBugFile.getValue()) {
				final String filePath = excludeBugFile.getKey();
				try {
					engine.excludeBaselineBugs(filePath);
				} catch (final IOException e) {
					LOGGER.error("ExcludeBaseLineBug files configuration failed.", e);
				} catch (final DocumentException e) {
					LOGGER.error("ExcludeBaseLineBug files configuration failed.", e);
				}
			}
		}
	}

	protected final void showWarning(@NotNull final String message) {
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			@Override
			public void run() {
				BalloonTipFactory.showToolWindowWarnNotifier(_project, message + " " + ResourcesLoader.getString("analysis.aborted"));
			}
		});
	}
}
