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


import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementVisitor;
import edu.umd.cs.findbugs.BugCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.collectors.StatelessClassAdder;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;
import org.twodividedbyzero.idea.findbugs.core.FindBugsStarter;
import org.twodividedbyzero.idea.findbugs.messages.AnalysisStateListener;
import org.twodividedbyzero.idea.findbugs.messages.MessageBusManager;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Reto Merz<reto.merz@gmail.com>
 * @version $Revision$
 * @since 0.9.99
 */
public final class AnalyzeScopeFiles extends BaseAnalyzeAction implements AnalysisStateListener {

	private static final Logger LOGGER = Logger.getInstance(AnalyzeScopeFiles.class.getName());

	private DataContext _dataContext;
	private boolean _enabled;
	private boolean _running;


	@Override
	public void actionPerformed(@NotNull final AnActionEvent e) {
		_dataContext = e.getDataContext();

		final com.intellij.openapi.project.Project project = DataKeys.PROJECT.getData(_dataContext);
		assert project != null;
		final Presentation presentation = e.getPresentation();

		// check a project is loaded
		if (isProjectNotLoaded(project, presentation)) {
			Messages.showWarningDialog("Project not loaded.", "FindBugs");
			return;
		}

		final FindBugsPreferences preferences = getPluginInterface(project).getPreferences();
		if (preferences.getBugCategories().containsValue("true") && preferences.getDetectors().containsValue("true")) {
			super.actionPerformed(e);
		} else {
			FindBugsPluginImpl.showToolWindowNotifier(project, "No bug categories or bug pattern detectors selected. analysis aborted.", MessageType.WARNING);
			ShowSettingsUtil.getInstance().editConfigurable(project, IdeaUtilImpl.getPluginComponent(project));
		}
	}


	@Override
	public void update(@NotNull final AnActionEvent event) {
		try {
			_dataContext = event.getDataContext();
			final Project project = DataKeys.PROJECT.getData(_dataContext);
			final Presentation presentation = event.getPresentation();

			if (isProjectNotLoaded(project, presentation)) {
				return;
			}
			isPluginAccessible(project);

			// check if tool window is registered
			final ToolWindow toolWindow = isToolWindowRegistered(project);
			if (toolWindow == null) {
				presentation.setEnabled(false);
				presentation.setVisible(false);
				return;
			}

			MessageBusManager.subscribeAnalysisState(project, this, this);

			if (!_running) {
				_enabled = project.isInitialized() && project.isOpen();
			}
			presentation.setEnabled(toolWindow.isAvailable() && isEnabled());
			presentation.setVisible(true);

		} catch (final Throwable e) {
			final FindBugsPluginException processed = FindBugsPluginImpl.processError("Action update failed", e);
			LOGGER.error("Action update failed", processed);
		}
	}


	@Override
	protected void analyze(@NotNull final Project project, final AnalysisScope scope) {
		final Module module = IdeaUtilImpl.getModule(_dataContext);
		final FindBugsPreferences preferences = FindBugsPreferences.getPreferences(project, module);
		new FindBugsStarter(project, "Running FindBugs analysis...", preferences) {
			@Override
			protected void configure(@NotNull final ProgressIndicator indicator, @NotNull final FindBugsProject findBugsProject) {

				indicator.setText("Collecting auxiliary classpath entries...");
				final VirtualFile[] files = IdeaUtilImpl.getProjectClasspath(_dataContext);
				findBugsProject.configureAuxClasspathEntries(files);

				indicator.setText("Configure source directories...");
				final VirtualFile[] sourceRoots = IdeaUtilImpl.getModulesSourceRoots(_dataContext);
				findBugsProject.configureSourceDirectories(sourceRoots);

				indicator.setText("Collecting files for analysis...");
				addClasses(indicator, project, scope, findBugsProject);

			}
		}.start();
	}


	protected boolean isRunning() {
		return _running;
	}


	protected boolean setRunning(final boolean running) {
		final boolean was = _running;
		if (_running != running) {
			_running = running;
		}
		return was;
	}


	@Override
	protected boolean isEnabled() {
		return _enabled;
	}


	@Override
	protected boolean setEnabled(final boolean enabled) {
		final boolean was = _enabled;
		if (_enabled != enabled) {
			_enabled = enabled;
		}
		return was;
	}


	private void addClasses(
			@NotNull final ProgressIndicator indicator,
			@NotNull final Project project,
			@NotNull final AnalysisScope scope,
			@NotNull final FindBugsProject findBugsProject
	) {

		final List<String> outputFiles = new ArrayList<String>();
		final StatelessClassAdder sca = new StatelessClassAdder(findBugsProject, project);
		final PsiManager psiManager = PsiManager.getInstance(project);
		psiManager.startBatchFilesProcessingMode();
		final int[] count = new int[1];
		try {
			scope.accept(new PsiRecursiveElementVisitor() {
				@Override
				public void visitFile(final PsiFile file) {
					if (IdeaUtilImpl.SUPPORTED_FILE_TYPES.contains(file.getFileType())) {
						final VirtualFile vf = file.getVirtualFile();
						outputFiles.add(vf.getPath());
						sca.addContainingClasses(vf);
						indicator.setText2("Files collected: " + ++count[0]);
					}
				}
			});
		} finally {
			psiManager.finishBatchFilesProcessingMode();
		}
		findBugsProject.setConfiguredOutputFiles(outputFiles);
	}


	@Override
	public void analysisStarted() {
		setEnabled(false);
		setRunning(true);
	}


	@Override
	public void analysisAborted() {
		setEnabled(true);
		setRunning(false);
	}


	@Override
	public void analysisFinished(@NotNull BugCollection bugCollection, @Nullable FindBugsProject findBugsProject) {
		setEnabled(true);
		setRunning(false);
	}
}
