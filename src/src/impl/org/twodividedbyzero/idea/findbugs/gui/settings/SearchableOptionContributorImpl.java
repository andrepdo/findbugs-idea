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

import com.intellij.ide.ui.search.SearchableOptionContributor;
import com.intellij.ide.ui.search.SearchableOptionProcessor;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import java.util.Locale;
import java.util.Set;

/**
 * Note that it is not (yet) supported to provide a pre generated searchableOptions.xml:
 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/206110459-Problems-generating-searchableOptions-xml
 */
public final class SearchableOptionContributorImpl extends SearchableOptionContributor {
	@Override
	public void processOptions(@NotNull final SearchableOptionProcessor processor) {
		// TODO
		index(processor, GeneralTab.getSearchPath(), GeneralTab.getSearchResourceKey());
		index(processor, ReportTab.getSearchPath(), ReportTab.getSearchResourceKey());
		index(processor, AnnotateTab.getSearchPath(), AnnotateTab.getSearchResourceKey());
		index(processor, ShareTab.getSearchPath(), ShareTab.getSearchResourceKey());
	}

	private static void index(
			@NotNull final SearchableOptionProcessor processor,
			@NotNull final String path,
			@NotNull final String[] resourceKey
	) {

		final Set<String> added = New.set();
		for (final String key : resourceKey) {
			final String text = ResourcesLoader.getString(key);
			for (final String word : StringUtil.getWordsIn(text)) {
				final String lowerCase = word.toLowerCase(Locale.US);
				if (added.add(lowerCase)) {
					processor.addOptions(
							lowerCase,
							path,
							text,
							ProjectConfigurableImpl.ID,
							ProjectConfigurableImpl.DISPLAY_NAME,
							false
					);
				}
			}
		}
	}
}
