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

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.intention.AddAnnotationFix;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
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
public class AddSuppressWarningAnnotationFix extends AddAnnotationFix {

	private final ExtendedProblemDescriptor _problemDescriptor;


	/*public AddSuppressWarningAnnotation(final String fqn, final PsiModifierListOwner modifierListOwner, final String... annotationsToRemove) {
		super(fqn, modifierListOwner, annotationsToRemove);
	}*/


	public AddSuppressWarningAnnotationFix(@NotNull final ExtendedProblemDescriptor problemDescriptor, @org.jetbrains.annotations.NonNls final String... annotationsToRemove) {
		super("org.twodividedbyzero.idea.findbugs.common.annotations.SuppressWarnings", annotationsToRemove);
		_problemDescriptor = problemDescriptor;
	}


	@NotNull
	@Override
	public String getText() {
		final String bugType = _problemDescriptor.getBugInstance().getBugPattern().getType();
		return ResourcesLoader.getString("findbugs.inspection.quickfix.supress.warning") + " '" + bugType + "' waring";
	}


	@NotNull
	@Override
	public String getName() {
		final String bugType = _problemDescriptor.getBugInstance().getBugPattern().getType();
		return ResourcesLoader.getString("findbugs.inspection.quickfix.supress.warning") + " '" + bugType + "' waring";
	}
	

	@NotNull
	@Override
	public String getFamilyName() {
		return "FindBugs-IDEA";
	}


	@Override
	public boolean isAvailable(@NotNull final Project project, final Editor editor, @Nullable final PsiElement element) {
		if (!super.isAvailable(project, editor, element)) {
			//return false;
		}

		//AnnotationUtil.findAnnotation()

		final PsiModifierListOwner owner = getContainer(element);
		if (owner == null || AnnotationUtil.isAnnotated(owner, getAnnotationsToRemove()[0], false)) {
			//return false;
		}
		if (owner instanceof PsiMethod) {
			final PsiType returnType = ((PsiMethod) owner).getReturnType();

			return returnType != null && !(returnType instanceof PsiPrimitiveType);
		}
		return true;
	}
}
