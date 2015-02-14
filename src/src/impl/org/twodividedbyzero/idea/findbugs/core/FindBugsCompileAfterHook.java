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
package org.twodividedbyzero.idea.findbugs.core;

import com.intellij.compiler.server.BuildManagerListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilationStatusListener;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.92
 */
public class FindBugsCompileAfterHook implements CompilationStatusListener, ProjectComponent {


	private static final ConcurrentMap<UUID, Set<VirtualFile>> CHANGED_BY_SESSION_ID = new ConcurrentHashMap<UUID, Set<VirtualFile>>();
	private static ChangeCollector CHANGE_COLLECTOR; // EDT thread confinement


	static {
		/**
		 * Note that ProjectData is cleared before BuildManagerListener#buildStarted is invoked,
		 * so we can not use BuildManager.getInstance().getFilesChangedSinceLastCompilation(project).
		 * There is no way to get the affect/compiled files (check with source of IC-140.2285.5).
		 * As a workaround, {@link ChangeCollector} will collect the changes.
		 */
		ApplicationManager.getApplication().getMessageBus().connect().subscribe(BuildManagerListener.TOPIC, new BuildManagerListener() {
			public void buildStarted(final Project project, final UUID sessionId, final boolean isAutomake) {
				if (isAutomake && isAfterAutoMakeEnabled(project)) {
					final Set<VirtualFile> changed = Changes.INSTANCE.getAndRemoveChanged(project);
					if (changed != null) {
						CHANGED_BY_SESSION_ID.put(sessionId, changed);
					}
				}
			}


			@Override
			public void buildFinished(final Project project, final UUID sessionId, final boolean isAutomake) {
				if (isAutomake) {
					final Set<VirtualFile> changed = CHANGED_BY_SESSION_ID.remove(sessionId);
					if (changed != null) {
						ApplicationManager.getApplication().runReadAction(new Runnable() {
							@Override
							public void run() {
								initWorkerForAutoMake(project, changed.toArray( new VirtualFile[changed.size()]));
							}
						});
					}
				} // else do nothing ; see FindBugsCompileAfterHook#compilationFinished
			}
		});
	}


	private final Project _project;


	public FindBugsCompileAfterHook(@NotNull final Project project) {
		_project = project;
	}


	@Override
	public void compilationFinished(final boolean aborted, final int errors, final int warnings, final CompileContext compileContext) {
		// note that this is not invoked when auto make trigger compilation
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


	/**
	 * Invoked by EDT.
	 */
	@Override
	public void projectClosed() {
		CompilerManager.getInstance(_project).removeCompilationStatusListener(this);
		if (isAfterAutoMakeEnabled(_project)) {
			final boolean empty = Changes.INSTANCE.removeListener(_project);
			if (empty) {
				if (CHANGE_COLLECTOR != null) {
					VirtualFileManager.getInstance().removeVirtualFileListener(CHANGE_COLLECTOR);
					CHANGE_COLLECTOR = null;
				}
			}
		}
	}


	public void disposeComponent() {
	}


	/**
	 * Invoked by EDT.
	 */
	@Override
	public void projectOpened() {
		CompilerManager.getInstance(_project).addCompilationStatusListener(this);
		if (isAfterAutoMakeEnabled(_project)) {
			Changes.INSTANCE.addListener(_project);
			if (CHANGE_COLLECTOR == null) {
				CHANGE_COLLECTOR = new ChangeCollector();
				VirtualFileManager.getInstance().addVirtualFileListener(CHANGE_COLLECTOR);
			}
		}
	}


	private static void initWorker(final CompileContext compileContext) {
		final com.intellij.openapi.project.Project project = compileContext.getProject();
		if (null == project) { // project reload, eg: open IDEA project with unknown JRE and fix it
			return;
		}
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
		worker.work("Running FindBugs analysis for affectes files...");
	}


	private static boolean isAfterAutoMakeEnabled(@NotNull final Project project) {
		final FindBugsPlugin findBugsPlugin = IdeaUtilImpl.getPluginComponent(project);
		final FindBugsPreferences preferences = findBugsPlugin.getPreferences();
		return Boolean.valueOf(preferences.getProperty(FindBugsPreferences.ANALYZE_AFTER_AUTOMAKE));
	}


	private static void initWorkerForAutoMake(@NotNull final Project project, @NotNull final VirtualFile[] changed) {
		final FindBugsWorker worker = new FindBugsWorker(project);

		// set aux classpath
		final Module[] modules = ModuleManager.getInstance(project).getModules();
		final List<VirtualFile> classpaths = new LinkedList<VirtualFile>();
		for (final Module module : modules) {
			IdeaUtilImpl.addProjectClasspath(module, classpaths);
		}
		worker.configureAuxClasspathEntries(classpaths.toArray(new VirtualFile[classpaths.size()]));

		// set source files
		worker.configureSourceDirectories(changed);

		// set class files
		worker.configureOutputFiles(changed);
		worker.work("Running FindBugs analysis for project '" + project.getName() + "'...");
	}
}
