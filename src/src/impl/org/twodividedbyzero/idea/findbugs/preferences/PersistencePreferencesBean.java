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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.90-dev
 */
public class PersistencePreferencesBean {

	// do not make fields final or private - initialized per reflection from intellij persistence framework
	public Map<String, String> _basePreferences;

	public Map<String, String> _detectors;
	public Map<String, String> _reportCategories;

	public List<String> _includeFilters;
	public List<String> _excludeFilters;
	public List<String> _excludeBaselineBugs;
	public List<String> _plugins;

	public List<String> _enabledModuleConfigs;


	public PersistencePreferencesBean() {
		_basePreferences = new HashMap<String, String>();

		_detectors = new HashMap<String, String>();
		_reportCategories = new HashMap<String, String>();

		_includeFilters = new ArrayList<String>();
		_excludeFilters = new ArrayList<String>();
		_excludeBaselineBugs = new ArrayList<String>();
		_plugins = new ArrayList<String>();

		_enabledModuleConfigs = new ArrayList<String>();
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


	public List<String> getEnabledModuleConfigs() {
		//noinspection ReturnOfCollectionOrArrayField
		return _enabledModuleConfigs;
	}


	public boolean isEmpty() {
		return _basePreferences.isEmpty() && _detectors.isEmpty();
	}


	/*public static void clearAndSet(final Collection<?> collection) {
		collection.clear();
		collection.addAll(collection);
	}*/

}
