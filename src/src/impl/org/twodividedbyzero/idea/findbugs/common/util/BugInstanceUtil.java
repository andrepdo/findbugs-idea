/*
 * Copyright 2009 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.twodividedbyzero.idea.findbugs.common.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.I18N;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.DoneCallback;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.gui.tree.GroupBy;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.84-dev
 */
@SuppressWarnings({"AnonymousInnerClass"})
public class BugInstanceUtil {

	private static final Logger LOGGER = Logger.getInstance(BugInstanceUtil.class.getName());


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


	public static int[] getSourceLines(final BugInstanceNode bugInstanceNode) {
		final BugInstance bugInstance = bugInstanceNode.getBugInstance();
		return getSourceLines(bugInstance);
	}


	public static int[] getSourceLines(final BugInstance bugInstance) {
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


	public static void getPsiElement(final Project project, final BugInstanceNode node, final DoneCallback<PsiFile>... callback) {
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			public void run() {
				final PsiClass[] psiClass = new PsiClass[1];
				psiClass[0] = IdeaUtilImpl.findJavaPsiClass(project, node.getSourcePath());
				for (final DoneCallback<PsiFile> doneCallback : callback) {
					doneCallback.onDone(IdeaUtilImpl.getPsiFile(psiClass[0]));
				}
			}
		});
	}


	/**
	 * NOTE: use {@link #getPsiElement(com.intellij.openapi.project.Project, org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode, org.twodividedbyzero.idea.findbugs.common.DoneCallback[])}
	 * instead of this method. this method is executed through ApplicationManager.getApplication().invokeAndWait so it's very deadlock prone.
	 *
	 * @param project
	 * @param node
	 * @return
	 */
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


	private static List<String> getBugInstanceGroupPath(final BugInstance bugInstance, final GroupBy[] groupBy) {
		final List<String> result = new ArrayList<String>();
		for (final GroupBy group : groupBy) {
			result.add(GroupBy.getGroupName(group, bugInstance));
		}

		//Collections.reverse(result);
		return result;
	}


	public static String[] getGroupPath(final BugInstance bugInstance, final int depth, final GroupBy[] groupBy) {
		final List<String> path = getBugInstanceGroupPath(bugInstance, groupBy);
		final String[] result = new String[depth];

		for (int i = 0; i < depth; i++) {
			final String str = path.get(i);
			result[i] = str;
		}

		return result;
	}


	public static String[] getFullGroupPath(final BugInstance bugInstance, final GroupBy[] groupBy) {
		final List<String> path = getBugInstanceGroupPath(bugInstance, groupBy);
		return getGroupPath(bugInstance, path.size(), groupBy);
	}
}
