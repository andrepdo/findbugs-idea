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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


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
	private VirtualFile _file;
	private int _lineStart;
	private int _lineEnd;
	private int _hash;


	public ExtendedProblemDescriptor(final VirtualFile file, final int[] lines) {
		_file = file;
		_lineStart = lines[0];
		_lineEnd = lines[1];
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


	/** {@inheritDoc} */
	public PsiElement getEndElement() {
		return _delegate.getEndElement();
	}


	/** {@inheritDoc} */
	public ProblemHighlightType getHighlightType() {
		return _delegate.getHighlightType();
	}


	/** {@inheritDoc} */
	public int getLineNumber() {
		return getLine();
	}


	/** {@inheritDoc} */
	public PsiElement getPsiElement() {
		return _delegate.getPsiElement();
	}


	/** {@inheritDoc} */
	public PsiElement getStartElement() {
		return _delegate.getStartElement();
	}


	/** {@inheritDoc} */
	public boolean isAfterEndOfLine() {
		return _delegate.isAfterEndOfLine();
	}


	/** {@inheritDoc} */
	@NotNull
	public String getDescriptionTemplate() {
		return _delegate.getDescriptionTemplate();
	}


	/** {@inheritDoc} */
	@Nullable
	public QuickFix<?>[] getFixes() {
		return _delegate.getFixes();
	}


	public void setFile(final VirtualFile file) {
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


	public VirtualFile getFile() {
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
