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

import edu.umd.cs.findbugs.config.UserPreferences;

import java.util.Locale;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.9-dev
 */
public enum AnalysisEffort {

	MIN(UserPreferences.EFFORT_MIN, "Minimal", 10),
	DEFAULT(UserPreferences.EFFORT_DEFAULT, "Default", 20),
	MAX(UserPreferences.EFFORT_MAX, "Maximal", 30);

	private final String _effortLevel;
	private final String _message;
	private final int _value;


	AnalysisEffort(final String effortLevel, final String message, final int value) {
		_effortLevel = effortLevel;
		_message = message;
		_value = value;
	}


	public String getEffortLevel() {
		return _effortLevel;
	}


	public static AnalysisEffort valueOfLevel(final String effortLevel) {
		return valueOf(effortLevel.toUpperCase(Locale.ENGLISH));
	}


	public String getMessage() {
		return _message;
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("AnalysisEffort");
		sb.append("{_effortLevel='").append(_effortLevel).append('\'');
		sb.append(", _message='").append(_message).append('\'');
		sb.append(", _value=").append(_value);
		sb.append('}');
		return sb.toString();
	}


	public int getValue() {
		return _value;
	}


	public static String getEffortLevelByValue(final int value) {
		for (final AnalysisEffort effort : AnalysisEffort.values()) {
			if(effort.getValue() == value) {
				return effort.getEffortLevel();
			}
		}
		return AnalysisEffort.DEFAULT.getEffortLevel();
	}
}
