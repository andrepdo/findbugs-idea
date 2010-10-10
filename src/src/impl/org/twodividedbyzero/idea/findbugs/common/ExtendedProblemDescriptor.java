/**
 * Copyright 2008 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class ExtendedProblemDescriptor implements ProblemDescriptor {


	private ProblemDescriptor _delegate;
	private int _column;
	private int _line;
	private PsiFile _file;
	private int _lineStart;
	private int _lineEnd;
	private int _hash;

	private final BugInstance _bugInstance;


	public ExtendedProblemDescriptor(final PsiFile file, final BugInstance bugInstance) {
		_file = file;
		_bugInstance = bugInstance;
		final int[] lines = BugInstanceUtil.getSourceLines(_bugInstance);
		_lineStart = lines[0] - 1;
		_lineEnd = lines[1] - 1;
		if(_lineStart < 0 || _lineEnd < 0) { // anonymous class? findbugs does not report line numbers for bug in anonymous/inner classes
			_lineStart = 0;
			_lineEnd = 0;
		}
	}


	public boolean showTooltip() {
		return true;
	}


	public BugInstance getBugInstance() {
		return _bugInstance;
	}


	/*public ExtendedProblemDescriptor(final ProblemDescriptor delegate, final int line, final int column) {
		if (delegate == null) {
			throw new IllegalArgumentException("Delegate may not be null.");
		}


		_delegate = delegate;
		_line = line;
		_column = column;
	}*/


	/**
	 * Get the column position of this error.
	 *
	 * @return the column position.
	 */
	public int getColumn() {
		return _column;
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
		return _line;
	}


	public void setTextAttributes(final TextAttributesKey key) {
		//TODO: implement
	}


	public PsiElement getEndElement() {
		return _delegate.getEndElement();
	}


	public ProblemHighlightType getHighlightType() {
		return _delegate.getHighlightType();
	}


	public int getLineNumber() {
		return getLine();
	}


	public PsiElement getPsiElement() {
		return _delegate.getPsiElement();
	}


	public PsiElement getStartElement() {
		return _delegate.getStartElement();
	}


	public boolean isAfterEndOfLine() {
		return _delegate.isAfterEndOfLine();
	}


	@NotNull
	public String getDescriptionTemplate() {
		return _delegate.getDescriptionTemplate();
	}


	@Nullable
	public QuickFix<?>[] getFixes() {
		return _delegate.getFixes();
	}


	public void setFile(final PsiFile file) {
		_file = file;
	}


	public void setLineStart(final int lineStart) {
		_lineStart = lineStart;
	}


	public void setLineEnd(final int lineEnd) {
		_lineEnd = lineEnd;
	}


	public void setHash(final int hash) {
		_hash = hash;
	}


	public PsiFile getFile() {
		return _file;
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
