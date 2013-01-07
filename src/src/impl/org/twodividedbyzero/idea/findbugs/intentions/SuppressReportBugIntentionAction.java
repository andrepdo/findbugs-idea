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
package org.twodividedbyzero.idea.findbugs.intentions;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.SuppressIntentionAction;
import com.intellij.codeInspection.SuppressManager;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.source.jsp.jspJava.JspHolderMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
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
@SuppressWarnings({"RedundantInterfaceDeclaration"})
@edu.umd.cs.findbugs.annotations.SuppressWarnings({"RI_REDUNDANT_INTERFACES"})
public class SuppressReportBugIntentionAction extends SuppressIntentionAction implements Iconable {

	private final String _bugPatternId;
	private String _key;
	private final String _suppressWarningsClassName;
	private final ExtendedProblemDescriptor _problemDescriptor;


	public SuppressReportBugIntentionAction(final ExtendedProblemDescriptor problemDescriptor) {
		_problemDescriptor = problemDescriptor;
		_bugPatternId = getBugId(problemDescriptor);

		final Project project = IdeaUtilImpl.getProject(problemDescriptor.getPsiFile());
		final FindBugsPreferences preferences = IdeaUtilImpl.getPluginComponent(project).getPreferences();
		_suppressWarningsClassName = preferences.getProperty(FindBugsPreferences.ANNOTATION_SUPPRESS_WARNING_CLASS);
	}


	@SuppressWarnings({"override", "HardcodedFileSeparator"}) // idea 8 compatibility
	public Icon getIcon(final int flags) {
		return ResourcesLoader.loadIcon("intentions/inspectionsOff.png");
		//return GuiUtil.getIcon(_problemDescriptor);
	}


	protected static String getBugId(final ExtendedProblemDescriptor problemDescriptor) {
		return problemDescriptor.getBugInstance().getBugPattern().getType();
	}


	@Override
	@NotNull
	public String getText() {
		return ResourcesLoader.getString("findbugs.inspection.quickfix.suppress.warning") + " '" + _bugPatternId + '\'';
	}


	@NotNull
	public String getFamilyName() {
		return ResourcesLoader.getString("findbugs.inspection.quickfix.bug.pattern") + " '" + _bugPatternId + '\'';
	}


	@Nullable
	protected PsiDocCommentOwner getContainer(final PsiElement context) {
		if (context == null || !context.getManager().isInProject(context)) {
			return null;
		}
		final PsiFile containingFile = context.getContainingFile();
		if (containingFile == null) {
			// for PsiDirectory
			return null;
		}
		if (!containingFile.getLanguage().isKindOf(StdLanguages.JAVA) || context instanceof PsiFile) {
			return null;
		}
		PsiElement container = context;
		while (container instanceof PsiAnonymousClass || !(container instanceof PsiDocCommentOwner) || container instanceof PsiTypeParameter) {
			container = PsiTreeUtil.getParentOfType(container, PsiDocCommentOwner.class);
			if (container == null) {
				return null;
			}
		}
		return (PsiDocCommentOwner) container;
	}


	@Override
	@SuppressWarnings({"SimplifiableIfStatement"})
	public boolean isAvailable(@NotNull final Project project, final Editor editor, @Nullable final PsiElement context) {
		final PsiDocCommentOwner container = getContainer(context);
		_key = container instanceof PsiClass ? "suppress.inspection.class" : container instanceof PsiMethod ? "suppress.inspection.method" : "suppress.inspection.field";
		final boolean isValid = container != null && !(container instanceof JspHolderMethod);
		if (!isValid) {
			return false;
		}
		return context != null && context.getManager().isInProject(context);
	}





