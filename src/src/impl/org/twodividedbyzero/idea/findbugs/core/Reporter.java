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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.util.Processor;
import com.intellij.util.containers.TransferToEDTQueue;
import edu.umd.cs.findbugs.AbstractBugReporter;
import edu.umd.cs.findbugs.AnalysisError;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.FindBugsProgress;
import edu.umd.cs.findbugs.ProjectStats;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.config.ProjectFilterSettings;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.messages.MessageBusManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

final class Reporter extends AbstractBugReporter implements FindBugsProgress {

	private static final Logger LOGGER = Logger.getInstance(Reporter.class.getName());
	private static final String ANALYZING_CLASSES_i18N = "Analyzing classes: ";

	@NotNull
	private final Project _project;

	@NotNull
	private final Module module;

	@NotNull
	private final SortedBugCollection _bugCollection;

	@NotNull
	private final ProjectFilterSettings projectFilterSettings;

	private final ProgressIndicator _indicator;
	private final AtomicBoolean _cancellingByUser;
	private final TransferToEDTQueue<Runnable> _transferToEDTQueue;

	private int pass = -1;
	private int bugCount;
	private int stepCount;
	private int goal;
	@NonNls
	private String _currentStageName;
	private boolean _canceled;
	private int analyzedClassCountOffset;


	Reporter(
			@NotNull final Project project,
			@NotNull final Module module,
			@NotNull final SortedBugCollection bugCollection,
			@NotNull final ProjectFilterSettings projectFilterSettings,
			@NotNull final ProgressIndicator indicator,
			@NotNull final AtomicBoolean cancellingByUser,
			final int analyzedClassCountOffset
	) {
		_project = project;
		this.module = module;
		_bugCollection = bugCollection;
		this.projectFilterSettings = projectFilterSettings;
		_indicator = indicator;
		_cancellingByUser = cancellingByUser;
		this.analyzedClassCountOffset = analyzedClassCountOffset;
		_transferToEDTQueue = new TransferToEDTQueue<Runnable>("Add New Bug Instance", new RunnableProcessor(), new Condition<Object>() {
			@Override
			public boolean value(final Object o) {
				return project.isDisposed() || _cancellingByUser.get() || _indicator.isCanceled();
			}
		}, 500);
	}


	private boolean checkCancel() {
		if (_canceled) {
			return true;
		}
		if (_indicator.isCanceled()) {
			cancelFindBugs();
			return true;
		}
		if (_cancellingByUser.get()) {
			cancelFindBugs();
			_indicator.cancel();
			return true;
		}
		return false;
	}


	@Override
	protected void doReportBug(@NotNull final BugInstance bug) {
		if (!projectFilterSettings.displayWarning(bug)) {
			return;
		}
		_bugCollection.add(bug);
		bugCount++;
		observeClass(bug.getPrimaryClass().getClassDescriptor());

		// Guarantee thread visibility *one* time.
		final AtomicReference<SortedBugCollection> bugCollectionRef = New.atomicRef(_bugCollection);
		final AtomicReference<BugInstance> bugRef = New.atomicRef(bug);
		final int analyzedClassCount = analyzedClassCountOffset + getProjectStats().getNumClasses();
		_transferToEDTQueue.offer(new Runnable() {
			/**
			 * Invoked by EDT.
			 */
			@Override
			public void run() {
				final Bug bug = new Bug(module, bugCollectionRef.get(), bugRef.get());
				MessageBusManager.publishNewBug(_project, bug, analyzedClassCount);
			}
		});
	}


	@Override
	public ProjectStats getProjectStats() {
		return _bugCollection.getProjectStats();
	}


