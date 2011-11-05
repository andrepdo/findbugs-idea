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
package org.twodividedbyzero.idea.findbugs.common.util;

import com.intellij.ide.DataManager;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ProjectRootsTraversing;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.collectors.RecurseClassCollector;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
@SuppressWarnings({"HardcodedFileSeparator", "UnusedDeclaration"})
public final class IdeaUtilImpl {

	@NotNull
	private static final VirtualFile[] EMPTY_VIRTUAL_FILE = new VirtualFile[0];
	@NotNull
	private static final String IDEA_PROJECT_DIR_VAR = "$PROJECT_DIR$";


	private IdeaUtilImpl() {
	}


	public static FindBugsPlugin getPluginComponent(@NotNull final Project project) {
		return project.getComponent(FindBugsPlugin.class);
	}


	public static FindBugsPlugin getModuleComponent(@NotNull final Module module) {
		return module.getComponent(FindBugsPlugin.class);
	}


	/**
	 * Get the base path of the project.
	 *
	 * @param project the open idea project
	 * @return the base path of the project.
	 */
	@Nullable
	public static File getProjectPath(@Nullable final Project project) {
		if (project == null) {
			return null;
		}

		final VirtualFile baseDir = project.getBaseDir();
		if (baseDir == null) {
			return null;
		}

		return new File(baseDir.getPath());
	}


	@Nullable
	public static Project getProject(@NotNull final DataContext dataContext) {
		return DataKeys.PROJECT.getData(dataContext);
	}


	/**
	 * @deprecated use {@link #getProject(com.intellij.psi.PsiElement)} or {@link #getProject(com.intellij.openapi.actionSystem.DataContext)}
	 *
	 * @return Project the current idea project
	 */
	@Deprecated
	@Nullable
	public static Project getProject() {
		return getProject(getDataContext());
	}


	public static Project getProject(final PsiElement psiElement) {
		return psiElement.getProject();
	}


	@Deprecated
	public static DataContext getDataContext() {
		return DataManager.getInstance().getDataContext();
	}


	/**
	 * Locates the current PsiFile.
	 *
	 * @param dataContext The IntelliJ DataContext (can usually be obtained from the action-event).
	 * @return The current PsiFile or null if not found.
	 */
	@Nullable
	private static PsiFile getPsiFile(@NotNull final DataContext dataContext) {
		return (PsiFile) dataContext.getData("psi.File");
	}


	@Nullable
	public static PsiFile getPsiFile(@NotNull final DataContext dataContext, @NotNull final VirtualFile virtualFile) {
		final Project project = IdeaUtilImpl.getProject(dataContext);
		if (project != null) {
			return PsiManager.getInstance(project).findFile(virtualFile);
		}
		return null;
	}


	@Nullable
	public static PsiFile getPsiFile(@NotNull final Project project, @NotNull final VirtualFile virtualFile) {
		return PsiManager.getInstance(project).findFile(virtualFile);
	}


	@Nullable
	public static PsiFile getPsiFile(@Nullable final PsiElement psiClass) {
		if (psiClass == null) {
			return null;
		}
		return psiClass.getContainingFile();
	}


	@Nullable
	private static VirtualFile getSelectedFile(@NotNull final DataContext dataContext) {
		final VirtualFile[] selectedFiles = getSelectedFiles(dataContext);
		if (selectedFiles.length == 0) {
			return null;
		} else {
			return selectedFiles[0];
		}
	}


	@NotNull
	private static VirtualFile[] getSelectedFiles(@NotNull final DataContext dataContext) {
		final Project project = getProject(dataContext);
		return FileEditorManager.getInstance(project).getSelectedFiles();
	}


	@NotNull
	public static List<VirtualFile> getAllModifiedFiles(@NotNull final DataContext dataContext) {
		final Project project = getProject(dataContext);
		final ChangeListManager changeListManager = ChangeListManager.getInstance(project);
		return changeListManager.getAffectedFiles();
	}


	// todo:
	/*public static void getSelectedChangelist() {
		final Project project = getProject();
		final ChangeListManager changeListManager = ChangeListManager.getInstance(project);
		for (final LocalChangeList list : changeListManager.getChangeLists()) {
		}

	}*/


