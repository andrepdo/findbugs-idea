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

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.Detector;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.event.EventListener;
import org.twodividedbyzero.idea.findbugs.common.event.EventManagerImpl;
import org.twodividedbyzero.idea.findbugs.common.event.filters.BugReporterEventFilter;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent;
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
public class BugAnnotator implements Annotator, EventListener<BugReporterEvent> {

	private boolean _analysisRunning;
	private boolean _isRegistered;

	public BugAnnotator() {
		_analysisRunning = false;
		_isRegistered = false;
	}


	public void annotate(@NotNull final PsiElement psiElement, @NotNull final AnnotationHolder annotationHolder) {
		if(_analysisRunning) {
			return;
		}
		if(!_isRegistered) {
			EventManagerImpl.getInstance().addEventListener(new BugReporterEventFilter(psiElement.getProject().getName()), this);
			_isRegistered = true;
		}
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
		if (problemDescriptor.getLineStart() <= 0) {
			return;
		}
		
		final BugInstance bugInstance = problemDescriptor.getBugInstance();
		final int priority = bugInstance.getPriority();
		final Annotation annotation;
		final TextRange textRange = problemDescriptor.getPsiElement().getTextRange();
		switch (priority) {
			case Detector.HIGH_PRIORITY :
			case Detector.NORMAL_PRIORITY :
			case Detector.EXP_PRIORITY :
				annotation = annotationHolder.createErrorAnnotation(textRange, getAnnotationText(problemDescriptor));
				annotation.registerFix(new AddSuppressWarningAnnotationFix(problemDescriptor), textRange);
				break;
			case Detector.LOW_PRIORITY :
				annotation = annotationHolder.createWarningAnnotation(textRange, getAnnotationText(problemDescriptor));
				annotation.registerFix(new AddSuppressWarningAnnotationFix(problemDescriptor), textRange);
				break;
			case Detector.IGNORE_PRIORITY :
				annotation = annotationHolder.createInfoAnnotation(textRange, getAnnotationText(problemDescriptor));
				annotation.registerFix(new AddSuppressWarningAnnotationFix(problemDescriptor), textRange);
				break;
			default:
			break;
		}
	}


	private static String getAnnotationText(final ExtendedProblemDescriptor problemDescriptor) {
		final StringBuilder buffer = new StringBuilder();
		buffer.append(BugInstanceUtil.getBugPatternShortDescription(problemDescriptor.getBugInstance())).append("\n");
		buffer.append(BugInstanceUtil.getDetailText(problemDescriptor.getBugInstance()));

		return buffer.toString();
	}


	public void onEvent(@NotNull final BugReporterEvent event) {
		switch (event.getOperation()) {

			case ANALYSIS_STARTED:
				_analysisRunning = true;
				break;
			case ANALYSIS_ABORTED:
				_analysisRunning = false;
				break;
			case ANALYSIS_FINISHED:
				_analysisRunning = false;
				break;
		}
	}
}
