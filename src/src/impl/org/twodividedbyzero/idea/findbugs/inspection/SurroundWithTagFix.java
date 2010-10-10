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
package org.twodividedbyzero.idea.findbugs.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.util.IncorrectOperationException;
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
		return "Surround with '" + _tagName + "'";
	}


	public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			public void run() {
				//final PsiElementFactory elfactory = parent.getManager().getElementFactory();
				final PsiElementFactory elfactory = JavaPsiFacade.getInstance(_parent.getProject()).getElementFactory();
				elfactory.createAnnotationFromText("@SomeAnnotation(someVariable = true)", descriptor.getPsiElement());

				try {
					final StringBuilder builder = new StringBuilder(_parent.getTextRange().getLength());
					for (PsiElement next = _firstChild; ; next = next.getNextSibling()) {
						assert next != null;
						builder.append(next.getText());
						if (next == _lastChild) {
							break;
						}
					}
					/*final XmlTag tag = elfactory.createTagFromText("<" + tagName + ">" + builder.toString() + "</" + tagName + ">");
					parent.addAfter(tag, lastChild);
					parent.deleteChildRange(firstChild, lastChild);*/
				} catch (IncorrectOperationException e) {
					throw new IllegalStateException(e);
				}
			}
		});
	}


	@NotNull
	public String getFamilyName() {
		return FindBugsPluginConstants.PLUGIN_ID;
	}
}
