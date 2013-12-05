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

package org.twodividedbyzero.idea.findbugs.core;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.Content;
import edu.umd.cs.findbugs.DetectorFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.9
 */
@SuppressWarnings("RedundantInterfaceDeclaration")
@State(
		name = FindBugsPluginConstants.PLUGIN_ID,
		storages = {@Storage(id = "other", file = "$MODULE_FILE$")
					/*@Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/findbugs-idea.xml", scheme = StorageScheme.DIRECTORY_BASED)*/})
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
		LOGGER.debug("project is opened");
	}


	public void projectClosed() {
		LOGGER.debug("project is being closed");
	}


	public void moduleAdded() {
		LOGGER.debug("module added");
	}


	@NotNull
	public String getComponentName() {
		return FindBugsPluginConstants.MODULE_ID;
	}


	public void initComponent() {
		LOGGER.debug("initComponent");
	}


	public void disposeComponent() {
		LOGGER.debug("disposeComponent");
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
		final ToolWindow toolWindow = IdeaUtilImpl.getToolWindowById(FindBugsPluginConstants.TOOL_WINDOW_ID, _project);
		final Content content = toolWindow.getContentManager().getContent(0);
		if (content != null) {
			return (ToolWindowPanel) content.getComponent();
		}
		return null;
	}


	public void activateToolWindow(final boolean activate) {
		if (activate) {
			IdeaUtilImpl.getToolWindowById(FindBugsPluginConstants.TOOL_WINDOW_ID, _project).show(null);
		} else {
			IdeaUtilImpl.getToolWindowById(FindBugsPluginConstants.TOOL_WINDOW_ID, _project).hide(null);
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


	public Map<PsiFile, List<ExtendedProblemDescriptor>> getProblems() {
		return getToolWindowPanel().getProblems();
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
		//noinspection ForLoopWithMissingComponent
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
				if (detectorFactory != null) {
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
