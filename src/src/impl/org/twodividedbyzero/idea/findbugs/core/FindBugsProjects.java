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
import com.intellij.openapi.util.NotNullComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.common.util.WithPluginClassloader;
import org.twodividedbyzero.idea.findbugs.gui.common.BalloonTipFactory;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

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

	public boolean addFiles(@NotNull final Iterable<VirtualFile> files, final boolean checkCompiled, final boolean includeTests) {
		for (final VirtualFile file : files) {
			if (!addFile(file, checkCompiled, includeTests) && checkCompiled) {
				return false;
			}
		}
		return true;
	}

	public boolean addFiles(@NotNull final VirtualFile[] files, final boolean checkCompiled, final boolean includeTests) {
		for (final VirtualFile file : files) {
			if (!addFile(file, checkCompiled, includeTests) && checkCompiled) {
				return false;
			}
		}
		return true;
	}

	public boolean addFile(@NotNull final VirtualFile file, final boolean checkCompiled, final boolean includeTests) {
		if (!IdeaUtilImpl.isValidFileType(file.getFileType())) {
			return true;
		}

		final Module module = ModuleUtilCore.findModuleForFile(file, project);
		if (module == null) {
			throw new IllegalStateException("No module found for " + file);
		}

		if (checkCompiled) {
			final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
			if (extension == null) {
				throw new IllegalStateException("No compiler extension for module " + module.getName());
			}
			final VirtualFile compilerOutputPath = extension.getCompilerOutputPath();
			if (compilerOutputPath == null) {
				showWarning("Source is not compiled.");
				return false;
			}
			final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
			if (psiFile != null && psiFile instanceof PsiJavaFile) { // think of scala, groovy, aj etc
				final PsiJavaFile javaFile = (PsiJavaFile) psiFile;
				String fileName = javaFile.getName();
				if (fileName.endsWith(".java")) {
					fileName = fileName.substring(0, fileName.length() - ".java".length());
				}
				final File outputPath = new File(compilerOutputPath.getCanonicalPath(), javaFile.getPackageName().replace(".", File.separator) + File.separator + fileName + ".class");
				if (!outputPath.exists()) {
					showWarning("Source is not compiled (" + outputPath + ").");
					return false;
				}
			}
		}

		final FindBugsProject findBugsProject = get(module, includeTests);
		findBugsProject.addOutputFile(file);
		return true;
	}

	@NotNull
	public FindBugsProject get(@NotNull final Module module, final boolean includeTests) {
		FindBugsProject ret = projects.get(module);
		if (ret == null) {

			ret = WithPluginClassloader.notNull(new NotNullComputable<FindBugsProject>() {
				@NotNull
				@Override
				public FindBugsProject compute() {
					return FindBugsProject.create(
							project,
							module,
							makeProjectName(module)
					);
				}
			});

			final VirtualFile[] sourceRoots = getSourceRoots(module, includeTests);
			for (final VirtualFile sourceRoot : sourceRoots) {
				if (!ret.addSourceDir(sourceRoot.getCanonicalPath())) {
					LOGGER.debug(String.format("Source directory '%s' of module '%s' already added", sourceRoot, module.getName()));
				}
			}

			final Collection<VirtualFile> compilerOutputPaths = getCompilerOutputPaths(module, includeTests);
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
	private Collection<VirtualFile> getCompilerOutputPaths(@NotNull final Module module, final boolean includeTests) {

		final Set<Module> modules = New.set();
		ModuleUtilCore.getDependencies(module, modules);
		modules.add(module);

		final List<VirtualFile> ret = new ArrayList<VirtualFile>(modules.size());
		boolean projectFallbackExecuted = false;

		for (final Module m : modules) {
			boolean added = false;
			final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(m);
			if (extension != null) {
				VirtualFile path = extension.getCompilerOutputPath();
				if (path != null) {
					ret.add(path);
					added = true;
				}
				if (includeTests) {
					path = extension.getCompilerOutputPathForTests();
					if (path != null) {
						ret.add(path);
						added = true;
					}
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
	private VirtualFile[] getSourceRoots(@NotNull final Module module, final boolean includeTests) {
		return ModuleRootManager.getInstance(module).getSourceRoots(includeTests);
	}

	private void showWarning(@NotNull final String message) {
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			@Override
			public void run() {
				BalloonTipFactory.showToolWindowWarnNotifier(project, message + " " + ResourcesLoader.getString("analysis.aborted"));
			}
		});
	}
}
