/*
 * Copyright 2010 Andre Pfeiler
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
