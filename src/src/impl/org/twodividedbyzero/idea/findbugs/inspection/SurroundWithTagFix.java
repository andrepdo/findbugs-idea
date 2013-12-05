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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.util.IncorrectOperationException;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.84-dev
 */
public class SurroundWithTagFix implements LocalQuickFix {

	private final PsiElement _parent;
	private final PsiElement _firstChild;
	private final PsiElement _lastChild;
	private final String _tagName;


	public SurroundWithTagFix(final PsiElement parent, final PsiElement firstChild, final PsiElement lastChild, final String tagName) {
		_parent = parent;
		_firstChild = firstChild;
		_lastChild = lastChild;
		_tagName = tagName;
	}


	@NotNull
	public String getName() {
		return "Surround with '" + _tagName + '\'';
	}


	public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			public void run() {
				//final PsiElementFactory elfactory = parent.getManager().getElementFactory();
				final PsiElementFactory elfactory = JavaPsiFacade.getInstance(_parent.getProject()).getElementFactory();
				elfactory.createAnnotationFromText("@SomeAnnotation(someVariable = true)", descriptor.getPsiElement());

				try {
					final StringBuilder builder = new StringBuilder(_parent.getTextRange().getLength());
					//noinspection ForLoopWithMissingComponent
					for (PsiElement next = _firstChild; ; next = next.getNextSibling()) {
						assert next != null;
						builder.append(next.getText());
						//noinspection ObjectEquality
						if (next == _lastChild) {
							break;
						}
					}
					/*final XmlTag tag = elfactory.createTagFromText("<" + tagName + ">" + builder.toString() + "</" + tagName + ">");
					parent.addAfter(tag, lastChild);
					parent.deleteChildRange(firstChild, lastChild);*/
				} catch (final IncorrectOperationException e) {
					throw new IllegalStateException(e);
				}
			}
		});
	}


	@SuppressWarnings({"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
	@NotNull
	public String getFamilyName() {
		return FindBugsPluginConstants.PLUGIN_ID;
	}
}
