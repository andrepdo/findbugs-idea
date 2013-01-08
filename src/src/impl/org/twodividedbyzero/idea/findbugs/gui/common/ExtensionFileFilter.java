/*
 * Copyright 2008-2013 Andre Pfeiler
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
