/*
 * Copyright 2010 Andre Pfeiler
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

package org.twodividedbyzero.idea.findbugs.collectors;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class RecurseFileCollector implements FileFilter {

	private static final Logger LOGGER = Logger.getInstance(RecurseFileCollector.class.getName());

	private final FindBugsProject _findBugsProject;
	private Set<String> _classes;


	private RecurseFileCollector(final FindBugsProject findBugsProject) {
		_findBugsProject = findBugsProject;
	}


	public boolean accept(final File file) {
		if (file.isDirectory()) {
			addFiles(_findBugsProject, file);
		} else {
			// add the classes to the list of files to be analysed
			final FileType type = IdeaUtilImpl.getFileTypeByName(file.getName());
			if (IdeaUtilImpl.isValidFileType(type)) {
				final String filePath = file.getAbsolutePath();
				_findBugsProject.addFile(filePath);
				LOGGER.debug("adding class file: " + filePath);
			}
		}
		return false;
	}


	/**
	 * recurse add all the files matching given name pattern inside the given directory
	 * and all subdirectories
	 *
	 * @param findBugsProject
	 * @param classesDir
	 */
	// TODO: STARTING POINT
	public static void addFiles(final FindBugsProject findBugsProject, final File classesDir) {
		if (classesDir.isDirectory()) {
			classesDir.listFiles(new RecurseFileCollector(findBugsProject));
		}
	}


	@SuppressWarnings({"ReturnOfCollectionOrArrayField"})
	public Set<String> getResult() {
		return _classes;
	}
}
