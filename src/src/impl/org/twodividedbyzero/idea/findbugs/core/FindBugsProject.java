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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import edu.umd.cs.findbugs.Project;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.collectors.RecurseFileCollector;
import org.twodividedbyzero.idea.findbugs.collectors.StatelessClassAdder;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class FindBugsProject extends Project {

	private static final Logger LOGGER = Logger.getInstance(FindBugsProject.class.getName());
	private List<String> _outputFiles;
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


	public void configureOutputFiles(@NotNull final com.intellij.openapi.project.Project project, @NotNull final Collection<VirtualFile> files) {
		_outputFiles = asPathList(files);
		final StatelessClassAdder sca = new StatelessClassAdder(this, project);
		for (final VirtualFile file : files) {
			if (IdeaUtilImpl.isValidFileType(file.getFileType())) {
				sca.addContainingClasses(file);
			}
		}
	}


	public void configureOutputFiles(@NotNull final com.intellij.openapi.project.Project project, @NotNull final VirtualFile[] files) {
		_outputFiles = asPathList(files);
		final StatelessClassAdder sca = new StatelessClassAdder(this, project);
		for (final VirtualFile file : files) {
			if (IdeaUtilImpl.isValidFileType(file.getFileType())) {
				sca.addContainingClasses(file);
			}
		}
	}


	public void configureOutputFile(@NotNull final com.intellij.openapi.project.Project project, final PsiClass psiClass) {
		final VirtualFile vFile = IdeaUtilImpl.getVirtualFile(psiClass);
		if (vFile != null) {
			_outputFiles = Arrays.asList(vFile.getPath());
			new StatelessClassAdder(this, project).addContainingClasses(psiClass);
		}
	}


	public void configureOutputFiles(final VirtualFile selectedPackage, final com.intellij.openapi.project.Project project) {
		final VirtualFile path = IdeaUtilImpl.getCompilerOutputPath(selectedPackage, project);
		assert path != null;
		_outputFiles = Arrays.asList(path.getPath());
		RecurseFileCollector.addFiles(this, new File(selectedPackage.getPath()));
	}


	public void configureOutputFiles(final String path) {
		final VirtualFile fileByPath = IdeaUtilImpl.findFileByPath(path);
		if (fileByPath != null) {
			_outputFiles = Arrays.asList(fileByPath.getPath());
		} else {
			LOGGER.error("Could not configure outputFiles! path=" + path);
		}
		RecurseFileCollector.addFiles(this, new File(path));
	}


	public void configureOutputFiles(@NotNull final Iterable<String> paths) {
		_outputFiles = new ArrayList<String>();
		for (final String path : paths) {
			_outputFiles.add(path);
			addFile(path);
		}
	}


	@NotNull
	public List<String> getConfiguredOutputFiles() {
		return _outputFiles != null ? new ArrayList<String>(_outputFiles) : Collections.<String>emptyList();
	}


	@NotNull
	private static List<String> asPathList(@NotNull final VirtualFile[] files) {
		final List<String> ret = new ArrayList<String>(files.length);
		for (final VirtualFile file : files) {
			ret.add(file.getPath());
		}
		return ret;
	}


	@NotNull
	private static List<String> asPathList(@NotNull final Collection<VirtualFile> files) {
		final List<String> ret = new ArrayList<String>(files.size());
		for (final VirtualFile file : files) {
			ret.add(file.getPath());
		}
		return ret;
	}


	/*public void setCollectorTask(final RecurseCollectorTask collectorTask) {
		_collectorTask = collectorTask;
	}


	public RecurseCollectorTask getCollectorTask() {
		return _collectorTask;
	}*/
}
