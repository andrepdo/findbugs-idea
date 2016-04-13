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
import edu.umd.cs.findbugs.PluginException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsCustomPluginUtil;
import org.twodividedbyzero.idea.findbugs.core.PluginSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import java.io.File;
import java.net.MalformedURLException;

final class PluginInfo {
	@NotNull
	final PluginSettings settings;

	@Nullable
	final String shortDescription;

	@Nullable
	final String detailedDescription;

	@Nullable
	final String website;

	@Nullable
	final String errorMessage;

	@Nullable
	final Throwable error;

	private PluginInfo(
			@NotNull final PluginSettings settings,
			@Nullable final String shortDescription,
			@Nullable final String detailedDescription,
			@Nullable final String website,
			@Nullable final String errorMessage,
			@Nullable final Throwable error
	) {
		this.settings = settings;
		this.shortDescription = shortDescription;
		this.detailedDescription = detailedDescription;
		this.website = website;
		this.errorMessage = errorMessage;
		this.error = error;
	}

	@NotNull
	static PluginInfo create(@NotNull final File path, @NotNull final Plugin plugin) throws MalformedURLException, PluginException {
		final PluginSettings settings = new PluginSettings();
		settings.id = plugin.getPluginId();
		settings.enabled = true; // enable ; do not use plugin.isEnabledByDefault();
		settings.path = path.getPath();
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
	}
}
