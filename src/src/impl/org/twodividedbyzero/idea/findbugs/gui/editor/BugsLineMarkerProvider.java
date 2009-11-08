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

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.GroupTreeModel;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;

import javax.swing.Icon;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.92
 */
public class BugsLineMarkerProvider implements LineMarkerProvider {

	@Nullable
	public LineMarkerInfo<?> getLineMarkerInfo(final PsiElement psiElement) {
		final PsiFile psiFile = IdeaUtilImpl.getPsiFile(psiElement);
		final Map<PsiFile, List<ExtendedProblemDescriptor>> problemCache = GroupTreeModel.getProblemCache();
		if (problemCache.containsKey(psiFile)) {
			final List<ExtendedProblemDescriptor> descriptors = problemCache.get(psiFile);
			for (final ExtendedProblemDescriptor problemDescriptor : descriptors) {


				final Icon icon = getIcon();
				if (icon != null) {
					final GutterIconNavigationHandler<PsiElement> navHandler = new GutterIconNavigationHandler<PsiElement>() {
						public void navigate(final MouseEvent e, final PsiElement psiElement) {
							//psiFileSystemItem.navigate(true);
						}
					};
					return new LineMarkerInfo<PsiElement>(problemDescriptor.getPsiElement(),
														  problemDescriptor.getLineStart(),
														  icon,
														  4,
														  null,
														  navHandler,
														  GutterIconRenderer.Alignment.LEFT);
				}

			}

		}

		return null;
	}


	public void collectSlowLineMarkers(final List<PsiElement> elements, final Collection<LineMarkerInfo> result) {
	}


	/*@Nullable
	public GutterIconRenderer createGutterRenderer() {
		if (myIcon == null) {
			return null;
		}
		return new LineMarkerGutterIconRenderer<T>(this);
	}


	@Nullable
	public String getLineMarkerTooltip() {
		T element = getElement();
		if (element == null || !element.isValid()) {
			return null;
		}
		if (myTooltipProvider != null) {
			return myTooltipProvider.fun(element);
		}
		return null;
	}*/


	@Nullable
	private static Icon getIcon() {
		return GuiResources.FINDBUGS_ICON;
	}
}
