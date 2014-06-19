/*
 * Copyright 2008-2013 Andre Pfeiler
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
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"SE_TRANSIENT_FIELD_NOT_RESTORED", "SE_BAD_FIELD"})
public class BugReporterInspectionEventImpl extends EventImpl implements BugReporterInspectionEvent {

	private static final long serialVersionUID = 0L;

	private static final String _toolWindowId = FindBugsPluginConstants.TOOL_WINDOW_ID; // todo: set on instance creation

	private final Operation _operation;
	private final BugInstance _bugInstance;
	private final Integer _bugCount;
	private final transient BugCollection _bugCollection;
	private final transient ProjectStats _projectStats;
	private final String _projectName;
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
