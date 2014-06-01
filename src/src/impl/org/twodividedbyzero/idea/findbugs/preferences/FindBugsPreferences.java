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
package org.twodividedbyzero.idea.findbugs.preferences;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.Plugin;
import edu.umd.cs.findbugs.config.ProjectFilterSettings;
import edu.umd.cs.findbugs.config.UserPreferences;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.StringUtil;
import org.twodividedbyzero.idea.findbugs.gui.preferences.AnnotationType;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.9
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings({"SE_TRANSIENT_FIELD_NOT_RESTORED"})
@SuppressWarnings({"HardCodedStringLiteral", "AssignmentToCollectionOrArrayFieldFromParameter"})
public class FindBugsPreferences extends Properties {

	private static final long serialVersionUID = 3L;

	private static final Logger LOGGER = Logger.getInstance(FindBugsPreferences.class.getName());
	@SuppressWarnings("StaticVariableMayNotBeInitialized")
	private static DetectorFactoryCollection _detectorFactoryCollection;

	//public static final String DEFAULT_CONFIG = "";

	//public static final String CONFIG_FILE = "config-file";

	/** The prefix for stored properties. */
	public static final String PROPERTIES_PREFIX = "property.";

	public static final String RUN_ANALYSIS_IN_BACKGROUND = PROPERTIES_PREFIX + "runAnalysisInBackground";
	public static final String ANALYSIS_EFFORT_LEVEL = PROPERTIES_PREFIX + "analysisEffortLevel";
	public static final String MIN_PRIORITY_TO_REPORT = PROPERTIES_PREFIX + "minPriorityToReport";
	public static final String SHOW_HIDDEN_DETECTORS = PROPERTIES_PREFIX + "showHiddenDetectors";

	public static final String TOOLWINDOW_TO_FRONT = PROPERTIES_PREFIX + "toolWindowToFront";
	public static final String COMPILE_BEFORE_ANALYZE = PROPERTIES_PREFIX + "compileBeforeAnalyse";

	public static final String ANALYZE_AFTER_COMPILE = PROPERTIES_PREFIX + "analyzeAfterCompile";

	public static final String EXPORT_BASE_DIR = PROPERTIES_PREFIX + "exportBaseDir";
	public static final String EXPORT_CREATE_ARCHIVE_DIR = PROPERTIES_PREFIX + "exportCreateArchiveDir";
	public static final String EXPORT_AS_HTML = PROPERTIES_PREFIX + "exportAsHtml";
	public static final String EXPORT_AS_XML = PROPERTIES_PREFIX + "exportAsXml";
	public static final String EXPORT_OPEN_BROWSER = PROPERTIES_PREFIX + "exportOpenBrowser";

	public static final String TOOLWINDOW_SCROLL_TO_SOURCE = PROPERTIES_PREFIX + "toolWindowScrollToSource";
	public static final String TOOLWINDOW_EDITOR_PREVIEW = PROPERTIES_PREFIX + "toolWindowEditorPreview";
	public static final String TOOLWINDOW_GROUP_BY = PROPERTIES_PREFIX + "toolWindowGroupBy";

	public static final String ANNOTATION_SUPPRESS_WARNING_CLASS = PROPERTIES_PREFIX + "annotationSuppressWarningsClass";
	public static final String ANNOTATION_GUTTER_ICON_ENABLED = PROPERTIES_PREFIX + "annotationGutterIconEnabled";
	public static final String ANNOTATION_TEXT_RAGE_MARKUP_ENABLED = PROPERTIES_PREFIX + "annotationTextRangeMarkupEnabled";
	public static final String ANNOTATION_TYPE_SETTINGS = PROPERTIES_PREFIX + "annotationTypeSettings";
	public static final String DEFAULT_ANNOTATION_CLASS_NAME = "edu.umd.cs.findbugs.annotations.SuppressFBWarnings";

