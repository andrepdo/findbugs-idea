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
package org.twodividedbyzero.idea.findbugs.common;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.QuickFix;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import edu.umd.cs.findbugs.BugInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.BugInstanceUtil;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class ExtendedProblemDescriptor implements ProblemDescriptor {


	private final PsiFile _psiFile;
	private PsiElement _psiElement;
	private int _lineStart;
	private int _lineEnd;
	private int _hash;

	private final BugInstanceNode _bugInstanceNode;


	public ExtendedProblemDescriptor(final PsiFile psiFile, final BugInstanceNode bugInstanceNode) {
		_psiFile = psiFile;
		_bugInstanceNode = bugInstanceNode;
		final int[] lines = BugInstanceUtil.getSourceLines(_bugInstanceNode);
		_lineStart = lines[0] - 1;
		_lineEnd = lines[1] - 1;
	}


	public boolean showTooltip() {
		return true;
	}


	public BugInstance getBugInstance() {
		return _bugInstanceNode.getBugInstance();
	}


	/**
	 * Get the column position of this error.
	 *
	 * @return the column position.
	 */
	public int getColumn() {
		return 0;
	}


	/**
	 * Get the line position of this error.
	 * <p/>
	 * This is the line as reported by FindBugs, rather than that computed
	 * by IDEA.
	 *
	 * @return the line position.
	 */
	public int getLine() {
		return _lineStart;
	}


	public void setTextAttributes(final TextAttributesKey key) {
		//TODO: implement
	}


	@Nullable
	public String getProblemGroup() {
		return "FindBugs-IDEA";
	}


	public void setProblemGroup(@Nullable final String s) {
	}


	public PsiElement getEndElement() {
		return getPsiElement();
	}


	public ProblemHighlightType getHighlightType() {
		return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
	}


	public int getLineNumber() {
		return getLine();
	}


	public PsiElement getPsiElement() {
		if (_psiElement != null) {
			return _psiElement;
		}
		if(_lineStart < 0) {
			_psiElement =  IdeaUtilImpl.findAnonymousClassPsiElement(_bugInstanceNode, _psiFile.getProject());
		} else {
			_psiElement = IdeaUtilImpl.getElementAtLine(_psiFile, _lineStart);
		}
		return _psiElement;
	}


	public PsiElement getStartElement() {
		return getPsiElement();
	}


	public boolean isAfterEndOfLine() {
		return false;
	}


	@NotNull
	public String getDescriptionTemplate() {
		return "<description template>";
	}


	@edu.umd.cs.findbugs.annotations.SuppressWarnings({"PZLA_PREFER_ZERO_LENGTH_ARRAYS"})
	@Nullable
	public QuickFix<?>[] getFixes() {
		return null;
	}


	public void setLineStart(final int lineStart) {
		_lineStart = lineStart;
	}


	public void setLineEnd(final int lineEnd) {
		_lineEnd = lineEnd;
	}


	public PsiFile getPsiFile() {
		return _psiFile;
	}


	public int getLineStart() {
		return _lineStart;
	}


	public int getLineEnd() {
		return _lineEnd;
	}


	public int getHash() {
		return _hash;
	}
}
