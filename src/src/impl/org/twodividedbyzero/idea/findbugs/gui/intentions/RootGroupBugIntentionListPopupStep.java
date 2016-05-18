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

import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Iconable;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.util.GuiUtil;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.Icon;
import java.util.List;

public class RootGroupBugIntentionListPopupStep extends BaseListPopupStep<GroupBugIntentionListPopupStep> implements Iconable {

	public RootGroupBugIntentionListPopupStep(final List<GroupBugIntentionListPopupStep> intentionGroups) {
		super(FindBugsPluginConstants.PLUGIN_NAME, intentionGroups);
	}


	@Override
	public PopupStep<?> onChosen(final GroupBugIntentionListPopupStep selectedValue, final boolean finalChoice) {
		return selectedValue;
	}


	@Override
	public boolean hasSubstep(final GroupBugIntentionListPopupStep selectedValue) {
		return selectedValue != null && !selectedValue.getIntentionActions().isEmpty();
	}


	@NotNull
	@Override
	public String getTextFor(final GroupBugIntentionListPopupStep value) {
		return hasSubstep(value) ? ResourcesLoader.getString("findbugs.inspection.quickfix.bug.pattern") + " '" + value.getIntentionActions().get(0).getBugPatternId() + "\'..." : value.getTitle();
	}


	@Override
	public boolean isSelectable(final GroupBugIntentionListPopupStep value) {
		return true;
	}


	@Override
	public Icon getIconFor(final GroupBugIntentionListPopupStep aValue) {
		// FIXME: combined icon
		return hasSubstep(aValue) ? GuiUtil.getCombinedIcon(aValue.getIntentionActions().get(0).getProblemDescriptor()) : aValue.getIcon(-1);
	}


	public Icon getIcon(final int i) {
		return GuiResources.FINDBUGS_ICON;
	}
}
