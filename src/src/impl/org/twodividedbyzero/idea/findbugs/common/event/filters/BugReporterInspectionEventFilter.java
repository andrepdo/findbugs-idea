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
package org.twodividedbyzero.idea.findbugs.common.event.filters;

import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.event.Event.EventType;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterInspectionEvent;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.7-dev
 */
public class BugReporterInspectionEventFilter implements EventFilter<BugReporterInspectionEvent> {

	private static final String ALL_PROJECTS = "";

	private final String _projectName;
	private final String _toolWindowTabId;


	public BugReporterInspectionEventFilter() {
		this(ALL_PROJECTS);
	}


	public BugReporterInspectionEventFilter(final String projectName) {
		this(projectName, FindBugsPluginConstants.TOOL_WINDOW_ID);
	}


	public BugReporterInspectionEventFilter(final String projectName, final String toolWindowTabId) {
		_projectName = projectName;
		_toolWindowTabId = toolWindowTabId;
	}


	public boolean acceptEvent(@NotNull final BugReporterInspectionEvent event) {
		return (event.getProjectName().equals(_projectName) || ALL_PROJECTS.equals(_projectName)) && event.getToolWindowTabId().equals(_toolWindowTabId);
	}


	public boolean acceptProject(@NotNull final String projectName) {
		return projectName.equals(_projectName);
	}


	public boolean acceptToolWindowTabId(@NotNull final String toolWindowId) {
		return _toolWindowTabId.equals(toolWindowId);
	}


	public EventType getEventType() {
		return EventType.FINDBUGS_INSPECTION;
	}


	public String getProjectName() {
		return _projectName;
	}


	public String getToolWindowTabId() {
		return _toolWindowTabId;
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("BugReporterInspectionEventFilter");
		sb.append("{_projectName='").append(_projectName).append('\'');
		sb.append(", _toolWindowId='").append(_toolWindowTabId).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
