/*
 * Copyright 2009 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.twodividedbyzero.idea.findbugs.inspection;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileTask;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.IFindBugsEngine2;
import org.twodividedbyzero.idea.findbugs.core.FindBugsWorker;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
import org.twodividedbyzero.idea.findbugs.report.BugReporter;
import org.twodividedbyzero.idea.findbugs.tasks.FindBugsTask;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.82-dev
 */
public class FindBugsInspector extends FindBugsWorker implements CompileTask {

	private FindBugsInspection _inspection;


	public FindBugsInspector(final com.intellij.openapi.project.Project project, final FindBugsInspection inspection) {
		this(project);
		_inspection = inspection;
	}


	public FindBugsInspector(final com.intellij.openapi.project.Project project) {
		super(project);
	}


	/**
	 * If using {@link org.twodividedbyzero.idea.findbugs.core.FindBugsWorker#compile(com.intellij.openapi.vfs.VirtualFile, com.intellij.openapi.project.Project, com.intellij.openapi.compiler.CompileTask)}
	 * there's no need to call  before or after.  will be invoked as compile after task {@link #execute(com.intellij.openapi.compiler.CompileContext)}.
	 *
	 * @return true on success otherwise false
	 */
	@Override
	public boolean work() {
		try {
			_inspection.registerEventListener();
			final IFindBugsEngine2 engine = createFindBugsEngine();
			// Create FindBugsTask
			final FindBugsTask findBugsTask = new FindBugsTask(_project, "Runnig FindBugs inspection...", true, engine, true);
			_bugReporter.setFindBugsTask(findBugsTask);
			findBugsTask.runFindBugs(engine);
			return true;
		} catch (Exception e) {
			LOGGER.debug("FindBugs inpsection failed.", e);
			_inspection.unregisterEventListner();
			return false;
		}

	}


	public boolean execute(final CompileContext context) {
		/*ApplicationManager.getApplication().runReadAction(new Runnable() {
			public void run() {
				_inspection.checkFile(_inspection.getPsiFile(), FindBugsInspection.getManager(), true);		
			}
		});

		return true;*/
		//DaemonCodeAnalyzer.getInstance(project).updateVisibleHighlighters(editor)
		//DaemonCodeAnalyser.getInstance(project).restart();
		DaemonCodeAnalyzer.getInstance(_project).restart();
		return work();
	}


	@Override
	protected IFindBugsEngine2 createFindBugsEngine() {
		//TODO: FindBugs.setHome(FindBugsPlugin.getFindBugsEnginePluginLocation());

		// Create BugReporter
		_bugReporter = new BugReporter(_project, true);
		_bugReporter.setPriorityThreshold(_userPrefs.getUserDetectorThreshold());

		// Create IFindBugsEngine
		final IFindBugsEngine2 engine = new FindBugs2();
		engine.setNoClassOk(true);
		engine.setBugReporter(_bugReporter);
		engine.setProject(_findBugsProject);
		engine.setProgressCallback(_bugReporter);

		// add plugins to detector collection
		final DetectorFactoryCollection dfc = FindBugsPreferences.getDetectorFactorCollection();
		engine.setDetectorFactoryCollection(dfc);

		// configure detectors.
		engine.setUserPreferences(_userPrefs);
		return engine;
	}


}