	@Override
	public void startArchive(final String s) {
	}


	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
	@Override
	public void reportQueuedErrors() {
		// Report unique errors in order of their sequence
		final List<Error> errorList = new ArrayList<Error>(getQueuedErrors());
		if (!errorList.isEmpty()) {
			Collections.sort(errorList, QUEUED_ERRORS_COMPARATOR);

			final Map<String, Map<String, Throwable>> status = new HashMap<String, Map<String, Throwable>>();
			final String key = "The following errors occurred during FindBugs analysis:";
			status.put(key, new HashMap<String, Throwable>());

			final Map<String, Throwable> map = status.get(key);
			for (final Error error : errorList) {
				map.put(error.getMessage(), error.getCause());
			}

			//FindBugsPluginImpl.processError(status); TODO
		}

		final Set<String> missingClasses = getMissingClasses();
		if (!missingClasses.isEmpty()) {
			final Map<String, Map<String, Throwable>> status = new HashMap<String, Map<String, Throwable>>();
			final String key = "The following classes needed for FindBugs analysis were missing:";
			status.put(key, new HashMap<String, Throwable>());

			final Map<String, Throwable> map = status.get(key);
			for (final String missingClass : missingClasses) {
				map.put(missingClass, null);
				LOGGER.info(missingClass);
			}
			//FindBugsPluginImpl.processError(status); TODO
		}
	}


	@Override
	public void finish() {
		EventDispatchThreadHelper.invokeAndWait(new EventDispatchThreadHelper.OperationAdapter() {
			@Override
			public void run() {
				_transferToEDTQueue.drain();
			}
		});
		_indicator.setText("Finished: Found " + bugCount + " bugs.");
		_indicator.finishNonCancelableSection();
	}


	boolean isCanceled() {
		return _canceled;
	}


	@Override
	public SortedBugCollection getBugCollection() {
		return _bugCollection;
	}


	@Override
	public void observeClass(@NotNull final ClassDescriptor classDescriptor) {
		if (checkCancel()) {
			return;
		}

		final String className = classDescriptor.getDottedClassName();
		_indicator.setText("Observing class: " + className);
		if (pass <= 0) {
			_indicator.setText("Prescanning... (found " + bugCount + ", checking " + className + ')');
		} else {
			_indicator.setText("Checking... (found " + bugCount + ", checking " + className + ')');
		}
	}

	private void cancelFindBugs() {
		Thread.currentThread().interrupt(); // causes break in FindBugs main loop
		_canceled = true;
	}

	@Override
	public void reportAnalysisError(final AnalysisError error) {
		// nothing to do, see reportQueuedErrors()
	}


	@Override
	public void reportMissingClass(final String missingClass) {
		// nothing to do, see reportQueuedErrors()
	}


	@Override
	public void finishArchive() {
		step();
	}


	@Override
	public void finishClass() {
		step();
	}


	@Override
	public void finishPerClassAnalysis() {
		_indicator.setText("Finishing analysis...");
	}


	@Override
	public void reportNumberOfArchives(final int numArchives) {
		beginStage("Scanning archives: ", numArchives);
		checkCancel(); // interrupt here has no effect, this is a FindBugs bug... bad for jumbo projects.
	}


	@Override
	public void startAnalysis(final int numClasses) {
		pass++;
		beginStage(ANALYZING_CLASSES_i18N, numClasses);
	}


	@Override
	public void predictPassCount(final int[] classesPerPass) {
		int expectedWork = 0;
		for (final int count : classesPerPass) {
			expectedWork += 2 * count;
		}
		expectedWork -= classesPerPass[0];
		_indicator.setText("Performing bug checking... " + expectedWork);
	}


	private void beginStage(final String stageName, final int goal) {
		stepCount = 0;
		this.goal = goal;
		_currentStageName = stageName;
		_indicator.setText2(stageName + " 0/" + this.goal);
	}


	private void step() {
		stepCount++;
		final int work = pass == 0 ? 1 : 2;
		_indicator.setText2(_currentStageName + ' ' + stepCount + '/' + goal + (ANALYZING_CLASSES_i18N.equals(_currentStageName) ? " (pass #" + work + "/2)" : ""));
	}


	private static class RunnableProcessor implements Processor<Runnable> {
		@Override
		public boolean process(Runnable runnable) {
			runnable.run();
			return true;
		}
	}


	private static final Comparator<Error> QUEUED_ERRORS_COMPARATOR = new Comparator<Error>() {
		@Override
		public int compare(final Error o1, final Error o2) {
			return Integer.signum(o1.getSequence() - o2.getSequence());
		}
	};
}
