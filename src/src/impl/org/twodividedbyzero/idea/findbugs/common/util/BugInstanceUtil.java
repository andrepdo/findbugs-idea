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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.I18N;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.Bug;
import org.twodividedbyzero.idea.findbugs.gui.tree.GroupBy;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"AnonymousInnerClass"})
public class BugInstanceUtil {

	private BugInstanceUtil() {
		throw new UnsupportedOperationException();
	}


	public static MethodAnnotation getPrimaryMethod(final BugInstance bugInstance) {
		return bugInstance.getPrimaryMethod();
	}


	public static FieldAnnotation getPrimaryField(final BugInstance bugInstance) {
		return bugInstance.getPrimaryField();
	}


	public static String getAbridgedMessage(final BugInstance bugInstance) {
		return bugInstance.getAbridgedMessage();
	}


	private static ClassAnnotation getPrimaryClass(final BugInstance bugInstance) {
		return bugInstance.getPrimaryClass();
	}


	public static String getSimpleClassName(final BugInstance bugInstance) {
		return bugInstance.getPrimaryClass().getSourceLines().getSimpleClassName();
	}


	public static String getPackageName(final BugInstance bugInstance) {
		return bugInstance.getPrimaryClass().getSourceLines().getPackageName();
	}


	public static String getJavaSourceMethodName(final BugInstance bugInstance) {
		return getPrimaryMethod(bugInstance).getJavaSourceMethodName();
	}


	public static String getFullMethod(final BugInstance bugInstance) {
		return getPrimaryMethod(bugInstance).getFullMethod(getPrimaryClass(bugInstance));
	}


	public static String getMethodName(final BugInstance bugInstance) {
		return getPrimaryMethod(bugInstance).getMethodName();
	}


	public static String getFieldName(final BugInstance bugInstance) {
		return getPrimaryField(bugInstance).getFieldName();
	}


	public static String getPriorityString(final BugInstance bugInstance) {
		return bugInstance.getPriorityString();
	}


	public static String getPriorityTypeString(final BugInstance bugInstance) {
		return bugInstance.getPriorityTypeString();
	}


	public static String getBugCategoryDescription(final BugInstance bugInstance) {
		return I18N.instance().getBugCategoryDescription(bugInstance.getBugPattern().getCategory());
	}


	public static String getBugTypeDescription(final BugInstance bugInstance) {
		return I18N.instance().getBugTypeDescription(bugInstance.getBugPattern().getAbbrev());
	}


	public static String getBugPatternShortDescription(final BugInstance bugInstance) {
		return bugInstance.getBugPattern().getShortDescription();
	}


	public static String getDetailHtml(final BugInstance bugInstance) {
		final String bugDetailsKey = bugInstance.getType();
		return I18N.instance().getDetailHTML(bugDetailsKey);
	}


	public static String getDetailText(final BugInstance bugInstance) {
		return bugInstance.getBugPattern().getDetailText();
	}

	@SuppressWarnings("ConstantConditions")
	@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
	public static int[] getSourceLines(@NotNull final BugInstance bugInstance) {
		final int[] lines = new int[2];
		final SourceLineAnnotation annotation = bugInstance.getPrimarySourceLineAnnotation();

		if (annotation != null) {
			lines[0] = annotation.getStartLine();
			lines[1] = annotation.getEndLine();
		} else {
			lines[0] = 1;
			lines[1] = 1;
		}

		return lines;
	}


	public static String getBugType(final BugInstance bugInstance) {
		return bugInstance.getBugPattern().getType();
	}


	@Nullable
	public static PsiFile findPsiElement(@NotNull final Project project, @NotNull final BugInstanceNode node) {
		final PsiClass psiClass = IdeaUtilImpl.findJavaPsiClass(project, node.getSourcePath());
		return IdeaUtilImpl.getPsiFile(psiClass);
	}


	/**
	 * NOTE: use {@link #findPsiElement(com.intellij.openapi.project.Project, org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode)}
	 * instead of this method. this method is executed through ApplicationManager.getApplication().invokeAndWait so it's very deadlock prone.
	 *
	 * @param project ..
	 * @param node    ..
	 * @return ..
	 * @deprecated use {@link #findPsiElement(com.intellij.openapi.project.Project, org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode)}
	 * instead of this method. this method is executed through ApplicationManager.getApplication().invokeAndWait so it's very deadlock prone.
	 */
	@Deprecated
	@Nullable
	public static PsiFile getPsiElement(final Project project, final BugInstanceNode node) {
		final PsiClass[] psiClass = new PsiClass[1];
		if (!EventQueue.isDispatchThread()) {
			ApplicationManager.getApplication().invokeAndWait(new Runnable() {
				public void run() {
					psiClass[0] = IdeaUtilImpl.findJavaPsiClass(project, node.getSourcePath());
				}
			}, ModalityState.defaultModalityState());
		} else {
			psiClass[0] = IdeaUtilImpl.findJavaPsiClass(project, node.getSourcePath());
		}

		return IdeaUtilImpl.getPsiFile(psiClass[0]);
	}


	private static List<String> getBugInstanceGroupPath(final Bug bug, final GroupBy[] groupBy) {
		final List<String> result = new ArrayList<String>();
		for (final GroupBy group : groupBy) {
			result.add(GroupBy.getGroupName(group, bug));
		}

		//Collections.reverse(result);
		return result;
	}


	public static String[] getGroupPath(final Bug bug, final int depth, final GroupBy[] groupBy) {
		final List<String> path = getBugInstanceGroupPath(bug, groupBy);
		final String[] result = new String[depth];

		for (int i = 0; i < depth; i++) {
			final String str = path.get(i);
			result[i] = str;
		}

		return result;
	}


	public static String[] getFullGroupPath(final Bug bug, final GroupBy[] groupBy) {
		final List<String> path = getBugInstanceGroupPath(bug, groupBy);
		return getGroupPath(bug, path.size(), groupBy);
	}
}
