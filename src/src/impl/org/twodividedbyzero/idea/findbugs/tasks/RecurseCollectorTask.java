/*
 * Copyright 2010 Andre Pfeiler
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
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class RecurseCollectorTask extends BackgroundableTask {

	// TODO: maybe remove this class
	private static final Logger LOGGER = Logger.getInstance(FindBugsTask.class.getName());

	private ProgressIndicator _indicator;
	private final FindBugsProject _findBugsProject;
	private VirtualFile[] _auxClasspathEntries;
	private VirtualFile[] _sourceDirectories;
	private Project _project;
	private VirtualFile[] _outputFile;


	public RecurseCollectorTask(@org.jetbrains.annotations.Nullable final Project project, @NotNull final String title, final boolean canBeCancelled, final FindBugsProject findBugsProject) {
		super(project, title, canBeCancelled);
		_findBugsProject = findBugsProject;
	}


	@Override
	public void run(@NotNull final ProgressIndicator indicator) {
		setProgressIndicator(indicator);

		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				_indicator.setText("Collecting classes/directories to analyze...");
				_indicator.setIndeterminate(true);
			}
		});

		configureFindbugsProject();

	}


	private void configureFindbugsProject() {
		_findBugsProject.configureAuxClasspathEntries(_auxClasspathEntries);
		_findBugsProject.configureSourceDirectories(_sourceDirectories);
		_findBugsProject.configureOutputFiles(_project, _outputFile);
	}


	@Override
	public void setProgressIndicator(@NotNull final ProgressIndicator indicator) {
		_indicator = indicator;
	}


	@Override
	public ProgressIndicator getProgressIndicator() {
		return _indicator;
	}


	public void setAuxClasspathEntries(final VirtualFile[] files) {
		_auxClasspathEntries = files.clone();
	}


	public void setSourceDirectories(final VirtualFile[] files) {
		_sourceDirectories = files.clone();
	}


	public void setOutputFiles(final Project project, final VirtualFile[] files) {
		_project = project;
		_outputFile = files.clone();
	}


	public boolean execute(final CompileContext context) {
		return false;  //TODO: implement
	}
}
