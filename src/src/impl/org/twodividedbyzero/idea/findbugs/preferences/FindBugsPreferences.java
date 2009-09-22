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
package org.twodividedbyzero.idea.findbugs.preferences;

import com.intellij.openapi.module.Module;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.I18N;
import edu.umd.cs.findbugs.config.ProjectFilterSettings;
import edu.umd.cs.findbugs.config.UserPreferences;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.exception.FindBugsPluginException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Collections;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.9-dev
 */
public class FindBugsPreferences extends Properties {

	private static final long serialVersionUID = 0L;

	private static DetectorFactoryCollection _detectorFactoryCollection;

	//public static final String DEFAULT_CONFIG = "";

	//public static final String CONFIG_FILE = "config-file";

	/** The prefix for stored properties. */
	public static final String PROPERTIES_PREFIX = "property.";

	public static final String RUN_ANALYSIS_IN_BACKGROUND = PROPERTIES_PREFIX + "runAnalysisInBackground";
	public static final String ANALYSIS_EFFORT_LEVEL = PROPERTIES_PREFIX + "analysisEffortLevel";
	public static final String MIN_PRIORITY_TO_REPORT = PROPERTIES_PREFIX + "minPriorityToReport";
	public static final String SHOW_HIDDEN_DETECTORS = PROPERTIES_PREFIX + "showHiddenDetectors";


	public Map<String, String> _detectors;
	public Map<String, String> _bugCategories;
	public List<String> _includeFilters;
	public List<String> _excludeFilters;
	public List<String> _excludeBaselineBugs;
	public List<String> _plugins;

	public List<String> _enabledModuleConfigs;

	public UserPreferences _userPreferences;

	public boolean COMPILE_BEFORE_ANALYZE = false;
	private boolean _isModified;


	private FindBugsPreferences() {
		super();
		initDefaults();
	}


	private void initDefaults() {
		_detectors = new HashMap<String, String>();
		_bugCategories = new HashMap<String, String>(/*getBugCategories()*/);
		_includeFilters = new ArrayList<String>();
		_excludeFilters = new ArrayList<String>();
		_excludeBaselineBugs = new ArrayList<String>();
		_plugins = new ArrayList<String>();
		_enabledModuleConfigs = new ArrayList<String>();
		_userPreferences = UserPreferences.createDefaultUserPreferences();
	}


	/**
	 * Get all FindBugs-IDEA properties defined in the configuration.
	 *
	 * @return a map of FindBugs-IDEA property names to values.
	 */
	public Map<String, String> getDefinedProperies() {
		final Map<String, String> values = new HashMap<String, String>();

		for (final Enumeration<?> properties = propertyNames(); properties.hasMoreElements();) {
			final String propertyName = (String) properties.nextElement();
			if (propertyName.startsWith(PROPERTIES_PREFIX)) {
				final String configPropertyName = propertyName.substring(PROPERTIES_PREFIX.length());
				final String configPropertyValue = getProperty(propertyName);

				values.put(configPropertyName, configPropertyValue);
				setModified(true);
			}
		}

		return values;
	}


	public void setDefaults(final FindBugsPreferences properties) {
		clear();
		for (final Enumeration<?> confNames = properties.propertyNames(); confNames.hasMoreElements();) {
			final String elementName = (String) confNames.nextElement();
			final String value = properties.getProperty(elementName);
			setProperty(elementName, value);
			setBugCategories(properties.getBugCategories());
			setDetectors(properties.getDetectors());
			setModified(true);
		}
	}


	public void setDefinedProperies(final Map<String, String> properties) {
		if (properties == null || properties.size() == 0) {
			return;
		}


		for (final String propertyName : properties.keySet()) {
			final String value = properties.get(propertyName);
			if (value != null) {
				setProperty(PROPERTIES_PREFIX + propertyName, value);
				setModified(true);
			}
		}
	}


	public void clearDefinedProperies() {
		final List<String> propertiesToRemove = new ArrayList<String>();

		for (final Enumeration<?> properties = propertyNames(); properties.hasMoreElements();) {
			final String propertyName = (String) properties.nextElement();
			if (propertyName.startsWith(PROPERTIES_PREFIX)) {
				// delay to stop concurrent modification
				propertiesToRemove.add(propertyName);
			}
		}

		for (final String property : propertiesToRemove) {
			remove(property);
			setModified(true);
		}
	}


	@NotNull
	public List<String> getListProperty(final String propertyName) {
		final List<String> returnValue = new ArrayList<String>();

		final String value = getProperty(propertyName);
		if (value != null) {
			final String[] parts = value.split(";");
			returnValue.addAll(Arrays.asList(parts));
		}

		return returnValue;
	}


	public void setProperty(final String propertyName, final List<String> value) {
		if (value == null) {
			setProperty(propertyName, (String) null);
			return;
		}

		final StringBuilder valueString = new StringBuilder();
		for (final String part : value) {
			if (valueString.length() > 0) {
				valueString.append(";");
			}
			valueString.append(part);
		}

		setProperty(propertyName, valueString.toString());
		setModified(true);
	}


