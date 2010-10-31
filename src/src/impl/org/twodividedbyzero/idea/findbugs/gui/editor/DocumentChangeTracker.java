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

package org.twodividedbyzero.idea.findbugs.gui.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


/**
 * $Date: 2010-10-10 16:30:08 +0200 (Sun, 10 Oct 2010) $
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision: 107 $
 * @since 0.9.92
 */
class DocumentChangeTracker implements DocumentListener {

	private static final Key<Object> ORPHAN_MARKER_KEY = Key.create("findbugs_idea.OrhpanMarker");

	private final Document _document;
	private final Set<Editor> _editors;
	private final Map<ExtendedProblemDescriptor, RangeMarker> _markers;


	DocumentChangeTracker(final Document document) {
		_document = document;

		_editors = new HashSet<Editor>();
		_markers = new IdentityHashMap<ExtendedProblemDescriptor, RangeMarker>();

		document.addDocumentListener(this);
	}


	@NotNull
	Set<Editor> getEditors() {
		return _editors;
	}


	@NotNull
	RangeMarker addMarker(@NotNull final ExtendedProblemDescriptor problem, final boolean orphanMarker) {
		final int lineStart = problem.getLineStart();
		final PsiElement element = IdeaUtilImpl.getElementAtLine(problem.getPsiFile(), lineStart);
		final RangeMarker marker;
		if (element != null) {
			marker = _document.createRangeMarker(element.getTextRange());
		} else {
			final int lineEnd = problem.getLineEnd();
			marker = _document.createRangeMarker(_document.getLineStartOffset(lineStart), _document.getLineEndOffset(lineEnd));
		}

		marker.putUserData(ORPHAN_MARKER_KEY, orphanMarker);
		_markers.put(problem, marker);

		return marker;
	}


	@Nullable
	RangeMarker getMarker(@NotNull final ExtendedProblemDescriptor problem) {
		return _markers.get(problem);
	}


	void removeMarker(@NotNull final ExtendedProblemDescriptor problem) {
		_markers.remove(problem);
	}


	boolean isOrphanMarker(final RangeMarker marker) {
		return Boolean.TRUE.equals(marker.getUserData(ORPHAN_MARKER_KEY));
	}


	public void beforeDocumentChange(final DocumentEvent event) {
	}


	public void documentChanged(final DocumentEvent event) {
		final int lineStart = _document.getLineNumber(event.getOffset());

		updateProblems(lineStart);
	}


	private void updateProblem(@NotNull final ExtendedProblemDescriptor problem, @NotNull final RangeMarker marker) {
		if (marker.isValid() && !isOrphanMarker(marker)) {
			final CharSequence fragment = marker.getDocument().getCharsSequence().subSequence(marker.getStartOffset(), marker.getEndOffset());
			final int lineStart = marker.getDocument().getLineNumber(marker.getStartOffset());
			int lineEnd = marker.getDocument().getLineNumber(marker.getEndOffset());

			if (StringUtil.endsWith(fragment, "\n")) {
				lineEnd--;
			}

			problem.setLineStart(lineStart);
			problem.setLineEnd(lineEnd);
		}
	}


	private void updateProblems(final int lineStart) {
		final Map<ExtendedProblemDescriptor, RangeMarker> markersCopy = new HashMap<ExtendedProblemDescriptor, RangeMarker>(_markers);
		for (final Map.Entry<ExtendedProblemDescriptor, RangeMarker> entry : markersCopy.entrySet()) {
			final ExtendedProblemDescriptor issue = entry.getKey();
			final RangeMarker marker = entry.getValue();
			if (issue.getLineStart() >= lineStart) {
				updateProblem(issue, marker);
			}
		}
	}


	void release() {
		_document.removeDocumentListener(this);
	}
}