	public transient boolean _annotationTextRangeMarkupEnabled;
	public transient boolean _annotationGutterIconEnabled;
	public transient String _annotationSuppressWarningsClass;

	public transient Map<String, String> _detectors;
	public transient Map<String, String> _bugCategories;
	public transient List<String> _includeFilters;
	public transient Map<String, Boolean> _includeFiltersMap;
	public transient List<String> _excludeFilters;
	public transient Map<String, Boolean> _excludeFiltersMap;
	public transient List<String> _excludeBaselineBugs;
	public transient Map<String, Boolean> _excludeBaselineBugsMap;
	/** URL's of extra plugins to load */
	public transient List<String> _plugins;
	/** A list of plugin ID's */
	public transient List<String> _disabledPlugins;

	public transient List<String> _enabledModuleConfigs;

	public transient UserPreferences _userPreferences;

	public transient Map<String, Map<String, String>> _annotationTypeSettings;

	private boolean _isModified;


	private FindBugsPreferences() {
		initDefaults();
	}


	private void initDefaults() {
		_detectors = new HashMap<String, String>();
		_bugCategories = new HashMap<String, String>(/*getBugCategories()*/);
		_includeFilters = new ArrayList<String>();
		_includeFiltersMap = new HashMap<String, Boolean>();
		_excludeFilters = new ArrayList<String>();
		_excludeFiltersMap = new HashMap<String, Boolean>();
		_excludeBaselineBugs = new ArrayList<String>();
		_excludeBaselineBugsMap = new HashMap<String, Boolean>();
		_plugins = new ArrayList<String>();
		_disabledPlugins = new ArrayList<String>();
		_enabledModuleConfigs = new ArrayList<String>();

		_annotationGutterIconEnabled = true;
		_annotationSuppressWarningsClass = DEFAULT_ANNOTATION_CLASS_NAME;
		_annotationTextRangeMarkupEnabled = true;

		_annotationTypeSettings = createDefaultAnnotationTypeSettings();

		_userPreferences = UserPreferences.createDefaultUserPreferences();
	}


	public static Map<String, Map<String, String>> createDefaultAnnotationTypeSettings() {
		final Map<String, Map<String, String>> annotationTypeSettings = new HashMap<String, Map<String, String>>();
		for (final AnnotationType annotationType : AnnotationType.values()) {
			final Map<String, String> value = new HashMap<String, String>();
			value.put(AnnotationType.FOREGROUND, String.valueOf(annotationType.getForegroundColor().getRGB()));
			value.put(AnnotationType.BACKGROUND, String.valueOf(annotationType.getBackgroundColor().getRGB()));
			value.put(AnnotationType.EFFECT_COLOR, String.valueOf(annotationType.getEffectColor().getRGB()));
			value.put(AnnotationType.EFFECT_TYPE, String.valueOf(annotationType.getEffectType().name()));
			value.put(AnnotationType.FONT, String.valueOf(annotationType.getFont()));
			annotationTypeSettings.put(annotationType.name(), value);
		}

		return annotationTypeSettings;
	}


