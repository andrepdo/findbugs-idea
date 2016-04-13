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
package org.twodividedbyzero.idea.findbugs.core;

import com.intellij.util.xmlb.annotations.Tag;
import edu.umd.cs.findbugs.BugRanker;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.config.ProjectFilterSettings;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.preferences.AnalysisEffort;

import java.util.Map;
import java.util.Set;

public abstract class AbstractSettings {

	public boolean compileBeforeAnalyze = true;

	public boolean analyzeAfterCompile = false;

	public boolean analyzeAfterAutoMake = false;

	public boolean runInBackground = false;

	public boolean toolWindowToFront = true;

	/**
	 * @see AnalysisEffort
	 * @see edu.umd.cs.findbugs.config.UserPreferences#setEffort(String)
	 */
	public String analysisEffort = AnalysisEffort.DEFAULT.getEffortLevel();

	/**
	 * @see ProjectFilterSettings#setMinRank(int)
	 */
	public int minRank = BugRanker.VISIBLE_RANK_MIN;

	/**
	 * @see ProjectFilterSettings#setMinPriority(String)
	 */
	public String minPriority = ProjectFilterSettings.DEFAULT_PRIORITY;

	/**
	 * @see ProjectFilterSettings#addCategory(String)
	 * @see ProjectFilterSettings#removeCategory(String)
	 * @see ProjectFilterSettings#containsCategory(String)
	 */
	public Set<String> hiddenBugCategory = New.asSet("NOISE");

	/**
	 * @see edu.umd.cs.findbugs.config.UserPreferences#setIncludeFilterFiles(Map)
	 */
	public Map<String, Boolean> includeFilterFiles = New.map();

	/**
	 * @see edu.umd.cs.findbugs.config.UserPreferences#setExcludeFilterFiles(Map)
	 */
	public Map<String, Boolean> excludeFilterFiles = New.map();

	/**
	 * @see edu.umd.cs.findbugs.config.UserPreferences#setExcludeBugsFiles(Map)
	 */
	public Map<String, Boolean> excludeBugsFiles = New.map();

	/**
	 * Note that the map only contains enabled state which are not equal to the default enable state
	 * {@link DetectorFactory#isDefaultEnabled()}.
	 * <p>
	 * Key = {@link DetectorFactory#getShortName()} (like {@link edu.umd.cs.findbugs.config.UserPreferences#detectorEnablementMap})
	 * Value = Enabled state
	 */
	@Tag(value = "detectors")
	public Set<DetectorSettings> detectors = New.set();
}
