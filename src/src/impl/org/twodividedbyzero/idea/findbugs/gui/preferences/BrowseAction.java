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
package org.twodividedbyzero.idea.findbugs.gui.preferences;

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
	private static String _lastDir = IdeaUtilImpl.getProjectRootPath();

	private final ConfigurationPanel _parent;
	private final transient FileFilter _fileFilter;
	private JList _list;
	private Collection<String> _collection;
	private FindBugsPreferences _preferences;

	private final transient BrowseActionCallback _callback;


	public BrowseAction(final ConfigurationPanel parent, final String name, final FileFilter fileFilter, final BrowseActionCallback callback) {

		_callback = callback;
		_fileFilter = fileFilter;
		_parent = parent;

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
			_lastDir = selectedFile.getPath();
			_callback.addSelection(selectedFile);
		}
	}


	public interface BrowseActionCallback {

		void addSelection(final File selectedFile);
	}
}
