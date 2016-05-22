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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import edu.umd.cs.findbugs.Plugin;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.collectors.RecurseFileCollector;
import org.twodividedbyzero.idea.findbugs.collectors.StatelessClassAdder;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.gui.PluginGuiCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FindBugsProject extends edu.umd.cs.findbugs.Project {

	private static final Logger LOGGER = Logger.getInstance(FindBugsProject.class);

	@NotNull
	private final Project project;

	@NotNull
	private final Module module;

	private List<String> _outputFiles;

	private StatelessClassAdder classAdder;

	private FindBugsProject(@NotNull final Project project, @NotNull final Module module) {
		this.project = project;
		this.module = module;
	}

	@NotNull
	public Module getModule() {
		return module;
	}

	@NotNull
	private StatelessClassAdder getClassAdder() {
		if (classAdder == null) {
			classAdder = new StatelessClassAdder(this, project);
		}
		return classAdder;
	}

	public void _configureSourceDirectories(@NotNull final ProgressIndicator indicator, @NotNull final Collection<VirtualFile> sourceDirs) {
		indicator.setText("Configure source directories...");
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


	public void _configureSourceDirectories(@NotNull final ProgressIndicator indicator, @NotNull final VirtualFile[] sourceDirs) {
		indicator.setText("Configure source directories...");
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

	public void addSourceDirectoryOf(@NotNull final ProgressIndicator indicator, @NotNull final VirtualFile file) {
		indicator.setText("Configure source directories...");
		if (IdeaUtilImpl.isValidFileType(file.getFileType())) {
			final VirtualFile parent = file.getParent();
			if (parent != null && parent.isDirectory()) {
				addSourceDir(parent.getPresentableUrl());
			}
		} else if (file.isDirectory()) { // package dir
			addSourceDir(file.getPresentableUrl());
		}
	}


	public void addAuxClasspathEntries(@NotNull final ProgressIndicator indicator, @NotNull final Collection<VirtualFile> classpathFiles) {
		indicator.setText("Collecting auxiliary classpath entries...");
		for (final VirtualFile file : classpathFiles) {
			addAuxClasspathEntry(file.getPresentableUrl());
		}
	}


	public void _addAuxClasspathEntries(@NotNull final ProgressIndicator indicator, @NotNull final VirtualFile[] classpathFiles) {
		indicator.setText("Collecting auxiliary classpath entries...");
		_addAuxClasspathEntries(classpathFiles);
	}


	public void _addAuxClasspathEntries(@NotNull final VirtualFile[] classpathFiles) {
		for (final VirtualFile file : classpathFiles) {
			addAuxClasspathEntry(file.getPresentableUrl());
		}
	}

	public void addOutputFile(@NotNull final com.intellij.openapi.project.Project project, @NotNull final VirtualFile file) {
		if (!IdeaUtilImpl.isValidFileType(file.getFileType())) {
			return;
		}
		if (_outputFiles == null) {
			_outputFiles = New.arrayList();
		}
		_outputFiles.add(file.getPath());
		getClassAdder().addContainingClasses(file);
	}

	public void _configureOutputFiles(@NotNull final com.intellij.openapi.project.Project project, @NotNull final Collection<VirtualFile> files) {
		_outputFiles = asPathList(files);
		final StatelessClassAdder sca = new StatelessClassAdder(this, project);
		for (final VirtualFile file : files) {
			if (IdeaUtilImpl.isValidFileType(file.getFileType())) {
				sca.addContainingClasses(file);
			}
		}
	}


	public void _configureOutputFiles(@NotNull final com.intellij.openapi.project.Project project, @NotNull final VirtualFile[] files) {
		_outputFiles = asPathList(files);
		final StatelessClassAdder sca = new StatelessClassAdder(this, project);
		for (final VirtualFile file : files) {
			if (IdeaUtilImpl.isValidFileType(file.getFileType())) {
				sca.addContainingClasses(file);
			}
		}
	}


	public void _configureOutputFile(@NotNull final com.intellij.openapi.project.Project project, final PsiClass psiClass) {
		final VirtualFile vFile = IdeaUtilImpl.getVirtualFile(psiClass);
		if (vFile != null) {
			_outputFiles = Arrays.asList(vFile.getPath());
			new StatelessClassAdder(this, project).addContainingClasses(psiClass);
		}
	}


	public void _configureOutputFiles(@NotNull final com.intellij.openapi.project.Project project, @NotNull ProgressIndicator indicator, @NotNull final String path) {
		final VirtualFile fileByPath = IdeaUtilImpl.findFileByPath(path);
		if (fileByPath != null) {
			_outputFiles = Arrays.asList(fileByPath.getPath());
		} else {
			LOGGER.error("Could not configure outputFiles! path=" + path);
		}
		indicator.setText("Collecting files for analysis...");
		final int[] count = new int[1];
		RecurseFileCollector.addFiles(project, indicator, this, new File(path), count);
	}


	public void _configureOutputFiles(@NotNull final com.intellij.openapi.project.Project project, @NotNull ProgressIndicator indicator, @NotNull final String[] paths) {
		_outputFiles = new ArrayList<String>();
		indicator.setText("Collecting files for analysis...");
		final int[] count = new int[1];
		for (final String path : paths) {
			_outputFiles.add(path);
			RecurseFileCollector.addFiles(project, indicator, this, new File(path), count);
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

	@NotNull
	static FindBugsProject create(
			@NotNull final Project project,
			@NotNull final Module module,
			@NotNull final String projectName,
			@NotNull final Set<PluginSettings> plugins
	) {
		final FindBugsProject ret = new FindBugsProject(project, module);
		ret.setProjectName(projectName);
		for (final Plugin plugin : Plugin.getAllPlugins()) {
			if (!plugin.isCorePlugin()) {
				boolean enabled = false;
				for (final PluginSettings pluginSettings : plugins) {
					if (plugin.getPluginId().equals(pluginSettings.id)) {
						if (pluginSettings.enabled) {
							enabled = true; // do not break loop here ; maybe there are multiple plugins (with same plugin id) configured and one is enabled
						}
					}
				}
				ret.setPluginStatusTrinary(plugin.getPluginId(), enabled);
			}
		}
		ret.setGuiCallback(new PluginGuiCallback(project));
		return ret;
	}
}
