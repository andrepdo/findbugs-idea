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
package org.twodividedbyzero.idea.findbugs.inspection;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;

import java.io.File;

/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
public class FindBugsInspectionProvider implements InspectionToolProvider, ApplicationComponent {

	private static final Logger LOGGER = Logger.getInstance(FindBugsInspectionProvider.class.getName());


	@Override
	public Class<?>[] getInspectionClasses() {
		return new Class[] {FindBugsInspection.class};
	}


	@edu.umd.cs.findbugs.annotations.SuppressWarnings({"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
	@Override
	@NonNls
	@NotNull
	public String getComponentName() {
		return "FindBugsInspectionProvider";
	}


	@Override
	public void initComponent() {
		final IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId(FindBugsPluginConstants.PLUGIN_NAME));
		if (plugin == null) {
			return;
		}
		final File jar = new File(plugin.getPath(), FindBugsPluginConstants.FINDBUGS_WEBCLOUD_CLIENT_JAR);
		if (jar.exists() && jar.canRead()) {
			System.setProperty(FindBugsPluginConstants.FINDBUGS_APP_ENGINE_PROPERTY_NAME, jar.toURI().toString());
		} else {
			LOGGER.info(FindBugsPluginConstants.FINDBUGS_APP_ENGINE_PROPERTY_NAME + " jar not found." + jar.toURI());
		}

	}


	@Override
	public void disposeComponent() {
		// no action required
	}

}
