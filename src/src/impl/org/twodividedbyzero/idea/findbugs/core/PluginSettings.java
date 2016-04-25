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

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Tag;
import edu.umd.cs.findbugs.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.MapUtil;
import org.twodividedbyzero.idea.findbugs.common.util.New;

import java.util.Map;
import java.util.Set;

@Tag(value = "plugin")
public final class PluginSettings implements Comparable<PluginSettings> {

	/**
	 * Plugin id ({@link Plugin#getPluginId()}).
	 */
	@Attribute
	public String id = "Unknown";

	@Attribute
	public boolean enabled = false;

	/**
	 * True if this settings belongs to a bundled plugin ({@link org.twodividedbyzero.idea.findbugs.plugins.Plugins}).
	 * Note that {@code url} is {@code null} for bundled plugins.
	 */
	@Attribute
	public boolean bundled;

	/**
	 * URL of plugin jar file. {@code null} if this settings belongs to a bundled plugin.
	 */
	@Tag
	public String url;

	/**
	 * Detector enabled state for this plugin.
	 * Like {@link AbstractSettings#detectors}.
	 */
	@Tag(value = "detectors")
	@MapAnnotation(
			surroundWithTag = false,
			surroundValueWithTag = false,
			surroundKeyWithTag = false,
			entryTagName = "detector",
			keyAttributeName = "name",
			valueAttributeName = "enabled"
	)
	public Map<String, Boolean> detectors = New.map();

	@Override
	public int compareTo(@NotNull final PluginSettings o) {
		int ret = id.compareTo(o.id);
		if (ret == 0) {
			ret = Boolean.valueOf(bundled).compareTo(o.bundled); // LATER: use Boolean.compare when JRE 1.6 support is gone
			if (ret == 0) {
				ret = StringUtil.compare(url, o.url, false);
				if (ret == 0) {
					ret = Boolean.valueOf(enabled).compareTo(o.enabled); // LATER: use Boolean.compare when JRE 1.6 support is gone
					if (ret == 0) {
						ret = MapUtil.compare(detectors, o.detectors);
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

		final PluginSettings that = (PluginSettings) o;

		if (enabled != that.enabled) return false;
		if (bundled != that.bundled) return false;
		if (!id.equals(that.id)) return false;
		if (url != null ? !url.equals(that.url) : that.url != null) return false;
		return detectors.equals(that.detectors);

	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + (enabled ? 1 : 0);
		result = 31 * result + (bundled ? 1 : 0);
		result = 31 * result + (url != null ? url.hashCode() : 0);
		//result = 31 * result + detectors.hashCode(); do not do this
		return result;
	}

	@Nullable
	public static PluginSettings findBundledById(@NotNull final Set<PluginSettings> plugins, @NotNull final String id) {
		for (final PluginSettings plugin : plugins) {
			if (plugin.bundled) {
				if (id.equals(plugin.id)) {
					return plugin;
				}
			}
		}
		return null;
	}

}
