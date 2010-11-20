/*
 * Copyright 2010 Andre Pfeiler
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
import org.twodividedbyzero.idea.findbugs.common.event.EventImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings({"SE_TRANSIENT_FIELD_NOT_RESTORED", "SE_BAD_FIELD"})
public class BugReporterEventImpl extends EventImpl implements BugReporterEvent {

	private static final long serialVersionUID = 0L;

	private Operation _operation;
	private BugInstance _bugInstance;
	private Integer _bugCount;


	private transient BugCollection _bugCollection;
	private transient ProjectStats _projectStats;
	private String _projectName;
	private FindBugsProject _findBugsProject;


	public BugReporterEventImpl(@NotNull final Operation operation, @NotNull final String projectName) {
		this(operation, null, projectName);
	}


	public BugReporterEventImpl(@NotNull final Operation operation, @Nullable final BugInstance bugInstance, @NotNull final String projectName) {
		this(operation, bugInstance, null, projectName);
	}


	public BugReporterEventImpl(@NotNull final Operation operation, @Nullable final Integer bugCount, @NotNull final BugCollection bugCollection, @NotNull final String projectName) {
		this(operation, null, bugCount, bugCollection, projectName);
	}


	public BugReporterEventImpl(@NotNull final Operation operation, @Nullable final Integer bugCount, @NotNull final BugCollection bugCollection, @NotNull final String projectName, @Nullable final FindBugsProject findBugsProject) {
		this(operation, null, bugCount, bugCollection, null, projectName, findBugsProject);
	}


	public BugReporterEventImpl(@NotNull final Operation operation, @Nullable final BugInstance bugInstance, @Nullable final Integer bugCount, @NotNull final String projectName) {
		this(operation, bugInstance, bugCount, null, null, projectName, null);
	}


	public BugReporterEventImpl(@NotNull final Operation operation, @Nullable final BugInstance bugInstance, @Nullable final Integer bugCount, @Nullable final ProjectStats projectStats, @NotNull final String projectName) {
		this(operation, bugInstance, bugCount, null, projectStats, projectName, null);
	}


	public BugReporterEventImpl(@NotNull final Operation operation, @Nullable final BugInstance bugInstance, @Nullable final Integer bugCount, @Nullable final BugCollection bugCollection, @NotNull final String projectName) {
		this(operation, bugInstance, bugCount, bugCollection, null, projectName, null);
	}


	public BugReporterEventImpl(@NotNull final Operation operation, @Nullable final BugInstance bugInstance, @Nullable final Integer bugCount, @Nullable final BugCollection bugCollection, @Nullable final ProjectStats projectStats, @NotNull final String projectName, @Nullable final FindBugsProject findBugsProject) {
		super(EventType.FINDBUGS);
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


	@Override
	public String toString() {
		return new StringBuilder().append("BugReporterEventImpl{").append("_operation=").append(_operation).append(", _bugInstance=").append(_bugInstance).append(", _bugCount=").append(_bugCount).append(", _bugCollection=").append(_bugCollection).append(", _projectStats=").append(_projectStats).append(", _projectName=").append(_projectName).append('}').toString();
	}
}
