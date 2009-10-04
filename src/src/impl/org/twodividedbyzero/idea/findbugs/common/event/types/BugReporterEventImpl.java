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
import org.twodividedbyzero.idea.findbugs.common.event.EventImpl;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class BugReporterEventImpl extends EventImpl implements BugReporterEvent {

	private static final long serialVersionUID = 0L;

	private Operation _operation;
	private BugInstance _bugInstance;
	private Integer _bugCount;

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
			value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
			justification = "because I know better")
	private transient BugCollection _bugCollection;

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
			value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
			justification = "because I know better")
	private transient ProjectStats _projectStats;
	private String _projectName;


	public BugReporterEventImpl(@NotNull final Operation operation, @NotNull final String projectName) {
		this(operation, null, projectName);
	}


	public BugReporterEventImpl(@NotNull final Operation operation, @Nullable final BugInstance bugInstance, @NotNull final String projectName) {
		this(operation, bugInstance, null, projectName);
	}


	public BugReporterEventImpl(@NotNull final Operation operation, @Nullable final Integer bugCount, @NotNull final BugCollection bugCollection, @NotNull final String projectName) {
		this(operation, null, bugCount, bugCollection, projectName);
	}


	public BugReporterEventImpl(@NotNull final Operation operation, @Nullable final BugInstance bugInstance, @Nullable final Integer bugCount, @NotNull final String projectName) {
		this(operation, bugInstance, bugCount, null, null, projectName);
	}


	public BugReporterEventImpl(@NotNull final Operation operation, @Nullable final BugInstance bugInstance, @Nullable final Integer bugCount, @Nullable final ProjectStats projectStats, @NotNull final String projectName) {
		this(operation, bugInstance, bugCount, null, projectStats, projectName);
	}


	public BugReporterEventImpl(@NotNull final Operation operation, @Nullable final BugInstance bugInstance, @Nullable final Integer bugCount, @Nullable final BugCollection bugCollection, @NotNull final String projectName) {
		this(operation, bugInstance, bugCount, bugCollection, null, projectName);
	}


	public BugReporterEventImpl(@NotNull final Operation operation, @Nullable final BugInstance bugInstance, @Nullable final Integer bugCount, @Nullable final BugCollection bugCollection, @Nullable final ProjectStats projectStats, @NotNull final String projectName) {
		super(EventType.FINDBUGS);
		_operation = operation;
		_bugInstance = bugInstance;
		_bugCount = bugCount;
		_bugCollection = bugCollection;
		_projectStats = projectStats;
		_projectName = projectName;
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


	@Override
	public String toString() {
		return "BugReporterEventImpl{" + "_operation=" + _operation + ", _bugInstance=" + _bugInstance + ", _bugCount=" + _bugCount + ", _bugCollection=" + _bugCollection + ", _projectStats=" + _projectStats + ", _projectName=" + _projectName + '}'; // NON-NLS
	}
}
