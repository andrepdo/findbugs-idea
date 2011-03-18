/*
 * Copyright 2010 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.gui.editor;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInspection.SuppressIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Function;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.Detector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.event.EventListener;
import org.twodividedbyzero.idea.findbugs.common.event.EventManagerImpl;
import org.twodividedbyzero.idea.findbugs.common.event.filters.BugReporterEventFilter;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent;
import org.twodividedbyzero.idea.findbugs.common.util.BugInstanceUtil;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;

import javax.swing.Icon;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.92
 */
public class BugsLineMarkerProvider implements LineMarkerProvider, EventListener<BugReporterEvent>/*, DumbAware*/ {

	private Map<PsiFile, List<ExtendedProblemDescriptor>> _problemCache;
	private boolean _analysisRunning;
	private boolean _isRegistered;


	public BugsLineMarkerProvider() {
		_analysisRunning = false;
		_isRegistered = false;
	}


	@Nullable
	public LineMarkerInfo<?> getLineMarkerInfo(final PsiElement psiElement) {
		if(!_isRegistered) {
			EventManagerImpl.getInstance().addEventListener(new BugReporterEventFilter(psiElement.getProject().getName()), this);
			_isRegistered = true;
		}
		if(_analysisRunning) {
			return null;
		}

		final PsiFile psiFile = IdeaUtilImpl.getPsiFile(psiElement);
		_problemCache = IdeaUtilImpl.getPluginComponent(psiElement.getProject()).getProblems();

		if (_problemCache.containsKey(psiFile)) {
			final List<ExtendedProblemDescriptor> matchingDescriptors = new ArrayList<ExtendedProblemDescriptor>();
			final List<ExtendedProblemDescriptor> descriptors = _problemCache.get(psiFile);
			for (final ExtendedProblemDescriptor problemDescriptor : descriptors) {

				final PsiElement problemPsiElement = problemDescriptor.getPsiElement();
				if (psiElement.equals(problemPsiElement)) {
					matchingDescriptors.add(problemDescriptor);
					if(psiElement instanceof PsiAnonymousClass) {
						//final Editor[] editors = com.intellij.openapi.editor.EditorFactory.getInstance().getEditors(IdeaUtilImpl.getDocument(psiFile.getProject(), problemDescriptor));
						//editors[0].getMarkupModel().addRangeHighlighter()
					}
				}
			}
			if (!matchingDescriptors.isEmpty()) {
				final GutterIconNavigationHandler<PsiElement> navHandler = new BugGutterIconNavigationHandler(psiElement, matchingDescriptors);
				return new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange().getStartOffset(), getIcon(matchingDescriptors.get(0)), 4, new TooltipProvider(matchingDescriptors), navHandler, GutterIconRenderer.Alignment.LEFT);
			}
		}

