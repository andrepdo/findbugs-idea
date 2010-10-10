package org.twodividedbyzero.idea.findbugs.collectors;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;

import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
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


	public Set<String> getResult() {
		return Collections.unmodifiableSet(_classes);
	}
}
