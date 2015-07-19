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
package org.twodividedbyzero.idea.findbugs.common.util;

import com.intellij.psi.PsiElement;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * $Date$
 *
 * @author Reto Merz<reto.merz@gmail.com>
 * @version $Revision$
 * @since 0.9.99
 */
@SuppressWarnings("unchecked")
@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
public final class FileModificationServiceUtil {

	private FileModificationServiceUtil() {
	}

	private static final Method getInstance;
	private static final Method preparePsiElementForWrite;

	static {
		Method _getInstance = null;
		Method _preparePsiElementForWrite = null;

		// FileModificationService ?
		try {
			final Class fileModificationService = Class.forName("com.intellij.codeInsight.FileModificationService");
			if (fileModificationService != null) {
				_getInstance = fileModificationService.getDeclaredMethod("getInstance");
				if (_getInstance != null) {
					_preparePsiElementForWrite = fileModificationService.getDeclaredMethod("preparePsiElementForWrite", PsiElement.class);
				}
			}
		} catch (final ClassNotFoundException ignored) { // ignore e;
		} catch (final NoSuchMethodException ignored) { // ignore e;
		}

		// or CodeInsightUtilBase ?
		if (_preparePsiElementForWrite == null) {
			try {
				final Class<?> codeInsightUtilBase = Class.forName("com.intellij.codeInsight.CodeInsightUtilBase");
				if (codeInsightUtilBase != null) {
					_getInstance = null;
					_preparePsiElementForWrite = codeInsightUtilBase.getDeclaredMethod("preparePsiElementForWrite", PsiElement.class);
				}
			} catch (final ClassNotFoundException ignored) { // ignore e;
			} catch (final NoSuchMethodException ignored) { // ignore e;
			}
		}

		if (_preparePsiElementForWrite == null) {
			throw new Error("Method preparePsiElementForWrite(PsiElement) not found in FileModificationService and CodeInsightUtilBase");
		}

		getInstance = _getInstance;
		preparePsiElementForWrite = _preparePsiElementForWrite;
	}

	public static boolean preparePsiElementForWrite(@org.jetbrains.annotations.Nullable final PsiElement element) {
		try {
			Object instance = null;
			if (getInstance != null) {
				instance = getInstance.invoke(null);
			}
			return (Boolean)preparePsiElementForWrite.invoke(instance, element);
		} catch (final IllegalAccessException e) {
			throw new Error(e);
		} catch ( final InvocationTargetException e ) {
			throw new Error(e);
		}
	}

}