	@Nullable
	public static ChangeList getChangeListByName(final String name, final Project project) {
		final ChangeListManager changeListManager = ChangeListManager.getInstance(project);
		return changeListManager.findChangeList(name);
	}


	@NotNull
	public static Collection<VirtualFile> getModifiedFilesByList(@NotNull final ChangeList list, final DataContext dataContext) {
		final Collection<VirtualFile> result = new ArrayList<VirtualFile>();
		final Collection<Change> changeCollection = list.getChanges();
		// (Change[]) DataProvider.getData(DataConstants.CHANGES)
		for (final Change change : changeCollection) {
			final ContentRevision contentRevision = change.getAfterRevision();
			if (contentRevision != null) {
				final FilePath path = contentRevision.getFile();
				result.add(path.getVirtualFile());
			}
		}
		return result;
	}


	public static VirtualFile[] getFilesForAllModules(final Project project) {
		return ProjectRootManager.getInstance(project).getFilesFromAllModules(OrderRootType.CLASSES);
	}


	@NotNull
	public static VirtualFile[] getFilesForModules(final Module module) {
		final ModuleRootManager mrm = ModuleRootManager.getInstance(module);
		// TODO: test
		return mrm.getFiles(OrderRootType.SOURCES);
	}


	@Nullable
	public static VirtualFile[] getVirtualFiles(@NotNull final DataContext dataContext) {
		return DataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
	}


	@Nullable
	public static VirtualFile getVirtualFile(@NotNull final DataContext dataContext) {
		return DataKeys.VIRTUAL_FILE.getData(dataContext);
	}


	@SuppressWarnings({"TypeMayBeWeakened"})
	@Nullable
	public static VirtualFile getVirtualFile(@NotNull final PsiClass psiClass) {
		final PsiFile containingFile = psiClass.getContainingFile();
		if (containingFile != null) {
			return containingFile.getVirtualFile();
		}
		return null;
	}


	@Nullable
	public static VirtualFile getVirtualFile(@NotNull final PsiFileSystemItem fileSystemItem) {
		return fileSystemItem.getVirtualFile();
	}


	@Nullable
	public static VirtualFile getVirtualFile(final PsiElement element) {
		return PsiUtil.getVirtualFile(element);
	}


	@NotNull
	public static PsiClass[] getPsiClasses(@NotNull final PsiClassOwner psiClassOwner) {
		return psiClassOwner.getClasses();
	}


	@NotNull
	public static PsiClass[] getContainingClasses(@NotNull final DataContext dataContext) {
		final Project project = getProject(dataContext);

		final Editor selectedEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
		if (selectedEditor == null) {
			throw new NullPointerException("Expected not null Editor");
		}
		if (project == null) {
			throw new NullPointerException("Expected not null project");
		}
		final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(selectedEditor.getDocument());

		//noinspection ZeroLengthArrayAllocation
		PsiClass[] clazzes = new PsiClass[0];
		if (psiFile instanceof PsiJavaFile) {
			clazzes = ((PsiJavaFile) psiFile).getClasses();
			if (clazzes.length > 0) {
				// note, there could be more then one class in the file
				// one needs to find public class or class with name = name of
				// file w/o extension
				// so adjust the index accordingly
				System.out.println(clazzes[0].getQualifiedName());
			}
		}

		return clazzes;
	}


	@NotNull
	public static String[] getCompilerOutputUrls(final Project project) {
		final VirtualFile[] vFiles = getCompilerOutputPaths(project);
		final int length = vFiles.length;
		final String[] files = new String[length];

		for (int i = 0; i < length; i++) {
			final VirtualFile file = vFiles[i];
			if (file != null) {
				files[i] = file.getPresentableUrl();
			} else {
				files[i] = ""; // todo: remove -> crude, getProjectOutputPath(final Module module) returns always null
			}
		}

		return files;
	}


	@NotNull
	private static VirtualFile[] getCompilerOutputPaths(final Project project) {
		final Module[] modules = getModules(project);
		final int length = modules.length;
		final VirtualFile[] vFiles = new VirtualFile[length];

		for (int i = 0; i < length; i++) {
			final Module module = modules[i];
			final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
			VirtualFile path = null;
			if (extension != null) {
				path = extension.getCompilerOutputPath();
			}
			if (path == null) {
				//throw new FindBugsPluginException("Make sure your module compiler output path configuration points to a existing directory in module (" + module.getName() + ")");
				path = getProjectOutputPath(module);
			}
			vFiles[i] = path;
			// TODO: facade
			//ModuleRootManager.getInstance(module).getCompilerOutputPath();
		}

		return vFiles;
	}


