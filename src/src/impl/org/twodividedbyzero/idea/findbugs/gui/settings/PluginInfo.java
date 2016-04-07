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
package org.twodividedbyzero.idea.findbugs.gui.settings;

import edu.umd.cs.findbugs.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsCustomPluginUtil;

final class PluginInfo {
	@Nullable
	private final String title;

	@Nullable
	private final String id;

	@Nullable
	private final String text;

	@NotNull
	private String url;

	@Nullable
	private String website;

	private boolean enabled;

	private PluginInfo(
			@Nullable final String title,
			@Nullable final String id,
			@Nullable final String text,
			@NotNull final String url,
			@Nullable final String website
	) {
		this.title = title;
		this.id = id;
		this.text = text;
		this.url = url;
		this.website = website;
	}

	@Nullable
	String getTitle() {
		return title;
	}

	@Nullable
	String getId() {
		return id;
	}

	@Nullable
	String getText() {
		return text;
	}

	@NotNull
	String getUrl() {
		return url;
	}

	@Nullable
	String getWebsite() {
		return website;
	}

	boolean isEnabled() {
		return enabled;
	}

	@NotNull
	static PluginInfo create(@NotNull final Plugin plugin) {
		return new PluginInfo(
				plugin.getShortDescription(),
				plugin.getPluginId(),
				plugin.getDetailedDescription(),
				FindBugsCustomPluginUtil.getAsString(plugin),
				plugin.getWebsite()
		);
	}
}
