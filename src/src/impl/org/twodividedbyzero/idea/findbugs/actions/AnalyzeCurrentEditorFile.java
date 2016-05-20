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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.Consumer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;
import org.twodividedbyzero.idea.findbugs.core.FindBugsStarter;
import org.twodividedbyzero.idea.findbugs.core.FindBugsState;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.gui.common.BalloonTipFactory;

public final class AnalyzeCurrentEditorFile extends AbstractAnalyzeAction {

	@Override
	void updateImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@Nullable final Module module,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state,
			@NotNull final ProjectSettings projectSettings,
			@NotNull final AbstractSettings settings
	) {

		final VirtualFile[] selectedSourceFiles = IdeaUtilImpl.getVirtualFiles(e.getDataContext());
		boolean enable = false;
		if (state.isIdle()) {
			enable = selectedSourceFiles != null &&
					selectedSourceFiles.length > 0 &&
					selectedSourceFiles[0].isValid() &&
					IdeaUtilImpl.isValidFileType(selectedSourceFiles[0].getFileType());
		}

		e.getPresentation().setEnabled(enable);
		e.getPresentation().setVisible(true);
	}

	@SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
	@Override
	void analyze(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@Nullable final Module module,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state,
			@NotNull final ProjectSettings projectSettings,
			@NotNull final AbstractSettings settings
	) {

		final VirtualFile[] files = IdeaUtilImpl.getProjectClasspath(e.getDataContext());
		final VirtualFile[] selectedSourceFiles = IdeaUtilImpl.getVirtualFiles(e.getDataContext());
		if (selectedSourceFiles == null) {
			BalloonTipFactory.showToolWindowWarnNotifier(project, "No current files");
			return;
		}

		new FindBugsStarter(project, "Running FindBugs analysis for editor files...", projectSettings, settings) {
			@Override
			protected void createCompileScope(@NotNull final CompilerManager compilerManager, @NotNull final Consumer<CompileScope> consumer) {
				consumer.consume(compilerManager.createFilesCompileScope(selectedSourceFiles));
			}

			@Override
			protected void configure(@NotNull final ProgressIndicator indicator, @NotNull final FindBugsProject findBugsProject) {
				findBugsProject.configureAuxClasspathEntries(indicator, files);
				findBugsProject.configureSourceDirectories(indicator, selectedSourceFiles);
				findBugsProject.configureOutputFiles(project, selectedSourceFiles);
			}
		}.start();
	}
}
