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
package org.twodividedbyzero.idea.findbugs.common;

import java.io.File;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class FindBugsPluginConstants {

	public static final String LOCALE_RESOURCES_PKG = "org.twodividedbyzero.idea.findbugs.resources.i18n.Messages";// NON-NLS

	public static final String ICON_RESOURCES_PKG = "/org/twodividedbyzero/idea/findbugs/resources/icons";// NON-NLS

	public static final String TOOL_WINDOW_ID = "FindBugs-IDEA";

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


	private FindBugsPluginConstants() {
	}
}
