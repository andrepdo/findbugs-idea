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
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiClass;
import com.intellij.util.Consumer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProjects;
import org.twodividedbyzero.idea.findbugs.core.FindBugsStarter;
import org.twodividedbyzero.idea.findbugs.core.FindBugsState;

public final class AnalyzeClassUnderCursor extends AbstractAnalyzeAction {

	@Override
	void updateImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		boolean enabled = false;
		if (state.isIdle()) {
			final VirtualFile selectedFile = IdeaUtilImpl.getVirtualFile(e.getDataContext());
			enabled = selectedFile != null &&
					IdeaUtilImpl.isValidFileType(selectedFile.getFileType()) &&
					IdeaUtilImpl.getCurrentClass(e.getDataContext()) != null;
		}

		e.getPresentation().setEnabled(enabled);
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

		final VirtualFile selectedFile = IdeaUtilImpl.getVirtualFile(e.getDataContext());
		final PsiClass psiClass = IdeaUtilImpl.getCurrentClass(e.getDataContext());

		new FindBugsStarter(project, "Running FindBugs analysis for current class...") {
			@Override
			protected void createCompileScope(@NotNull final CompilerManager compilerManager, @NotNull final Consumer<CompileScope> consumer) {
				consumer.consume(createFilesCompileScope(compilerManager, new VirtualFile[]{selectedFile}));
			}

			@Override
			protected boolean configure(@NotNull final ProgressIndicator indicator, @NotNull final FindBugsProjects projects, final boolean justCompiled) {
				final Module module = ModuleUtilCore.findModuleForFile(selectedFile, project);
				if (module == null) {
					throw new IllegalStateException("No module found for " + selectedFile);
				}
				final boolean isTest = ProjectRootManager.getInstance(project).getFileIndex().isInTestSourceContent(selectedFile);
				final FindBugsProject findBugsProject = projects.get(module, isTest);
				findBugsProject.addOutputFile(selectedFile, psiClass);
				return true;
			}
		}.start();
	}
}
