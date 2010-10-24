/**
 * Copyright 2009 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.common.event.filters;

import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.event.Event.EventType;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.7-dev
 */
public class BugReporterEventFilter implements EventFilter<BugReporterEvent> {

	private static final String ALL_PROJECTS = "";
	
	private final String _projectName;
	private final String _toolWindowTabId;


	public BugReporterEventFilter() {
		this(ALL_PROJECTS);
	}


	public BugReporterEventFilter(final String projectName) {
		this(projectName, FindBugsPluginConstants.TOOL_WINDOW_ID);
	}


	public BugReporterEventFilter(final String projectName, final String toolWindowTabId) {
		_projectName = projectName;
		_toolWindowTabId = toolWindowTabId;
	}


	public boolean acceptEvent(@NotNull final BugReporterEvent event) {
		return event.getProjectName().equals(_projectName) || ALL_PROJECTS.equals(_projectName);
	}


	public boolean acceptProject(@NotNull final String projectName) {
		return projectName.equals(_projectName);
	}


	public boolean acceptToolWindowTabId(@NotNull final String toolWindowId) {
		return _toolWindowTabId.equals(toolWindowId);
	}


	public EventType getEventType() {
		return EventType.FINDBUGS;
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
		sb.append("BugReporterEventFilter");
		sb.append("{_projectName='").append(_projectName).append('\'');
		sb.append(", _toolWindowId='").append(_toolWindowTabId).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
