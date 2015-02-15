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
package org.twodividedbyzero.idea.findbugs.collectors;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * Note that 'this' should not escape since a instance is reusable.
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public final class RecurseClassCollector {

	private static final Logger LOGGER = Logger.getInstance(RecurseClassCollector.class.getName());
	public static final String CLASS_FILE_SUFFIX = ".class";
	private static final String ANONYMOUS_CLASS_DELIMITER = "$";

	private final FindBugsProject _findBugsProject;
	private final Project _project;
	private final boolean _collectAndAdd;
	private final PsiManager _psiManager;
	private final Map<String, PsiElement> _classes; // TODO: collect all classes url's and addFile later


	public RecurseClassCollector(final FindBugsProject findBugsProject, @NotNull final Project project, final boolean collectAndAdd) {
		_findBugsProject = findBugsProject;
		_project = project;
		_collectAndAdd = collectAndAdd;
		_psiManager = PsiManager.getInstance(_project);
		if (_collectAndAdd) {
			_classes = null;
		} else {
			_classes = new HashMap<String, PsiElement>();
		}
	}


	private void put(@NotNull final String fqp, @NotNull final PsiElement element) {
		if (_collectAndAdd) {
			addFile(fqp + CLASS_FILE_SUFFIX);
		} else {
			_classes.put(fqp + CLASS_FILE_SUFFIX, element);
		}
	}


	public void addContainingClasses(@NotNull final VirtualFile virtualFile) {

		final PsiFile psiFile = _psiManager.findFile(virtualFile);

		if (psiFile instanceof PsiClassOwner) {
			final PsiClassOwner psiClassOwner = (PsiClassOwner) psiFile;
			final PsiClass[] psiClasses = psiClassOwner.getClasses();

			for (final PsiClass psiClass : psiClasses) {
				final VirtualFile compilerOutputPath = IdeaUtilImpl.getCompilerOutputPath(virtualFile, _project);
				if (compilerOutputPath == null) {
					continue;
				}
				final String fqp = buildFullQualifiedPath(compilerOutputPath.getPresentableUrl(), psiClass);
				put(fqp, psiClass);
				addAnonymousClasses(psiClass, fqp);
				addInnerClasses(psiClass, fqp);
			}
		}
	}


	// analyze class under cursor
	public void addContainingClasses(@NotNull final PsiClass selectedPsiClass) {
		final VirtualFile virtualFile = IdeaUtilImpl.getVirtualFile(selectedPsiClass);

		assert virtualFile != null;
		final File file = VfsUtil.virtualToIoFile(virtualFile);
		if (!file.getAbsoluteFile().exists()) {
			return;
		}

		if (!IdeaUtilImpl.isValidFileType(virtualFile.getFileType())) {
			return;
		}

		final PsiFile psiFile = _psiManager.findFile(virtualFile);

		if (psiFile instanceof PsiClassOwner) {
			final PsiClassOwner psiClassOwner = (PsiClassOwner) psiFile;
			final PsiClass[] psiClasses = psiClassOwner.getClasses();

			for (final PsiClass psiClass : psiClasses) {

				final String s = selectedPsiClass.getName();
				final String s1 = psiClass.getName();
				assert s != null;

				final VirtualFile compilerOutputPath = IdeaUtilImpl.getCompilerOutputPath(virtualFile, _project);
				if (compilerOutputPath == null) {
					LOGGER.warn("No output path specified for " + virtualFile + " in " + _project);
					return; // f. e. project/module compiled and then compiler output path removed (empty text field)
				}

				final String fqp = buildFullQualifiedPath(compilerOutputPath.getPresentableUrl(), psiClass);
				if (s.equals(s1)) {
					put(fqp, psiClass);
				} else {
					addAnonymousClasses(psiClass, fqp, s);
					addInnerClasses(psiClass, fqp, s);
				}
			}
		}

	}


	private void addInnerClasses(final PsiClass psiClass, final String fullQualifiedPath) {
		addInnerClasses(psiClass, fullQualifiedPath, null);
	}


	private void addInnerClasses(final PsiClass psiClass, final String fullQualifiedPath, @Nullable final String findClass) {
		final PsiClass[] psiClasses = psiClass.getInnerClasses();

		for (final PsiClass innerPsiClass : psiClasses) {
			final String innerClassName = innerPsiClass.getName();
			final String fqp = fullQualifiedPath + ANONYMOUS_CLASS_DELIMITER + innerClassName;

			assert innerClassName != null;
			if (findClass != null && innerClassName.equals(findClass)) {
				put(fqp, innerPsiClass);
				return;

			} else if (findClass != null) {
				addAnonymousClasses(innerPsiClass, fqp, findClass);
				addInnerClasses(innerPsiClass, fqp, findClass);
			} else { // default branch: no analyze class under cursor
				put(fqp, innerPsiClass);
				addAnonymousClasses(innerPsiClass, fqp);
				addInnerClasses(innerPsiClass, fqp);
			}
		}
	}


	private void addInnerClasses(final PsiElement psiElement, final String fullQualifiedPath, final int anonymousClassPrefix) {
		addInnerClasses(psiElement, fullQualifiedPath, anonymousClassPrefix, null);
	}


	private void addInnerClasses(final PsiElement psiElement, final String fullQualifiedPath, final int anonymousClassPrefix, @Nullable final String findClass) {

		final PsiElement[] classes = PsiTreeUtil.collectElements(psiElement, new InnerClassPsiElementFilter(psiElement));

		for (final PsiElement element : classes) {
			final PsiClass psiClass = (PsiClass) element;
			final String className = psiClass.getName();

			if (!"null".equals(className)) {
				final String fqp = fullQualifiedPath + ANONYMOUS_CLASS_DELIMITER + anonymousClassPrefix + className;

				if (findClass != null && String.valueOf(anonymousClassPrefix).equals(findClass)) {
					put(fqp, element);
					return;

				} else if (findClass != null) {
					addAnonymousClasses(psiElement, fqp, findClass);
					addInnerClasses(psiClass, fqp, anonymousClassPrefix, findClass);
				} else { // default branch: no analyze class under cursor
					put(fqp, element);
					// add nested inner/anonymous classes of current anonymous psiClass/Element
					addAnonymousClasses(psiElement, fqp);
					addInnerClasses(psiClass, fqp);
				}
			}
		}
	}


	private void addAnonymousClasses(final PsiElement psiElement, final String fullQualifiedPath) {
		addAnonymousClasses(psiElement, fullQualifiedPath, null);
	}


	private void addAnonymousClasses(final PsiElement psiElement, final String fullQualifiedPath, @Nullable final String findClass) {

		final PsiElement[] classes = PsiTreeUtil.collectElements(psiElement, new AnonymousClassPsiElementFilter(psiElement));

		final int length = classes.length;
		for (int i = 0; i < length; i++) {

			final int anonymousClassPrefix = i + 1;
			final String fqp = fullQualifiedPath + ANONYMOUS_CLASS_DELIMITER + anonymousClassPrefix;

			if (findClass != null && String.valueOf(anonymousClassPrefix).equals(findClass)) {
				put(fqp, classes[i]);
				return;

			} else if (findClass != null) {
				addAnonymousClasses(classes[i], fqp, findClass);
				addInnerClasses(classes[i], fqp, anonymousClassPrefix, findClass);
			} else { // default branch: no analyze class under cursor
				put(fqp, classes[i]);
				// add nested inner/anonymous classes of current anonymous psiClass/Element
				addAnonymousClasses(classes[i], fqp);
				addInnerClasses(classes[i], fqp, anonymousClassPrefix);
			}
		}
	}


	@NotNull
	private static String buildFullQualifiedPath(@NotNull final String compileOutputDir, @NotNull final PsiClass psiClass) {
		final String packageUrl = IdeaUtilImpl.getPackageUrl(psiClass);
		final StringBuilder fqn = new StringBuilder();

		fqn.append(compileOutputDir);
		fqn.append(FindBugsPluginConstants.FILE_SEPARATOR);
		if (!packageUrl.isEmpty()) {
			fqn.append(packageUrl);
			fqn.append(FindBugsPluginConstants.FILE_SEPARATOR);
		}

		//fqn.append(getParentClassNotation(psiClass));
		fqn.append(psiClass.getName());
		return fqn.toString();
	}


	/*public static String getParentClassNotation(final PsiClass psiClass) {
		final StringBuilder fqn = new StringBuilder("");
		PsiClass containingClass = psiClass.getContainingClass();
		final List<PsiClass> innerClassesList = new LinkedList<PsiClass>();

		while (containingClass != null) {
			innerClassesList.add(containingClass);
			containingClass = containingClass.getContainingClass();
		}

		final ListIterator<PsiClass> iterator = innerClassesList.listIterator(innerClassesList.size());
		while (iterator.hasPrevious()) {			//addAnonymousClasses(psiClass);
			fqn.append(iterator.previous().getName()).append('$');
		}

		return fqn.toString();
	}*/


	private void addFile(final String fullQualifiedName) {
		if (new File(fullQualifiedName).exists()) {
			_findBugsProject.addFile(fullQualifiedName);
			LOGGER.debug("adding class file: " + fullQualifiedName);
		} else {
			LOGGER.debug("class file: " + fullQualifiedName + " does not exists. maybe an inner/anonymous class? try to recompile your sources.");
		}
	}


	/**
	 * Get the collected class files. use {@link RecurseClassCollector#addFile(String)} to add
	 * them to a {@link org.twodividedbyzero.idea.findbugs.core.FindBugsProject}. if using the collectAndAdd flag <code>true</code>
	 * {@link RecurseClassCollector#RecurseClassCollector(FindBugsProject, com.intellij.openapi.project.Project, boolean)}
	 * the files were automaticaly added to the given FindBugsProject.
	 *
	 * @return the collected full qualified class names and path in the file system
	 */
	@SuppressWarnings({"ReturnOfCollectionOrArrayField"})
	public Map<String, PsiElement> getResult() {
		return _classes;
	}


}
