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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.92
 */
class DocumentChangeTracker implements DocumentListener {

	private static final Key<Object> ORPHAN_MARKER_KEY = Key.create("findbugs_idea.OrhpanMarker");

	private final Document _document;
	private final Set<Editor> _editors;
	private final Map<ExtendedProblemDescriptor, RangeMarker> _markers;


	public DocumentChangeTracker(final Document document) {
		this._document = document;

		_editors = new HashSet<Editor>();
		_markers = new IdentityHashMap<ExtendedProblemDescriptor, RangeMarker>();

		document.addDocumentListener(this);
	}


	@NotNull
	Set<Editor> getEditors() {
		return _editors;
	}


	@NotNull
	RangeMarker addMarker(@NotNull final ExtendedProblemDescriptor issue, final boolean orphanMarker) {
		final RangeMarker marker = _document.createRangeMarker(_document.getLineStartOffset(issue.getLineStart()), _document.getLineEndOffset(issue.getLineEnd()));

		marker.putUserData(ORPHAN_MARKER_KEY, orphanMarker);
		_markers.put(issue, marker);

		return marker;
	}


	@Nullable
	RangeMarker getMarker(@NotNull final ExtendedProblemDescriptor issue) {
		return _markers.get(issue);
	}


	void removeMarker(@NotNull final ExtendedProblemDescriptor issue) {
		_markers.remove(issue);
	}


	boolean isOrphanMarker(final RangeMarker marker) {
		return Boolean.TRUE.equals(marker.getUserData(ORPHAN_MARKER_KEY));
	}


	public void beforeDocumentChange(final DocumentEvent event) {
	}


	public void documentChanged(final DocumentEvent event) {
		final int lineStart = _document.getLineNumber(event.getOffset());

		updateIssues(lineStart);
	}


	private void updateIssue(@NotNull final ExtendedProblemDescriptor issue, @NotNull final RangeMarker marker) {
		if ((marker.isValid()) && (!isOrphanMarker(marker))) {
			final CharSequence fragment = marker.getDocument().getCharsSequence().subSequence(marker.getStartOffset(), marker.getEndOffset());
			final int lineStart = marker.getDocument().getLineNumber(marker.getStartOffset());
			int lineEnd = marker.getDocument().getLineNumber(marker.getEndOffset());

			if (StringUtil.endsWith(fragment, "\n")) {
				lineEnd--;
			}

			issue.setLineStart(lineStart);
			issue.setLineEnd(lineEnd);
		}

		//issue.getReview().fireIssueUpdated(issue);
	}


	private void updateIssues(final int lineStart) {
		final Map<ExtendedProblemDescriptor, RangeMarker> markersCopy = new HashMap<ExtendedProblemDescriptor, RangeMarker>(_markers);
		for (final Map.Entry<ExtendedProblemDescriptor, RangeMarker> entry : markersCopy.entrySet()) {
			final ExtendedProblemDescriptor issue = entry.getKey();
			final RangeMarker marker = entry.getValue();
			if (issue.getLineStart() >= lineStart) {
				updateIssue(issue, marker);
			}
		}
	}


	void release() {
		_document.removeDocumentListener(this);
	}
}
