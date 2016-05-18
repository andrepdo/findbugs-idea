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
package org.twodividedbyzero.idea.findbugs.gui.intentions;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.intentions.SuppressReportBugIntentionAction;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;

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
		ReadonlyStatusHandler.getInstance(project).ensureFilesWritable();


		new WriteCommandAction.Simple/*<Object>*/(project, "Add findbugs-idea Suppress warning", _psiElement.getContainingFile()) {

			@Override
			protected void run() throws Throwable {
				final Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
				selectedValue.invoke(project, editor, _psiElement);
			}
		}.execute();

		return super.onChosen(selectedValue, finalChoice);
	}


	@Override
	public boolean hasSubstep(final SuppressReportBugIntentionAction selectedValue) {
		return false;
	}


	@NotNull
	@Override
	public String getTextFor(final SuppressReportBugIntentionAction value) {
		return value.getText(); // FIXME:
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
