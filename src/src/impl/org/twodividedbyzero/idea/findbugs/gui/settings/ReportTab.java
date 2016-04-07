/*
 * Copyright 2016 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.gui.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.gui.common.HAlignment;
import org.twodividedbyzero.idea.findbugs.gui.common.VAlignment;
import org.twodividedbyzero.idea.findbugs.gui.common.VerticalFlowLayout;

import javax.swing.JPanel;
import java.awt.BorderLayout;

final class ReportTab extends JPanel implements SettingsOwner<AbstractSettings> {

	private AnalysisEffortPane analysisEffort;
	private MinRankPane minRank;
	private MinPriorityPane minPriority;
	private BugCategoryPane bugCategory;

	ReportTab() {
		super(new VerticalFlowLayout(HAlignment.Left, VAlignment.Top, 0, UIUtil.DEFAULT_HGAP, true, false));
		add(getAnalysisEffort());
		add(getMinRank());
		add(getMinPriority());

		final JPanel bottomPane = new JPanel(new BorderLayout());
		bottomPane.add(getBugCategory(), BorderLayout.NORTH);
		add(bottomPane);
	}

	@NotNull
	private AnalysisEffortPane getAnalysisEffort() {
		if (analysisEffort == null) {
			analysisEffort = new AnalysisEffortPane(140);
		}
		return analysisEffort;
	}

	@NotNull
	private MinRankPane getMinRank() {
		if (minRank == null) {
			minRank = new MinRankPane(140);
		}
		return minRank;
	}

	@NotNull
	private MinPriorityPane getMinPriority() {
		if (minPriority == null) {
			minPriority = new MinPriorityPane(140);
		}
		return minPriority;
	}

	@NotNull
	BugCategoryPane getBugCategory() {
		if (bugCategory == null) {
			bugCategory = new BugCategoryPane();
		}
		return bugCategory;
	}

	@Override
	public boolean isModified(@NotNull final AbstractSettings settings) {
		return getAnalysisEffort().isModified(settings) ||
				getMinRank().isModified(settings) ||
				getMinPriority().isModified(settings) ||
				getBugCategory().isModified(settings);
	}

	@Override
	public void apply(@NotNull final AbstractSettings settings) throws ConfigurationException {
		getAnalysisEffort().apply(settings);
		getMinRank().apply(settings);
		getMinPriority().apply(settings);
		getBugCategory().apply(settings);
	}

	@Override
	public void reset(@NotNull final AbstractSettings settings) {
		getAnalysisEffort().reset(settings);
		getMinRank().reset(settings);
		getMinPriority().reset(settings);
		getBugCategory().reset(settings);
	}
}
