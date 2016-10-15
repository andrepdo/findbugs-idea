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
package org.twodividedbyzero.idea.findbugs.preferences;

import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Legacy. Do not use this anymore. This will be converted by {@link org.twodividedbyzero.idea.findbugs.gui.preferences.LegacyProjectSettingsConverter}.
 */
public class PersistencePreferencesBean {
	// do not make fields final or private - initialized per reflection from intellij persistence framework
	public Map<String, String> _basePreferences;

	public Map<String, String> _detectors;
	public Map<String, String> _reportCategories;

	public List<String> _includeFilters;
	public List<String> _excludeFilters;
	public List<String> _excludeBaselineBugs;

	/**
	 * URL's, use {@link org.twodividedbyzero.idea.findbugs.common.util.FindBugsCustomPluginUtil#getAsFile(String)} etc.
	 */
	public List<String> _plugins;
	public Set<String> _enabledUserPluginIds;
	public Set<String> _disabledUserPluginIds;
	public Set<String> _enabledBundledPluginIds;
	public Set<String> _disabledBundledPluginIds;

	public List<String> _enabledModuleConfigs;

	public boolean _annotationTextRangeMarkupEnabled;
	public boolean _annotationGutterIconEnabled;
	public String _annotationSuppressWarningsClass;
	public Map<String, String> _annotationTypeSettings;


	public PersistencePreferencesBean() {
		_basePreferences = new HashMap<String, String>();

		_detectors = new HashMap<String, String>();
		_reportCategories = new HashMap<String, String>();

		_includeFilters = new ArrayList<String>();
		_excludeFilters = new ArrayList<String>();
		_excludeBaselineBugs = new ArrayList<String>();
		_plugins = new ArrayList<String>();
		_enabledUserPluginIds = new HashSet<String>();
		_disabledUserPluginIds = new HashSet<String>();
		_enabledBundledPluginIds = new HashSet<String>();
		_disabledBundledPluginIds = new HashSet<String>();

		_enabledModuleConfigs = new ArrayList<String>();

		_annotationGutterIconEnabled = true;
		_annotationSuppressWarningsClass = FindBugsPluginConstants.DEFAULT_SUPPRESS_WARNINGS_CLASSNAME;
		_annotationTextRangeMarkupEnabled = true;
		_annotationTypeSettings = new HashMap<String, String>();
	}


	public Map<String, String> getBasePreferences() {
		//noinspection ReturnOfCollectionOrArrayField
		return _basePreferences;
	}


	public Map<String, String> getDetectors() {
		//noinspection ReturnOfCollectionOrArrayField
		return _detectors;
	}


	public Map<String, String> getBugCategories() {
		//noinspection ReturnOfCollectionOrArrayField
		return _reportCategories;
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


	public Collection<String> getDisabledUserPluginIds() {
		return _disabledUserPluginIds;
	}


	public Collection<String> getEnabledBundledPluginIds() {
		return _enabledBundledPluginIds;
	}


	public Collection<String> getDisabledBundledPluginIds() {
		return _disabledBundledPluginIds;
	}


	public List<String> getEnabledModuleConfigs() {
		//noinspection ReturnOfCollectionOrArrayField
		return _enabledModuleConfigs;
	}


	public boolean isEmpty() {
		return _basePreferences.isEmpty() && _detectors.isEmpty();
	}


	public boolean isAnnotationTextRangeMarkupEnabled() {
		return _annotationTextRangeMarkupEnabled;
	}


	public void setAnnotationTextRangeMarkupEnabled(final boolean annotationTextRangeMarkupEnabled) {
		_annotationTextRangeMarkupEnabled = annotationTextRangeMarkupEnabled;
	}


	public boolean isAnnotationGutterIconEnabled() {
		return _annotationGutterIconEnabled;
	}


	public void setAnnotationGutterIconEnabled(final boolean annotationGutterIconEnabled) {
		_annotationGutterIconEnabled = annotationGutterIconEnabled;
	}


	public String getAnnotationSuppressWarningsClass() {
		return _annotationSuppressWarningsClass;
	}


	public void setAnnotationSuppressWarningsClass(final String annotationSuppressWarningsClass) {
		_annotationSuppressWarningsClass = annotationSuppressWarningsClass;
	}


	public Map<String, String> getAnnotationTypeSettings() {
		return _annotationTypeSettings;
	}


	public void setAnnotationTypeSettings(final Map<String, String> annotationTypeSettings) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_annotationTypeSettings = annotationTypeSettings;
	}
}