	@Nullable
	public static VirtualFile getCompilerOutputPath(final Module module) {
		final CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module);
		if (compilerModuleExtension != null) {
			return compilerModuleExtension.getCompilerOutputPath();
		}
		return null;
	}


	@Nullable
	public static VirtualFile getCompilerOutputPath(@NotNull final DataContext dataContext) {
		final Module module = getModule(dataContext);

		// TODO: facade
		final CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module);
		if (compilerModuleExtension != null) {
			return compilerModuleExtension.getCompilerOutputPath();
		}
		return null;
		//return ModuleRootManager.getInstance(module).getCompilerOutputPath();
	}


	@Nullable
	public static VirtualFile getCompilerOutputPath(@NotNull final VirtualFile virtualFile, @NotNull final Project project) {
		final Module module = findModuleForFile(virtualFile, project);

		// TODO: facade
		final CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module);
		if (compilerModuleExtension != null) {
			return compilerModuleExtension.getCompilerOutputPath();
		}
		return null;
		//return ModuleRootManager.getInstance(module).getCompilerOutputPath();
	}


	@Nullable
	private static VirtualFile getProjectOutputPath(@NotNull final Module module) {
		final Project project = module.getProject();
		final CompilerProjectExtension compilerProjectExtension = CompilerProjectExtension.getInstance(project);
		if (compilerProjectExtension != null) {
			return compilerProjectExtension.getCompilerOutput();
		}
		return null;
	}


	@NotNull
	private static String getPackage(@NotNull final PsiElement psiElement) {
		return ((PsiClassOwner) psiElement.getContainingFile()).getPackageName();
	}


	public static String getPackageUrl(@NotNull final PsiElement psiElement) {
		return getPackage(psiElement).replace('.', '/');
	}


	@Nullable
	public static PsiElement getPackage(@NotNull final DataContext datacontext) {
		final PsiElement element = (PsiElement) datacontext.getData("psi.Element");
		if (element instanceof PsiPackage) {
			return element;
		}

		return null;
	}

	/*public static Set<PsiPackage> getPackagesForModule(final Module module, final Project project) {
				final Set<PsiPackage> packages = new HashSet<PsiPackage>();
		final PsiManager psiMan = PsiManager.getInstance(project);
		final PsiPackage psiPackage = psiMan.findPackage("");
		final GlobalSearchScope scope = module.getModuleRuntimeScope(false);
		getSubPackages(psiPackage, scope, packages);

		return packages;
	}*/


	private static void getSubPackages(@NotNull final PsiPackage psiPackage, @NotNull final GlobalSearchScope scope, @NotNull final Set<PsiPackage> result) {
		for (final PsiPackage child : psiPackage.getSubPackages(scope)) {
			result.add(child);
			getSubPackages(child, scope, result);
		}
	}


	public static SourceFolder[] getSourceFolders(@NotNull final DataContext dataContext) {
		final Module module = getModule(dataContext);
		final ContentEntry[] contentEntries = ModuleRootManager.getInstance(module).getContentEntries();

		return contentEntries[0].getSourceFolders();
	}


	public static String getPackageAsPath(@NotNull final com.intellij.openapi.project.Project project, final VirtualFile packagePath, @NotNull final VirtualFile[] sourceRoots) {
		final StringBuilder result = new StringBuilder(30);
		final List<String> list = getPackagePathAsList(project, packagePath, sourceRoots);
		for (final String dir : list) {
			result.append(dir);
		}

		return result.toString();
	}


	@NotNull
	private static List<String> getPackagePathAsList(@NotNull final com.intellij.openapi.project.Project project, @NotNull final VirtualFile packagePath, @NotNull final VirtualFile[] sourceRoots) {
		final Module module = IdeaUtilImpl.findModuleForFile(packagePath, project);
		if (module == null) {
			throw new NullPointerException("Expected not null module.");
		}
		final List<String> parentPath = new ArrayList<String>();
		final Collection<String> sourcesDirs = new ArrayList<String>();

		for (final VirtualFile vFile : sourceRoots) {
			sourcesDirs.add(vFile.getName());
		}

		VirtualFile parent = packagePath;
		while (parent != null && !parent.getName().equals(module.getName()) && !sourcesDirs.contains(parent.getName())) {
			parentPath.add(FindBugsPluginConstants.FILE_SEPARATOR + parent.getName());
			parent = parent.getParent();
		}

		Collections.reverse(parentPath);
		return parentPath;
	}


	public static VirtualFile[] getModulesSourceRoots(@NotNull final DataContext dataContext) {
		final Project project = getProject(dataContext);
		final ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);

		return projectRootManager.getContentSourceRoots();
	}


	@NotNull
	public static Module[] getProjectModules(@NotNull final DataContext dataContext) {
		final Project project = getProject(dataContext);

		return ModuleManager.getInstance(project).getModules();
	}


	@NotNull
	public static VirtualFile[] getProjectClasspath(@NotNull final DataContext dataContext) {
		final Module module = getModule(dataContext);

		return getProjectClasspath(module);
	}


	@NotNull
	public static VirtualFile[] getProjectClasspath(final Module module) {
		final VirtualFile[] files;
		try {
			final ModuleRootManager mrm = ModuleRootManager.getInstance(module);
			files = mrm.getFiles(OrderRootType.COMPILATION_CLASSES);
		} catch (Exception e) {
			throw new FindBugsPluginException("ModuleRootManager must not be null. may be the current class is not a project/module class. check your project/module outpath configuration.", e);
		}
		return files;
	}


	@Nullable
	public static Module findModuleForFile(@NotNull final VirtualFile virtualFile, @NotNull final Project project) {
		return ModuleUtil.findModuleForFile(virtualFile, project);
	}

	/*public static VirtualFile[] getModuleClasspath(final DataContext dataContext) {
		final Module module = getModuleByFile(dataContext);
	}*/


	/*@Nullable
	public static Module getModuleByFile(final DataContext dataContext) {
		final VirtualFile virtualFile;
		final PsiElement psiElement = getCurrentElement(dataContext);
		final PsiFile containingFile = psiElement.getContainingFile();

		virtualFile = containingFile.getVirtualFile();
		if (virtualFile != null) {
			final Project currentProject = getProject(dataContext);
			final ProjectRootManager projectRootManager = ProjectRootManager.getInstance(currentProject);
			final ProjectFileIndex projectFileIndex = projectRootManager.getFileIndex();

			return projectFileIndex.getModuleForFile(virtualFile);
		}

		return null;
	}*/


	@Nullable
	public static Module getModuleForFile(@NotNull final VirtualFile virtualFile, final Project project) {
		final ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
		final ProjectFileIndex projectFileIndex = projectRootManager.getFileIndex();

		return projectFileIndex.getModuleForFile(virtualFile);
	}


	@Nullable
	public static Module getModule(@NotNull final DataContext dataContext) {
		final Project project = getProject(dataContext);
		//final VirtualFile file = getVirtualFile(dataContext);
		final VirtualFile selectedFile = getSelectedFile(dataContext);
		Module module = null;
		if (selectedFile != null) {
			module = ModuleUtil.findModuleForFile(selectedFile, project);
		}

		if (module == null) {
			module = DataKeys.MODULE.getData(dataContext);
		}

		if (module == null) {
			final Module[] modules = getModules(project);
			if (modules.length < 1) {
				throw new FindBugsPluginException("You have no modules configured inside your project (" + project + ')');
			}
			module = modules[0];
		}

		return module;
		//return com.intellij.openapi.vfs.VfsUtil.getModuleForFile(project, file);
		//PsiClass.getContainingFile().getVirtualFile()
	}


	@NotNull
	private static Module[] getModules(final Project project) {
		final ModuleManager moduleManager = ModuleManager.getInstance(project);
		return moduleManager.getModules();
	}


	// TODO: maybe not needed


	public static File[] getFileForModules(@NotNull final Module[] modules, final FileType fileType) {
		final Collection<File> resolvedFiles = new HashSet<File>();

		final ProjectRootsTraversing.RootTraversePolicy traversePolicy = null;
		for (final Module module : modules) {
			final Collection<VirtualFile> virtualFiles = new ArrayList<VirtualFile>();
			final VirtualFile outputDirectory = CompilerPaths.getModuleOutputDirectory(module, false);
			if (outputDirectory != null) {
				virtualFiles.add(outputDirectory);
			}
			for (final VirtualFile virtualFile : virtualFiles) {
				final File file = VfsUtil.virtualToIoFile(virtualFile);
				resolvedFiles.add(file.getAbsoluteFile());
			}
		}

		return resolvedFiles.toArray(new File[resolvedFiles.size()]);
	}


	/**
	 * Retrieves the current PsiElement.
	 *
	 * @param dataContext The IntelliJ DataContext (can usually be obtained from the action-event).
	 * @return The current PsiElement or null if not found.
	 */
	@Nullable
	private static PsiElement getCurrentElement(@NotNull final DataContext dataContext) {
		// Try directly on dataContext
		final PsiElement psiElement = (PsiElement) dataContext.getData("psi.Element");
		//psiElement.getContainingFile().getOriginalFile().getVirtualFile()

		if (psiElement != null) {
			// success
			return psiElement;
		}

		// Try through editor + PsiFile
		//final Editor editor = (Editor) dataContext.getData(DataConstants.EDITOR);// DataConstants.EDITOR
		final Editor editor = DataKeys.EDITOR.getData(dataContext);


		final PsiFile psiFile = getPsiFile(dataContext);
		if (editor != null && psiFile != null) {
			return psiFile.findElementAt(editor.getCaretModel().getOffset());
		}
		// Unable to find currentElement
		return null;
	}


	/**
	 * Retrieves the current PsiClass.
	 *
	 * @param dataContext The IntelliJ DataContext (can usually be obtained from the action-event).
	 * @return The current PsiClass or null if not found.
	 */
	@Nullable
	public static PsiClass getCurrentClass(@NotNull final DataContext dataContext) {
		return findClass(getCurrentElement(dataContext));
	}


	/**
	 * Retrieves the current PsiField.
	 *
	 * @param dataContext The IntelliJ DataContext (can usually be obtained from the action-event).
	 * @return The current PsiField or null if not found.
	 */
	@Nullable
	public static PsiField getCurrentField(@NotNull final DataContext dataContext) {
		return findField(getCurrentElement(dataContext));
	}


	@Nullable
	public static VirtualFile getCurrentFolder(@NotNull final DataContext dataContext) {
		final VirtualFile file = DataKeys.VIRTUAL_FILE.getData(dataContext);
		VirtualFile folder = null;

		if (file != null) {
			folder = file.getParent();
		}
		return folder;
	}


	/**
	 * Retrieves the current PsiMethod.
	 *
	 * @param dataContext The IntelliJ DataContext (can usually be obtained from the action-event).
	 * @return The current PsiMethod or null if not found.
	 */
	@Nullable
	public static PsiMethod getCurrentMethod(@NotNull final DataContext dataContext) {
		return findMethod(getCurrentElement(dataContext));
	}


	/**
	 * Retrieves the current SelectionModel.
	 *
	 * @param dataContext The IntelliJ DataContext (can usually be obtained from the action-event).
	 * @return The current SelectionModel or null if not found.
	 */
	@Nullable
	public static SelectionModel getSelectionModel(@NotNull final DataContext dataContext) {
		//final Editor editor = (Editor) dataContext.getData(DataConstants.EDITOR);
		final Editor editor = DataKeys.EDITOR.getData(dataContext);
		if (editor != null) {
			final SelectionModel model = editor.getSelectionModel();
			if (model.hasSelection()) {
				return model;
			}
		}
		return null;
	}


	/**
	 * Finds the PsiClass for a specific PsiElement.
	 *
	 * @param element The PsiElement to locate the class for.
	 * @return The PsiClass you're looking for or null if not found.
	 */
	@Nullable
	private static PsiClass findClass(final PsiElement element) {
		final PsiClass psiClass = element instanceof PsiClass ? (PsiClass) element : PsiTreeUtil.getParentOfType(element, PsiClass.class);
		if (psiClass instanceof PsiAnonymousClass) {
			return findClass(psiClass.getParent());
		}
		return psiClass;
	}


	/**
	 * Finds the PsiField for a specific PsiElement.
	 *
	 * @param element The PsiElement to locate the field for.
	 * @return The PsiField you're looking for or null if not found.
	 */
	@Nullable
	private static PsiField findField(final PsiElement element) {
		final PsiField psiField = element instanceof PsiField ? (PsiField) element : PsiTreeUtil.getParentOfType(element, PsiField.class);
		if (psiField != null && psiField.getContainingClass() instanceof PsiAnonymousClass) {
			return findField(psiField.getParent());
		}
		return psiField;
	}


	/**
	 * Finds the PsiMethod for a specific PsiElement.
	 *
	 * @param element The PsiElement to locate the method for.
	 * @return The PsiMethod you're looking for or null if not found.
	 */
	@Nullable
	private static PsiMethod findMethod(final PsiElement element) {
		final PsiMethod method = element instanceof PsiMethod ? (PsiMethod) element : PsiTreeUtil.getParentOfType(element, PsiMethod.class);
		if (method != null && method.getContainingClass() instanceof PsiAnonymousClass) {
			return findMethod(method.getParent());
		}
		return method;
	}


	@Nullable
	public static VirtualFile findFileByIoFile(final File file) {
		return LocalFileSystem.getInstance().findFileByIoFile(file);
	}


	@Nullable
	public static VirtualFile findFileByPath(@NotNull final String path) {
		return LocalFileSystem.getInstance().findFileByPath(path.replace(File.separatorChar, '/'));
		//return LocalFileSystem.getInstance().findFileByPath(path);
	}


	/**
	 * Find a PsiClass using {@link GlobalSearchScope#allScope(com.intellij.openapi.project.Project)} }
	 *
	 * @param project   the idea project to search in
	 * @param classname like java/lang/Object.java or java.lang.Object.java or without file extension
	 * @return the PsiClass element
	 */
	@Nullable
	public static PsiClass findJavaPsiClass(@NotNull final Project project, @NotNull final String classname) {
		final String fqn = classname.endsWith(".java") ? classname.replaceFirst(".java", "") : classname;
		final String dottedName = fqn.contains("/") ? fqn.replace('/', '.') : fqn;
		final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
		return findJavaPsiClass(project, dottedName, scope);
	}


	@Nullable
	private static PsiClass findJavaPsiClass(final Project project, @NotNull final String dottedFqClassName, @NotNull final GlobalSearchScope searchScope) {
		return JavaPsiFacade.getInstance(project).findClass(dottedFqClassName, searchScope);
	}


	@Nullable
	public static Module findModuleForPsiElement(@NotNull final PsiElement element, final Project project) {
		return ModuleUtil.findModuleForPsiElement(element);
	}


	@Nullable
	public static PsiElement getElementAtLine(@NotNull final PsiFile file, final int line) {
		final Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
		PsiElement element = null;
		try {
			final int offset = document.getLineStartOffset(line);
			element = file.getViewProvider().findElementAt(offset);
			if (document.getLineNumber(element.getTextOffset()) != line) {
				element = element.getNextSibling();
			}
		} catch (@NotNull final IndexOutOfBoundsException ignore) {
		}

		return element;
	}


	@Nullable
	public static String getPluginId() {
		final PluginId pluginId = PluginManager.getPluginByClassName(FindBugsPluginImpl.class.getName());
		if (pluginId != null) {
			return pluginId.getIdString();
		}
		return null;
	}


	@Nullable
	public static PluginDescriptor getPluginDescriptor(final String pluginId) {
		return PluginManager.getPlugin(PluginId.getId(pluginId));
	}


	public static boolean isValidFileType(@Nullable final FileType fileType) {
		return fileType != null && StdFileTypes.JAVA.equals(fileType) || StdFileTypes.CLASS.equals(fileType);
	}


	@NotNull
	public static FileType getFileTypeByName(@NotNull final String filename) {
		return FileTypeManager.getInstance().getFileTypeByFileName(filename);
	}


	private static ToolWindow getToolWindowById(final String uniqueIdentifier, @NotNull final DataContext dataContext) {
		final Project project = getProject(dataContext);
		return ToolWindowManager.getInstance(project).getToolWindow(uniqueIdentifier);
	}


	public static ToolWindow getToolWindowById(final String uniqueIdentifier, @NotNull final Project project) {
		return ToolWindowManager.getInstance(project).getToolWindow(uniqueIdentifier);
	}


	public static void activateToolWindow(final String toolWindowId, @NotNull final DataContext dataContext) {
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			public void run() {
				final ToolWindow toolWindow = getToolWindowById(toolWindowId, dataContext);
				activateToolWindow(toolWindow);
			}
		});
	}


	public static void activateToolWindow(@NotNull final ToolWindow toolWindow) {
		if (!toolWindow.isActive() && toolWindow.isAvailable()) {
			toolWindow.show(null);
		}
	}


	public static String getIdeaBuildNumber() {
		return ApplicationInfo.getInstance().getBuildNumber();

	}


	public static String getIdeaMajorVersion() {
		return ApplicationInfo.getInstance().getMajorVersion();
	}


	public static boolean isIdea9() {
		return "9".equals(getIdeaMajorVersion());
	}


	public static boolean isIdea10() {
		return "10".equals(getIdeaMajorVersion());
	}


	public static boolean isVersionGreaterThanIdea9() {
		return Integer.valueOf(getIdeaMajorVersion()) > 9;
	}


	@Nullable
	private static PsiFile getPsiFile(@NotNull final Project project, @NotNull final ExtendedProblemDescriptor problem) {
		final PsiFile file = problem.getPsiFile();
		return file == null ? null : PsiManager.getInstance(project).findFile(file.getVirtualFile());
	}


	@Nullable
	public static Document getDocument(@NotNull final Project project, @NotNull final ExtendedProblemDescriptor issue) {
		final PsiFile psiFile = getPsiFile(project, issue);
		return psiFile == null ? null : PsiDocumentManager.getInstance(project).getDocument(psiFile);
	}
	

	@Nullable
	public static PsiElement findAnonymousClassPsiElement(@NotNull final BugInstanceNode bugInstanceNode, @NotNull final Project project) {
		final PsiFile psiFile = bugInstanceNode.getPsiFile();
		return findAnonymousClassPsiElement(psiFile, bugInstanceNode, project);
	}


	@Nullable
	public static PsiElement findAnonymousClassPsiElement(@Nullable final PsiFileSystemItem psiFile, @NotNull final BugInstanceNode bugInstanceNode, @NotNull final Project project) {
		if (psiFile != null) {
			final String classNameToFind = BugInstanceUtil.getSimpleClassName(bugInstanceNode.getBugInstance());
			final RecurseClassCollector recurseClassCollector = new RecurseClassCollector(null, project, false);
			recurseClassCollector.addContainingClasses(psiFile.getVirtualFile());
			final Map<String, PsiElement> result = recurseClassCollector.getResult();

			for (final Entry<String, PsiElement> entry : result.entrySet()) {
				final String fileName = new File(entry.getKey()).getName();
				if (fileName.equals(classNameToFind + RecurseClassCollector.CLASS_FILE_SUFFIX)) {
					return entry.getValue();
				}
			}
		}
		return null;
	}


	public static String getProjectRootPath(final Project project) {
		final ProjectRootManager projectManager = ProjectRootManager.getInstance(project);
        final VirtualFile rootFiles[] = projectManager.getContentRoots();
		return rootFiles[0].getPath();
	}


	public static String replace$PROJECT_DIR$(final Project project, @NotNull final String path) {
		final StringBuilder result = new StringBuilder(20);
		final String rootPath = getProjectRootPath(project);
		if(path.contains(rootPath)) {
			result.append(path.replace(rootPath, IDEA_PROJECT_DIR_VAR));
			return result.toString();
		} else if (path.contains(IDEA_PROJECT_DIR_VAR)) {
			result.append(path.replace(IDEA_PROJECT_DIR_VAR, rootPath));
			return result.toString();
		}
		return path;
	}


	/*public static void collapsePathMacro() {
		PathMacroManager macroManager = PathMacroManager.getInstance(project);
		String pathWithMacro = macroManager.collapsePath("/path/to/your/project/your/file");
		assert pathWithMacro.equals("$PROJECT_DIR$/your/file");

	}*/
}
