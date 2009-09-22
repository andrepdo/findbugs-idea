/*
 * Copyright 2009 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.twodividedbyzero.idea.findbugs;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import edu.umd.cs.findbugs.DetectorFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.gui.preferences.ModuleConfigurationPanel;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.ToolWindowPanel;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
import org.twodividedbyzero.idea.findbugs.preferences.PersistencePreferencesBean;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.9
 */
@State(
		name = FindBugsPluginConstants.PLUGIN_ID,
		storages = {@Storage(
				id = "other",
				file = "$MODULE_FILE$")})
public class ModuleComponentImpl implements FindBugsPlugin, ModuleComponent, Configurable, PersistentStateComponent<PersistencePreferencesBean> {

	private static final Logger LOGGER = Logger.getInstance(FindBugsPluginImpl.class.getName());

	private ModuleConfigurationPanel _configPanel;
	private final FindBugsPreferences _preferences;
	private final Project _project;
	private final Module _module;
	//private final ToolWindow _toolWindow;


	public ModuleComponentImpl(final Project project, final Module module) {
		_project = project;
		_module = module;
		_preferences = FindBugsPreferences.createDefault();
	}


	public void projectOpened() {
		LOGGER.debug("project is opened");// NON-NLS
	}


	public void projectClosed() {
		LOGGER.debug("project is being closed");// NON-NLS
	}


	public void moduleAdded() {
		LOGGER.debug("module added");// NON-NLS
	}


	@NotNull
	public String getComponentName() {
		return FindBugsPluginConstants.MODULE_ID;
	}


	public void initComponent() {
		LOGGER.debug("initComponent");// NON-NLS
	}


	public void disposeComponent() {
		LOGGER.debug("disposeComponent");// NON-NLS
	}


	@Nls
	public String getDisplayName() {
		return ResourcesLoader.getString("findbugs.plugin.configuration.name");
	}


	public Icon getIcon() {
		return GuiResources.FINDBUGS_ICON;
	}


	public String getHelpTopic() {
		return FindBugsPluginConstants.FINDBUGS_EXTERNAL_HELP_URI;
	}


	public ToolWindowPanel getToolWindowPanel() {
		/*final Content content = _toolWindow.getContentManager().getContent(0);
		if (content != null) {
			return ((ToolWindowPanel) content.getComponent());
		}*/
		return (ToolWindowPanel) IdeaUtilImpl.getToolWindowById(FindBugsPluginConstants.TOOL_WINDOW_ID);
	}


	public void activateToolWindow(final boolean activate) {
		if (activate) {
			IdeaUtilImpl.getToolWindowById(FindBugsPluginConstants.TOOL_WINDOW_ID).show(null);
		} else {
			IdeaUtilImpl.getToolWindowById(FindBugsPluginConstants.TOOL_WINDOW_ID).hide(null);
		}
	}


	public String getInternalToolWindowId() {
		return FindBugsPluginConstants.TOOL_WINDOW_ID;
	}


	public FindBugsPreferences getProjectPreferences() {
		return IdeaUtilImpl.getPluginComponent(_project).getPreferences();
	}


	public FindBugsPreferences getPreferences() {
		//noinspection ReturnOfCollectionOrArrayField
		return _preferences;
	}


	public Project getProject() {
		return _project;
	}


	public Module getModule() {
		return _module;
	}


	public boolean isModuleComponent() {
		return true;
	}


	public JComponent createComponent() {
		if (_configPanel == null) {
			_configPanel = new ModuleConfigurationPanel(this);
		}
		return _configPanel;
	}


	public boolean isModified() {
		return _preferences.isModified();
	}


	public void apply() throws ConfigurationException {
		_preferences.setModified(false);
	}


	public void reset() {

	}


	public void disposeUIResources() {
		if (_configPanel != null) {
			_configPanel.setVisible(false);
		}
		//noinspection AssignmentToNull
		_configPanel = null;
	}


	public PersistencePreferencesBean getState() {
		final PersistencePreferencesBean preferencesBean = new PersistencePreferencesBean();
		for (final Enumeration<?> confNames = _preferences.propertyNames(); confNames.hasMoreElements();) {
			final String elementName = (String) confNames.nextElement();
			preferencesBean.getBasePreferences().put(elementName, _preferences.getProperty(elementName));
		}
		preferencesBean.getDetectors().putAll(_preferences.getDetectors());
		preferencesBean.getBugCategories().putAll(_preferences.getBugCategories());

	 	preferencesBean.getIncludeFilters().addAll(_preferences.getIncludeFilters());
		preferencesBean.getExcludeFilters().addAll(_preferences.getExcludeFilters());
		preferencesBean.getExcludeBaselineBugs().addAll(_preferences.getExcludeBaselineBugs());

		return preferencesBean;
	}


	public void loadState(final PersistencePreferencesBean state) {
		_preferences.clear();

		if (state != null && state.getBasePreferences() != null) {
			for (final String key : state.getBasePreferences().keySet()) {
				_preferences.setProperty(key, state.getBasePreferences().get(key));
			}

			final Map<String, String> detectors = state.getDetectors();
			_preferences.setDetectors(detectors);
			for (final Entry<String, String> entry : detectors.entrySet()) {
				final DetectorFactory detectorFactory = FindBugsPreferences.getDetectorFactorCollection().getFactory(entry.getKey());
				if(detectorFactory != null) {
					_preferences.getUserPreferences().enableDetector(detectorFactory, Boolean.valueOf(entry.getValue()));
				}
			}

			_preferences.setBugCategories(state.getBugCategories());
			_preferences.setIncludeFilters(state.getIncludeFilters());
			_preferences.setExcludeFilters(state.getExcludeFilters());
			_preferences.setExcludeBaselineBugs(state.getExcludeBaselineBugs());
		}
	}
}
