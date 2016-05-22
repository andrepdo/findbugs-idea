/*
 * Copyright 2016 Andre Pfeiler
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
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.collectors.RecurseFileCollector;
import org.twodividedbyzero.idea.findbugs.common.util.New;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FindBugsProjects {

	private static final Logger LOGGER = Logger.getInstance(FindBugsProjects.class);

	@NotNull
	private final Project project;

	@NotNull
	private final Map<Module, FindBugsProject> projects;

	FindBugsProjects(@NotNull final Project project) {
		this.project = project;
		projects = New.map();
	}

	public void addFiles(@NotNull final Iterable<VirtualFile> files) {
		for (final VirtualFile file : files) {
			addFile(file);
		}
	}

	public void addFiles(@NotNull final VirtualFile[] files) {
		for (final VirtualFile file : files) {
			addFile(file);
		}
	}

	public void addFile(@NotNull final VirtualFile file) {
		final Module module = ModuleUtilCore.findModuleForFile(file, project);
		if (module == null) {
			throw new IllegalStateException("No module found for " + file);
		}
		final FindBugsProject findBugsProject = get(module);
		findBugsProject.addOutputFile(project, file);
	}

	@NotNull
	public FindBugsProject get(@NotNull final Module module) {
		FindBugsProject ret = projects.get(module);
		if (ret == null) {

			ret = FindBugsProject.create(
					project,
					module,
					makeProjectName(module),
					getPlugins(module)
			);

			final VirtualFile[] sourceRoots = getSourceRoots(module);
			for (final VirtualFile sourceRoot : sourceRoots) {
				if (!ret.addSourceDir(sourceRoot.getCanonicalPath())) {
					LOGGER.debug(String.format("Source directory '%s' of module '%s' already added", sourceRoot, module.getName()));
				}
			}

			final Collection<VirtualFile> compilerOutputPaths = getCompilerOutputPaths(module);
			for (final VirtualFile compilerOutputPath : compilerOutputPaths) {
				if (!ret.addAuxClasspathEntry(compilerOutputPath.getCanonicalPath())) {
					LOGGER.debug(String.format("Aux classpath '%s' of module '%s' already added", compilerOutputPath, module.getName()));
				}
			}

			projects.put(module, ret);
		}
		return ret;
	}

	@NotNull
	private String makeProjectName(@Nullable final Module module) {
		if (module != null) {
			return project.getName() + "[" + module.getName() + "]";
		}
		return project.getName();
	}

	@NotNull
	Map<Module, FindBugsProject> getProjects() {
		return projects;
	}

	@NotNull
	private Set<PluginSettings> getPlugins(@Nullable final Module module) {
		return ProjectSettings.getInstance(project).plugins; // TODO
	}

	@NotNull
	private Collection<VirtualFile> getCompilerOutputPaths(@NotNull final Module module) {

		final Set<Module> modules = New.set();
		ModuleUtilCore.collectModulesDependsOn(module, modules);
		modules.add(module);

		final List<VirtualFile> ret = new ArrayList<VirtualFile>(modules.size());
		boolean projectFallbackExecuted = false;

		for (final Module m : modules) {
			boolean added = false;
			final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(m);
			if (extension != null) {
				final VirtualFile path = extension.getCompilerOutputPath(); // FIXME: think of getCompilerOutputPathForTests()
				if (path != null) {
					ret.add(path);
					added = true;
				}
			}
			if (!added) {
				if (!projectFallbackExecuted) {
					projectFallbackExecuted = true;
					final CompilerProjectExtension compilerProjectExtension = CompilerProjectExtension.getInstance(project);
					if (compilerProjectExtension != null) {
						final VirtualFile path = compilerProjectExtension.getCompilerOutput();
						if (path != null) {
							ret.add(path);
						}
					}
				}
			}
		}
		return ret;
	}

	@NotNull
	private VirtualFile[] getSourceRoots(@NotNull final Module module) {
		return ModuleRootManager.getInstance(module).getSourceRoots();
	}
}
