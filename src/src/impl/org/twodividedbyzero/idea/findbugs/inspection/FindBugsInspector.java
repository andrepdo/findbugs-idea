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
package org.twodividedbyzero.idea.findbugs.inspection;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileTask;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.IFindBugsEngine;
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


	@Override
	public boolean work(final String text) {
		try {
			_inspection.registerEventListener(getProject());
			final IFindBugsEngine engine = createFindBugsEngine();
			final FindBugsTask findBugsTask = new FindBugsTask(_project, _bugCollection, "Running FindBugs inspection...", true, engine, true);
			_bugReporter.setFindBugsTask(findBugsTask);
			findBugsTask.runFindBugs(engine);
			return true;
		} catch (final Exception e) {
			LOGGER.debug("FindBugs inspection failed.", e);
			return false;
		} finally {
			_inspection.unregisterEventListener();
			//noinspection AssignmentToNull
			_inspection = null;
		}

	}


	@Override
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
		return work("Running FindBugs inspection...");
	}


	@Override
	protected IFindBugsEngine createFindBugsEngine() {
		//TODO: FindBugs.setHome(FindBugsPlugin.getFindBugsEnginePluginLocation());

		// Create BugReporter
		_bugReporter = new BugReporter(_project, true, _bugCollection, _findBugsProject);
		_bugReporter.setPriorityThreshold(_userPrefs.getUserDetectorThreshold());

		// Create IFindBugsEngine
		final IFindBugsEngine engine = new FindBugs2();
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
