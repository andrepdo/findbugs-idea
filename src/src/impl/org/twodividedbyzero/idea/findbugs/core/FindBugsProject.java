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


	public void configureSourceDirectories(final VirtualFile file) {
		final VirtualFile[] files = {file};
		configureSourceDirectories(files);
	}


	public void configureSourceDirectories(@NotNull final ProgressIndicator indicator, @NotNull final VirtualFile[] sourceDirs) {
		indicator.setText("Configure source directories...");
		configureSourceDirectories(sourceDirs);
	}


	public void configureSourceDirectories(@NotNull final VirtualFile[] sourceDirs) {
		for (final VirtualFile file : sourceDirs) {
			if (IdeaUtilImpl.isValidFileType(file.getFileType())) {
				final VirtualFile parent = file.getParent();
				if (parent != null && parent.isDirectory()) {
					addSourceDir(parent.getPresentableUrl());
				}
			} else if (file.isDirectory()) { // package dir
				addSourceDir(file.getPresentableUrl());
			}
		}
	}


	public void configureAuxClasspathEntries(@NotNull final ProgressIndicator indicator, @NotNull final VirtualFile[] classpathFiles) {
		indicator.setText("Collecting auxiliary classpath entries...");
		configureAuxClasspathEntries(classpathFiles);
	}


	public void configureAuxClasspathEntries(@NotNull final VirtualFile[] classpathFiles) {
		for (final VirtualFile file : classpathFiles) {
			addAuxClasspathEntry(file.getPresentableUrl());
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
		return _outputFiles != null ? _outputFiles : Collections.<String>emptyList();
	}


	public void setConfiguredOutputFiles(@NotNull final List<String> files) {
		_outputFiles = files;
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


}
