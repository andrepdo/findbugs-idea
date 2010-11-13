/**
 * Copyright 2008 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.twodividedbyzero.idea.findbugs.common.event.types;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.ProjectStats;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.event.Event;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public interface BugReporterEvent extends Event {

	enum Operation {

		ANALYSIS_STARTED,
		ANALYSIS_ABORTED,
		ANALYSIS_FINISHED,
		NEW_BUG_INSTANCE
	}

	@NotNull
	Operation getOperation();


	@Nullable
	BugInstance getBugInstance();


	Integer getBugCount();


	@Nullable
	BugCollection getBugCollection();


	@Nullable
	ProjectStats getProjectStats();

	/**
	 * The Intellij-IDEA project name.
	 *
	 * @return the project name or empty string for all projects
	 */
	@NotNull
	String getProjectName();


	@Nullable
	FindBugsProject getFindBugsProject();

}
