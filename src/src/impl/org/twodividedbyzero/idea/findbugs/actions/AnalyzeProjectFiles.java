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
package org.twodividedbyzero.idea.findbugs.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.Consumer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.collectors.RecurseFileCollector;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProjects;
import org.twodividedbyzero.idea.findbugs.core.FindBugsStarter;
import org.twodividedbyzero.idea.findbugs.core.FindBugsState;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import java.io.File;
import java.util.List;

public final class AnalyzeProjectFiles extends AbstractAnalyzeAction {

	@Override
	void updateImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		final boolean enable = state.isIdle();

		e.getPresentation().setEnabled(enable);
		e.getPresentation().setVisible(true);
	}

	@SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
	@Override
	void analyze(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		final int answer = askIncludeTest(project);
		if (answer == Messages.CANCEL) {
			return;
		}
		final boolean includeTests = Messages.YES == answer;

		new FindBugsStarter(project, "Running FindBugs analysis for project '" + project.getName() + "'...") {
			@Override
			protected void createCompileScope(@NotNull final CompilerManager compilerManager, @NotNull final Consumer<CompileScope> consumer) {
				consumer.consume(compilerManager.createProjectCompileScope(project));
			}

			@Override
			protected boolean configure(@NotNull final ProgressIndicator indicator, @NotNull final FindBugsProjects projects, final boolean justCompiled) {
				final Module[] modules = ModuleManager.getInstance(project).getModules();
				final List<Pair.NonNull<Module, VirtualFile>> compilerOutputPaths = New.arrayList();
				for (final Module module : modules) {
					final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
					if (extension == null) {
						throw new IllegalStateException("No compiler extension for module " + module.getName());
					}
					final VirtualFile compilerOutputPath = extension.getCompilerOutputPath();
					if (compilerOutputPath != null) {
						/**
						 * Otherwise ignore it. Maybe this module is only used to contains fact (think of Android)
						 * or to aggregate modules (think of maven).
						 */
						compilerOutputPaths.add(Pair.createNonNull(module, compilerOutputPath));
					}
					if (includeTests) {
						final VirtualFile compilerOutputPathForTests = extension.getCompilerOutputPathForTests();
						if (compilerOutputPathForTests != null) {
							compilerOutputPaths.add(Pair.createNonNull(module, compilerOutputPathForTests));
						}
					}
				}

				if (compilerOutputPaths.isEmpty()) {
					showWarning(ResourcesLoader.getString("analysis.noOutputPaths"));
					return false;
				}

				indicator.setText("Collecting files for analysis...");
				final int[] count = new int[1];
				for (final Pair.NonNull<Module, VirtualFile> compilerOutputPath : compilerOutputPaths) {
					final FindBugsProject findBugsProject = projects.get(compilerOutputPath.getFirst(), includeTests);
					RecurseFileCollector.addFiles(project, indicator, findBugsProject, new File(compilerOutputPath.getSecond().getCanonicalPath()), count);
				}
				return true;
			}
		}.start();
	}
}
