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
package org.twodividedbyzero.idea.findbugs.report;


import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import edu.umd.cs.findbugs.AbstractBugReporter;
import edu.umd.cs.findbugs.AnalysisError;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.FindBugsProgress;
import edu.umd.cs.findbugs.ProjectStats;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import org.jetbrains.annotations.NonNls;
import org.twodividedbyzero.idea.findbugs.common.event.EventManagerImpl;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent.Operation;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEventImpl;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterInspectionEvent;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterInspectionEventImpl;
import org.twodividedbyzero.idea.findbugs.common.ui.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
import org.twodividedbyzero.idea.findbugs.tasks.FindBugsTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
@SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
public class BugReporter extends AbstractBugReporter implements FindBugsProgress {

	private static final Logger LOGGER = Logger.getInstance(BugReporter.class.getName());

	private final Project _project;


	/** Persistent store of reported warnings. */
	private final SortedBugCollection _bugCollection;

	private int _pass = -1;
	private int _filteredBugCount;
	private FindBugsTask _findBugsTask;
	private int _count;
	private int _goal;
	@NonNls
	private String _currentStageName;
	private static final String ANALYZING_CLASSES_i18N = "Analyzing classes: ";
	private boolean _isInspectionRun;
	private boolean _isRunning;
	private FindBugsPreferences _preferences;


	/**
	 * Constructor.
	 *
	 * @param project the project whose classes are being analyzed for bugs
	 */
	public BugReporter(final Project project) {
		this(project, false);
	}


	public BugReporter(final Project project, final boolean isInspectionRun) {
		//this.monitor = monitor;
		_project = project;
		_preferences = IdeaUtilImpl.getPluginComponent(project).getPreferences();
		_isInspectionRun = isInspectionRun;
		_bugCollection = new SortedBugCollection();
	}


	/* (non-Javadoc)
	 * @see edu.umd.cs.findbugs.AbstractBugReporter#doReportBug(edu.umd.cs.findbugs.BugInstance)
	 */
	@Override
	protected void doReportBug(final BugInstance bug) {
		getBugCollection().add(bug);
		/*if (MarkerUtil.displayWarning(bug, userPrefs.getFilterSettings())) {
			MarkerUtil.createMarker(bug, project, getBugCollection());
			filteredBugCount++;
		}*/

		if (isHiddenBugGroup(bug)) {
			return;
		}
		_filteredBugCount++;
		observeClass(bug.getPrimaryClass().getClassDescriptor());

		if (_isInspectionRun) {
			EventManagerImpl.getInstance().fireEvent(new BugReporterInspectionEventImpl(BugReporterInspectionEvent.Operation.NEW_BUG_INSTANCE, bug, _filteredBugCount, getProjectStats(), _project.getName()));
		} else {
			EventManagerImpl.getInstance().fireEvent(new BugReporterEventImpl(BugReporterEvent.Operation.NEW_BUG_INSTANCE, bug, _filteredBugCount, getProjectStats(), _project.getName()));
		}
	}


	public boolean isHiddenBugGroup(final BugInstance bug) {
		final Map<String, String> categories = _preferences.getBugCategories();
		final String category = bug.getBugPattern().getCategory();
		return categories.containsKey(category) && "false".equals(categories.get(category));
	}


	@Override
	public ProjectStats getProjectStats() {
		return _bugCollection.getProjectStats();
	}


