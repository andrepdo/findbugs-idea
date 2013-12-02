/*
 * Copyright 2008-2013 Andre Pfeiler
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
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;

import java.io.File;
import java.lang.reflect.Field;

/**
 * @author Reto Merz<reto.merz@gmail.com>
 * @version $Revision$
 * @since 0.9.99
 */
public final class AnalyzeUtil {

	private AnalyzeUtil() {
	}

	private static final Logger LOGGER = Logger.getInstance(BaseAnalyzeAction.class.getName());
	public static final DataKey<AnalysisScope> KEY;

	static {
		DataKey<AnalysisScope> _key = null;

		try {
			_key = getStaticDataKey(Class.forName("com.intellij.analysis.AnalysisScopeUtil"));
		} catch (ClassNotFoundException e) { // ignore e;
		} catch (NoSuchFieldException e) { // ignore e;
		} catch (IllegalAccessException e) {
			throw new Error(e); // we are expecting a public field
		}

		if (_key == null) {
			try {
				_key = getStaticDataKey(AnalysisScope.class);
			} catch (NoSuchFieldException e) {
				throw new Error(e);
			} catch (IllegalAccessException e) {
				throw new Error(e); // we are expecting a public field
			}
		}

		if (_key == null) {
			throw new Error("No data key");
		}

		KEY = _key;
	}


	@SuppressWarnings("unchecked")
	@Nullable
	private static DataKey<AnalysisScope> getStaticDataKey(@Nullable Class clazz) throws NoSuchFieldException, IllegalAccessException {
		if (clazz != null) {
			final Field key = clazz.getDeclaredField("KEY");
			if (key != null) {
				return (DataKey<AnalysisScope>)key.get(null);
			}
		}
		return null;
	}


	/**
	 * Copy from \ideaIC-129.354\plugins\ByteCodeViewer\src\com\intellij\byteCodeViewer\ByteCodeViewerManager#getByteCode(PsiElement)
	 * (first unused part "detect module" removed)
	 */
	@Nullable
	public static String getOutputClassFilePathForJavaFile(PsiFile psiFile, Project project) {
		try {
			final Module module = IdeaUtilImpl.findModuleForPsiElement(psiFile, project);
			final VirtualFile virtualFile = psiFile.getVirtualFile();
			if (virtualFile == null) {
				LOGGER.warn("No virtual file for psi file: " + psiFile);
				return null;
			}
			final CompilerModuleExtension moduleExtension = CompilerModuleExtension.getInstance(module);
			if (moduleExtension == null) return null;
			String classPath;
			if (ProjectRootManager.getInstance(module.getProject()).getFileIndex().isInTestSourceContent(virtualFile)) {
				final VirtualFile pathForTests = moduleExtension.getCompilerOutputPathForTests();
				if (pathForTests == null) {
					LOGGER.warn("No compiler test output path for:" + virtualFile);
					return null;
				}
				classPath = pathForTests.getPath();
			} else {
				final VirtualFile compilerOutputPath = moduleExtension.getCompilerOutputPath();
				if (compilerOutputPath == null) {
					LOGGER.warn("No compiler output path for:" + virtualFile);
					return null;
				}
				classPath = compilerOutputPath.getPath();
			}

			final String packageName = IdeaUtilImpl.getPackage(psiFile);
			String className = psiFile.getName();
			if (className.endsWith(".java")) {
				className = className.substring(0, className.length()-".java".length());
			} else {
				LOGGER.warn("Unexpected file type:" + virtualFile);
				return null;
			}
			classPath += "/" + packageName.replace('.', '/') + "/" + className + ".class";

			final File classFile = new File(classPath);
			if (!classFile.exists()) {
				LOGGER.warn("Compiled class file for source file '" + virtualFile + "' not exists: " + classPath);
				return null;
			}
			return classFile.getCanonicalPath();
		}
		catch (Exception e1) {
			LOGGER.error(e1);
		}
		return null;
	}
}
