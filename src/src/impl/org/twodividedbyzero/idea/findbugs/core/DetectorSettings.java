/*
 * Copyright 2016 Andre Pfeiler
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

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;

@Tag(value = "detector")
public final class DetectorSettings implements Comparable<DetectorSettings> {

	@Attribute
	public String pluginId = "Unknown";

	@Attribute
	public boolean enabled = false;

	@Attribute(value = "name")
	public String shortName = "Unknown";

	@Override
	public int compareTo(@NotNull final DetectorSettings o) {
		int ret = pluginId.compareTo(o.pluginId);
		if (ret == 0) {
			ret = shortName.compareTo(o.shortName);
			if (ret == 0) {
				ret = Boolean.valueOf(enabled).compareTo(o.enabled);
			}
		}
		return ret;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final DetectorSettings that = (DetectorSettings) o;

		if (enabled != that.enabled) return false;
		if (!pluginId.equals(that.pluginId)) return false;
		return shortName.equals(that.shortName);

	}

	@Override
	public int hashCode() {
		int result = pluginId.hashCode();
		result = 31 * result + (enabled ? 1 : 0);
		result = 31 * result + shortName.hashCode();
		return result;
	}
}