		return null;
	}


	public void collectSlowLineMarkers(final List<PsiElement> elements, final Collection<LineMarkerInfo> result) {
	}


	@edu.umd.cs.findbugs.annotations.SuppressWarnings({"DB_DUPLICATE_SWITCH_CLAUSES"})
	@Nullable
	private static Icon getIcon(final ExtendedProblemDescriptor problemDescriptor) {
		final BugInstance bugInstance = problemDescriptor.getBugInstance();
		final int priority = bugInstance.getPriority();
		final Icon icon;
		switch (priority) {
			case Detector.HIGH_PRIORITY :
				icon = GuiResources.PRIORITY_HIGH_ICON;
				break;
			case Detector.NORMAL_PRIORITY :
				icon = GuiResources.PRIORITY_NORMAL_ICON;
				break;
			case Detector.LOW_PRIORITY :
				icon = GuiResources.PRIORITY_LOW_ICON;
				break;
			case Detector.EXP_PRIORITY :
				icon = GuiResources.PRIORITY_EXP_ICON;
				break;
			case Detector.IGNORE_PRIORITY :
			default:
				icon = GuiResources.PRIORITY_HIGH_ICON;
			break;

		}
		return icon;
	}


	public void onEvent(@NotNull final BugReporterEvent event) {
		switch (event.getOperation()) {

			case ANALYSIS_STARTED:
				_analysisRunning = true;
				break;
			case ANALYSIS_ABORTED:
			case ANALYSIS_FINISHED:
				_analysisRunning = false;
				break;
			case NEW_BUG_INSTANCE:
				break;
			default:
		}
	}


	private static class BugGutterIconNavigationHandler implements GutterIconNavigationHandler<PsiElement> {

		private final List<ExtendedProblemDescriptor> _descriptors;
		private final PsiElement _psiElement;


		private BugGutterIconNavigationHandler(final PsiElement psiElement, final List<ExtendedProblemDescriptor> descriptors) {
			_descriptors = descriptors;
			_psiElement = psiElement;
			buildPopupMenu();
		}


		@SuppressWarnings({"AnonymousInnerClass", "AnonymousInnerClassMayBeStatic"})
		private JBPopup buildPopupMenu() {
			final List<SuppressIntentionAction> intentionActions = new ArrayList<SuppressIntentionAction>(_descriptors.size());

			for (final ExtendedProblemDescriptor problemDescriptor : _descriptors) {
				intentionActions.add(new AddSuppressReportBugFix(problemDescriptor));
				intentionActions.add(new AddSuppressReportBugForClassFix(problemDescriptor));
				intentionActions.add(new ClearBugIntentionAction(problemDescriptor));
				intentionActions.add(new ClearAndSuppressBugAction(problemDescriptor));
			}

			final JBPopupFactory factory = JBPopupFactory.getInstance();
			return factory.createListPopup(new BaseListPopupStep<SuppressIntentionAction>("FindBugs-IDEA", intentionActions) {
				@Override
				public PopupStep<?> onChosen(final SuppressIntentionAction selectedValue, final boolean finalChoice) {
					final Project project = _psiElement.getProject();
					final Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
					ApplicationManager.getApplication().runWriteAction(new Runnable() {
						public void run() {
							selectedValue.invoke(project, editor, _psiElement);		
						}
					});
					return super.onChosen(selectedValue, finalChoice);
				}
			});
		}


		public void navigate(final MouseEvent e, final PsiElement psiElement) {
			// todo: goto tree node - fb result panel view
			buildPopupMenu().showInScreenCoordinates(e.getComponent(), e.getPoint());
		}

	}

	private static class TooltipProvider implements Function<PsiElement, String> {

		private final List<ExtendedProblemDescriptor> _problemDescriptors;
		private static final Pattern PATTERN = Pattern.compile("\n");


		private TooltipProvider(final List<ExtendedProblemDescriptor> problemDescriptors) {
			_problemDescriptors = problemDescriptors;
		}


		public String fun(final PsiElement psiElement) {
			return getTooltipText(_problemDescriptors);
		}


		private static String getTooltipText(final List<ExtendedProblemDescriptor> problemDescriptors) {
			final StringBuilder buffer = new StringBuilder();
			buffer.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
			buffer.append("<HTML><HEAD><TITLE>");

			for (int i = 0, problemDescriptorsSize = problemDescriptors.size(); i < problemDescriptorsSize; i++) {
				final ExtendedProblemDescriptor problemDescriptor = problemDescriptors.get(i);
				buffer.append("");
				buffer.append("</TITLE></HEAD><BODY><H3>");
				buffer.append(BugInstanceUtil.getBugPatternShortDescription(problemDescriptor.getBugInstance()));
				buffer.append("</H3>");
				buffer.append(PATTERN.matcher(BugInstanceUtil.getDetailText(problemDescriptor.getBugInstance())).replaceAll(""));
				if (i < problemDescriptors.size() - 1) {
					buffer.append("<HR>");
				}

			}

			buffer.append("</BODY></HTML>");
			return buffer.toString();
		}


		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("TooltipProvider");
			sb.append("{_problemDescriptor=").append(_problemDescriptors);
			sb.append('}');
			return sb.toString();
		}
	}
}
