/**
 * Copyright 2008 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;
import org.twodividedbyzero.idea.findbugs.common.ui.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public final class IdeaUtilImpl {

	private static final VirtualFile[] EMPTY_VIRTUAL_FILE = new VirtualFile[0];


	private IdeaUtilImpl() {
	}


	public static FindBugsPlugin getPluginComponent(final Project project) {
		return project.getComponent(FindBugsPlugin.class);
	}


	public static FindBugsPlugin getModuleComponent(final Module module) {
		return module.getComponent(FindBugsPlugin.class);
	}


	/**
	 * Get the base path of the project.
	 *
	 * @param project the open idea project
	 * @return the base path of the project.
	 */
	@Nullable
	public static File getProjectPath(final Project project) {
		if (project == null) {
			return null;
		}

		final VirtualFile baseDir = project.getBaseDir();
		if (baseDir == null) {
			return null;
		}

		return new File(baseDir.getPath());
	}


	public static Project getProject(final DataContext dataContext) {
		return DataKeys.PROJECT.getData(dataContext);
	}


	public static Project getProject() {
		return getProject(getDataContext());
	}


	public static DataContext getDataContext() {
		return DataManager.getInstance().getDataContext();
	}


	/**
	 * Locates the current PsiFile.
	 *
	 * @param dataContext The IntelliJ DataContext (can usually be obtained from the action-event).
	 * @return The current PsiFile or null if not found.
	 */
	private static PsiFile getPsiFile(final DataContext dataContext) {
		return (PsiFile) dataContext.getData("psi.File");
	}


	public static PsiFile getPsiFile(final DataContext dataContext, final VirtualFile virtualFile) {
		final Project project = IdeaUtilImpl.getProject(dataContext);
		return PsiManager.getInstance(project).findFile(virtualFile);
	}


	public static PsiFile getPsiFile(final Project project, final VirtualFile virtualFile) {
		return PsiManager.getInstance(project).findFile(virtualFile);
	}


	@Nullable
	public static PsiFile getPsiFile(final PsiElement psiClass) {
		if (psiClass == null) {
			return null;
		}
		return psiClass.getContainingFile();
	}


	@Nullable
	private static VirtualFile getSelectedFile(final DataContext dataContext) {
		final VirtualFile[] selectedFiles = getSelectedFiles(dataContext);
		if (selectedFiles.length == 0) {
			return null;
		} else {
			return selectedFiles[0];
		}
	}


	private static VirtualFile[] getSelectedFiles(final DataContext dataContext) {
		final Project project = getProject(dataContext);
		return FileEditorManager.getInstance(project).getSelectedFiles();
	}


	public static List<VirtualFile> getAllModifiedFiles(final DataContext dataContext) {
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


	public static ChangeList getChangeListByName(final String name) {
		final Project project = getProject();
		final ChangeListManager changeListManager = ChangeListManager.getInstance(project);
		return changeListManager.findChangeList(name);
	}


	public static Collection<VirtualFile> getModifiedFilesByList(final ChangeList list, final DataContext dataContext) {
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


	public static VirtualFile[] getFilesForModules(final Module module) {
		final ModuleRootManager mrm = ModuleRootManager.getInstance(module);
		// TODO: test
		return mrm.getFiles(OrderRootType.SOURCES);
	}


	public static VirtualFile[] getVirtualFiles(final DataContext dataContext) {
		//final VirtualFile[] files = (VirtualFile[]) e.getDataContext().getData(DataConstants.VIRTUAL_FILE_ARRAY);
		return DataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
		//return files;
	}


	public static VirtualFile getVirtualFile(final DataContext dataContext) {
		//final VirtualFile[] files = (VirtualFile[]) e.getDataContext().getData(DataConstants.VIRTUAL_FILE_ARRAY);
		return DataKeys.VIRTUAL_FILE.getData(dataContext);
		//return files;
	}


	@Nullable
	public static VirtualFile getVirtualFile(final PsiClass psiClass) {
		final PsiFile containingFile = psiClass.getContainingFile();
		if (containingFile != null) {
			return containingFile.getVirtualFile();
		}
		return null;
	}


	public static VirtualFile getVirtualFile(final PsiFile psiFile) {
		return psiFile.getVirtualFile();
	}


	public static VirtualFile getVirtualFile(final PsiElement element) {
		return PsiUtil.getVirtualFile(element);
	}


	public static PsiClass[] getPsiClasses(final PsiJavaFile psiFile) {
		return psiFile.getClasses();
	}


	public static PsiClass[] getContainingClasses(final DataContext dataContext) {
		final Project project = getProject(dataContext);

		final Editor selectedEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
		assert selectedEditor != null;
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


	public static String[] getCompilerOutputUrls(final Project project) {
		final VirtualFile[] vFiles = getCompilerOutputPaths(project);
		final String[] files = new String[vFiles.length];

		for (int i = 0; i < vFiles.length; i++) {
			final VirtualFile file = vFiles[i];
			if (file != null) {
				files[i] = file.getPresentableUrl();
			} else {
				files[i] = ""; // todo: remove -> crude, getProjectOutputPath(final Module module) returns always null
			}
		}

		return files;
	}


	private static VirtualFile[] getCompilerOutputPaths(final Project project) {
		final Module[] modules = getModules(project);
		final VirtualFile[] vFiles = new VirtualFile[modules.length];

		for (int i = 0; i < modules.length; i++) {
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


	public static VirtualFile getCompilerOutputPath(final Module module) {
		return CompilerModuleExtension.getInstance(module).getCompilerOutputPath();
	}


	public static VirtualFile getCompilerOutputPath(final DataContext dataContext) {
		final Module module = getModule(dataContext);

		// TODO: facade
		return CompilerModuleExtension.getInstance(module).getCompilerOutputPath();
		//return ModuleRootManager.getInstance(module).getCompilerOutputPath();
	}


	@Nullable
	public static VirtualFile getCompilerOutputPath(final VirtualFile virtualFile, final Project project) {
		final Module module = findModuleForFile(virtualFile, project);

		// TODO: facade
		return CompilerModuleExtension.getInstance(module).getCompilerOutputPath();
		//return ModuleRootManager.getInstance(module).getCompilerOutputPath();
	}


	private static VirtualFile getProjectOutputPath(final Module module) {
		final Project project = module.getProject();
		return CompilerProjectExtension.getInstance(project).getCompilerOutput();
	}


	private static String getPackage(final PsiClass psiClass) {
		return ((PsiJavaFile) psiClass.getContainingFile()).getPackageName();
	}


	public static String getPackageUrl(final PsiClass psiClass) {
		return getPackage(psiClass).replace('.', '/');
	}


	@Nullable
	public static PsiElement getPackage(final DataContext datacontext) {
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


	private static void getSubPackages(final PsiPackage psiPackage, final GlobalSearchScope scope, final Set<PsiPackage> result) {
		for (final PsiPackage child : psiPackage.getSubPackages(scope)) {
			result.add(child);
			getSubPackages(child, scope, result);
		}
	}


	public static SourceFolder[] getSourceFolders(final DataContext dataContext) {
		final Module module = getModule(dataContext);
		final ContentEntry[] contentEntries = ModuleRootManager.getInstance(module).getContentEntries();

		return contentEntries[0].getSourceFolders();
	}


	public static String getPackageAsPath(final com.intellij.openapi.project.Project project, final VirtualFile packagePath, final VirtualFile[] sourceRoots) {
		final StringBuilder result = new StringBuilder();
		final List<String> list = getPackagePathAsList(project, packagePath, sourceRoots);
		for (final String dir : list) {
			result.append(dir);
		}

		return result.toString();
	}


	private static List<String> getPackagePathAsList(final com.intellij.openapi.project.Project project, final VirtualFile packagePath, final VirtualFile[] sourceRoots) {
		final Module module = IdeaUtilImpl.findModuleForFile(packagePath, project);
		final List<String> parentPath = new ArrayList<String>();
		final List<String> sourcesDirs = new ArrayList<String>();

		for (final VirtualFile vFile : sourceRoots) {
			sourcesDirs.add(vFile.getName());
		}

		VirtualFile parent = null;
		if (packagePath != null) {
			parent = packagePath;
		}
		while (parent != null && !parent.getName().equals(module.getName()) && !sourcesDirs.contains(parent.getName())) {
			parentPath.add(FindBugsPluginConstants.FILE_SEPARATOR + parent.getName());
			parent = parent.getParent();
		}

		Collections.reverse(parentPath);
		return parentPath;
	}


	public static VirtualFile[] getModulesSourceRoots(final DataContext dataContext) {
		final Project project = getProject(dataContext);
		final ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);

		return projectRootManager.getContentSourceRoots();
	}


	public static Module[] getProjectModules(final DataContext dataContext) {
		final Project project = getProject(dataContext);

		return ModuleManager.getInstance(project).getModules();
	}


	public static VirtualFile[] getProjectClasspath(final DataContext dataContext) {
		final Module module = getModule(dataContext);

		return getProjectClasspath(module);
	}


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


	public static Module findModuleForFile(final VirtualFile virtualFile, final Project project) {
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


	public static Module getModuleForFile(final VirtualFile virtualFile, final Project project) {
		final ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
		final ProjectFileIndex projectFileIndex = projectRootManager.getFileIndex();

		return projectFileIndex.getModuleForFile(virtualFile);
	}


	public static Module getModule(final DataContext dataContext) {
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
				throw new FindBugsPluginException("You have no modules configured inside your project (" + project.getName() + ")");
			}
			module = modules[0];
		}

		return module;
		//return com.intellij.openapi.vfs.VfsUtil.getModuleForFile(project, file);
		//PsiClass.getContainingFile().getVirtualFile()
	}


	private static Module[] getModules(final Project project) {
		final ModuleManager moduleManager = ModuleManager.getInstance(project);
		return moduleManager.getModules();
	}


	// TODO: maybe not needed


	public static File[] getFileForModules(final Module[] modules, final FileType fileType) {
		final Collection<File> resolvedFiles = new HashSet<File>();

		final ProjectRootsTraversing.RootTraversePolicy traversePolicy = null;
		for (final Module module : modules) {
			final List<VirtualFile> virtualFiles = new ArrayList<VirtualFile>();
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
	private static PsiElement getCurrentElement(final DataContext dataContext) {
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
	public static PsiClass getCurrentClass(final DataContext dataContext) {
		return findClass(getCurrentElement(dataContext));
	}


	/**
	 * Retrieves the current PsiField.
	 *
	 * @param dataContext The IntelliJ DataContext (can usually be obtained from the action-event).
	 * @return The current PsiField or null if not found.
	 */
	public static PsiField getCurrentField(final DataContext dataContext) {
		return findField(getCurrentElement(dataContext));
	}


	public static VirtualFile getCurrentFolder(final DataContext dataContext) {
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
	public static PsiMethod getCurrentMethod(final DataContext dataContext) {
		return findMethod(getCurrentElement(dataContext));
	}


	/**
	 * Retrieves the current SelectionModel.
	 *
	 * @param dataContext The IntelliJ DataContext (can usually be obtained from the action-event).
	 * @return The current SelectionModel or null if not found.
	 */
	public static SelectionModel getSelectionModel(final DataContext dataContext) {
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
	private static PsiMethod findMethod(final PsiElement element) {
		final PsiMethod method = element instanceof PsiMethod ? (PsiMethod) element : PsiTreeUtil.getParentOfType(element, PsiMethod.class);
		if (method != null && method.getContainingClass() instanceof PsiAnonymousClass) {
			return findMethod(method.getParent());
		}
		return method;
	}


	public static VirtualFile findFileByIoFile(final File file) {
		return LocalFileSystem.getInstance().findFileByIoFile(file);
	}


	public static VirtualFile findFileByPath(final String path) {
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
	public static PsiClass findJavaPsiClass(final Project project, final String classname) {
		final String fqn = classname.endsWith(".java") ? classname.replaceFirst(".java", "") : classname;
		final String dottedName = fqn.contains("/") ? fqn.replace('/', '.') : fqn;
		final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
		return findJavaPsiClass(project, dottedName, scope);
	}


	@Nullable
	private static PsiClass findJavaPsiClass(final Project project, final String dottedFqClassName, final GlobalSearchScope searchScope) {
		return JavaPsiFacade.getInstance(project).findClass(dottedFqClassName, searchScope);
	}


	public static Module findModuleForFile(final VirtualFile vFile) {
		return ModuleUtil.findModuleForFile(vFile, getProject());
	}


	public static Module findModuleForPsiElement(final PsiElement element) {
		return ModuleUtil.findModuleForPsiElement(element);
	}


	@Nullable
	public static PsiElement getElementAtLine(final PsiFile file, final int line) {
		final Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
		PsiElement element = null;
		try {
			final int offset = document.getLineStartOffset(line);
			element = file.getViewProvider().findElementAt(offset);
			if (document.getLineNumber(element.getTextOffset()) != line) {
				element = element.getNextSibling();
			}
		} catch (final IndexOutOfBoundsException ignore) {
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


	public static PluginDescriptor getPluginDescriptor(final String pluginId) {
		return PluginManager.getPlugin(PluginId.getId(pluginId));
	}


	public static boolean isValidFileType(final FileType fileType) {
		return fileType != null && StdFileTypes.JAVA.equals(fileType) || StdFileTypes.CLASS.equals(fileType);
	}


	public static FileType getFileTypeByName(final String filename) {
		return FileTypeManager.getInstance().getFileTypeByFileName(filename);
	}


	private static ToolWindow getToolWindowById(final String uniqueIdentifier, final DataContext dataContext) {
		final Project project = getProject(dataContext);
		return ToolWindowManager.getInstance(project).getToolWindow(uniqueIdentifier);
	}


	public static ToolWindow getToolWindowById(final String uniqueIdentifier) {
		final Project project = getProject();
		return ToolWindowManager.getInstance(project).getToolWindow(uniqueIdentifier);
	}


	public static void activateToolWindow(final String toolWindowId, final DataContext dataContext) {
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			public void run() {
				final ToolWindow toolWindow = getToolWindowById(toolWindowId, dataContext);
				activateToolWindow(toolWindow);
			}
		});
	}


	public static void activateToolWindow(final ToolWindow toolWindow) {
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


	@Nullable
	private static PsiFile getPsiFile(@NotNull final Project project, @NotNull final ExtendedProblemDescriptor problem) {
		final PsiFile file = problem.getFile();
		return file == null ? null : PsiManager.getInstance(project).findFile(file.getVirtualFile());
	}


	@Nullable
	public static Document getDocument(@NotNull final Project project, @NotNull final ExtendedProblemDescriptor issue) {
		final PsiFile psiFile = getPsiFile(project, issue);
		return psiFile == null ? null : PsiDocumentManager.getInstance(project).getDocument(psiFile);
	}
}
