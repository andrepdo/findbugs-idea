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
package org.twodividedbyzero.idea.findbugs.common.util;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.collectors.AbstractClassAdder;
import org.twodividedbyzero.idea.findbugs.collectors.ClassCollector;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPluginImpl;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@SuppressWarnings({"HardcodedFileSeparator"})
public final class IdeaUtilImpl {

	private static final Logger LOGGER = Logger.getInstance(IdeaUtilImpl.class.getName());

	@NotNull
	private static final VirtualFile[] EMPTY_VIRTUAL_FILE = new VirtualFile[0];
	@NotNull
	private static final String IDEA_PROJECT_DIR_VAR = "$PROJECT_DIR$";

	private static final Set<String> SUPPORTED_FILE_TYPES_EXT = new THashSet<String>(Arrays.asList("java", "scala", "groovy", "gradle", "aj"));
	public static final Set<FileType> SUPPORTED_FILE_TYPES;

	static {
		final THashSet<FileType> supported = new THashSet<FileType>(4);
		supported.add(StdFileTypes.JAVA);
		supported.add(StdFileTypes.CLASS);
		final FileType scala = FileTypeManager.getInstance().getFileTypeByExtension("SCALA");
		if (!(scala instanceof UnknownFileType)) {
			supported.add(scala);
		}
		final FileType groovy = FileTypeManager.getInstance().getFileTypeByExtension("GROOVY");
		if (!(groovy instanceof UnknownFileType)) {
			supported.add(groovy);
		}
		final FileType aspectJ = FileTypeManager.getInstance().getFileTypeByExtension("AJ");
		if (!(aspectJ instanceof UnknownFileType)) {
			supported.add(aspectJ);
		}
		SUPPORTED_FILE_TYPES = supported;
	}

	private static final Set<Language> SUPPORTED_LANGUAGES;

	static {
		final THashSet<Language> supported = new THashSet<Language>(3);
		supported.add(JavaLanguage.INSTANCE);
		final Language scala = Language.findLanguageByID("Scala");
		if (scala != null) {
			supported.add(scala);
		}
		final Language groovy = Language.findLanguageByID("Groovy");
		if (groovy != null) {
			supported.add(groovy);
		}
		final Language aspectJ = Language.findLanguageByID("AspectJ");
		if (aspectJ != null) {
			supported.add(aspectJ);
		}
		SUPPORTED_LANGUAGES = supported;
	}

	public static boolean isLanguageSupported(@NotNull final Language language) {
		return language.isKindOf(JavaLanguage.INSTANCE) || language.isKindOf("Scala") || language.isKindOf("Groovy") || language.isKindOf("AspectJ");
	}

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


	public static Project getProject(final PsiElement psiElement) {
		return psiElement.getProject();
	}


