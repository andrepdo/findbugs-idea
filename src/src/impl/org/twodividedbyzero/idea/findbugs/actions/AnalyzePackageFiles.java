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
package org.twodividedbyzero.idea.findbugs.actions;


import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;
import org.twodividedbyzero.idea.findbugs.core.FindBugsStarter;
import org.twodividedbyzero.idea.findbugs.core.FindBugsState;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;


/**
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public final class AnalyzePackageFiles extends AbstractAnalyzeAction {

	@Override
	void updateImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@Nullable final Module module,
			@NotNull final FindBugsPlugin plugin,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state,
			@NotNull final FindBugsPreferences preferences
	) {

		final VirtualFile[] selectedSourceFiles = IdeaUtilImpl.getVirtualFiles(e.getDataContext());

		boolean enable = false;
		if (state.isIdle()) {
			enable = selectedSourceFiles != null && selectedSourceFiles.length == 1 &&
					(IdeaUtilImpl.isValidFileType(selectedSourceFiles[0].getFileType()) || selectedSourceFiles[0].isDirectory()) &&
					null != getPackagePath(selectedSourceFiles, project);
		}

		e.getPresentation().setEnabled(enable);
		e.getPresentation().setVisible(true);
	}


	@Override
	void analyze(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@Nullable final Module module,
			@NotNull final FindBugsPlugin plugin,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state,
			@NotNull final FindBugsPreferences preferences
	) {

		final VirtualFile[] files = IdeaUtilImpl.getProjectClasspath(e.getDataContext());
		final VirtualFile[] selectedSourceFiles = IdeaUtilImpl.getVirtualFiles(e.getDataContext());
		final VirtualFile packagePath = getPackagePath(selectedSourceFiles, project);
		if (packagePath == null) {
			throw new IllegalStateException("No package path"); // see updateImpl
		}
		final VirtualFile outPath = IdeaUtilImpl.getCompilerOutputPath(packagePath, project);
		final String packageUrl = IdeaUtilImpl.getPackageAsPath(project, packagePath);
		final VirtualFile[] sourceRoots = IdeaUtilImpl.getModulesSourceRoots(e.getDataContext());

		if (outPath != null) {
			final String output = outPath.getPresentableUrl() + packageUrl;
			new FindBugsStarter(project, "Running FindBugs analysis for directory '" + output + "'...", preferences) {
				@Override
				protected void configure(@NotNull ProgressIndicator indicator, @NotNull FindBugsProject findBugsProject) {
					findBugsProject.configureAuxClasspathEntries(indicator, files);
					findBugsProject.configureSourceDirectories(indicator, sourceRoots);
					findBugsProject.configureOutputFiles(output);
				}
			}.start();

		} else {
			FindBugsPluginImpl.showToolWindowNotifier(project, "No output path specified. Analysis aborted.", MessageType.WARNING);
		}
	}


	@Nullable
	private static VirtualFile getPackagePath(@Nullable final VirtualFile[] selectedSourceFiles, @NotNull final Project project) {
		VirtualFile packagePath = null;
		if (selectedSourceFiles != null && selectedSourceFiles.length > 0) {
			for (final VirtualFile virtualFile : selectedSourceFiles) {
				final Module moduleOfFile = IdeaUtilImpl.findModuleForFile(virtualFile, project);
				if (moduleOfFile == null) {
					return null;
				}

				if (virtualFile.isDirectory()) {
					if (!virtualFile.getPath().endsWith(moduleOfFile.getName())) {
						packagePath = virtualFile;
					}
				} else {
					final VirtualFile parent = virtualFile.getParent();
					if (parent != null && !parent.getPath().endsWith(moduleOfFile.getName())) {
						packagePath = parent;
					}
				}
			}
		}
		return packagePath;
	}
}