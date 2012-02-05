/*
 * Copyright 2011 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.twodividedbyzero.idea.findbugs.common.util;

import org.jsoup.Jsoup;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
public class StringUtil {

	private StringUtil() {
	}


	public static String addLineSeparatorAt(final String text, final int maxWidth) {
		final StringBuilder result = new StringBuilder();
		String calctext = text;
		while (calctext.length() > maxWidth) {
			//noinspection HardcodedLineSeparator
			result.append(calctext.substring(0, maxWidth)).append('\n');
			calctext = calctext.substring(maxWidth, calctext.length());
		}
		if (calctext.length() > 0) {
			result.append(calctext);
		}

		return result.length() == 0 ? calctext : result.toString();
	}


	public static StringBuilder addLineSeparatorAt(final StringBuilder buffer, final int maxWidth) {
		return new StringBuilder(addLineSeparatorAt(buffer.toString(), maxWidth));
	}


	public static String html2text(String html) {
		return Jsoup.parse(html).text();
	}

}