	public boolean getBooleanProperty(final String propertyName, final boolean defaultValue) {
		return Boolean.valueOf(getProperty(propertyName, Boolean.toString(defaultValue)));
	}


	public void setProperty(final String propertyName, final boolean value) {
		setProperty(propertyName, Boolean.toString(value));
		setModified(true);
	}


	@Override
	public Object setProperty(final String key, @NotNull final String value) {
		setModified(true);
		return super.setProperty(key, value);
	}


	public boolean isModified() {
		return _isModified;
	}


	public void setModified(final boolean modified) {
		_isModified = modified;
	}


	public Map<String, String> getDetectors() {
		//noinspection ReturnOfCollectionOrArrayField
		return _detectors;
	}


	public void setDetectors(final Map<String, String> detectors) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_detectors = detectors;
		syncDetectors();
	}


	private void syncDetectors() {
		for (final Map.Entry<String, String> entry : getDetectors().entrySet()) {
			final DetectorFactoryCollection detectorFactoryCollection = FindBugsPreferences.getDetectorFactorCollection();
			final DetectorFactory factory = detectorFactoryCollection.getFactory(entry.getKey());
			if (factory != null) {
				getUserPreferences().enableDetector(factory, Boolean.valueOf(entry.getValue()));
				//DetectorFactoryCollection.rawInstance().setPluginList(new URL[0])
				//detectorFactoryCollection.setPluginList();
			}
		}
	}


	public static void loadPlugins(final List<String> pluginList) {
		if (pluginList != null && !pluginList.isEmpty()) {
			final URL[] result = new URL[pluginList.size()];
			for (int i = 0; i < pluginList.size(); i++) {
				final String s = pluginList.get(i);
				try {
					result[i] = new File(s).toURI().toURL();  // NON-NLS
				} catch (MalformedURLException e) {
					throw new FindBugsPluginException("plugin '" + s + "' can not be injected.", e);
				}
			}

			try {
				getDetectorFactorCollection().setPluginList(result);
				getDetectorFactorCollection().ensureLoaded();
			} catch (IllegalStateException e) {
				throw new FindBugsPluginException("Plugin needs to be set first before DetectorFactoryCollection instance is created.", e);
			}
		}
	}


	public void setIncludeFilters(final List<String> includeFilters) {
		_includeFilters.clear();
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_includeFilters = includeFilters;
	}


	public void setExcludeFilters(final List<String> excludeFilters) {
		_excludeFilters.clear();
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_excludeFilters = excludeFilters;
	}


	public void setExcludeBaselineBugs(final List<String> excludeBaselineBugs) {
		_excludeBaselineBugs.clear();
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_excludeBaselineBugs = excludeBaselineBugs;
	}


	public void setPlugins(final List<String> plugins) {
		_plugins.clear();
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_plugins = plugins;
		//loadPlugins(_plugins);
	}


	public Map<String, String> getBugCategories() {
		//noinspection ReturnOfCollectionOrArrayField
		return _bugCategories;
	}


	public List<String> getIncludeFilters() {
		//noinspection ReturnOfCollectionOrArrayField
		return _includeFilters;
	}


	public List<String> getExcludeFilters() {
		//noinspection ReturnOfCollectionOrArrayField
		return _excludeFilters;
	}


	public List<String> getExcludeBaselineBugs() {
		//noinspection ReturnOfCollectionOrArrayField
		return _excludeBaselineBugs;
	}


	public List<String> getPlugins() {
		//noinspection ReturnOfCollectionOrArrayField
		return _plugins;
	}


	public void setBugCategories(final Map<String, String> bugCategories) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_bugCategories = bugCategories;
	}


	public UserPreferences getUserPreferences() {
		return _userPreferences;
	}


	public void setUserPreferences(final UserPreferences userPreferences) {
		_userPreferences = userPreferences;
	}


	public void addIncludeFilter(final String path) {
		_includeFilters.add(path);
		setModified(true);
	}


	public void removeIncludeFilter(final String path) {
		_includeFilters.remove(path);
		setModified(true);
	}


	public void removeIncludeFilter(final int index) {
		final String path = _includeFilters.get(index);
		removeIncludeFilter(path);
	}


	public void addExcludeFilter(final String path) {
		_excludeFilters.add(path);
		setModified(true);
	}


	public void removeExcludeFilter(final String path) {
		_excludeFilters.remove(path);
		setModified(true);
	}


	public void removeExcludeFilter(final int index) {
		final String path = _excludeFilters.get(index);
		removeExcludeFilter(path);
	}


	public void addBaselineExcludeFilter(final String path) {
		_excludeBaselineBugs.add(path);
		setModified(true);
	}


	public void removeBaselineExcludeFilter(final String path) {
		_excludeBaselineBugs.remove(path);
		setModified(true);
	}


	public void removeBaselineExcludeFilter(final int index) {
		final String path = _excludeBaselineBugs.get(index);
		removeBaselineExcludeFilter(path);
	}


	public void addPlugin(final String path) {
		_plugins.add(path);
		setModified(true);
	}


	public void removePlugin(final String path) {
		_plugins.remove(path);
		setModified(true);
	}


	public void removePlugin(final int index) {
		final String path = _plugins.get(index);
		removePlugin(path);
	}


	public List<String> getEnabledModuleConfigs() {
		//noinspection ReturnOfCollectionOrArrayField
		return _enabledModuleConfigs;
	}


	public void setEnabledModuleConfigs(final List<String> enabledModuleConfigs) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_enabledModuleConfigs = enabledModuleConfigs;
		setModified(true);
	}


	public void enabledModuleConfig(final String moduleName, final boolean enabled) {
		if (_enabledModuleConfigs.contains(moduleName) && !enabled) {
			_enabledModuleConfigs.remove(moduleName);
			setModified(true);
		} else if (!_enabledModuleConfigs.contains(moduleName)) {
			_enabledModuleConfigs.add(moduleName);
			setModified(true);
		}
	}


	public boolean isModuleConfigEnabled(final Module module) {
		return isModuleConfigEnabled(module.getName());
	}


	public boolean isModuleConfigEnabled(final String moduleName) {
		return _enabledModuleConfigs.contains(moduleName);
	}


	@Override
	public void clear() {
		super.clear();

		getDetectors().clear();
		getBugCategories().clear();
		getIncludeFilters().clear();
		getExcludeFilters().clear();
		getExcludeBaselineBugs().clear();
		getEnabledModuleConfigs().clear();
		getPlugins().clear();
		setModified(true);
	}


	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof FindBugsPreferences)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}

		final FindBugsPreferences that = (FindBugsPreferences) o;

		return COMPILE_BEFORE_ANALYZE == that.COMPILE_BEFORE_ANALYZE;

	}


	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (COMPILE_BEFORE_ANALYZE ? 1 : 0);
		return result;
	}


	public static FindBugsPreferences createEmpty(final List<String> pluginList) {
		loadPlugins(pluginList);
		final UserPreferences userPrefs = UserPreferences.createDefaultUserPreferences();
		final ProjectFilterSettings filterSettings = userPrefs.getFilterSettings();
		final FindBugsPreferences preferences = new FindBugsPreferences();
		preferences.setUserPreferences(userPrefs);
		preferences.clear();
		preferences.setProperty(FindBugsPreferences.RUN_ANALYSIS_IN_BACKGROUND, false);
		//_preferences.setProperty(FindBugsPreferences.ANALYSIS_EFFORT_LEVEL, AnalysisEffort.valueOfLevel(AnalysisEffort.DEFAULT.getMessage()).getEffortLevel());
		preferences.setProperty(FindBugsPreferences.ANALYSIS_EFFORT_LEVEL, userPrefs.getEffort());
		//_preferences.setProperty(FindBugsPreferences.MIN_PRIORITY_TO_REPORT, ReportConfiguration.DEFAULT_PRIORITY);
		preferences.setProperty(FindBugsPreferences.MIN_PRIORITY_TO_REPORT, filterSettings.getMinPriority());
		preferences.setProperty(FindBugsPreferences.SHOW_HIDDEN_DETECTORS, false);

		final Map<String, String> bugCategories = new HashMap<String, String>();
		final Collection<String> categoryList = I18N.instance().getBugCategories();
		for (final String category : categoryList) {
			bugCategories.put(category, String.valueOf(filterSettings.containsCategory(category)));
		}
		preferences.setBugCategories(bugCategories);

		return preferences;
	}


	public static FindBugsPreferences createDefault() {
		final FindBugsPreferences preferences = createEmpty(Collections.<String>emptyList());
		final UserPreferences userPrefs = preferences.getUserPreferences();

		final Map<String, String> detectorsAvailableList = new HashMap<String, String>();
		//final Map<DetectorFactory, String> factoriesToBugAbbrev = new HashMap<DetectorFactory, String>();
		final Iterator<DetectorFactory> iterator = FindBugsPreferences.getDetectorFactorCollection().factoryIterator();
		while (iterator.hasNext()) {
			final DetectorFactory factory = iterator.next();

			// Only configure non-hidden factories
			if (factory.isHidden()) {
				//continue;
			}

			detectorsAvailableList.put(factory.getShortName(), String.valueOf(userPrefs.isDetectorEnabled(factory)));
			//addBugsAbbreviation(factory);
		}

		preferences.setDetectors(detectorsAvailableList);


		return preferences;
	}


	public static synchronized DetectorFactoryCollection getDetectorFactorCollection() {
		if (_detectorFactoryCollection == null) {
			_detectorFactoryCollection = DetectorFactoryCollection.rawInstance();
		}
		return _detectorFactoryCollection;
	}


}
