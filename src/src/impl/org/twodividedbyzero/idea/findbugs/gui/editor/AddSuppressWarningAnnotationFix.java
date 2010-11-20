/*
 * Copyright 2010 Andre Pfeiler
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
