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
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.Consumer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.collectors.RecurseFileCollector;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProjects;
import org.twodividedbyzero.idea.findbugs.core.FindBugsStarter;
import org.twodividedbyzero.idea.findbugs.core.FindBugsState;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import java.io.File;

public final class AnalyzeModuleFiles extends AbstractAnalyzeAction {

	@Override
	void updateImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		boolean enable = false;
		if (state.isIdle()) {
			final Module module = getModule(e);
			enable = null != module;
		}

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

		final Module module = getModule(e);

		new FindBugsStarter(project, "Running FindBugs analysis for module'" + module.getName() + "'...") {
			@Override
			protected void createCompileScope(@NotNull final CompilerManager compilerManager, @NotNull final Consumer<CompileScope> consumer) {
				consumer.consume(compilerManager.createModuleCompileScope(module, true));
			}

			@Override
			protected boolean configure(@NotNull final ProgressIndicator indicator, @NotNull final FindBugsProjects projects, final boolean justCompiled) {
				final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
				if (extension == null) {
					throw new IllegalStateException("No compiler extension for module " + module.getName());
				}

				final VirtualFile compilerOutputPath = extension.getCompilerOutputPath();
				if (compilerOutputPath == null) {
					showWarning(ResourcesLoader.getString("analysis.moduleNotCompiled", module.getName()));
					return false;
				}
				VirtualFile compilerOutputPathForTests = null;
				if (includeTests) {
					compilerOutputPathForTests = extension.getCompilerOutputPathForTests();
				}

				indicator.setText("Collecting files for analysis...");
				final FindBugsProject findBugsProject = projects.get(module, includeTests && compilerOutputPathForTests != null);
				final int[] count = new int[1];
				RecurseFileCollector.addFiles(project, indicator, findBugsProject, new File(compilerOutputPath.getCanonicalPath()), count);

				if (compilerOutputPathForTests != null) {
					RecurseFileCollector.addFiles(project, indicator, findBugsProject, new File(compilerOutputPathForTests.getCanonicalPath()), count);
				}
				return true;
			}
		}.start();
	}

	@Nullable
	private static Module getModule(@NotNull final AnActionEvent e) {
		return DataKeys.MODULE.getData(e.getDataContext());
	}
}
