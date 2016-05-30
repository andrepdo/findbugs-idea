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

import com.google.gson.GsonBuilder;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.io.HttpRequests;
import com.intellij.util.io.RequestBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.IoUtil;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import java.io.IOException;
import java.io.Reader;

final class PluginVersionChecker {

	private PluginVersionChecker() {
	}

	@Nullable
	static String getLatestVersion(@NotNull final ProgressIndicator indicator) throws IOException {
		final String url = getLatestReleaseUrl();
		indicator.setText(ResourcesLoader.getString("error.submitReport.retrieve", url));
		final RequestBuilder request = HttpRequests
				.request(url)
				.accept("application/vnd.github.v2+json");
		return request.connect(new HttpRequests.RequestProcessor<String>() {
			@Override
			public String process(@NotNull final HttpRequests.Request request) throws IOException {
				final Reader reader = request.getReader();
				try {
					LatestRelease latestRelease = new GsonBuilder().create().fromJson(reader, LatestRelease.class);
					return latestRelease.name;
				} finally {
					IoUtil.safeClose(reader);
				}
			}
		});
	}

	@NotNull
	private static String getLatestReleaseUrl() {
		// https support only
		return "https://api.github.com/repos/andrepdo/findbugs-idea/releases/latest";
	}

	private static class LatestRelease {
		public String name;
	}
}
