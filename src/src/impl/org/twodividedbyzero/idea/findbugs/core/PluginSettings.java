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

import edu.umd.cs.findbugs.Plugin;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.New;

import java.util.Iterator;
import java.util.Map;

public final class PluginSettings implements Comparable<PluginSettings> {

	/**
	 * Plugin id ({@link Plugin#getPluginId()}).
	 */
	public String id = "Unknown";

	public boolean enabled = false;

	/**
	 * File path to the plugin jar file.
	 */
	public String path = "Unknown";

	/**
	 * Detector enabled state for this plugin.
	 * Like {@link AbstractSettings#detectors}.
	 */
	public Map<String, Boolean> detectors = New.map();

	@Override
	public int compareTo(@NotNull final PluginSettings o) {
		int ret = id.compareTo(o.id);
		if (ret == 0) {
			ret = path.compareTo(o.path);
			if (ret == 0) {
				ret = Boolean.valueOf(enabled).compareTo(o.enabled);
				if (ret == 0) {
					final Iterator<Map.Entry<String, Boolean>> a = detectors.entrySet().iterator();
					final Iterator<Map.Entry<String, Boolean>> b = o.detectors.entrySet().iterator();
					while (true) {
						final boolean aHasNext = a.hasNext();
						final boolean bHasNext = b.hasNext();
						if (!aHasNext && !bHasNext) {
							return 0;
						} else if (aHasNext && bHasNext) {
							final Map.Entry<String, Boolean> aNext = a.next();
							final Map.Entry<String, Boolean> bNext = b.next();
							ret = aNext.getValue().compareTo(bNext.getValue());
							if (ret != 0) {
								return ret;
							}
							ret = aNext.getValue().compareTo(bNext.getValue());
							if (ret != 0) {
								return ret;
							}
						} else if (aHasNext) {
							return 1;
						} else {
							return -1;
						}
					}
				}
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
		if (!path.equals(settings.path)) return false;
		return detectors.equals(settings.detectors);

	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + (enabled ? 1 : 0);
		result = 31 * result + path.hashCode();
		result = 31 * result + detectors.hashCode();
		return result;
	}
}
