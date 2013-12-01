package org.twodividedbyzero.idea.findbugs.common.util;

import com.intellij.psi.PsiElement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * $Date$
 *
 * @author Reto Merz<reto.merz@gmail.com>
 * @version $Revision$
 * @since 0.9.99
 */
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
		} catch (ClassNotFoundException e) { // ignore e;
		} catch (NoSuchMethodException e) { // ignore e;
		}

		// or CodeInsightUtilBase ?
		if (_preparePsiElementForWrite == null) {
			try {
				final Class codeInsightUtilBase = Class.forName("com.intellij.codeInsight.CodeInsightUtilBase");
				if (codeInsightUtilBase != null) {
					_getInstance = null;
					_preparePsiElementForWrite = codeInsightUtilBase.getDeclaredMethod("preparePsiElementForWrite", PsiElement.class);
				}
			} catch (ClassNotFoundException e) { // ignore e;
			} catch (NoSuchMethodException e) { // ignore e;
			}
		}

		if (_preparePsiElementForWrite == null) {
			throw new Error("Method preparePsiElementForWrite(PsiElement) not found in FileModificationService and CodeInsightUtilBase");
		}

		getInstance = _getInstance;
		preparePsiElementForWrite = _preparePsiElementForWrite;
	}

	public static boolean preparePsiElementForWrite(@org.jetbrains.annotations.Nullable PsiElement element) {
		try {
			Object instance = null;
			if (getInstance != null) {
				instance = getInstance.invoke(null);
			}
			return (Boolean)preparePsiElementForWrite.invoke(instance, element);
		} catch (IllegalAccessException e) {
			throw new Error(e);
		} catch ( InvocationTargetException e ) {
			throw new Error(e);
		}
	}

}