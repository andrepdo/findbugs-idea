/*
 * Copyright 2009 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.gui.common;

import org.jetbrains.annotations.NotNull;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.84-dev
 */
public final class ExtensionFileFilter extends FileFilter {

	private final Map<String, String> _extensions;
	private final String _description;


	public ExtensionFileFilter(@NotNull final Map<String, String> extensions) {
		_extensions = Collections.unmodifiableMap(extensions);
		final StringBuilder buffer = new StringBuilder();
		for (final Entry<String, String> entry : _extensions.entrySet()) {
			final String key = entry.getKey();
			buffer.append(entry.getValue()).append(" (").append(key).append(')');
		}
		_description = buffer.toString();
	}


	@Override
	public boolean accept(final File f) {
		if (f.isDirectory()) {
			return true;
		}

		final String fileName = f.getName();
		final int index = fileName.toLowerCase(Locale.ENGLISH).lastIndexOf('.');
		return index != -1 && _extensions.containsKey(fileName.toLowerCase(Locale.ENGLISH).substring(index));
	}


	@Override
	public String getDescription() {
		return _description;
	}
}
