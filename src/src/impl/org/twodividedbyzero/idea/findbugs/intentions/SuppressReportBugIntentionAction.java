/*
 * Copyright 2008-2016 Andre Pfeiler
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

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.SuppressIntentionAction;
import com.intellij.codeInspection.SuppressManager;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.source.jsp.jspJava.JspHolderMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.util.FileModificationServiceUtil;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.ModuleSettings;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.ToolWindowPanel;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.Icon;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"RedundantInterfaceDeclaration"})
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"RI_REDUNDANT_INTERFACES"})
public class SuppressReportBugIntentionAction extends SuppressIntentionAction implements Iconable {

	private final String _bugPatternId;
	private String _key;
	private final ExtendedProblemDescriptor _problemDescriptor;

	public SuppressReportBugIntentionAction(final ExtendedProblemDescriptor problemDescriptor) {
		_problemDescriptor = problemDescriptor;
		_bugPatternId = getBugId(problemDescriptor);
	}

	@SuppressWarnings({"override", "HardcodedFileSeparator"}) // idea 8 compatibility
	public Icon getIcon(final int flags) {
		return ResourcesLoader.loadIcon("intentions/inspectionsOff.png");
		//return GuiUtil.getIcon(_problemDescriptor);
	}

	private static String getBugId(final ExtendedProblemDescriptor problemDescriptor) {
		return problemDescriptor.getBug().getInstance().getBugPattern().getType();
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
		if (!IdeaUtilImpl.isLanguageSupported(containingFile.getLanguage()) || context instanceof PsiFile) {
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

	/**
	 * Invoked by EDT.
	 *
	 * @param project ..
	 * @param editor  ..
	 * @param element ..
	 * @throws IncorrectOperationException ..
	 */
	@Override
	public void invoke(@NotNull final Project project, final Editor editor, @NotNull final PsiElement element) throws IncorrectOperationException {

		if (!element.getLanguage().isKindOf(JavaLanguage.INSTANCE)) {
			new Notification("FindBugs Missing Feature", "Not Supported", "Sorry, insert annotation not supported for this language.", NotificationType.INFORMATION).notify(project);
			/**
			 * FIXME Scala Plugin:
			 * org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
			 * crash with PsiManager and ScalaPsiManager#instance always return null
			 */
			return;
		}

		/*final IntentionsInfo intentionsInfo = new IntentionsInfo();
		intentionsInfo.filterActions();
		IntentionHintComponent.showIntentionHint(project, element.getContainingFile(), editor, intentionsInfo, true);*/
		final PsiDocCommentOwner container = getContainer(element);
		assert container != null;
		if (!FileModificationServiceUtil.preparePsiElementForWrite(container)) {
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
			Messages.showErrorDialog(editor.getComponent(), "Add suppress annotation is not supported for Java 1.3 and older", "Unsupported");
		}
		final ToolWindowPanel toolWindow = ToolWindowPanel.getInstance(project);
		final Map<PsiFile, List<ExtendedProblemDescriptor>> problems = toolWindow.getProblems();
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

	@SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
	@SuppressWarnings("HardcodedLineSeparator")
	private void addSuppressAnnotation(
			final Project project,
			final Editor editor,
			@NotNull final PsiElement container,
			@NotNull final PsiModifierList modifierList,
			final String id
	) throws IncorrectOperationException {

		final String suppressWarningsClassName = getSuppressWarningsClassName(container);
		PsiAnnotation annotation = modifierList.findAnnotation(suppressWarningsClassName);
		if (annotation != null) {

			String value = null;
			String justification = null;
			String name;
			final PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
			for (PsiNameValuePair attribute : attributes) {
				name = attribute.getName();
				if (name == null || "value".equalsIgnoreCase(name)) { // name is null if annotation use "simple" syntax (=only default value is specified)
					value = attribute.getLiteralValue();
					if (value == null) { // getLiteralValue return null if syntax use array {}
						//noinspection ConstantConditions
						value = attribute.getValue().getText();
						if (value.startsWith("{\"") && value.endsWith("\"}")) {
							value = value.substring(2, value.length() - 2);
						}
					}
				} else if ("justification".equalsIgnoreCase(name)) {
					justification = attribute.getLiteralValue();
				}
			}

			if (value != null) {
				// LATER: respect Java CodeStyle Space settings (because, at the moment, createAnnotationFromText is the only way to create an annotation)
				if (justification != null) {
					value = "value = {\"" + value + "\", \"" + id + "\"}";
					justification = ", justification = \"" + justification + "\"";
				} else {
					value = "{\"" + value + "\", \"" + id + "\"}";
					justification = "";
				}
				final String annotationText = '@' + suppressWarningsClassName + "(" + value + justification + ")\r\n";
				annotation.replace(createAnnotationFromText(project, annotationText, container));
			} else {
				if (!ApplicationManager.getApplication().isUnitTestMode() && editor != null) {
					Messages.showErrorDialog(editor.getComponent(), InspectionsBundle.message("suppress.inspection.annotation.syntax.error", annotation.getText()));
				}
			}

		} else {
			annotation = createAnnotationFromText(project, '@' + suppressWarningsClassName + "(\"" + id + "\")\r\n", container);
			modifierList.addBefore(annotation, modifierList.getFirstChild());
			JavaCodeStyleManager.getInstance(project).shortenClassReferences(modifierList);
			if (needsImportStatement(suppressWarningsClassName)) {
				addImport(project, container);
			}
		}
	}

	@NotNull
	private static PsiAnnotation createAnnotationFromText(final Project project, @NotNull @NonNls String annotationText, @Nullable PsiElement context) {
		return JavaPsiFacade.getInstance(project).getElementFactory().createAnnotationFromText(annotationText, context);
	}

	private boolean needsImportStatement(@NotNull final String suppressWarningsClassName) {
		final String result = suppressWarningsClassName.substring(0, suppressWarningsClassName.lastIndexOf('.'));
		return result.indexOf('.') != -1;
	}

	private static boolean use15Suppressions(final PsiDocCommentOwner container) {
		return SuppressManager.getInstance().canHave15Suppressions(container) && !SuppressManager.getInstance().alreadyHas14Suppressions(container);
	}

	@Override
	public boolean startInWriteAction() {
		return true;
	}

	private String getID(final PsiElement place) {
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

	@NotNull
	private String getSuppressWarningsClassName(@NotNull final PsiElement psiElement) {
		final VirtualFile file = psiElement.getContainingFile().getVirtualFile();
		final Module module = ModuleUtilCore.findModuleForFile(file, psiElement.getProject());
		if (module == null) {
			throw new IllegalStateException("No module found for " + file);
		}
		String ret = null;
		final ModuleSettings moduleSettings = ModuleSettings.getInstance(module);
		if (moduleSettings.overrideProjectSettings) {
			ret = moduleSettings.suppressWarningsClassName;
		}
		if (StringUtil.isEmptyOrSpaces(ret)) {
			ret = ProjectSettings.getInstance(psiElement.getProject()).suppressWarningsClassName;
		}
		if (StringUtil.isEmptyOrSpaces(ret)) {
			ret = FindBugsPluginConstants.DEFAULT_SUPPRESS_WARNINGS_CLASSNAME;
		}
		return ret;
	}

	@Override
	public String toString() {
		return getText();
	}
}