	/**
	 * Locates the current PsiFile.
	 *
	 * @param dataContext The IntelliJ DataContext (can usually be obtained from the action-event).
	 * @return The current PsiFile or null if not found.
	 */
	@Nullable
	private static PsiFile getPsiFile(@NotNull final DataContext dataContext) {
		return DataKeys.PSI_FILE.getData(dataContext);
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
				files[i] = ""; // FIXME: remove -> crude, getProjectOutputPath(final Module module) returns always null
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
			// FIXME: facade
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
	public static VirtualFile getCompilerOutputPath(@NotNull final VirtualFile virtualFile, @NotNull final Project project) {
		final Module module = findModuleForFile(virtualFile, project);
		if (module == null) {
			return null;
		}
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
	public static String getPackage(@NotNull final PsiElement psiElement) {
		return ((PsiClassOwner) psiElement.getContainingFile()).getPackageName();
	}


	@NotNull
	public static String getPackageUrl(@NotNull final PsiElement psiElement) {
		return getPackage(psiElement).replace('.', '/');
	}


	public static String getPackageAsPath(@NotNull final com.intellij.openapi.project.Project project, final VirtualFile packagePath) {
		final StringBuilder result = new StringBuilder(30);
		final List<String> list = getPackagePathAsList(project, packagePath);
		for (final String dir : list) {
			result.append(dir);
		}

		return result.toString();
	}


	@NotNull
	private static List<String> getPackagePathAsList(@NotNull final com.intellij.openapi.project.Project project, @NotNull final VirtualFile packagePath) {
		final List<String> parentPath = new ArrayList<String>();
		final String packageName = DirectoryIndex.getInstance(project).getPackageName(packagePath);
		if (packageName != null && !packageName.isEmpty()) {
			final String[] path = packageName.split("\\.");
			for (final String p : path) {
				parentPath.add(FindBugsPluginConstants.FILE_SEPARATOR + p);
			}
		}
		return parentPath;
	}


	@NotNull
	public static VirtualFile[] getModulesSourceRoots(@NotNull final DataContext dataContext) {
		final Project project = getProject(dataContext);
		//noinspection ConstantConditions
		return getModulesSourceRoots(project);
	}


	@NotNull
	public static VirtualFile[] getModulesSourceRoots(@NotNull final Project project) {
		final ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
		return projectRootManager.getContentSourceRoots();
	}


	@NotNull
	public static VirtualFile[] getProjectClasspath(@NotNull final DataContext dataContext) {
		final Module module = getModule(dataContext);
		return getProjectClasspath(module);
	}


	@NotNull
	public static VirtualFile[] getProjectClasspath(@Nullable final Module module) {
		if (module == null) {
			return EMPTY_VIRTUAL_FILE;
		}
		final List<VirtualFile> found = new LinkedList<VirtualFile>();
		addProjectClasspath(module, found);
		return found.toArray(new VirtualFile[found.size()]);
	}


	public static void addProjectClasspath(@Nullable final Module module, @NotNull List<VirtualFile> classpath) {
		try {
			@SuppressWarnings("ConstantConditions")
			final ModuleRootManager mrm = ModuleRootManager.getInstance(module);
			final OrderEntry[] orderEntries = mrm.getOrderEntries();
			for (final OrderEntry entry : orderEntries) {
				Collections.addAll(classpath, entry.getFiles(OrderRootType.CLASSES));
			}
		} catch (final Exception e) {
			throw new FindBugsPluginException("ModuleRootManager must not be null. may be the current class is not a project/module class. check your project/module outpath configuration.", e);
		}
	}


	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
	@Nullable
	public static Module findModuleForFile(@NotNull final VirtualFile virtualFile, @NotNull final Project project) {
		//noinspection ConstantConditions
		if (virtualFile == null || project == null) {
			LOGGER.debug("Expected not null arguments. virtualFile=" + virtualFile + " project=" + project);
			return null;
		}
		return ModuleUtilCore.findModuleForFile(virtualFile, project);
	}


	@Nullable
	public static Module getModule(@NotNull final DataContext dataContext) {
		return getModule(dataContext, getProject(dataContext));
	}


	@Nullable
	public static Module getModule(@NotNull final DataContext dataContext, @NotNull final Project project) {
		final VirtualFile selectedFile = getSelectedFile(dataContext);
		Module module = null;
		if (selectedFile != null) {
			module = ModuleUtilCore.findModuleForFile(selectedFile, project);
		}

		if (module == null) {
			module = DataKeys.MODULE.getData(dataContext);
		}

		if (module == null) {
			final Module[] modules = getModules(project);
			if (modules.length > 0) {
				module = modules[0];
			}
		}

		return module;
	}


	@NotNull
	private static Module[] getModules(final Project project) {
		final ModuleManager moduleManager = ModuleManager.getInstance(project);
		return moduleManager.getModules();
	}


	/**
	 * Retrieves the current PsiElement.
	 *
	 * @param dataContext The IntelliJ DataContext (can usually be obtained from the action-event).
	 * @return The current PsiElement or null if not found.
	 */
	@Nullable
	private static PsiElement getCurrentElement(@NotNull final DataContext dataContext) {

		/**
		 * Do not use "psi.Element" because this element could be contextless.
		 */
		//final PsiElement psiElement = (PsiElement) dataContext.getData("psi.Element");

		final Editor editor = DataKeys.EDITOR.getData(dataContext);
		if (editor != null) {
			final PsiFile psiFile = getPsiFile(dataContext);
			if (psiFile != null) {
				return psiFile.findElementAt(editor.getCaretModel().getOffset());
			}
		}
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
	 * Retrieves the current PsiMethod.
	 *
	 * @param dataContext The IntelliJ DataContext (can usually be obtained from the action-event).
	 * @return The current PsiMethod or null if not found.
	 */
	@SuppressWarnings("UnusedDeclaration")
	@Nullable
	public static PsiMethod getCurrentMethod(@NotNull final DataContext dataContext) {
		return findMethod(getCurrentElement(dataContext));
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
			//noinspection TailRecursion
			return findClass(psiClass.getParent());
		}
		return psiClass;
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
			//noinspection TailRecursion
			return findMethod(method.getParent());
		}
		return method;
	}


	@SuppressWarnings("UnusedDeclaration")
	@Nullable
	public static VirtualFile findFileByIoFile(final File file) {
		return LocalFileSystem.getInstance().findFileByIoFile(file);
	}


	@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
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
		final String fqn = removeExtension(classname);
		final String dottedName = fqn.contains("/") ? fqn.replace('/', '.') : fqn;
		final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
		return findJavaPsiClass(project, dottedName, scope);
	}


