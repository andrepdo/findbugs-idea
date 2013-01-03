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
package org.twodividedbyzero.idea.findbugs.intentions;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.Icon;
import java.util.List;
import java.util.Map;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
public class ClearBugIntentionAction extends SuppressReportBugIntentionAction {

	public ClearBugIntentionAction(final ExtendedProblemDescriptor problemDescriptor) {
		super(problemDescriptor);
	}


	@Override
	public void invoke(@NotNull final Project project, final Editor editor, @NotNull final PsiElement element) throws IncorrectOperationException {
		final Map<PsiFile, List<ExtendedProblemDescriptor>> problems = IdeaUtilImpl.getPluginComponent(project).getProblems();
		problems.get(element.getContainingFile()).remove(getProblemDescriptor());
		DaemonCodeAnalyzer.getInstance(project).restart(element.getContainingFile());
	}


	@Override
	@Nullable
	protected PsiDocCommentOwner getContainer(final PsiElement element) {
		PsiDocCommentOwner container = super.getContainer(element);
		if (container == null || container instanceof PsiClass) {
			return null;
		}
		while (container != null) {
			final PsiClass parentClass = PsiTreeUtil.getParentOfType(container, PsiClass.class);
			if ((parentClass == null || container.getParent() instanceof PsiDeclarationStatement) && container instanceof PsiClass) {
				return container;
			}
			container = parentClass;
		}
		return container;
	}


	@Override
	@NotNull
	public String getText() {
		return ResourcesLoader.getString("findbugs.inspection.quickfix.clear") + " '" + getBugPatternId() + '\'';
	}


	@SuppressWarnings("HardcodedFileSeparator")
	@Override
	public Icon getIcon(final int flags) {
		return ResourcesLoader.loadIcon("intentions/stop.png");
	}
}
