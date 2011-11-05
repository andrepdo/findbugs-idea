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
package org.twodividedbyzero.idea.findbugs.gui.preferences;

import com.intellij.openapi.project.Project;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.84-dev
 */
public final class BrowseAction extends AbstractAction {

	private static final long serialVersionUID = 0L;
	private static String _lastDir;

	private final ConfigurationPanel _parent;
	private final transient FileFilter _fileFilter;
	private JList _list;
	private Collection<String> _collection;
	private FindBugsPreferences _preferences;

	private final transient BrowseActionCallback _callback;


	@edu.umd.cs.findbugs.annotations.SuppressWarnings({"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"})
	public BrowseAction(final ConfigurationPanel parent, final String name, final FileFilter fileFilter, final BrowseActionCallback callback) {

		_callback = callback;
		_fileFilter = fileFilter;
		_parent = parent;
		final Project project = _parent.getProject();
		//noinspection AssignmentToStaticFieldFromInstanceMethod
		_lastDir = IdeaUtilImpl.getProjectRootPath(project);

		putValue(Action.NAME, name);
		putValue(Action.SHORT_DESCRIPTION, name);
		putValue(Action.LONG_DESCRIPTION, name);
	}


	public void actionPerformed(final ActionEvent e) {
		final JFileChooser fileChooser = new JFileChooser(_lastDir);
		fileChooser.setFileFilter(_fileFilter);

		final int result = fileChooser.showOpenDialog(_parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = fileChooser.getSelectedFile();
			//noinspection AssignmentToStaticFieldFromInstanceMethod
			_lastDir = selectedFile.getPath();
			_callback.addSelection(selectedFile);
		}
	}


	public interface BrowseActionCallback {

		void addSelection(final File selectedFile);
	}
}
