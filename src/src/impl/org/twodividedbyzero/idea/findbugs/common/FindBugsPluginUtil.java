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

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.NotNull;

public class FindBugsPluginUtil {

	private FindBugsPluginUtil() {
	}

	@NotNull
	public static IdeaPluginDescriptor getIdeaPluginDescriptor() {
		final IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId(FindBugsPluginConstants.PLUGIN_NAME));
		if (plugin == null) {
			throw new IllegalStateException(FindBugsPluginConstants.PLUGIN_NAME + " could not be instantiated! PluginManager returned null!");
		}
		return plugin;
	}
}