	@Override
	public void invoke(@NotNull final Project project, final Editor editor, @NotNull final PsiElement element) throws IncorrectOperationException {
		/*final IntentionsInfo intentionsInfo = new IntentionsInfo();
		intentionsInfo.filterActions();
		IntentionHintComponent.showIntentionHint(project, element.getContainingFile(), editor, intentionsInfo, true);*/
		final PsiDocCommentOwner container = getContainer(element);
		assert container != null;
		if (!CodeInsightUtilBase.preparePsiElementForWrite(container)) {
			return;
		}
		@SuppressWarnings({"ConstantConditions"})
		final ReadonlyStatusHandler.OperationStatus status = ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(container.getContainingFile().getVirtualFile());
		if (status.hasReadonlyFiles()) {
			return;
		}
		if (use15Suppressions(container)) {
			final PsiModifierList modifierList = container.getModifierList();
			if (modifierList != null) {
				addSuppressAnnotation(project, editor, container, modifierList, getID(container));
			}
		} else {
			PsiDocComment docComment = container.getDocComment();
			final PsiManager manager = PsiManager.getInstance(project);
			if (docComment == null) {
				/*final String commentText = "*//** @" + SUPPRESS_INSPECTIONS_TAG_NAME + ' ' + getID(container) + "*//*";
				docComment = JavaPsiFacade.getInstance(manager.getProject()).getElementFactory().createDocCommentFromText(commentText, null);
				final PsiElement firstChild = container.getFirstChild();
				container.addBefore(docComment, firstChild);*/
			} else {
				/*final PsiDocTag noInspectionTag = docComment.findTagByName(SUPPRESS_INSPECTIONS_TAG_NAME);
				if (noInspectionTag != null) {
					final PsiDocTagValue valueElement = noInspectionTag.getValueElement();
					final String tagText = '@' + SUPPRESS_INSPECTIONS_TAG_NAME + ' ' + (valueElement != null ? valueElement.getText() + ',' : "") + getID(container);
					noInspectionTag.replace(JavaPsiFacade.getInstance(manager.getProject()).getElementFactory().createDocTagFromText(tagText, null));

					addImport(project, element);

				} else {
					final String tagText = '@' + SUPPRESS_INSPECTIONS_TAG_NAME + ' ' + getID(container);
					docComment.add(JavaPsiFacade.getInstance(manager.getProject()).getElementFactory().createDocTagFromText(tagText, null));

					addImport(project, element);
				}*/
			}
		}
		final Map<PsiFile, List<ExtendedProblemDescriptor>> problems = IdeaUtilImpl.getPluginComponent(project).getProblems();
		problems.get(element.getContainingFile()).remove(getProblemDescriptor());
		DaemonCodeAnalyzer.getInstance(project).restart();
	}


	private void addImport(final Project project, final PsiElement element) {
		/*final PsiFile file = element.getContainingFile();
		if (file instanceof PsiJavaFile) {
			final PsiImportList list = ((PsiJavaFile) file).getImportList();
			
			if (list != null && list.findSingleClassImportStatement(_suppressWarningsName) == null && list.findOnDemandImportStatement(_suppressWarningsName.substring(0, _suppressWarningsName.lastIndexOf('.'))) == null) {
				final PsiElementFactory elementFactory = JavaPsiFacade.getInstance(project).getElementFactory();
				final PsiImportStatement statement = elementFactory.createImportStatementOnDemand(_suppressWarningsName);
				list.add(statement);
			}
		}*/
	}


	public void addSuppressAnnotation(final Project project, final Editor editor, final PsiElement container, final PsiModifierList modifierList, final String id) throws IncorrectOperationException {
		PsiAnnotation annotation = modifierList.findAnnotation(_suppressWarningsClassName);
		if (annotation != null) {
			if (!annotation.getText().contains("{")) {
				final PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
				if (attributes.length == 1) {
					final String suppressedWarnings = attributes[0].getText();
					final PsiAnnotation newAnnotation = JavaPsiFacade.getInstance(project).getElementFactory().createAnnotationFromText('@' + _suppressWarningsClassName + "({" + suppressedWarnings + ", \"" + id + "\"})\n", container);
					annotation.replace(newAnnotation);
				}
			} else {
				final int curlyBraceIndex = annotation.getText().lastIndexOf('}');
				if (curlyBraceIndex > 0) {
					annotation.replace(JavaPsiFacade.getInstance(project).getElementFactory().createAnnotationFromText(annotation.getText().substring(0, curlyBraceIndex) + ", \"" + id + "\"})\n", container));
				} else if (!ApplicationManager.getApplication().isUnitTestMode() && editor != null) {
					Messages.showErrorDialog(editor.getComponent(), InspectionsBundle.message("suppress.inspection.annotation.syntax.error", annotation.getText()));
				}
			}
		} else {
			annotation = JavaPsiFacade.getInstance(project).getElementFactory().createAnnotationFromText('@' + _suppressWarningsClassName + "({\"" + id + "\"})\n", container);
			modifierList.addBefore(annotation, modifierList.getFirstChild());
			JavaCodeStyleManager.getInstance(project).shortenClassReferences(modifierList);

			if (needsImportStatement()) {
				addImport(project, container);
			}
		}
	}


	private boolean needsImportStatement() {
		final String result = _suppressWarningsClassName.substring(0, _suppressWarningsClassName.lastIndexOf('.'));
		return result.indexOf('.') != -1;
	}


	protected static boolean use15Suppressions(final PsiDocCommentOwner container) {
		return SuppressManager.getInstance().canHave15Suppressions(container) && !SuppressManager.getInstance().alreadyHas14Suppressions(container);
	}


	@Override
	public boolean startInWriteAction() {
		return true;
	}


	public String getID(final PsiElement place) {
		/*if (myAlternativeID != null) {
			final Module module = ModuleUtil.findModuleForPsiElement(place);
			if (module != null) {
				//if (!ClasspathStorage.getStorageType(module).equals(ClasspathStorage.DEFAULT_STORAGE)) {
					return myAlternativeID;
				//}
			}
		}*/

		return _bugPatternId;
	}


	public ExtendedProblemDescriptor getProblemDescriptor() {
		return _problemDescriptor;
	}


	public String getBugPatternId() {
		return _bugPatternId;
	}


	@Override
	public String toString() {
		return getText();
	}
}
