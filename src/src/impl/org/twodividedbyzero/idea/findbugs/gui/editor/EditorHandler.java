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

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * $Date: 2010-10-10 16:30:08 +0200 (Sun, 10 Oct 2010) $
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision: 107 $
 * @since 0.9.92
 */
public class EditorHandler implements ProjectComponent {

	private final Project _project;
	private final Map<VirtualFile, DocumentChangeTracker> _changeTrackers;
	private final Map<Editor, Map<Integer, CustomGutterIconRenderer>> _renderers;
	private final Map<Editor, Map<ExtendedProblemDescriptor, RangeHighlighter>> _highlighters;
	private final EditorFactoryListener _editorFactoryListener;


	public EditorHandler(final Project project) {
		_project = project;

		_highlighters = new HashMap<Editor, Map<ExtendedProblemDescriptor, RangeHighlighter>>();
		_renderers = new HashMap<Editor, Map<Integer, CustomGutterIconRenderer>>();
		_changeTrackers = new HashMap<VirtualFile, DocumentChangeTracker>();

		// Listeners
		_editorFactoryListener = new CustomEditorFactoryListener();
	}


	public void projectOpened() {
		EditorFactory.getInstance().addEditorFactoryListener(_editorFactoryListener);
	}


	public void projectClosed() {
		EditorFactory.getInstance().removeEditorFactoryListener(_editorFactoryListener);
	}


	@NotNull
	public String getComponentName() {
		return FindBugsPluginConstants.PLUGIN_ID + "#EditorHandler";
	}


	public void initComponent() {
		//EditorFactory.getInstance().addEditorFactoryListener(_editorFactoryListener);
		//final FindBugsPlugin findBugsPlugin = _project.getComponent(FindBugsPlugin.class);
	}


	public void disposeComponent() {
	}


	@Nullable
	private RangeMarker findMarker(@NotNull final ExtendedProblemDescriptor problem) {
		final DocumentChangeTracker documentChangeTracker = _changeTrackers.get(problem.getFile().getVirtualFile());
		if (documentChangeTracker == null) {
			return null;
		}

		return documentChangeTracker.getMarker(problem);
	}


	private RangeMarker addMarker(@Nullable final Editor editor, @NotNull final ExtendedProblemDescriptor problem, final boolean orphanMarker) {
		if (problem.getLineStart() == -1) {
			return null;
		}

		final DocumentChangeTracker documentChangeTracker = _changeTrackers.get(problem.getFile().getVirtualFile());
		if (documentChangeTracker == null) {
			return null;
		}

		final RangeMarker marker = documentChangeTracker.addMarker(problem, orphanMarker);
		if (editor == null) {
			for (final Editor editor2 : documentChangeTracker.getEditors()) {
				installHightlighters(editor2, problem, marker);
			}
		} else {
			installHightlighters(editor, problem, marker);
		}

		return marker;
	}


	private void installHightlighters(@NotNull final Editor editor, @NotNull final ExtendedProblemDescriptor problem, @NotNull final RangeMarker marker) {
		Map<Integer, CustomGutterIconRenderer> editorRenderers = _renderers.get(editor);
		if (editorRenderers == null) {
			editorRenderers = new HashMap<Integer, CustomGutterIconRenderer>();
			_renderers.put(editor, editorRenderers);
		}

		Map<ExtendedProblemDescriptor, RangeHighlighter> editorHighlighters = _highlighters.get(editor);
		if (editorHighlighters == null) {
			editorHighlighters = new IdentityHashMap<ExtendedProblemDescriptor, RangeHighlighter>();
			_highlighters.put(editor, editorHighlighters);
		}

		final RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(marker.getStartOffset(), marker.getEndOffset(), HighlighterLayer.FIRST - 1, null, HighlighterTargetArea.LINES_IN_RANGE);
		editorHighlighters.put(problem, highlighter);
		EditorFactory.getInstance().refreshAllEditors(); // todo: find non crude way= LineMarkerHighlighter

		// Gutter renderer, only one renderer for same line start
		CustomGutterIconRenderer renderer = editorRenderers.get(problem.getLineStart());
		if (renderer == null) {
			renderer = new CustomGutterIconRenderer(this, problem.getLineStart());
			editorRenderers.put(problem.getLineStart(), renderer);

			// Only set gutter icon for first highlighter with same line start
			highlighter.setGutterIconRenderer(renderer);
		}

		renderer.addProblem(problem, highlighter);
	}


