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

import org.jetbrains.annotations.NotNull;

public final class PluginSettings implements Comparable<PluginSettings> {
	public String id = "Unknown";

	public boolean enabled = false;

	public String path = "Unknown";

	@Override
	public int compareTo(@NotNull final PluginSettings o) {
		int ret = id.compareTo(o.id);
		if (ret == 0) {
			ret = path.compareTo(o.path);
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

		final PluginSettings settings = (PluginSettings) o;

		if (enabled != settings.enabled) return false;
		if (!id.equals(settings.id)) return false;
		return path.equals(settings.path);

	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + (enabled ? 1 : 0);
		result = 31 * result + path.hashCode();
		return result;
	}
}
