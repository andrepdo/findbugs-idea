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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.Consumer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProjects;
import org.twodividedbyzero.idea.findbugs.core.FindBugsStarter;
import org.twodividedbyzero.idea.findbugs.core.FindBugsState;

import java.util.List;

public final class AnalyzeChangelistFiles extends AbstractAnalyzeAction {

	@Override
	void updateImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		boolean enable = false;
		final List<VirtualFile> modifiedFiles = IdeaUtilImpl.getAllModifiedFiles(e.getDataContext());
		if (state.isIdle() && !modifiedFiles.isEmpty()) {
			final ChangeList[] changeLists = e.getData(VcsDataKeys.CHANGE_LISTS);
			if (changeLists != null) {
				for (final ChangeList changeList : changeLists) {
					for (final Change change : changeList.getChanges()) {
						final VirtualFile file = change.getVirtualFile();
						if (file != null) {
							if (IdeaUtilImpl.isValidFileType(file.getFileType())) {
								enable = true;
								break;
							}
						}
					}
					if (enable) {
						break;
					}
				}
			}
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

		final List<VirtualFile> files = New.arrayList();
		final ChangeList[] changeLists = e.getData(VcsDataKeys.CHANGE_LISTS);
		final StringBuilder sb = new StringBuilder();
		for (final ChangeList changeList : changeLists) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(changeList.getName());
			for (final Change change : changeList.getChanges()) {
				final VirtualFile file = change.getVirtualFile();
				if (file != null) {
					files.add(file);
				}
			}
		}

		new FindBugsStarter(project, "Running FindBugs analysis for changelist(s) '" + sb.toString() + "'...") {
			@Override
			protected void createCompileScope(@NotNull final CompilerManager compilerManager, @NotNull final Consumer<CompileScope> consumer) {
				consumer.consume(createFilesCompileScope(compilerManager, files));
			}

			@Override
			protected boolean configure(@NotNull final ProgressIndicator indicator, @NotNull final FindBugsProjects projects, final boolean justCompiled) {
				return projects.addFiles(files, !justCompiled, hasTests(files));
			}
		}.start();
	}
}
