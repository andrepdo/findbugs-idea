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
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.event.EventImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(
		value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
		justification = "")
public class BugReporterInspectionEventImpl extends EventImpl implements BugReporterInspectionEvent {

	private static final long serialVersionUID = 0L;

	private static final String _toolWindowId = FindBugsPluginConstants.TOOL_WINDOW_ID; // todo: set on instance creation

	private Operation _operation;
	private BugInstance _bugInstance;
	private Integer _bugCount;
	private transient BugCollection _bugCollection;
	private transient ProjectStats _projectStats;
	private String _projectName;
	private final FindBugsProject _findBugsProject;


	public BugReporterInspectionEventImpl(@NotNull final Operation operation, @NotNull final String projectName) {
		this(operation, null, projectName);
	}


	public BugReporterInspectionEventImpl(@NotNull final Operation operation, @Nullable final BugInstance bugInstance, @NotNull final String projectName) {
		this(operation, bugInstance, null, projectName);
	}


	public BugReporterInspectionEventImpl(@NotNull final Operation operation, @Nullable final Integer bugCount, @NotNull final BugCollection bugCollection, @NotNull final String projectName) {
		this(operation, null, bugCount, bugCollection, projectName);
	}


	public BugReporterInspectionEventImpl(@NotNull final Operation operation, @Nullable final BugInstance bugInstance, @Nullable final Integer bugCount, @NotNull final String projectName) {
		this(operation, bugInstance, bugCount, null, null, projectName, null);
	}


	public BugReporterInspectionEventImpl(@NotNull final Operation operation, @Nullable final BugInstance bugInstance, @Nullable final Integer bugCount, @Nullable final ProjectStats projectStats, @NotNull final String projectName) {
		this(operation, bugInstance, bugCount, null, projectStats, projectName, null);
	}


	public BugReporterInspectionEventImpl(@NotNull final Operation operation, @Nullable final BugInstance bugInstance, @Nullable final Integer bugCount, @Nullable final BugCollection bugCollection, @NotNull final String projectName) {
		this(operation, bugInstance, bugCount, bugCollection, null, projectName, null);
	}


	public BugReporterInspectionEventImpl(@NotNull final Operation operation, @Nullable final Integer bugCount, @NotNull final BugCollection bugCollection, @NotNull final String projectName, @Nullable final FindBugsProject findBugsProject) {
		this(operation, null, bugCount, bugCollection, null, projectName, findBugsProject);
	}


	public BugReporterInspectionEventImpl(@NotNull final Operation operation, @Nullable final BugInstance bugInstance, @Nullable final Integer bugCount, @Nullable final BugCollection bugCollection, @Nullable final ProjectStats projectStats, @NotNull final String projectName, @Nullable final FindBugsProject findBugsProject) {
		super(EventType.FINDBUGS_INSPECTION);
		_operation = operation;
		_bugInstance = bugInstance;
		_bugCount = bugCount;
		_bugCollection = bugCollection;
		_projectStats = projectStats;
		_projectName = projectName;
		_findBugsProject = findBugsProject;
	}


	@NotNull
	public Operation getOperation() {
		return _operation;
	}


	@Nullable
	public BugInstance getBugInstance() {
		return _bugInstance;
	}


	@Nullable
	public Integer getBugCount() {
		return _bugCount;
	}


	@Nullable
	public BugCollection getBugCollection() {
		return _bugCollection;
	}


	@Nullable
	public ProjectStats getProjectStats() {
		return _projectStats;
	}


	/**
	 * The Intellij-IDEA project name.
	 *
	 * @return the project name string
	 */
	@NotNull
	public String getProjectName() {
		return _projectName;
	}


	@Nullable
	public FindBugsProject getFindBugsProject() {
		return _findBugsProject;
	}


	public String getToolWindowTabId() {
		return _toolWindowId;
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("BugReporterInspectionEventImpl");
		sb.append("{_operation=").append(_operation);
		sb.append(", _bugInstance=").append(_bugInstance);
		sb.append(", _bugCount=").append(_bugCount);
		sb.append(", _bugCollection=").append(_bugCollection);
		sb.append(", _projectStats=").append(_projectStats);
		sb.append(", _projectName='").append(_projectName).append('\'');
		sb.append(", _toolWindowId='").append(_toolWindowId).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
