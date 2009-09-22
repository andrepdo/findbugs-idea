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
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.84-dev
 */
public final class ExtensionFileFilter extends FileFilter {

	private final Set<String> _extensions;


	public ExtensionFileFilter(@NotNull final Set<String> extensions) {
		_extensions = Collections.unmodifiableSet(extensions);
	}


	@Override
	public boolean accept(final File f) {
		if (f.isDirectory()) {
			return true;
		}

		final String fileName = f.getName();
		final int index = fileName.toLowerCase(Locale.ENGLISH).lastIndexOf('.');
		return index != -1 && _extensions.contains(fileName.toLowerCase(Locale.ENGLISH).substring(index));

		//return fileName.toLowerCase(Locale.ENGLISH).endsWith("." + _extensions);
	}


	@Override
	public String getDescription() {
		return Arrays.toString(_extensions.toArray());
	}
}
