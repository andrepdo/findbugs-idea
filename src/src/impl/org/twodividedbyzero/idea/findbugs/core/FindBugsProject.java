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
package org.twodividedbyzero.idea.findbugs.core;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import edu.umd.cs.findbugs.Project;
import org.twodividedbyzero.idea.findbugs.collectors.RecurseClassCollector;
import org.twodividedbyzero.idea.findbugs.collectors.RecurseFileCollector;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;

import java.io.File;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class FindBugsProject extends Project {

	private static final Logger LOGGER = Logger.getInstance(FindBugsProject.class.getName());
	private VirtualFile[] _outputFiles;
	private static final VirtualFile[] EMPTY_VIRTUAL_FILES_ARRAY = new VirtualFile[]{};
	//private RecurseCollectorTask _collectorTask;


	public void configureSourceDirectories(final VirtualFile file) {
		final VirtualFile[] files = {file};
		configureSourceDirectories(files);
	}


	public void configureSourceDirectories(final VirtualFile[] selectedSourceFiles) {
		for (final VirtualFile file : selectedSourceFiles) {
			if (IdeaUtilImpl.isValidFileType(file.getFileType())) {
				final VirtualFile parent = file.getParent();
				if (parent != null && parent.isDirectory()) {
					addSourceDir(parent.getPresentableUrl());
					LOGGER.debug("adding source dir: " + parent.getPresentableUrl());

					/*ApplicationManager.getApplication().invokeLater(new Runnable() {
						public void run() {*/
					//_collectorTask.setIndicatorText("adding source dir: " + parent.getPresentableUrl());
					/*}
					});*/
				}
			} else if (file.isDirectory()) { // package dir
				addSourceDir(file.getPresentableUrl());
				LOGGER.debug("adding source dir: " + file.getPresentableUrl());
			}
		}
	}


	public void configureAuxClasspathEntries(final VirtualFile[] classpathFiles) {
		for (final VirtualFile file : classpathFiles) {
			addAuxClasspathEntry(file.getPresentableUrl());
			//_findBugsTask.getProgressIndicator().setText("Collecting auxiliary classpath entires...");
			LOGGER.debug("adding aux classpath entry: " + file.getPresentableUrl());

			/*ApplicationManager.getApplication().invokeLater(new Runnable() {
				public void run() {*/
			//_collectorTask.setIndicatorText("adding aux classpath entry: " + file.getPresentableUrl());
			/*}
			});*/
		}
	}


	public void configureOutputFiles(final com.intellij.openapi.project.Project project, final VirtualFile[] selectedSourceFiles) {
		//final com.intellij.openapi.project.Project project = IdeaUtilImpl.getProject(dataContext);
		_outputFiles = selectedSourceFiles.clone();

		RecurseClassCollector classCollector = null;
		for (final VirtualFile file : selectedSourceFiles) {
			if (IdeaUtilImpl.isValidFileType(file.getFileType())) {
				classCollector = new RecurseClassCollector(this, project);
				//classCollector.setVirtualFile(file);
				classCollector.addContainingClasses(file);
			}
		}

		// clear for gc
		if (classCollector != null) {
			classCollector.getResult().clear();
		}
	}


	public void configureOutputFile(final com.intellij.openapi.project.Project project, final PsiClass selectedPsiClass) {
		//final com.intellij.openapi.project.Project project = IdeaUtilImpl.getProject(dataContext);
		final VirtualFile vFile = IdeaUtilImpl.getVirtualFile(selectedPsiClass);
		if (vFile != null) {
			_outputFiles = new VirtualFile[] {vFile};

			final RecurseClassCollector classCollector = new RecurseClassCollector(this, project);
			//classCollector.setVirtualFile(file);
			classCollector.addContainingClasses(selectedPsiClass);

			// clear for gc
			classCollector.getResult().clear();
		}
	}


	public void configureOutputFiles(final VirtualFile selectedPackage, final com.intellij.openapi.project.Project project) {
		final VirtualFile path = IdeaUtilImpl.getCompilerOutputPath(selectedPackage, project);
		assert path != null;
		_outputFiles = new VirtualFile[] {path};
		RecurseFileCollector.addFiles(this, new File(selectedPackage.getPath()));
	}


	public void configureOutputFiles(final String path) {
		_outputFiles = new VirtualFile[] {IdeaUtilImpl.findFileByPath(path)};
		RecurseFileCollector.addFiles(this, new File(path));
	}


	public VirtualFile[] getConfiguredOutputFiles() {
		return _outputFiles != null ? _outputFiles.clone() : EMPTY_VIRTUAL_FILES_ARRAY;
	}


	/*public void setCollectorTask(final RecurseCollectorTask collectorTask) {
		_collectorTask = collectorTask;
	}


	public RecurseCollectorTask getCollectorTask() {
		return _collectorTask;
	}*/
}
