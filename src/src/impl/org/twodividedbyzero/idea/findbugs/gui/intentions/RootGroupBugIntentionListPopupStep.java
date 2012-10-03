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


/**
 * $Date$
 *
 * @author Andre Pfeiler<andre.pfeiler@gmail.com>
 * @version $Revision$
 * @since 0.9.97
 */
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
		// todo: combined icon
		return hasSubstep(aValue) ?  GuiUtil.getCombinedIcon(aValue.getIntentionActions().get(0).getProblemDescriptor()) : aValue.getIcon(-1);
	}


	public Icon getIcon(final int i) {
		return GuiResources.FINDBUGS_ICON;
	}
}
