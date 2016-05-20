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
import edu.umd.cs.findbugs.config.ProjectFilterSettings;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.gui.common.HAlignment;
import org.twodividedbyzero.idea.findbugs.gui.common.VAlignment;
import org.twodividedbyzero.idea.findbugs.gui.common.VerticalFlowLayout;
import org.twodividedbyzero.idea.findbugs.preferences.AnalysisEffort;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BorderLayout;

final class ReportTab extends JPanel implements SettingsOwner<AbstractSettings> {

	private AnalysisEffortPane analysisEffort;
	private MinRankPane minRank;
	private MinPriorityPane minPriority;
	private BugCategoryPane bugCategory;

	ReportTab() {
		super(new VerticalFlowLayout(HAlignment.Left, VAlignment.Top, 0, UIUtil.DEFAULT_HGAP, true, false));
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		add(getAnalysisEffort());
		add(getMinRank());
		add(getMinPriority());

		final JPanel bottomPane = new JPanel(new BorderLayout());
		bottomPane.add(getBugCategory(), BorderLayout.NORTH);
		add(bottomPane);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		getAnalysisEffort().setEnabled(enabled);
		getMinRank().setEnabled(enabled);
		getMinPriority().setEnabled(enabled);
		getBugCategory().setEnabled(enabled);
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

	@NotNull
	static String getSearchPath() {
		return ResourcesLoader.getString("settings.report");
	}

	@NotNull
	static String[] getSearchResourceKey() {
		return new String[]{

				// AnalysisEffortPane
				"effort.text",
				"effort.description",

				// MinRankPane
				"minRank.text",
				"minRank.description",

				// MinPriorityPane
				"minPriority.text",
				"minPriority.description",

				// BugCategoryPane
				"bugCategory.title"
		};
	}

	@NotNull
	static String[] getSearchTexts() {
		return new String[]{

				//AnalysisEffortPane
				AnalysisEffort.MIN.getEffortLevel(),
				AnalysisEffort.MIN.getMessage(),
				AnalysisEffort.DEFAULT.getEffortLevel(),
				AnalysisEffort.DEFAULT.getMessage(),
				AnalysisEffort.MAX.getEffortLevel(),
				AnalysisEffort.MAX.getMessage(),

				// MinRankPane
				"Scariest",
				"Scary",
				"Troubling",
				"Of Concern",

				// MinPriorityPane
				ProjectFilterSettings.HIGH_PRIORITY,
				ProjectFilterSettings.MEDIUM_PRIORITY,
				ProjectFilterSettings.LOW_PRIORITY,

				// BugCategoryPane
				"BAD_PRACTICE", "Bad practice",
				"MALICIOUS_CODE", "Malicious code vulnerability",
				"CORRECTNESS", "Correctness",
				"PERFORMANCE", "Performance",
				"SECURITY", "Security",
				"STYLE", "Dodgy code",
				"EXPERIMENTAL", "Experimental",
				"MT_CORRECTNESS", "Multithreaded correctness",
				"I18N", "Internationalization"
		};
	}
}
