/*
 * Copyright 2009 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

	private Project _project;


	public FindBugsCompileAfterHook(final Project project) {
		_project = project;
	}

	public void compilationFinished(final boolean aborted, final int errors, final int warnings, final CompileContext compileContext) {
		initWorker(compileContext);
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
			final ToolWindow toolWindow = IdeaUtilImpl.getToolWindowById(findBugsPlugin.getInternalToolWindowId());
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