	/**
	 * Get all FindBugs-IDEA properties defined in the configuration.
	 *
	 * @return a map of FindBugs-IDEA property names to values.
	 */
	public Map<String, String> getDefinedProperties() {
		final Map<String, String> values = new HashMap<String, String>();

		final Enumeration<?> properties = propertyNames();
		while (properties.hasMoreElements()) {
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
		//noinspection ForLoopWithMissingComponent
		for (final Enumeration<?> confNames = properties.propertyNames(); confNames.hasMoreElements(); ) {
			final String elementName = (String) confNames.nextElement();
			final String value = properties.getProperty(elementName);
			setProperty(elementName, value);
			setBugCategories(properties.getBugCategories());
			setDetectors(properties.getDetectors());
			setAnnotationTypeSettings(properties.getAnnotationTypeSettings());
			setModified(true);
		}
	}


	public void setDefinedProperties(final Map<String, String> properties) {
		if (properties == null || properties.isEmpty()) {
			return;
		}


		for (final java.util.Map.Entry<String, String> entry : properties.entrySet()) {
			final String value = entry.getValue();
			if (value != null) {
				setProperty(PROPERTIES_PREFIX + entry, value);
				setModified(true);
			}
		}

		/*for (final String propertyName : properties.keySet()) {
			final String value = properties.get(propertyName);
			if (value != null) {
				setProperty(PROPERTIES_PREFIX + propertyName, value);
				setModified(true);
			}
		}*/
	}


	public void clearDefinedProperies() {
		final Collection<String> propertiesToRemove = new ArrayList<String>();

		//noinspection ForLoopWithMissingComponent
		for (final Enumeration<?> properties = propertyNames(); properties.hasMoreElements(); ) {
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


	public void setProperty(final String propertyName, final Iterable<String> value) {
		if (value == null) {
			//noinspection ConstantConditions
			setProperty(propertyName, (String) null);
			return;
		}

		final StringBuilder valueString = new StringBuilder();
		for (final String part : value) {
			if (valueString.length() > 0) {
				valueString.append(';');
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
	public synchronized Object setProperty(final String key, @NotNull final String value) {
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
		applyDetectors();
		setModified(true);
	}


	private void setDetectorEnabled(final DetectorFactory detector, boolean enabled) {
		if (enabled) {
			getDetectors().put(detector.getShortName(), String.valueOf(enabled));
		} else {
			getDetectors().remove(detector.getShortName());
		}
	}


	public void applyDetectors() {
		final DetectorFactoryCollection detectorFactoryCollection = FindBugsPreferences.getDetectorFactorCollection();

		final Iterator<DetectorFactory> iterator = detectorFactoryCollection.factoryIterator();
		while (iterator.hasNext()) {
			final DetectorFactory factory = iterator.next();
			final Plugin plugin = factory.getPlugin();
			final boolean enabledByUser = Boolean.valueOf(getDetectors().get(factory.getShortName()));
			final boolean enable = enabledByUser && plugin.isGloballyEnabled();
			getUserPreferences().enableDetector(factory, enable);
		}
		_detectors = getAvailableDetectors(getUserPreferences());
	}


	private void loadPlugins(final List<String> plugins, final List<String> disabledPlugins) {
		_plugins.clear();
		_plugins.addAll(plugins);
		_disabledPlugins.clear();
		_disabledPlugins.addAll(disabledPlugins);

		// TODO: show notification on error/not found
		for (final String pluginUrl : plugins) {
			try {
				final File file = getPluginAsFile(pluginUrl);
				if (checkPlugin(file)) {
					final Plugin plugin = Plugin.loadCustomPlugin(new URL(pluginUrl), null);
					final boolean enable = !disabledPlugins.contains(plugin.getPluginId());
					plugin.setGloballyEnabled(enable);
				} else {
					LOGGER.warn("Plugin '" + pluginUrl + "' not loaded. Archive '" + file + "' does not exists.");
				}
			} catch (final Exception e) {
				LOGGER.warn("Could not load custom findbugs plugin: " + pluginUrl, e);
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


	public static List<String> collectInvalidPlugins(final Iterable<String> plugins) {
		final List<String> invalid = new ArrayList<String>();
		for (final String plugin : plugins) {
			try {
				final File file = getPluginAsFile(plugin);
				if (!checkPlugin(file)) {
					invalid.add(plugin);
				}
			} catch (final MalformedURLException e) {
				LOGGER.debug("invalid plugin.", e);
				invalid.add(plugin);
			}
		}
		return invalid;
	}


	public static boolean checkPlugin(final File plugin) {
		return plugin.exists() && plugin.canRead();
	}


	public static String getPluginAsString(final Plugin plugin) {
		return plugin.getPluginLoader().getURL().toExternalForm();
	}


	public static String getPluginAsString(final File plugin) throws MalformedURLException {
		return plugin.toURI().toURL().toExternalForm();
	}


	public static File getPluginAsFile(final String plugin) throws MalformedURLException {
		return new File(new URL(plugin).getFile());
	}


	public Map<String, String> getBugCategories() {
		//noinspection ReturnOfCollectionOrArrayField
		return _bugCategories;
	}


	public Collection<String> getIncludeFilters() {
		//noinspection ReturnOfCollectionOrArrayField
		return _includeFilters;
	}


	public Map<String, Boolean> getIncludeFiltersMap() {
		if (_includeFiltersMap.size() != _includeFilters.size()) {
			for (final String includeFilter : _includeFilters) {
				_includeFiltersMap.put(includeFilter, true);
			}

		}
		//noinspection ReturnOfCollectionOrArrayField
		return _includeFiltersMap;
	}


	public Collection<String> getExcludeFilters() {
		//noinspection ReturnOfCollectionOrArrayField
		return _excludeFilters;
	}


	public Map<String, Boolean> getExcludeFiltersMap() {
		if (_excludeFiltersMap.size() != _excludeFilters.size()) {
			for (final String excludeFilter : _excludeFilters) {
				_excludeFiltersMap.put(excludeFilter, true);
			}

		}
		//noinspection ReturnOfCollectionOrArrayField
		return _excludeFiltersMap;
	}


	public Collection<String> getExcludeBaselineBugs() {
		//noinspection ReturnOfCollectionOrArrayField
		return _excludeBaselineBugs;
	}


	public Map<String, Boolean> getExcludeBaselineBugsMap() {
		if (_excludeBaselineBugsMap.size() != _excludeBaselineBugs.size()) {
			for (final String excludeBaseLineBug : _excludeBaselineBugs) {
				_excludeBaselineBugsMap.put(excludeBaseLineBug, true);
			}

		}
		//noinspection ReturnOfCollectionOrArrayField
		return _excludeBaselineBugsMap;
	}


	public Collection<String> getPlugins() {
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


	public void addPlugin(final Plugin plugin) {
		_plugins.add(getPluginAsString(plugin));
		enablePlugin(plugin, true);
		setModified(true);
	}


	public void removePlugin(final Plugin plugin) {
		_plugins.remove(getPluginAsString(plugin));
		setModified(true);
	}


	public Collection<String> getEnabledModuleConfigs() {
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
	public synchronized void clear() {
		super.clear();

		getDetectors().clear();
		getBugCategories().clear();
		getIncludeFilters().clear();
		getIncludeFiltersMap().clear();
		getExcludeFilters().clear();
		getExcludeFiltersMap().clear();
		getExcludeBaselineBugs().clear();
		getExcludeBaselineBugsMap().clear();
		getEnabledModuleConfigs().clear();
		getPlugins().clear();
		getAnnotationTypeSettings().clear();
		_disabledPlugins.clear();
		_annotationTypeSettings.clear();
		_annotationGutterIconEnabled = true;
		_annotationTextRangeMarkupEnabled = true;
		_annotationSuppressWarningsClass = DEFAULT_ANNOTATION_CLASS_NAME;

		setModified(true);
	}


	@Override
	public synchronized boolean equals(final Object o) {
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

		//noinspection AccessStaticViaInstance
		return COMPILE_BEFORE_ANALYZE.equals(that.COMPILE_BEFORE_ANALYZE);

	}


	@Override
	public synchronized int hashCode() {
		int result = super.hashCode();
		result = 31 * result;
		return result;
	}


	public static FindBugsPreferences createEmpty(final List<String> plugins, final List<String> disabledPlugins) {
		final FindBugsPreferences preferences = new FindBugsPreferences();
		preferences.clear();
		preferences.loadPlugins(plugins, disabledPlugins);

		final UserPreferences userPrefs = UserPreferences.createDefaultUserPreferences();
		final ProjectFilterSettings filterSettings = userPrefs.getFilterSettings();
		preferences.setUserPreferences(userPrefs);
		preferences.setProperty(FindBugsPreferences.RUN_ANALYSIS_IN_BACKGROUND, false);
		//_preferences.setProperty(FindBugsPreferences.ANALYSIS_EFFORT_LEVEL, AnalysisEffort.valueOfLevel(AnalysisEffort.DEFAULT.getMessage()).getEffortLevel());
		preferences.setProperty(FindBugsPreferences.ANALYSIS_EFFORT_LEVEL, userPrefs.getEffort());
		//_preferences.setProperty(FindBugsPreferences.MIN_PRIORITY_TO_REPORT, ReportConfiguration.DEFAULT_PRIORITY);
		preferences.setProperty(FindBugsPreferences.MIN_PRIORITY_TO_REPORT, filterSettings.getMinPriority());
		preferences.setProperty(FindBugsPreferences.SHOW_HIDDEN_DETECTORS, false);
		preferences.setProperty(FindBugsPreferences.TOOLWINDOW_TO_FRONT, true);
		preferences.setProperty(FindBugsPreferences.ANALYZE_AFTER_COMPILE, false);

		preferences.setProperty(FindBugsPreferences.EXPORT_AS_HTML, true);
		preferences.setProperty(FindBugsPreferences.EXPORT_AS_XML, true);
		preferences.setProperty(FindBugsPreferences.EXPORT_BASE_DIR, "");
		preferences.setProperty(FindBugsPreferences.EXPORT_CREATE_ARCHIVE_DIR, false);
		preferences.setProperty(FindBugsPreferences.EXPORT_OPEN_BROWSER, true);

		final Map<String, String> bugCategories = getDefaultBugCategories(filterSettings);
		preferences.setBugCategories(bugCategories);

		preferences.setProperty(FindBugsPreferences.ANNOTATION_SUPPRESS_WARNING_CLASS, DEFAULT_ANNOTATION_CLASS_NAME);
		preferences.setProperty(FindBugsPreferences.ANNOTATION_GUTTER_ICON_ENABLED, true);
		preferences.setProperty(FindBugsPreferences.ANNOTATION_TEXT_RAGE_MARKUP_ENABLED, true);

		preferences.setAnnotationTypeSettings(createDefaultAnnotationTypeSettings());

		preferences.setDetectors(FindBugsPreferences.getAvailableDetectors(preferences.getUserPreferences()));


		return preferences;
	}


	public static FindBugsPreferences createDefault() {
		final FindBugsPreferences preferences = createEmpty(Collections.<String>emptyList(), Collections.<String>emptyList());
		final UserPreferences userPrefs = preferences.getUserPreferences();

		final Map<String, String> detectorsAvailableList = getAvailableDetectors(userPrefs);
		preferences.setDetectors(detectorsAvailableList);

		preferences.setAnnotationTypeSettings(createDefaultAnnotationTypeSettings());

		return preferences;
	}


	public static Map<String, String> getDefaultBugCategories(final ProjectFilterSettings filterSettings) {
		final Map<String, String> bugCategories = new HashMap<String, String>();
		final Collection<String> categoryList = DetectorFactoryCollection.instance().getBugCategories();
		for (final String category : categoryList) {
			bugCategories.put(category, String.valueOf(filterSettings.containsCategory(category)));
		}
		return bugCategories;
	}


	public static Map<String, String> getAvailableDetectors(final UserPreferences userPrefs) {
		final Map<String, String> detectorsAvailableList = new HashMap<String, String>();
		//final Map<DetectorFactory, String> factoriesToBugAbbrev = new HashMap<DetectorFactory, String>();
		final Iterator<DetectorFactory> iterator = FindBugsPreferences.getDetectorFactorCollection().factoryIterator();
		while (iterator.hasNext()) {
			final DetectorFactory factory = iterator.next();

			// Only configure non-hidden factories
			/*if (factory.isHidden()) {
				//continue;
			}*/

			detectorsAvailableList.put(factory.getShortName(), String.valueOf(userPrefs.isDetectorEnabled(factory)));
			//addBugsAbbreviation(factory);
		}
		return detectorsAvailableList;
	}


	public static synchronized DetectorFactoryCollection getDetectorFactorCollection() {
		if (_detectorFactoryCollection == null) {
			_detectorFactoryCollection = DetectorFactoryCollection.instance();
		}
		return _detectorFactoryCollection;
	}


	public void enablePlugin(final Plugin plugin, final boolean enabled) {
		final String pluginId = plugin.getPluginId();
		if (enabled) {
			_disabledPlugins.remove(pluginId);
		} else {
			_disabledPlugins.add(pluginId);
		}
		plugin.setGloballyEnabled(enabled);
		for (final DetectorFactory detector : plugin.getDetectorFactories()) {
			setDetectorEnabled(detector, enabled && detector.isDefaultEnabled());
		}
		setModified(true);
	}


	public Collection<String> getDisabledPlugins() {
		return _disabledPlugins;
	}


	public boolean isPluginInstalled(final Plugin plugin) {
		return getPlugins().contains(getPluginAsString(plugin));
	}


	public boolean isPluginDisabled(final Plugin plugin) {
		return isPluginDisabled(plugin.getPluginId());
	}


	public boolean isPluginDisabled(final String pluginId) {
		return _disabledPlugins.contains(pluginId);
	}


	public boolean isAnnotationTextRangeMarkupEnabled() {
		return _annotationTextRangeMarkupEnabled;
	}


	public void setAnnotationTextRangeMarkupEnabled(final boolean annotationTextRangeMarkupEnabled) {
		_annotationTextRangeMarkupEnabled = annotationTextRangeMarkupEnabled;
		setProperty(ANNOTATION_TEXT_RAGE_MARKUP_ENABLED, annotationTextRangeMarkupEnabled);
	}


	public boolean isAnnotationGutterIconEnabled() {
		return _annotationGutterIconEnabled;
	}


	public void setAnnotationGutterIconEnabled(final boolean annotationGutterIconEnabled) {
		_annotationGutterIconEnabled = annotationGutterIconEnabled;
		setProperty(ANNOTATION_GUTTER_ICON_ENABLED, annotationGutterIconEnabled);
	}


	public String getAnnotationSuppressWarningsClass() {
		return _annotationSuppressWarningsClass;
	}


	public void setAnnotationSuppressWarningsClass(final String annotationSuppressWarningsClass) {
		_annotationSuppressWarningsClass = StringUtil.isEmpty(annotationSuppressWarningsClass) ? DEFAULT_ANNOTATION_CLASS_NAME : annotationSuppressWarningsClass;
		setProperty(ANNOTATION_SUPPRESS_WARNING_CLASS, _annotationSuppressWarningsClass);
	}


	public Map<String, Map<String, String>> getAnnotationTypeSettings() {
		if (_annotationTypeSettings == null || _annotationTypeSettings.isEmpty()) {
			_annotationTypeSettings = createDefaultAnnotationTypeSettings();
		}
		return _annotationTypeSettings;
	}


	public Map<String, String> getFlattendAnnotationTypeSettings() {
		return AnnotationType.flatten(getAnnotationTypeSettings());
	}


	public void setAnnotationTypeSettings(final Map<String, Map<String, String>> annotationTypeSettings) {
		_annotationTypeSettings = annotationTypeSettings;
		getAnnotationTypeSettings();
	}


	public void setFlattendAnnotationTypeSettings(final Map<String, String> annotationTypeSettings) {
		_annotationTypeSettings = AnnotationType.complex(annotationTypeSettings);
	}
}
