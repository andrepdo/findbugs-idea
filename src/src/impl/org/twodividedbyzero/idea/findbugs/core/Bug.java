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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Bug {
	@Nullable
	private final Module module;

	@NotNull
	private final SortedBugCollection bugCollection;

	@NotNull
	private final BugInstance instance;

	public Bug(
			@Nullable final Module module,
			@NotNull final SortedBugCollection bugCollection,
			@NotNull final BugInstance instance
	) {
		this.module = module;
		this.bugCollection = bugCollection;
		this.instance = instance;
	}

	@Nullable
	public Module getModule() {
		return module;
	}

	@NotNull
	public SortedBugCollection getBugCollection() {
		return bugCollection;
	}

	@NotNull
	public BugInstance getInstance() {
		return instance;
	}

	@SuppressWarnings("SimplifiableIfStatement")
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Bug bug = (Bug) o;
		if (module != null ? !module.equals(bug.module) : bug.module != null) return false;
		if (!bugCollection.equals(bug.bugCollection)) return false;
		return instance.equals(bug.instance);
	}

	@Override
	public int hashCode() {
		int result = module != null ? module.hashCode() : 0;
		result = 31 * result + bugCollection.hashCode();
		result = 31 * result + instance.hashCode();
		return result;
	}

	@SuppressWarnings("RedundantIfStatement")
	public static boolean equalsBugType(@NotNull final Bug bugA, @NotNull final Bug bugB) {
		final BugInstance a = bugA.getInstance();
		final BugInstance b = bugB.getInstance();
		if (!StringUtil.equals(a.getType(), b.getType())) {
			return false;
		}
		return true;
	}
}
