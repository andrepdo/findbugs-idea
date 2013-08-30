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

package org.twodividedbyzero.idea.findbugs.common.util;

import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompileTask;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
public class CompileManagerFacade {

	private static final Logger LOGGER = Logger.getInstance(CompileManagerFacade.class.getName());


	private final Project _project;


	public CompileManagerFacade(final Project project) {
		_project = project;
	}


	public void compile(final VirtualFile[] virtualFiles, final CompileStatusNotification notification, @Nullable final CompileTask afterTask, final boolean idea9flag) {
		final CompilerManager compilerManager = CompilerManager.getInstance(_project);

		try {
			if (IdeaUtilImpl.isVersionGreaterThanIdea9()) {
				final Method method = compilerManager.getClass().getDeclaredMethod("compile", VirtualFile[].class, CompileStatusNotification.class);
				method.invoke(virtualFiles, afterTask != null ? null : notification);
			} else {
				final Method method = compilerManager.getClass().getDeclaredMethod("compile", VirtualFile[].class, CompileStatusNotification.class, boolean.class);
				method.invoke(virtualFiles, afterTask != null ? null : notification, false);
			}
		} catch (NoSuchMethodException e) {
			LOGGER.debug(e);
		} catch (InvocationTargetException e) {
			LOGGER.debug(e.getTargetException());
		} catch (IllegalAccessException e) {
			LOGGER.debug(e);
		}
	}

}