	private void removeMarker(final ExtendedProblemDescriptor problem) {
		final DocumentChangeTracker documentChangeTracker = _changeTrackers.get(problem.getFile().getVirtualFile());
		if (documentChangeTracker == null) {
			return;
		}

		documentChangeTracker.removeMarker(problem);
		for (final Editor editor : documentChangeTracker.getEditors()) {
			final Map<ExtendedProblemDescriptor, RangeHighlighter> editorHighlighters = _highlighters.get(editor);
			if (editorHighlighters != null) {
				final RangeHighlighter highlighter = editorHighlighters.remove(problem);

				if (highlighter != null) {
					editor.getMarkupModel().removeHighlighter(highlighter);

					final Map<Integer, CustomGutterIconRenderer> editorRenderers = _renderers.get(editor);
					final CustomGutterIconRenderer renderer = editorRenderers.get(problem.getLineStart());
					if (renderer != null) {
						renderer.removeProblem(problem);
						if (renderer.isEmpty()) {
							editorRenderers.remove(renderer.getLineStart());
						} else if (highlighter.getGutterIconRenderer() != null) {
							// Reaffecct share gutter icon to first remaining highlighter
							final RangeHighlighter marker = (RangeHighlighter) renderer.getProblems().values().iterator().next();
							marker.setGutterIconRenderer(renderer);
						}
					}
				}
			}
		}
	}


	public boolean isSynchronized(@NotNull final ExtendedProblemDescriptor problem, final boolean checkEvenIfEditorNotAvailable) {
		if (problem.getLineStart() == -1) {
			return true;
		}

		RangeMarker marker = findMarker(problem);
		if (marker == null && checkEvenIfEditorNotAvailable) {
			final Document document = IdeaUtilImpl.getDocument(_project, problem);
			if (document != null) {
				marker = document.createRangeMarker(document.getLineStartOffset(problem.getLineStart()), document.getLineEndOffset(problem.getLineEnd()));
			}
		}

		return isSynchronized(problem, marker);
	}


	public boolean isSynchronized(@NotNull final ExtendedProblemDescriptor problem, @Nullable final RangeMarker marker) {
		if (problem.getLineStart() == -1) {
			return true;
		}

		if (marker == null) {
			return true;
		}

		if (!marker.isValid()) {
			return false;
		}

		final CharSequence fragment = marker.getDocument().getCharsSequence().subSequence(marker.getStartOffset(), marker.getEndOffset());

		return problem.getHash() == fragment.toString().hashCode();
	}


	public int buildNewHash(final ExtendedProblemDescriptor problem) {
		final RangeMarker marker = findMarker(problem);
		if (marker == null) {
			return 0;
		}

		final CharSequence fragment = marker.getDocument().getCharsSequence().subSequence(marker.getStartOffset(), marker.getEndOffset());

		return fragment.toString().hashCode();
	}


	private class CustomEditorFactoryListener implements EditorFactoryListener {

		public void editorCreated(final EditorFactoryEvent event) {
			final Editor editor = event.getEditor();
			final Project project = event.getEditor().getProject();

			final VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
			if (vFile == null) {
				return;
			}

			DocumentChangeTracker documentChangeTracker = _changeTrackers.get(vFile);
			if (documentChangeTracker == null) {
				documentChangeTracker = new DocumentChangeTracker(editor.getDocument());
				_changeTrackers.put(vFile, documentChangeTracker);
			}
			documentChangeTracker.getEditors().add(editor);

			final PsiFile psiFile = IdeaUtilImpl.getPsiFile(project, vFile);
			final Map<PsiFile, List<ExtendedProblemDescriptor>> problems = IdeaUtilImpl.getPluginComponent(project).getProblems();
			if (problems.containsKey(psiFile)) {
				for (final ExtendedProblemDescriptor problemDescriptor : problems.get(psiFile)) {
					addMarker(editor, problemDescriptor, false);
				}

			}
			//((FindBugsPluginImpl) IdeaUtilImpl.getPluginComponent(_project)).getBugCollection()
		}


		public void editorReleased(final EditorFactoryEvent event) {
			final Editor editor = event.getEditor();
			final Project project = event.getEditor().getProject();
			final VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
			if (vFile == null) {
				return;
			}

			final DocumentChangeTracker documentChangeTracker = _changeTrackers.get(vFile);
			if (documentChangeTracker != null) {
				final Set<Editor> editors = documentChangeTracker.getEditors();
				editors.remove(editor);
				if (editors.isEmpty()) {
					_changeTrackers.remove(vFile);
					documentChangeTracker.release();
				}
			}

			_renderers.remove(editor);
			_highlighters.remove(editor);

			final PsiFile psiFile = IdeaUtilImpl.getPsiFile(project, vFile);
			final Map<PsiFile, List<ExtendedProblemDescriptor>> problems = IdeaUtilImpl.getPluginComponent(project).getProblems();
			if (problems.containsKey(psiFile)) {
				for (final ExtendedProblemDescriptor problemDescriptor : problems.get(psiFile)) {
					removeMarker(problemDescriptor);
				}

			}
		}


	}

}
