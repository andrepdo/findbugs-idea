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
package org.twodividedbyzero.idea.findbugs.common;

import java.io.File;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class FindBugsPluginConstants {

	public static final String LOCALE_RESOURCES_PKG = "org.twodividedbyzero.idea.findbugs.resources.i18n.Messages";

	public static final String ICON_RESOURCES_PKG = "/org/twodividedbyzero/idea/findbugs/resources/icons";

	public static final String TOOL_WINDOW_ID = "FindBugs-IDEA";
	public static final String DEFAULT_EXPORT_DIR = System.getProperty("user.home") + File.separatorChar + TOOL_WINDOW_ID;

	public static final String PLUGIN_ID = "org.twodividedbyzero.idea.findbugs";
	public static final String MODULE_ID = "org.twodividedbyzero.idea.findbugs.module";

	// The action group for the plug-in tool window.
	public static final String ACTION_GROUP_LEFT = "FindBugs.ToolBarActions.left";

	public static final String ACTION_GROUP_RIGHT = "FindBugs.ToolBarActions.right";

	public static final String ACTION_GROUP_NAVIGATION = "FindBugs.ToolBarActions.navigation";

	public static final String ACTION_GROUP_UTILS = "FindBugs.ToolBarActions.utils";

	public static final String ACTION_GROUP_GUTTER = "FindBugs.GutterPopup.group";

	public static final String CURRENT_FILE_ACTION = "FindBugs.CurrentFileAction";

	public static final String CLOSE_ACTION = "FindBugs.CloseAction";

	public static final String STOP_ACTION = "FindBugs.StopAction";

	public static final String HELP_ACTION = "FindBugs.HelpAction";

	public static final String ACTIVE_CHANGELIST_ACTION = "FindBugs.ActiveChangeListAction";

	public static final String FILE_SEPARATOR = File.separator;

	public static final String FINDBUGS_PLUGIN_PATH = "lib/plugin";

	public static final String FINDBUGS_EXTERNAL_HELP_URI = "http://findbugs.sourceforge.net/manual/filter.html";

	public static final String FINDBUGS_PROJECT_HOLDER = "FindBugs.ProjectHolder";


	private FindBugsPluginConstants() {
	}
}
