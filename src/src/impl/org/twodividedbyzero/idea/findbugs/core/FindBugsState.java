/*
 * Copyright 2008-2015 Andre Pfeiler
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


import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.New;

import java.util.Map;


/**
 * @author $Author: reto.merz@gmail.com $
 * @version $Revision: 376 $
 * @since 0.9.995
 */
public enum FindBugsState {

	Cleared,
	Started,
	Aborting,
	Aborted,
	Finished;

	private static final Map<Project, FindBugsState> _stateByProject = New.concurrentMap();


	public boolean isStarted() {
		return Started.equals(this);
	}


	public boolean isAborting() {
		return Aborting.equals(this);
	}


	public boolean isAborted() {
		return Aborted.equals(this);
	}


	public boolean isFinished() {
		return Finished.equals(this);
	}


	public boolean isIdle() {
		return !isStarted() && !isAborting();
	}


	public static void set(@NotNull final Project project, @NotNull final FindBugsState state) {
		_stateByProject.put(project, state);
	}


	@NotNull
	public static FindBugsState get(@NotNull final Project project) {
		FindBugsState ret = _stateByProject.get(project);
		if (ret == null) {
			ret = Cleared;
		}
		return ret;
	}


	public static void dispose(@NotNull final Project project) {
		_stateByProject.remove(project);
	}
}