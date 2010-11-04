/*
 * Copyright 2010 Andre Pfeiler
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

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.SuppressIntentionAction;
import com.intellij.codeInspection.SuppressManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.source.jsp.jspJava.JspHolderMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
// todo:
public class AddSuppressReportBugFix extends SuppressIntentionAction {

	@NonNls
	public static final String SUPPRESS_INSPECTIONS_TAG_NAME = "noinspection";
	public static final String SUPPRESS_INSPECTIONS_ANNOTATION_NAME = "org.twodividedbyzero.idea.findbugs.common.annotations.SuppressWarnings";

	private String _bugPatternId;
	private String _key;


	/*public AddSuppressBugFix(HighlightDisplayKey key) {
		this(key.getID());
		myAlternativeID = HighlightDisplayKey.getAlternativeID(key);
	}*/


	public AddSuppressReportBugFix(final String ID) {
		_bugPatternId = ID;
	}


	public AddSuppressReportBugFix(final ExtendedProblemDescriptor problemDescriptor) {
		_bugPatternId = getBugId(problemDescriptor);
	}


	protected String getBugId(final ExtendedProblemDescriptor problemDescriptor) {
		return problemDescriptor.getBugInstance().getBugPattern().getType();
	}


	@Override
	@NotNull
	public String getText() {
		//  myKey != null ? InspectionsBundle.message(myKey) : "Suppress for member";
		return ResourcesLoader.getString("findbugs.inspection.quickfix.supress.warning") + " '" + _bugPatternId + '\'';
	}


	@NotNull
	public String getFamilyName() {
		return ResourcesLoader.getString("findbugs.inspection.quickfix.supress.warning");
	}


	@Nullable
	protected PsiDocCommentOwner getContainer(final PsiElement context) {
		if (context == null /*|| !(context.getContainingFile().getLanguage() instanceof JavaLanguage)*/ || context instanceof PsiFile) {
			return null;
		}
		PsiElement container = context;
		while (container instanceof PsiAnonymousClass || !(container instanceof PsiDocCommentOwner) || container instanceof PsiTypeParameter) {
			container = PsiTreeUtil.getParentOfType(container, PsiDocCommentOwner.class);
			if (container == null) {
				return null;
			}
		}
		return (PsiDocCommentOwner) container;
	}


	@Override
	@SuppressWarnings({"SimplifiableIfStatement"})
	public boolean isAvailable(@NotNull final Project project, final Editor editor, @Nullable final PsiElement context) {
		final PsiDocCommentOwner container = getContainer(context);
		_key = container instanceof PsiClass ? "suppress.inspection.class" : container instanceof PsiMethod ? "suppress.inspection.method" : "suppress.inspection.field";
		final boolean isValid = container != null && !(container instanceof JspHolderMethod);
		if (!isValid) {
			return false;
		}
		return context != null && context.getManager().isInProject(context);
	}


	@Override
	public void invoke(final Project project, final Editor editor, final PsiElement element) throws IncorrectOperationException {
		final PsiDocCommentOwner container = getContainer(element);
		assert container != null;
		@SuppressWarnings({"ConstantConditions"})
		final ReadonlyStatusHandler.OperationStatus status = ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(container.getContainingFile().getVirtualFile());
		if (status.hasReadonlyFiles()) {
			return;
		}
		if (use15Suppressions(container)) {
			final PsiModifierList modifierList = container.getModifierList();
			if (modifierList != null) {
				addSuppressAnnotation(project, editor, container, modifierList, getID(container));
			}
		} else {
			PsiDocComment docComment = container.getDocComment();
			final PsiManager manager = PsiManager.getInstance(project);
			if (docComment == null) {
				/*final String commentText = "*//** @" + SUPPRESS_INSPECTIONS_TAG_NAME + ' ' + getID(container) + "*//*";
				docComment = JavaPsiFacade.getInstance(manager.getProject()).getElementFactory().createDocCommentFromText(commentText, null);
				final PsiElement firstChild = container.getFirstChild();
				container.addBefore(docComment, firstChild);*/
			} else {
				/*final PsiDocTag noInspectionTag = docComment.findTagByName(SUPPRESS_INSPECTIONS_TAG_NAME);
				if (noInspectionTag != null) {
					final PsiDocTagValue valueElement = noInspectionTag.getValueElement();
					final String tagText = '@' + SUPPRESS_INSPECTIONS_TAG_NAME + ' ' + (valueElement != null ? valueElement.getText() + ',' : "") + getID(container);
					noInspectionTag.replace(JavaPsiFacade.getInstance(manager.getProject()).getElementFactory().createDocTagFromText(tagText, null));

					addImport(project, element);

				} else {
					final String tagText = '@' + SUPPRESS_INSPECTIONS_TAG_NAME + ' ' + getID(container);
					docComment.add(JavaPsiFacade.getInstance(manager.getProject()).getElementFactory().createDocTagFromText(tagText, null));

					addImport(project, element);
				}*/
			}
		}
		DaemonCodeAnalyzer.getInstance(project).restart();
	}


	private void addImport(final Project project, final PsiElement element) {
		final PsiFile file = element.getContainingFile();
		if (file instanceof PsiJavaFile) {
			final PsiElementFactory elementFactory = JavaPsiFacade.getInstance(project).getElementFactory();
			final PsiClass psiClass = elementFactory.createClass(SUPPRESS_INSPECTIONS_ANNOTATION_NAME);
			((PsiJavaFile) file).importClass(psiClass);
			JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiClass);
		}
	}


	public void addSuppressAnnotation(final Project project, final Editor editor, final PsiElement container, final PsiModifierList modifierList, final String id) throws IncorrectOperationException {
		PsiAnnotation annotation = modifierList.findAnnotation(SUPPRESS_INSPECTIONS_ANNOTATION_NAME);
		if (annotation != null) {
			if (!annotation.getText().contains("{")) {
				final PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
				if (attributes.length == 1) {
					final String suppressedWarnings = attributes[0].getText();
					final PsiAnnotation newAnnotation = JavaPsiFacade.getInstance(project).getElementFactory().createAnnotationFromText('@' + SUPPRESS_INSPECTIONS_ANNOTATION_NAME + "({" + suppressedWarnings + ", \"" + id + "\"})", container);
					annotation.replace(newAnnotation);
				}
			} else {
				final int curlyBraceIndex = annotation.getText().lastIndexOf('}');
				if (curlyBraceIndex > 0) {
					annotation.replace(JavaPsiFacade.getInstance(project).getElementFactory().createAnnotationFromText(annotation.getText().substring(0, curlyBraceIndex) + ", \"" + id + "\"})", container));
				} else if (!ApplicationManager.getApplication().isUnitTestMode() && editor != null) {
					Messages.showErrorDialog(editor.getComponent(), InspectionsBundle.message("suppress.inspection.annotation.syntax.error", annotation.getText()));
				}
			}
		} else {
			annotation = JavaPsiFacade.getInstance(project).getElementFactory().createAnnotationFromText('@' + SUPPRESS_INSPECTIONS_ANNOTATION_NAME + "({\"" + id + "\"})", container);
			modifierList.addBefore(annotation, modifierList.getFirstChild());
			JavaCodeStyleManager.getInstance(project).shortenClassReferences(modifierList);

			addImport(project, container);
		}
	}


	protected static boolean use15Suppressions(final PsiDocCommentOwner container) {
		return SuppressManager.getInstance().canHave15Suppressions(container) && !SuppressManager.getInstance().alreadyHas14Suppressions(container);
	}


	@Override
	public boolean startInWriteAction() {
		return true;
	}


	public String getID(final PsiElement place) {
		/*if (myAlternativeID != null) {
			final Module module = ModuleUtil.findModuleForPsiElement(place);
			if (module != null) {
				//if (!ClasspathStorage.getStorageType(module).equals(ClasspathStorage.DEFAULT_STORAGE)) {
					return myAlternativeID;
				//}
			}
		}*/

		return _bugPatternId;
	}
}
