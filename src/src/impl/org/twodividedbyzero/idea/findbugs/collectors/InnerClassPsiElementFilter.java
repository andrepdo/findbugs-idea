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
package org.twodividedbyzero.idea.findbugs.collectors;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;


/**
* $Date$
*
* @author Andre Pfeiler<andrepdo@dev.java.net>
* @version $Revision$
* @since 0.0.1
*/
class InnerClassPsiElementFilter implements PsiElementFilter {

	private final PsiElement _psiElement;


	InnerClassPsiElementFilter(final PsiElement psiElement) {
		_psiElement = psiElement;
	}


	public boolean isAccepted(final PsiElement e) {
		return e instanceof PsiClass && _psiElement.equals(PsiTreeUtil.getParentOfType(e, PsiClass.class));
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("InnerClassPsiElementFilter");
		sb.append("{_psiElement=").append(_psiElement);
		sb.append('}');
		return sb.toString();
	}
}
