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

import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Tag;
import edu.umd.cs.findbugs.BugRanker;
import edu.umd.cs.findbugs.config.ProjectFilterSettings;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.preferences.AnalysisEffort;

import java.util.Map;
import java.util.Set;

public abstract class AbstractSettings {

	/**
	 * @see AnalysisEffort
	 * @see edu.umd.cs.findbugs.config.UserPreferences#setEffort(String)
	 */
	@Tag
	public String analysisEffort = AnalysisEffort.DEFAULT.getEffortLevel();

	/**
	 * @see ProjectFilterSettings#setMinRank(int)
	 */
	@Tag
	public int minRank = BugRanker.VISIBLE_RANK_MIN;

	/**
	 * @see ProjectFilterSettings#setMinPriority(String)
	 */
	@Tag
	public String minPriority = ProjectFilterSettings.DEFAULT_PRIORITY;

	/**
	 * @see ProjectFilterSettings#addCategory(String)
	 * @see ProjectFilterSettings#removeCategory(String)
	 * @see ProjectFilterSettings#containsCategory(String)
	 */
	@Tag(value = "hiddenBugCategory")
	@AbstractCollection(surroundWithTag = false, elementTag = "category", elementValueAttribute = "name")
	public Set<String> hiddenBugCategory = New.asSet("NOISE");

	/**
	 * @see edu.umd.cs.findbugs.config.UserPreferences#setIncludeFilterFiles(Map)
	 */
	@Tag(value = "includeFilterFiles")
	@MapAnnotation(
			surroundWithTag = false,
			surroundValueWithTag = false,
			surroundKeyWithTag = false,
			entryTagName = "filter",
			keyAttributeName = "file",
			valueAttributeName = "enabled"
	)
	public Map<String, Boolean> includeFilterFiles = New.map();

	/**
	 * @see edu.umd.cs.findbugs.config.UserPreferences#setExcludeFilterFiles(Map)
	 */
	@Tag(value = "excludeFilterFiles")
	@MapAnnotation(
			surroundWithTag = false,
			surroundValueWithTag = false,
			surroundKeyWithTag = false,
			entryTagName = "filter",
			keyAttributeName = "file",
			valueAttributeName = "enabled"
	)
	public Map<String, Boolean> excludeFilterFiles = New.map();

	/**
	 * @see edu.umd.cs.findbugs.config.UserPreferences#setExcludeBugsFiles(Map)
	 */
	@Tag(value = "excludeBugsFiles")
	@MapAnnotation(
			surroundWithTag = false,
			surroundValueWithTag = false,
			surroundKeyWithTag = false,
			entryTagName = "bugs",
			keyAttributeName = "file",
			valueAttributeName = "enabled"
	)
	public Map<String, Boolean> excludeBugsFiles = New.map();
}
