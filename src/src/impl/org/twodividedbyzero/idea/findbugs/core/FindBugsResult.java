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

import edu.umd.cs.findbugs.ProjectStats;
import edu.umd.cs.findbugs.SortedBugCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.New;

import java.util.Collection;
import java.util.Map;

public final class FindBugsResult {
	@NotNull
	private Map<FindBugsProject, SortedBugCollection> results;

	private int numClasses; // TODO kick this

	public FindBugsResult() {
		this.results = New.map();
	}

	public void put(@Nullable final FindBugsProject project, @Nullable final SortedBugCollection bugCollection, int numClasses) {
		if (results.put(project, bugCollection) != null) {
			throw new IllegalStateException("Duplicate project " + project);
		}
		this.numClasses += numClasses;
	}

	public boolean isBugCollectionEmpty() {
		for (final SortedBugCollection bugCollection : results.values()) {
			if (!bugCollection.getCollection().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public int getNumClassesSafe() {
		final Integer ret = getNumClasses();
		if (ret != null) {
			return ret;
		}
		return 0;
	}

	@Nullable
	public Integer getNumClasses() {
		int ret = -1;
		for (final SortedBugCollection bugCollection : results.values()) {
			final ProjectStats stats = bugCollection.getProjectStats();
			if (stats != null) {
				if (ret == -1) {
					ret = 0;
				}
				ret += stats.getNumClasses();
			}
		}
		if (ret != -1) {
			return ret;
		}
		return null;
	}

	@NotNull
	public Collection<FindBugsProject> getProjects() {
		return results.keySet();
	}

	@NotNull
	public Map<FindBugsProject, SortedBugCollection> getResults() {
		return results;
	}
}
