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

package org.twodividedbyzero.idea.findbugs.common.event.types;


import com.intellij.openapi.project.Project;
import edu.umd.cs.findbugs.BugCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;


/**
 * $Date: 2015-02-13 23:19:10 +0100 (Fr, 13 Feb 2015) $
 *
 * $Id: ErrorUtil.java 308 2015-02-13 22:19:10Z reto.merz@gmail.com $
 *
 * @author $Author: reto.merz@gmail.com $
 * @version $Revision: 338 $
 * @since 0.9.995
 */
public final class BugReporterEventFactory {


	private BugReporterEventFactory() {
	}


	@NotNull
	public static BugReporterEvent newAborted(@NotNull final Project project) {
		return new BugReporterEventImpl(
				BugReporterEvent.Operation.ANALYSIS_ABORTED,
				null,
				null,
				null,
				null,
				project.getName(),
				null
		);
	}


	@NotNull
	public static BugReporterEvent newFinished(@NotNull final BugCollection bugCollection, @NotNull final Project project, @Nullable final FindBugsProject findBugsProject) {
		return new BugReporterEventImpl(
				BugReporterEvent.Operation.ANALYSIS_FINISHED,
				null,
				null,
				bugCollection,
				null,
				project.getName(),
				findBugsProject
		);
	}

}