	@Nullable
	private static PsiClass findJavaPsiClass(final Project project, @NotNull final String dottedFqClassName, @NotNull final GlobalSearchScope searchScope) {
		/**
		 * Do not use findClass(), the returned class is "random" (eg: could be ClsClassImpl or PsiClassImpl), see javadoc
		 */
		final PsiClass[] classes = JavaPsiFacade.getInstance(project).findClasses(dottedFqClassName, searchScope);
		if (classes.length > 0) {
			if (classes.length > 1) {
				for (final PsiClass c : classes) {
					if (!(c instanceof PsiCompiledElement)) { // prefer non compiled
						return c;
					}
				}
			}
			return classes[0];
		}
		return null;
	}


	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
	@Nullable
	public static PsiElement getElementAtLine(@NotNull final PsiFile file, final int line) {
		//noinspection ConstantConditions
		if (file == null) {
			return null;
		}
		final Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
		PsiElement element = null;
		try {
			if (document != null) {
				final int offset = document.getLineStartOffset(line);
				element = file.getViewProvider().findElementAt(offset);
				if (element != null) {
					if (document.getLineNumber(element.getTextOffset()) != line) {
						element = element.getNextSibling();
					}
				}
			}
		} catch (@NotNull final IndexOutOfBoundsException ignore) {
		}

		return element;
	}


	@SuppressWarnings("UnusedDeclaration")
	@Nullable
	public static String getPluginId() {
		final PluginId pluginId = PluginManager.getPluginByClassName(FindBugsPluginImpl.class.getName()); // use PluginManager here for IDEA 12.1.x compatibility (PluginManagerCore extension was introduced with 13.x)
		if (pluginId != null) {
			return pluginId.getIdString();
		}
		return null;
	}


	@SuppressFBWarnings("DM_CONVERT_CASE")
	@NotNull
	public static String removeExtension(@NotNull final String name) {
		int pos = name.lastIndexOf('.');
		if (pos != -1) {
			final String ext = name.substring(pos + 1).toLowerCase();
			if (SUPPORTED_FILE_TYPES_EXT.contains(ext)) {
				return name.substring(0, pos);
			}
		}
		return name;
	}


	public static boolean isValidFileType(@Nullable final FileType fileType) {
		return fileType != null && SUPPORTED_FILE_TYPES.contains(fileType);
	}


	@NotNull
	public static FileType getFileTypeByName(@NotNull final String filename) {
		return FileTypeManager.getInstance().getFileTypeByFileName(filename);
	}


	public static ToolWindow getToolWindowById(final String uniqueIdentifier, @NotNull final Project project) {
		return ToolWindowManager.getInstance(project).getToolWindow(uniqueIdentifier);
	}


	public static void activateToolWindow(@NotNull final ToolWindow toolWindow) {
		if (!toolWindow.isActive() && toolWindow.isAvailable()) {
			toolWindow.show(null);
		}
	}


	@Nullable
	private static PsiFile getPsiFile(@NotNull final Project project, @NotNull final ExtendedProblemDescriptor problem) {
		final PsiFile file = problem.getPsiFile();
		return file == null ? null : PsiManager.getInstance(project).findFile(file.getVirtualFile());
	}


	@SuppressWarnings("UnusedDeclaration")
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
			final ClassCollector cc = new ClassCollector(project);
			cc.addContainingClasses(psiFile.getVirtualFile());
			final Map<String, PsiElement> classes = cc.getClasses();

			for (final Entry<String, PsiElement> entry : classes.entrySet()) {
				final String fileName = new File(entry.getKey()).getName();
				if (fileName.equals(classNameToFind + AbstractClassAdder.CLASS_FILE_SUFFIX)) {
					return entry.getValue();
				}
			}
		}
		return null;
	}


	@Nullable
	public static String getFirstProjectRootPath(final Project project) {
		final ProjectRootManager projectManager = ProjectRootManager.getInstance(project);
		final VirtualFile rootFiles[] = projectManager.getContentRoots();
		if (rootFiles.length == 0) {
			return null;
		}
		return rootFiles[0].getPath();
	}

	@Deprecated
	public static String collapsePathMacro(final ComponentManager project, @NotNull final String path) {
		final PathMacroManager macroManager = PathMacroManager.getInstance(project);
		return macroManager.collapsePath(path);
	}

	@Deprecated
	public static String expandPathMacro(final ComponentManager project, @NotNull final String path) {
		final PathMacroManager macroManager = PathMacroManager.getInstance(project);
		return macroManager.expandPath(path);
	}
}
