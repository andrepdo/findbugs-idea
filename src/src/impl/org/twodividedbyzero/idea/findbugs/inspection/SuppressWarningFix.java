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
package org.twodividedbyzero.idea.findbugs.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.84-dev
 */
public class SuppressWarningFix /*extends SuppressIntentionAction*/ implements LocalQuickFix {

	private static final Logger LOGGER = Logger.getInstance(SuppressWarningFix.class.getName());

	private final String _bugType;
	private final String _annotation;


	public SuppressWarningFix(final String annotationFqn, final String bugType) {
		_bugType = bugType;
		_annotation = annotationFqn;
	}


	/*@Override
	public void invoke(final Project project, final Editor editor, final PsiElement element) throws IncorrectOperationException {
		final ExternalAnnotationsManager annotationsManager = ExternalAnnotationsManager.getInstance(project);
		//final PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
		annotationsManager.annotateExternally(PsiTreeUtil.getParentOfType(element, PsiModifierListOwner.class, false), _annotation, element.getContainingFile());
	}


	@Override
	public boolean isAvailable(@NotNull final Project project, final Editor editor, @Nullable final PsiElement element) {
		if (element == null || !element.isValid()) {
			return false;
		}
		if (!PsiUtil.isLanguageLevel5OrHigher(element)) {
			return false;
		}
		final PsiModifierListOwner owner;

		if (!element.getManager().isInProject(element) || CodeStyleSettingsManager.getSettings(project).USE_EXTERNAL_ANNOTATIONS) {
			owner = getContainer(element);
		} else {
			//noinspection AssignmentToNull
			owner = null;
		}
		//return owner != null && !AnnotationUtil.isAnnotated(owner, _annotation, false);
		return true;
	}


	@Override
	@NotNull
	public String getText() {
		return getName();
	}*/


	@NotNull
	public String getName() {
		return ResourcesLoader.getString("findbugs.inspection.quickfix.suppress.warning") + " '" + _bugType + "' waring";
	}


	@NotNull
	public String getFamilyName() {
		return FindBugsPluginConstants.PLUGIN_ID;
	}


	@SuppressWarnings("HardcodedLineSeparator")
	public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
		try {
			/*final PsiBinaryExpression binaryExpression = (PsiBinaryExpression) descriptor.getPsiElement();
			final IElementType opSign = binaryExpression.getOperationSign().getTokenType();
			final PsiExpression lExpr = binaryExpression.getLOperand();
			final PsiExpression rExpr = binaryExpression.getROperand();
			if (rExpr == null) {
				return;
			}

			final PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
			final PsiMethodCallExpression equalsCall = (PsiMethodCallExpression) factory.createExpressionFromText("a.equals(b)", null);
*/
			final PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
			//final PsiAnnotation annotation = factory.createAnnotationFromText("@SomeAnnotation(someVariable = true)", descriptor.getPsiElement());
			//final PsiAnnotation annotation = factory.createAnnotationFromText("@" + _annotation + "({" + _bugType + "})", descriptor.getPsiElement().getContext());
			final PsiAnnotation annotation = factory.createAnnotationFromText('@' + _annotation + "({\"" + _bugType + "\"})\r\n", descriptor.getPsiElement());
			//factory.createImportStatement()
			descriptor.getPsiElement().addBefore(annotation, descriptor.getStartElement());

			/*equalsCall.getMethodExpression().getQualifierExpression().replace(lExpr);
			equalsCall.getArgumentList().getExpressions()[0].replace(rExpr);

			final PsiExpression result = (PsiExpression) binaryExpression.replace(equalsCall);

			if (opSign == JavaTokenType.NE) {
				final PsiPrefixExpression negation = (PsiPrefixExpression) factory.createExpressionFromText("!a", null);
				negation.getOperand().replace(result);
				result.replace(negation);
			}*/
		} catch (IncorrectOperationException e) {
			LOGGER.error(e);
		}
	}


	@Nullable
	protected static PsiModifierListOwner getContainer(final PsiElement element) {
		PsiModifierListOwner listOwner = PsiTreeUtil.getParentOfType(element, PsiParameter.class, false);
		if (listOwner == null) {
			final PsiIdentifier psiIdentifier = PsiTreeUtil.getParentOfType(element, PsiIdentifier.class, false);
			if (psiIdentifier != null && psiIdentifier.getParent() instanceof PsiModifierListOwner) {
				listOwner = (PsiModifierListOwner) psiIdentifier.getParent();
			}
		}
		return listOwner;
	}
}
