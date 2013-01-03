/*
 * Copyright 2010 Andre Pfeiler
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

import com.intellij.openapi.compiler.CompilationStatusListener;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.92
 */
public class FindBugsCompileAfterHook implements CompilationStatusListener, ProjectComponent {

	private final Project _project;


	public FindBugsCompileAfterHook(final Project project) {
		_project = project;
	}


	public void compilationFinished(final boolean aborted, final int errors, final int warnings, final CompileContext compileContext) {
		initWorker(compileContext);
	}


	public void fileGenerated(final String s, final String s1) {
	}


	@SuppressWarnings("UnusedDeclaration")
	public void fileGenerated(final String s) {
	}


	@NotNull
	public String getComponentName() {
		return FindBugsPluginConstants.PLUGIN_ID + "#FindBugsCompileAfterHook";
	}


	public void initComponent() {
	}


	public void projectClosed() {
		CompilerManager.getInstance(_project).removeCompilationStatusListener(this);
	}


	public void disposeComponent() {
	}


	public void projectOpened() {
		CompilerManager.getInstance(_project).addCompilationStatusListener(this);
	}


	private static void initWorker(final CompileContext compileContext) {
		final com.intellij.openapi.project.Project project = compileContext.getProject();
		final FindBugsPlugin findBugsPlugin = IdeaUtilImpl.getPluginComponent(project);
		final FindBugsPreferences preferences = findBugsPlugin.getPreferences();

		if (!Boolean.valueOf(preferences.getProperty(FindBugsPreferences.ANALYZE_AFTER_COMPILE))) {
			return;
		}

		final CompileScope compileScope = compileContext.getCompileScope();
		final VirtualFile[] affectedFiles = compileScope.getFiles(StdFileTypes.JAVA, true);

		if (Boolean.valueOf(preferences.getProperty(FindBugsPreferences.TOOLWINDOW_TO_FRONT))) {
			final ToolWindow toolWindow = IdeaUtilImpl.getToolWindowById(findBugsPlugin.getInternalToolWindowId(), project);
			IdeaUtilImpl.activateToolWindow(toolWindow);
		}

		final FindBugsWorker worker = new FindBugsWorker(project, true);

		// set aux classpath
		final Collection<VirtualFile> auxFiles = new ArrayList<VirtualFile>();
		for (final VirtualFile affectedFile : affectedFiles) {
			final Module module = compileContext.getModuleByFile(affectedFile);
			final VirtualFile[] files = IdeaUtilImpl.getProjectClasspath(module);
			auxFiles.addAll(Arrays.asList(files));
		}

		worker.configureAuxClasspathEntries(auxFiles.toArray(new VirtualFile[auxFiles.size()]));

		// set source dirs
		worker.configureSourceDirectories(affectedFiles);

		// set class files
		worker.configureOutputFiles(affectedFiles);
		worker.work();
	}
}