	public void startArchive(final String s) {
		LOGGER.info("StartArchive: " + s);
	}


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
			//FindbugsPlugin.getDefault().getLog().log(status);
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
			//FindBugsPlugin.getDefault().getLog().log(status);
			FindBugsPluginImpl.processError(status);
		}
	}


	/** @see edu.umd.cs.findbugs.BugReporter#finish() */
	public void finish() {
		final String message = "Finished: Found " + _filteredBugCount + " bugs.";
		_findBugsTask.setIndicatorText(message);
		//LOGGER.debug(message);

		try {
			Thread.sleep(500);
		} catch (InterruptedException ignore) {
			Thread.interrupted();
		}
		//monitor.done();
		final ProgressIndicator progressIndicator = _findBugsTask.getProgressIndicator();
		if (progressIndicator != null) {
			progressIndicator.finishNonCancelableSection();
		}

		if (_isInspectionRun) {
			EventManagerImpl.getInstance().fireEvent(new BugReporterInspectionEventImpl(org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterInspectionEvent.Operation.ANALYSIS_FINISHED, null, getBugCollection(), _project.getName()));
		} else {
			EventManagerImpl.getInstance().fireEvent(new BugReporterEventImpl(Operation.ANALYSIS_FINISHED, null, getBugCollection(), _project.getName()));
		}

		/*InfoPopup.popup(_warningButton, "Changelist Collision Detected!", ResourcesLoader.loadIcon(GuiResources.FINDBUGS_ICON), "Click here to clear this alert and open the Changes toolwindow.", new AbstractAction() {
			public void actionPerformed(final ActionEvent e) {
				//showChangesToolwindow(_projectComponent.getProject());
			}
		}, 0);*/
		setRunning(false);
	}


	public boolean isRunning() {
		return _isRunning;
	}


	public void setRunning(final boolean running) {
		_isRunning = running;
	}


	/**
	 * Returns the list of bugs found in this project.
	 *
	 * @return The collection that hold the bugs found in this project.
	 */
	public SortedBugCollection getBugCollection() {
		return _bugCollection;
	}


	public void observeClass(final ClassDescriptor classDescriptor) {
		final ProgressIndicator progressIndicator = _findBugsTask.getProgressIndicator();
		if (progressIndicator != null && progressIndicator.isCanceled()) {
			// causes break in FindBugs main loop
			Thread.currentThread().interrupt();

			if (_isInspectionRun) {
				EventManagerImpl.getInstance().fireEvent(new BugReporterInspectionEventImpl(org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterInspectionEvent.Operation.ANALYSIS_ABORTED, _project.getName()));
			} else if (!isRunning()) {
				EventManagerImpl.getInstance().fireEvent(new BugReporterEventImpl(Operation.ANALYSIS_ABORTED, _project.getName()));
			}
		}

		final String className = classDescriptor.getDottedClassName();

		String message = "Observing class: " + className;
		LOGGER.debug(message);
		_findBugsTask.setIndicatorText(message);

		// Update progress monitor
		if (_pass <= 0) {
			message = "Prescanning... (found " + _filteredBugCount + ", checking " + className + ")";
			_findBugsTask.setIndicatorText(message);
			LOGGER.debug(message);
		} else {
			message = "Checking... (found " + _filteredBugCount + ", checking " + className + ")";
			_findBugsTask.setIndicatorText(message);
			LOGGER.debug(message);
		}
		//monitor.worked(work);
		//_findBugsTask.getProgressIndicator().setFraction(work);
	}


	@Override
	public void reportAnalysisError(final AnalysisError error) {
		// nothing to do, see reportQueuedErrors()
	}


	@Override
	public void reportMissingClass(final String missingClass) {
		// nothing to do, see reportQueuedErrors()
	}


	public edu.umd.cs.findbugs.BugReporter getRealBugReporter() {
		return this;
	}


	public void finishArchive() {
		step();
	}


	public void finishClass() {
		step();
	}


	public void finishPerClassAnalysis() {
		_findBugsTask.setIndicatorText("Finishing analysis...");
		/*SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				stageNameLabel.setText(L10N.getLocalString("msg.finishedanalysis_txt", "Finishing analysis"));
			}
		});*/
	}


	public void reportNumberOfArchives(final int numArchives) {
		//_findBugsTask.setIndicatorText("Scanning archives: " + numArchives);
		beginStage("Scanning archives: ", numArchives);
	}


	public void startAnalysis(final int numClasses) {
		_pass++;
		//_findBugsTask.setIndicatorText("Analyzing classes: " + numClasses);
		beginStage(ANALYZING_CLASSES_i18N, numClasses);

		if (_isInspectionRun) {
			EventManagerImpl.getInstance().fireEvent(new BugReporterInspectionEventImpl(org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterInspectionEvent.Operation.ANALYSIS_STARTED, null, 0, _project.getName()));
		} else if (!isRunning()) {
			EventManagerImpl.getInstance().fireEvent(new BugReporterEventImpl(Operation.ANALYSIS_STARTED, null, 0, _project.getName()));
		}
	}


	public void predictPassCount(final int[] classesPerPass) {
		int expectedWork = 0;
		for (final int count : classesPerPass) {
			expectedWork += 2 * count;
		}
		expectedWork -= classesPerPass[0];

		//_findBugsTask.getProgressIndicator().setFraction(0);
		final String message = "Performing bug checking... " + expectedWork;
		_findBugsTask.setIndicatorText(message);
		//LOGGER.debug(message);
	}


	private void beginStage(final String stageName, final int goal) {
		synchronized (_bugCollection) {
			_count = 0;
			_goal = goal;
		}

		EventDispatchThreadHelper.invokeLater(new Runnable() {
			public void run() {
				final int goal = getGoal();
				//stageNameLabel.setText(stageName);
				//_findBugsTask.setTitle(stageName + " 0/" + goal);
				_currentStageName = stageName;
				_findBugsTask.setIndicatorText2(stageName + " 0/" + goal);
				//_findBugsTask.setIndicatorText("0/" + goal);
				//countValueLabel.setText("0/" + goal);
				//progressBar.setMaximum(goal);
				//_findBugsTask.getProgressIndicator().setFraction((double) 0);
				//progressBar.setValue(0);
			}
		});
	}


	private void step() {
		synchronized (_bugCollection) {
			_count++;
		}

		EventDispatchThreadHelper.invokeLater(new Runnable() {
			public void run() {
				final int count = getCount();
				final int goal = getGoal();
				final int work = _pass == 0 ? 1 : 2;

				//EventManagerImpl.getInstance().fireEvent(new BugReporterEventImpl(Operation.NEW_BUG_INSTANCE, null, _filteredBugCount, getProjectStats()));
				_findBugsTask.setIndicatorText2(_currentStageName + " " + count + "/" + goal + (ANALYZING_CLASSES_i18N.equals(_currentStageName) ? " (pass #" + work + "/2)" : ""));
				//countValueLabel.setText(count + "/" + goal);
				//progressBar.setValue(count);
				//_findBugsTask.getProgressIndicator().setFraction(100/(double) count);
			}
		});
	}


	public FindBugsTask getFindBugsTask() {
		return _findBugsTask;
	}


	public void setFindBugsTask(final FindBugsTask findBugsTask) {
		_findBugsTask = findBugsTask;
	}


	private synchronized int getGoal() {
		return _goal;
	}


	private synchronized int getCount() {
		return _count;
	}


	private static class QueuedErrorsComparator implements Comparator<Error>, Serializable {

		private static final long serialVersionUID = 0L;


		public int compare(final Error o1, final Error o2) {
			return o1.getSequence() - o2.getSequence();
		}
	}
}
