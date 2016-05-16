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
package org.twodividedbyzero.idea.findbugs.plugins;

import edu.umd.cs.findbugs.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.PluginSettings;

import java.util.Comparator;

public final class PluginInfo {
	@NotNull
	public final PluginSettings settings;

	@Nullable
	public final String shortDescription;

	@Nullable
	public final String detailedDescription;

	@Nullable
	public final String website;

	@Nullable
	public final String errorMessage;

	private PluginInfo(
			@NotNull final PluginSettings settings,
			@Nullable final String shortDescription,
			@Nullable final String detailedDescription,
			@Nullable final String website,
			@Nullable final String errorMessage
	) {
		this.settings = settings;
		this.shortDescription = shortDescription;
		this.detailedDescription = detailedDescription;
		this.website = website;
		this.errorMessage = errorMessage;
	}

	@NotNull
	static PluginInfo create(@NotNull final PluginSettings settings, @NotNull final Plugin plugin) {
		return new PluginInfo(
				settings,
				plugin.getShortDescription(),
				plugin.getDetailedDescription(),
				plugin.getWebsite(),
				null
		);
	}

	@NotNull
	static PluginInfo create(@NotNull final PluginSettings settings, @NotNull final String error) {
		return new PluginInfo(
				settings,
				null,
				null,
				null,
				error
		);
	}

	/*
	@NotNull
	static PluginInfo createUser(@NotNull final Plugin plugin) throws MalformedURLException, PluginException {
		final PluginSettings settings = new PluginSettings();
		settings.id = plugin.getPluginId();
		settings.bundled = false;
		settings.enabled = true; // enable ; do not use plugin.isEnabledByDefault();
		settings.url = FindBugsCustomPluginUtil.getAsString(plugin);
		return new PluginInfo(
				settings,
				plugin.getShortDescription(),
				plugin.getDetailedDescription(),
				plugin.getWebsite(),
				null,
				null
		);
	}

	@NotNull
	static PluginInfo load(@NotNull final PluginSettings settings) {

		String shortDescription = null;
		String detailedDescription = null;
		String website = null;
		String errorMessage = null;
		Throwable error = null;

		final File file = new File(settings.path);
		if (!file.exists()) {
			errorMessage = ResourcesLoader.getString(ResourcesLoader.getString("error.path.exists", file.getPath()));
		} else if (!file.isFile()) {
			errorMessage = ResourcesLoader.getString(ResourcesLoader.getString("error.path.type", file.getPath()));
		} else if (!file.canRead()) {
			errorMessage = ResourcesLoader.getString(ResourcesLoader.getString("error.file.readable", file.getPath()));
		} else {
			Plugin plugin = null;
			try {
				plugin = FindBugsCustomPluginUtil.loadTemporary(file);
				shortDescription = plugin.getShortDescription();
				detailedDescription = plugin.getDetailedDescription();
				website = plugin.getWebsite();
			} catch (final Throwable e) {
				errorMessage = e.getMessage();
				error = e;
			} finally {
				if (plugin != null) {
					FindBugsCustomPluginUtil.unload(plugin);
				}
			}
		}
		return new PluginInfo(
				settings,
				shortDescription,
				detailedDescription,
				website,
				errorMessage,
				error
		);
	}*/

	public static final Comparator<PluginInfo> ByShortDescription = new Comparator<PluginInfo>() {
		@Override
		public int compare(final PluginInfo o1, final PluginInfo o2) {
			String a = o1.shortDescription;
			if (a == null) {
				a = "";
			}
			String b = o2.shortDescription;
			if (b == null) {
				b = "";
			}
			return a.compareToIgnoreCase(b);
		}
	};
}
