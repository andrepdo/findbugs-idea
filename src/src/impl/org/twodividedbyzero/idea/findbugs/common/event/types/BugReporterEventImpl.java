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
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"SE_TRANSIENT_FIELD_NOT_RESTORED", "SE_BAD_FIELD"})
final class BugReporterEventImpl extends EventImpl implements BugReporterEvent {

	private static final long serialVersionUID = 0L;

	private final Operation _operation;
	private final BugInstance _bugInstance;
	private final Integer _bugCount;
	private final transient BugCollection _bugCollection;
	private final transient ProjectStats _projectStats;
	private final String _projectName;
	private final FindBugsProject _findBugsProject;


	BugReporterEventImpl(
			@NotNull final Operation operation,
			@Nullable final BugInstance bugInstance,
			@Nullable final Integer bugCount,
			@Nullable final BugCollection bugCollection,
			@Nullable final ProjectStats projectStats,
			@NotNull final String projectName,
			@Nullable final FindBugsProject findBugsProject
	) {
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
	 * {@inheritDoc}
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
		return "BugReporterEventImpl{" +
				"_operation=" + _operation +
				", _bugInstance=" + _bugInstance +
				", _bugCount=" + _bugCount +
				", _bugCollection=" + _bugCollection +
				", _projectStats=" + _projectStats +
				", _projectName=" + _projectName +
				'}';
	}
}
