/*
 * Copyright 2008-2015 Andre Pfeiler
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

package org.twodividedbyzero.idea.findbugs.core;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.common.util.New;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * $Date: 2015-02-14 17:45:23 +0100 (Sa, 14 Feb 2015) $
 *
 * @author $Author: reto.merz@gmail.com $
 * @version $Revision: 343 $
 * @since 0.9.995
 */
final class Changes {


	static final Changes INSTANCE = new Changes();
	private final Set<Project> _listeners = new THashSet<Project>();
	private final Map<Project, Set<VirtualFile>> _changed = new THashMap<Project, Set<VirtualFile>>();


	private Changes() {
	}


	synchronized void addListener(@NotNull final Project project) {
		_listeners.add(project);
	}


	synchronized boolean removeListener(@NotNull final Project project) {
		_listeners.remove(project);
		_changed.remove(project);
		return _listeners.isEmpty();
	}


	synchronized void addChanged(@NotNull final Collection<File> paths) {
		final LocalFileSystem lfs = LocalFileSystem.getInstance();
		VirtualFile vf;
		List<VirtualFile> vfs = null;
		for (File f : paths) {
			vf = lfs.findFileByIoFile(f);
			if (vf != null && vf.isValid() && !vf.isDirectory() && IdeaUtilImpl.isValidFileType(vf.getFileType())) {
				if (vfs == null) {
					vfs = New.arrayList();
				}
				vfs.add(vf);
			}
		}

		if (vfs != null) {
			ProjectFileIndex index;
			Set<VirtualFile> changesPerProject;
			for (Project project : _listeners) {
				index = ProjectFileIndex.SERVICE.getInstance( project );
				changesPerProject = null;
				for (VirtualFile f : vfs) {
					if (index.isInSource(f)) {
						if (changesPerProject == null) {
							changesPerProject = _changed.get(project);
							if (changesPerProject == null) {
								changesPerProject = New.tSet();
								_changed.put(project, changesPerProject);
							}
						}
						changesPerProject.add(f);
					}
				}
			}
		}
	}


	@Nullable
	synchronized Set<VirtualFile> getAndRemoveChanged(@NotNull final Project project) {
		return _changed.remove(project);
	}
}