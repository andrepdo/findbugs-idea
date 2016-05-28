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

import com.intellij.analysis.AnalysisScope;
import com.intellij.analysis.AnalysisScopeBundle;
import com.intellij.analysis.AnalysisUIOptions;
import com.intellij.analysis.BaseAnalysisActionDialog;
import com.intellij.ide.highlighter.ArchiveFileType;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.util.Consumer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProjects;
import org.twodividedbyzero.idea.findbugs.core.FindBugsStarter;
import org.twodividedbyzero.idea.findbugs.core.FindBugsState;

import javax.swing.Action;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class AnalyzeScopeFiles extends AbstractAnalyzeAction {

	@Override
	void updateImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		e.getPresentation().setEnabled(state.isIdle());
		e.getPresentation().setVisible(true);
	}


	@Override
	void analyze(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		final DataContext dataContext = e.getDataContext();
		final Module module = e.getData(LangDataKeys.MODULE);
		AnalysisScope scope = getInspectionScope(dataContext);
		final boolean rememberScope = e.getPlace().equals(ActionPlaces.MAIN_MENU);
		final AnalysisUIOptions uiOptions = AnalysisUIOptions.getInstance(project);
		final PsiElement element = LangDataKeys.PSI_ELEMENT.getData(dataContext);
		final BaseAnalysisActionDialog dlg = new BaseAnalysisActionDialog(AnalysisScopeBundle.message("specify.analysis.scope", "FindBugs Analyze"),
				AnalysisScopeBundle.message("analysis.scope.title", "Analyze"),
				project,
				scope,
				module != null && scope.getScopeType() != AnalysisScope.MODULE ? getModuleNameInReadAction(module) : null,
				rememberScope, AnalysisUIOptions.getInstance(project), element) {

			@Override
			protected void doHelpAction() {
				HelpManager.getInstance().invokeHelp(getHelpTopic());
			}

			@SuppressFBWarnings({"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
			@NotNull
			@Override
			protected Action[] createActions() {
				return new Action[]{getOKAction(), getCancelAction(), getHelpAction()};
			}
		};
		dlg.show();
		if (!dlg.isOK()) {
			return;
		}
		final int oldScopeType = uiOptions.SCOPE_TYPE;
		scope = dlg.getScope(uiOptions, scope, project, module);
		if (!rememberScope) {
			uiOptions.SCOPE_TYPE = oldScopeType;
		}
		uiOptions.ANALYZE_TEST_SOURCES = dlg.isInspectTestSources();
		FileDocumentManager.getInstance().saveAllDocuments();

		analyzeImpl(e, project, scope, dlg.isInspectTestSources());
	}


	private void analyzeImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final AnalysisScope scope,
			final boolean includeTests
	) {
		new FindBugsStarter(project, "Running FindBugs analysis...") {
			@Override
			protected void createCompileScope(@NotNull final CompilerManager compilerManager, @NotNull final Consumer<CompileScope> consumer) {
				consumer.consume(compilerManager.createProjectCompileScope(project));
			}

			@Override
			protected boolean configure(@NotNull final ProgressIndicator indicator, @NotNull final FindBugsProjects projects, final boolean justCompiled) {
				addClasses(indicator, project, scope, projects, includeTests);
				return true;
			}
		}.start();
	}

	@SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
	private void addClasses(
			@NotNull final ProgressIndicator indicator,
			@NotNull final Project project,
			@NotNull final AnalysisScope scope,
			@NotNull final FindBugsProjects projects,
			final boolean includeTests
	) {

		final PsiManager psiManager = PsiManager.getInstance(project);
		psiManager.startBatchFilesProcessingMode();
		final int[] count = new int[1];
		try {
			scope.accept(new PsiRecursiveElementVisitor() {
				@Override
				public void visitFile(final PsiFile file) {
					if (indicator.isCanceled() || FindBugsState.get(project).isAborting()) {
						throw new ProcessCanceledException();
					}
					if (IdeaUtilImpl.SUPPORTED_FILE_TYPES.contains(file.getFileType())) {
						final VirtualFile vf = file.getVirtualFile();
						projects.addFile(vf, false, includeTests); // LATER: profile this
						indicator.setText2("Files collected: " + ++count[0]);
					}
				}
			});
		} finally {
			psiManager.finishBatchFilesProcessingMode();
		}
	}

	@NonNls
	private String getHelpTopic() {
		return "reference.dialogs.analyzeDependencies.scope";
	}

	@Nullable
	private AnalysisScope getInspectionScope(@NotNull final DataContext dataContext) {
		if (PlatformDataKeys.PROJECT.getData(dataContext) == null) return null;

		final AnalysisScope scope = getInspectionScopeImpl(dataContext);

		return scope != null && scope.getScopeType() != AnalysisScope.INVALID ? scope : null;
	}

	@Nullable
	private AnalysisScope getInspectionScopeImpl(@NotNull final DataContext dataContext) {
		//Possible scopes: file, directory, package, project, module.
		final Project projectContext = PlatformDataKeys.PROJECT_CONTEXT.getData(dataContext);
		if (projectContext != null) {
			return new AnalysisScope(projectContext);
		}

		final AnalysisScope analysisScope = AnalyzeUtil.KEY.getData(dataContext);
		if (analysisScope != null) {
			return analysisScope;
		}

		final Module moduleContext = LangDataKeys.MODULE_CONTEXT.getData(dataContext);
		if (moduleContext != null) {
			return new AnalysisScope(moduleContext);
		}

		final Module[] modulesArray = LangDataKeys.MODULE_CONTEXT_ARRAY.getData(dataContext);
		if (modulesArray != null) {
			return new AnalysisScope(modulesArray);
		}
		final PsiFile psiFile = LangDataKeys.PSI_FILE.getData(dataContext);
		if (psiFile != null && psiFile.getManager().isInProject(psiFile)) {
			final VirtualFile file = psiFile.getVirtualFile();
			if (file != null && file.isValid() && file.getFileType() instanceof ArchiveFileType && acceptNonProjectDirectories()) {
				final VirtualFile jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(file);
				if (jarRoot != null) {
					final PsiDirectory psiDirectory = psiFile.getManager().findDirectory(jarRoot);
					if (psiDirectory != null) {
						return new AnalysisScope(psiDirectory);
					}
				}
			}
			return new AnalysisScope(psiFile);
		}

		final VirtualFile[] virtualFiles = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
		final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
		if (virtualFiles != null && project != null) { //analyze on selection
			final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
			if (virtualFiles.length == 1) {
				final PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(virtualFiles[0]);
				if (psiDirectory != null && (acceptNonProjectDirectories() || psiDirectory.getManager().isInProject(psiDirectory))) {
					return new AnalysisScope(psiDirectory);
				}
			}
			final Set<VirtualFile> files = new HashSet<VirtualFile>();
			for (VirtualFile vFile : virtualFiles) {
				if (fileIndex.isInContent(vFile)) {
					if (vFile instanceof VirtualFileWindow) {
						files.add(vFile);
						vFile = ((VirtualFileWindow) vFile).getDelegate();
					}
					collectFilesUnder(vFile, files);
				}
			}
			return new AnalysisScope(project, files);
		}
		return project == null ? null : new AnalysisScope(project);
	}

	private boolean acceptNonProjectDirectories() {
		return false;
	}

	private static void collectFilesUnder(@NotNull final VirtualFile vFile, @NotNull final Collection<VirtualFile> files) {
		VfsUtilCore.visitChildrenRecursively(vFile, new VirtualFileVisitor() {
			@Override
			public boolean visitFile(@NotNull final VirtualFile file) {
				if (!file.isDirectory()) {
					files.add(file);
				}
				return true;
			}
		});
	}

	private static String getModuleNameInReadAction(@NotNull final Module module) {
		return new ReadAction<String>() {
			protected void run(@NotNull final Result<String> result) throws Throwable {
				result.setResult(module.getName());
			}
		}.execute().getResultObject();
	}
}
