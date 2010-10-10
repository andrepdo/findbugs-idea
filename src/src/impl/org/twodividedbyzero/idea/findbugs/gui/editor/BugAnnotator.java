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
package org.twodividedbyzero.idea.findbugs.gui.editor;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.util.BugInstanceUtil;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;

import java.util.List;
import java.util.Map;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.94
 */
public class BugAnnotator implements Annotator {

	public void annotate(@NotNull final PsiElement psiElement, @NotNull final AnnotationHolder annotationHolder) {
		final Project project = psiElement.getProject();
		final Map<PsiFile, List<ExtendedProblemDescriptor>> problems = IdeaUtilImpl.getPluginComponent(project).getProblems();

		final PsiFile psiFile = psiElement.getContainingFile();
		if (problems.containsKey(psiFile)) {
			addAnnotation(problems.get(psiFile), annotationHolder);
		}
	}


	private static void addAnnotation(final Iterable<ExtendedProblemDescriptor> problemDescriptors, @NotNull final AnnotationHolder annotationHolder) {
		for (final ExtendedProblemDescriptor descriptor : problemDescriptors) {
			addAnnotation(descriptor, annotationHolder);
		}
	}


	private static void addAnnotation(final ExtendedProblemDescriptor problemDescriptor, @NotNull final AnnotationHolder annotationHolder) {
		if (problemDescriptor.getLineStart() == -1) {
			return;
		}
		final TextRange textRange = new TextRange(problemDescriptor.getLineStart(), problemDescriptor.getLineEnd());
		annotationHolder.createErrorAnnotation(textRange, getAnnotationText(problemDescriptor));
	}


	private static String getAnnotationText(final ExtendedProblemDescriptor problemDescriptor) {
		final StringBuilder buffer = new StringBuilder("<html><body>");
		buffer.append(BugInstanceUtil.getDetailHtml(problemDescriptor.getBugInstance())).append("<hr/>");
		buffer.append("</body><html>");

		return buffer.toString();
	}
}
