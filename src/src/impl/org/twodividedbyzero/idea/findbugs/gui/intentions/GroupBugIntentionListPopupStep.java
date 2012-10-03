/*
 * Copyright 2012 Andre Pfeiler
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

package org.twodividedbyzero.idea.findbugs.gui.intentions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.intentions.SuppressReportBugIntentionAction;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andre.pfeiler@gmail.com>
 * @version $Revision$
 * @since 0.9.97
 */
public class GroupBugIntentionListPopupStep extends BaseListPopupStep<SuppressReportBugIntentionAction> implements Iconable {


	private final List<SuppressReportBugIntentionAction> _intentionActions;
	private final PsiElement _psiElement;


	public GroupBugIntentionListPopupStep(final PsiElement psiElement, final List<SuppressReportBugIntentionAction> intentionActions) {
		super(intentionActions.get(0).getText(), intentionActions);
		_psiElement = psiElement;
		_intentionActions = new ArrayList<SuppressReportBugIntentionAction>(intentionActions);
	}


	public List<SuppressReportBugIntentionAction> getIntentionActions() {
		return _intentionActions;
	}


	@Override
	public PopupStep<?> onChosen(final SuppressReportBugIntentionAction selectedValue, final boolean finalChoice) {
		final Project project = _psiElement.getProject();
		final Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			public void run() {
				selectedValue.invoke(project, editor, _psiElement);
			}
		});
		return super.onChosen(selectedValue, finalChoice);
	}


	@Override
	public boolean hasSubstep(final SuppressReportBugIntentionAction selectedValue) {
		return false;
	}


	@NotNull
	@Override
	public String getTextFor(final SuppressReportBugIntentionAction value) {
		return value.getText(); // todo:
	}


	@SuppressWarnings({"override"}) // idea 8 compatibility
	public Icon getIcon(final int flags) {
		//return ResourcesLoader.loadIcon("intentions/inspectionsOff.png");
		return _intentionActions.get(0).getIcon(flags);
	}


	@Override
	public Icon getIconFor(final SuppressReportBugIntentionAction aValue) {
		//return ResourcesLoader.loadIcon("intentions/inspectionsOff.png");
		return aValue.getIcon(-1);
		//return GuiUtil.getIcon(aValue.getProblemDescriptor());
		//return aValue.getIcon(-1);
	}
}
