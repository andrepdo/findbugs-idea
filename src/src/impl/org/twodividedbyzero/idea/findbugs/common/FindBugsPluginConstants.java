/*
 * Copyright 2008-2016 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.common;

import java.io.File;

@SuppressWarnings({"HardCodedStringLiteral"})
public final class FindBugsPluginConstants {

	public static final String PLUGIN_NAME = "FindBugs-IDEA";
	public static final String TOOL_WINDOW_ID = PLUGIN_NAME; // see plugin.xml
	public static final String FINDBUGS_WEBCLOUD_CLIENT_JAR = "plugin/findbugsCommunalCloud.jar";
	public static final String FINDBUGS_APP_ENGINE_PROPERTY_NAME = "findbugs.plugin.appengine";
	public static final String DEFAULT_EXPORT_DIR = System.getProperty("user.home") + File.separatorChar + TOOL_WINDOW_ID;

	public static final String PLUGIN_ID = "org.twodividedbyzero.idea.findbugs";
	public static final String MODULE_ID = "org.twodividedbyzero.idea.findbugs.module";

	// The action group for the plug-in tool window.
	public static final String ACTION_GROUP_LEFT = "FindBugs.ToolBarActions.left";

	public static final String ACTION_GROUP_RIGHT = "FindBugs.ToolBarActions.right";

	public static final String ACTION_GROUP_NAVIGATION = "FindBugs.ToolBarActions.navigation";

	public static final String ACTION_GROUP_UTILS = "FindBugs.ToolBarActions.utils";

	public static final String FILE_SEPARATOR = File.separator;

	public static final String FINDBUGS_EXTERNAL_HELP_URI = "http://findbugs.sourceforge.net/manual/filter.html";

	public static final String FINDBUGS_CORE_PLUGIN_ID = "edu.umd.cs.findbugs.plugins.core";

	public static final String DEFAULT_SUPPRESS_WARNINGS_CLASSNAME = "edu.umd.cs.findbugs.annotations.SuppressFBWarnings";

	private FindBugsPluginConstants() {
	}
}
