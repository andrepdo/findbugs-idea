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


import com.intellij.openapi.diagnostic.Logger;
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
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.event.EventManagerImpl;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEventFactory;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.messages.MessageBusManager;
import org.twodividedbyzero.idea.findbugs.messages.NewBugInstanceListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


/**
 * @author Reto Merz<reto.merz@gmail.com>
 * @version $Revision: 369 $
 * @since 0.9.995
 */
final class Reporter extends AbstractBugReporter implements FindBugsProgress {

	private static final Logger LOGGER = Logger.getInstance(Reporter.class.getName());
	private static final String ANALYZING_CLASSES_i18N = "Analyzing classes: ";

	private final Project _project;
	private final SortedBugCollection _bugCollection;
	private final FindBugsProject _findBugsProject;
	private final Map<String, String> _bugCategories;
	private final ProgressIndicator _indicator;
	private final AtomicBoolean _cancellingByUser;
	private final TransferToEDTQueue<Runnable> _transferToEDTQueue;

	private int _pass = -1;
	private int _filteredBugCount;
	private int _count;
	private int _goal;
	@NonNls
	private String _currentStageName;
	private boolean _canceled;


	Reporter(
			@NotNull final Project project,
			@NotNull final SortedBugCollection bugCollection,
			@NotNull final FindBugsProject findBugsProject,
			@NotNull final Map<String, String> bugCategories,
			@NotNull final ProgressIndicator indicator,
			@NotNull final AtomicBoolean cancellingByUser
	) {
		_project = project;
		_bugCollection = bugCollection;
		_findBugsProject = findBugsProject;
		_bugCategories = bugCategories;
		_indicator = indicator;
		_cancellingByUser = cancellingByUser;
		_transferToEDTQueue = new TransferToEDTQueue<Runnable>("Add New Bug Instance", new Processor<Runnable>() {
			@Override
			public boolean process(Runnable runnable) {
				runnable.run();
				return true;
			}
		}, new Condition<Object>() {
			@Override
			public boolean value(Object o) {
				return project.isDisposed() || _cancellingByUser.get() || _indicator.isCanceled();
			}
		}, 500);
	}


	@Override
	protected void doReportBug(@NotNull final BugInstance bug) {
		_bugCollection.add(bug);

		if (isHiddenBugGroup(bug)) {
			return;
		}
		_filteredBugCount++;
		observeClass(bug.getPrimaryClass().getClassDescriptor());

		/**
		 * Guarantee thread visibility *one* time.
		 */
		final AtomicReference<BugInstance> bugRef = New.atomicRef(bug);
		final AtomicReference<ProjectStats> projectStatsRef = New.atomicRef(getProjectStats());
		_transferToEDTQueue.offer(new Runnable() {
			/**
			 * Invoked by EDT.
			 */
			@Override
			public void run() {
				MessageBusManager.publish(NewBugInstanceListener.TOPIC).newBugInstance(bugRef.get(), projectStatsRef.get());
			}
		});
	}


	private boolean isHiddenBugGroup(final BugInstance bug) {
		final String category = bug.getBugPattern().getCategory();
		return _bugCategories.containsKey(category) && "false".equals(_bugCategories.get(category));
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
			Collections.sort(errorList, new QueuedErrorsComparator());

			final Map<String, Map<String, Throwable>> status = new HashMap<String, Map<String, Throwable>>();
			final String key = "The following errors occurred during FindBugs analysis:";
			status.put(key, new HashMap<String, Throwable>());

			final Map<String, Throwable> map = status.get(key);
			for (final Error error : errorList) {
				map.put(error.getMessage(), error.getCause());
			}

			FindBugsPluginImpl.processError(status);
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
			FindBugsPluginImpl.processError(status);
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
		_indicator.setText("Finished: Found " + _filteredBugCount + " bugs.");
		_indicator.finishNonCancelableSection();

		MessageBusManager.publishAnalysisFinishedToEDT(getBugCollection(), _findBugsProject);
		EventManagerImpl.getInstance().fireEvent(BugReporterEventFactory.newFinished(getBugCollection(), _project, _findBugsProject));
	}


	@Override
	public SortedBugCollection getBugCollection() {
		return _bugCollection;
	}


	@Override
	public void observeClass(@NotNull final ClassDescriptor classDescriptor) {
		if (_canceled) {
			return;
		}

		if (_indicator.isCanceled()) {
			cancelFindBugs();
			MessageBusManager.publishAnalysisAbortedToEDT();
			EventManagerImpl.getInstance().fireEvent(BugReporterEventFactory.newAborted(_project));
		} else if (_cancellingByUser.get()) {
			cancelFindBugs();
			_indicator.cancel();
		}

		final String className = classDescriptor.getDottedClassName();
		_indicator.setText("Observing class: " + className);
		if (_pass <= 0) {
			_indicator.setText("Prescanning... (found " + _filteredBugCount + ", checking " + className + ')');
		} else {
			_indicator.setText("Checking... (found " + _filteredBugCount + ", checking " + className + ')');
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
	}


	@Override
	public void startAnalysis(final int numClasses) {
		_pass++;
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
		_count = 0;
		_goal = goal;
		_currentStageName = stageName;
		_indicator.setText2(stageName + " 0/" + _goal);
	}


	private void step() {
		_count++;
		final int work = _pass == 0 ? 1 : 2;
		_indicator.setText2(_currentStageName + ' ' + _count + '/' + _goal + (ANALYZING_CLASSES_i18N.equals(_currentStageName) ? " (pass #" + work + "/2)" : ""));
	}


	private static class QueuedErrorsComparator implements Comparator<Error> {
		public int compare(final Error o1, final Error o2) {
			return o1.getSequence() - o2.getSequence();
		}
	}
}