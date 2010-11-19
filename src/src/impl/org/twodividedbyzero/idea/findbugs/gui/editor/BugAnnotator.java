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
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
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
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import java.awt.Color;
import java.awt.Font;
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
	private final FindBugsPreferences _preferences;


	public BugAnnotator() {
		_analysisRunning = false;
		_isRegistered = false;
		_preferences = IdeaUtilImpl.getPluginComponent(IdeaUtilImpl.getProject()).getPreferences();
	}


	public void annotate(@NotNull final PsiElement psiElement, @NotNull final AnnotationHolder annotationHolder) {
		if (_analysisRunning) {
			return;
		}
		if (!_isRegistered) {
			EventManagerImpl.getInstance().addEventListener(new BugReporterEventFilter(psiElement.getProject().getName()), this);
			_isRegistered = true;
		}
		final Project project = psiElement.getProject();
		final Map<PsiFile, List<ExtendedProblemDescriptor>> problems = IdeaUtilImpl.getPluginComponent(project).getProblems();

		final PsiFile psiFile = psiElement.getContainingFile();
		if (problems.containsKey(psiFile)) {
			addAnnotation(psiElement, problems.get(psiFile), annotationHolder);
		}
	}


	private static void addAnnotation(@NotNull final PsiElement psiElement, final Iterable<ExtendedProblemDescriptor> problemDescriptors, @NotNull final AnnotationHolder annotationHolder) {
		for (final ExtendedProblemDescriptor descriptor : problemDescriptors) {
			final PsiElement problemPsiElement = descriptor.getPsiElement();

			final PsiAnonymousClass psiAnonymousClass = PsiTreeUtil.getParentOfType(psiElement, PsiAnonymousClass.class);
			if (psiElement.equals(problemPsiElement)) {
				addAnnotation(descriptor, psiElement, annotationHolder);
			} else if (psiAnonymousClass != null && psiAnonymousClass.equals(problemPsiElement)) {
				addAnnotation(descriptor, psiAnonymousClass, annotationHolder);
			}
		}
	}


	private static void addAnnotation(final ExtendedProblemDescriptor problemDescriptor, final PsiElement psiElement, @NotNull final AnnotationHolder annotationHolder) {
		final BugInstance bugInstance = problemDescriptor.getBugInstance();
		final int priority = bugInstance.getPriority();
		final Annotation annotation;
		final PsiElement problemElement = problemDescriptor.getPsiElement();
		final TextRange textRange = problemElement.getTextRange();


		switch (priority) {
			case Detector.HIGH_PRIORITY:

				if (psiElement instanceof PsiAnonymousClass) {
					final PsiElement firstChild = psiElement.getFirstChild();
					annotation = annotationHolder.createErrorAnnotation(firstChild == null ? psiElement : firstChild, getAnnotationText(problemDescriptor));
					annotation.setEnforcedTextAttributes(new TextAttributes(Color.BLACK, Color.RED.brighter(), Color.WHITE, EffectType.BOXED, Font.PLAIN));
				} else {
					annotation = annotationHolder.createErrorAnnotation(textRange, getAnnotationText(problemDescriptor));
					annotation.setEnforcedTextAttributes(new TextAttributes(Color.BLACK, Color.WHITE, Color.RED, EffectType.WAVE_UNDERSCORE, Font.PLAIN));
				}

				annotation.registerFix(new AddSuppressReportBugFix(problemDescriptor), textRange);
				annotation.registerFix(new AddSuppressReportBugForClassFix(problemDescriptor), textRange);

				break;

			case Detector.NORMAL_PRIORITY:

				if (psiElement instanceof PsiAnonymousClass) {
					final PsiElement firstChild = psiElement.getFirstChild();
					annotation = annotationHolder.createErrorAnnotation(firstChild == null ? psiElement : firstChild, getAnnotationText(problemDescriptor));
				} else {
					annotation = annotationHolder.createErrorAnnotation(textRange, getAnnotationText(problemDescriptor));
				}
				annotation.setEnforcedTextAttributes(new TextAttributes(Color.BLACK, Color.WHITE, Color.YELLOW.darker(), EffectType.WAVE_UNDERSCORE, Font.PLAIN));
				annotation.registerFix(new AddSuppressReportBugFix(problemDescriptor), textRange);
				annotation.registerFix(new AddSuppressReportBugForClassFix(problemDescriptor), textRange);

				break;

			case Detector.EXP_PRIORITY:

				if (problemElement instanceof PsiAnonymousClass) {
					final PsiElement firstChild = psiElement.getFirstChild();
					annotation = annotationHolder.createErrorAnnotation(firstChild == null ? psiElement : firstChild, getAnnotationText(problemDescriptor));
				} else {
					annotation = annotationHolder.createErrorAnnotation(textRange, getAnnotationText(problemDescriptor));
				}
				annotation.setEnforcedTextAttributes(new TextAttributes(Color.BLACK, Color.WHITE, Color.GRAY, EffectType.WAVE_UNDERSCORE, Font.PLAIN));
				annotation.registerFix(new AddSuppressReportBugFix(problemDescriptor), textRange);
				annotation.registerFix(new AddSuppressReportBugForClassFix(problemDescriptor), textRange);

				break;
			case Detector.LOW_PRIORITY:

				if (psiElement instanceof PsiAnonymousClass) {
					final PsiElement firstChild = psiElement.getFirstChild();
					annotation = annotationHolder.createErrorAnnotation(firstChild == null ? psiElement : firstChild, getAnnotationText(problemDescriptor));
				} else {
					annotation = annotationHolder.createErrorAnnotation(textRange, getAnnotationText(problemDescriptor));
				}
				annotation.setEnforcedTextAttributes(new TextAttributes(Color.BLACK, Color.WHITE, Color.GREEN, EffectType.WAVE_UNDERSCORE, Font.PLAIN));
				annotation.registerFix(new AddSuppressReportBugFix(problemDescriptor), textRange);
				annotation.registerFix(new AddSuppressReportBugForClassFix(problemDescriptor), textRange);

				break;
			case Detector.IGNORE_PRIORITY:
				if (problemElement instanceof PsiAnonymousClass) {
					final PsiElement firstChild = psiElement.getFirstChild();
					annotation = annotationHolder.createErrorAnnotation(firstChild == null ? psiElement : firstChild, getAnnotationText(problemDescriptor));
					annotation.setEnforcedTextAttributes(new TextAttributes(Color.BLACK, Color.WHITE, Color.MAGENTA.brighter(), EffectType.WAVE_UNDERSCORE, Font.PLAIN));
				} else {
					annotation = annotationHolder.createErrorAnnotation(textRange, getAnnotationText(problemDescriptor));
				}
				annotation.setEnforcedTextAttributes(new TextAttributes(Color.BLACK, Color.WHITE, Color.MAGENTA.darker().darker(), EffectType.WAVE_UNDERSCORE, Font.PLAIN));
				annotation.registerFix(new AddSuppressWarningAnnotationFix(problemDescriptor), textRange);
				annotation.registerFix(new AddSuppressReportBugForClassFix(problemDescriptor), textRange);

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
			case ANALYSIS_FINISHED:
				_analysisRunning = false;
				break;
			case NEW_BUG_INSTANCE:
				break;
			default:
		}
	}


	/*private static class AnonymousInnerClassMayBeStaticVisitor extends BaseInspectionVisitor {

		@Override
		public void visitClass(@NotNull PsiClass aClass) {
			if (!(aClass instanceof PsiAnonymousClass)) {
				return;
			}
			if (aClass instanceof PsiEnumConstantInitializer) {
				return;
			}
			final PsiMember containingMember = PsiTreeUtil.getParentOfType(aClass, PsiMember.class);
			if (containingMember == null || containingMember.hasModifierProperty(PsiModifier.STATIC)) {
				return;
			}
			final PsiAnonymousClass anAnonymousClass = (PsiAnonymousClass) aClass;
			final InnerClassReferenceVisitor visitor = new InnerClassReferenceVisitor(anAnonymousClass);
			anAnonymousClass.accept(visitor);
			if (!visitor.canInnerClassBeStatic()) {
				return;
			}
			registerClassError(aClass);
		}
	}*/
}
