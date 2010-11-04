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
package org.twodividedbyzero.idea.findbugs.inspection;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
public class FindBugsInspectionProvider implements InspectionToolProvider, ApplicationComponent {


	public Class<?>[] getInspectionClasses() {
		return new Class[] {FindBugsInspection.class};
	}


	@NonNls
	@NotNull
	public String getComponentName() {
		return "FindBugsInspectionProvider";
	}


	public void initComponent() {
		// no action required
		Application app = ApplicationManager.getApplication();
		IdeaPluginDescriptor plugin = app.getPlugin(PluginId.getId("FindBugs-IDEA"));
		File jar = new File(plugin.getPath(), "lib/webCloudClient.jar");
		System.setProperty("findbugs.plugin.appengine", jar.toURI().toString());
	}


	public void disposeComponent() {
		// no action required
	}

}
