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
package org.twodividedbyzero.idea.findbugs.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;

public final class StringUtilFb {

	private StringUtilFb() {
	}


	public static String addLineSeparatorAt(final String text, final int maxWidth) {
		final StringBuilder result = new StringBuilder();
		String calctext = text;
		while (calctext.length() > maxWidth) {
			//noinspection HardcodedLineSeparator
			result.append(calctext.substring(0, maxWidth)).append('\n');
			calctext = calctext.substring(maxWidth, calctext.length());
		}
		if (!calctext.isEmpty()) {
			result.append(calctext);
		}

		return result.length() == 0 ? calctext : result.toString();
	}


	public static StringBuilder addLineSeparatorAt(final StringBuilder buffer, final int maxWidth) {
		return new StringBuilder(addLineSeparatorAt(buffer.toString(), maxWidth));
	}


	public static String html2text(final String html) {
		return Jsoup.parse(html).text();
	}


	public static boolean isEmpty(@Nullable final String s) {
		return null == s || s.trim().isEmpty();
	}


	@NotNull
	public static String trim(@NotNull String text, @NotNull char... chars) {
		int len = 0;
		while (len != text.length()) {
			len = text.length();
			final char firstChar = text.charAt(0);
			for (final char c : chars) {
				if (c == firstChar) {
					text = text.substring(1);
					break;
				}
			}
			if (text.length() > 0) {
				final char lastChar = text.charAt(text.length() - 1);
				for (final char c : chars) {
					if (c == lastChar) {
						text = text.substring(0, text.length() - 1);
						break;
					}
				}
			}
		}
		return text;
	}
}
