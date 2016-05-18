/*
 * Copyright 2016 Andre Pfeiler
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

import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ModulePathMacroManager can only expand/collapse MODULE_DIR, but not PROJECT_DIR.
 * <p>
 * FIXME:
 * It seems that the IDEA settings deserializer automatically expandPaths.
 * f. e. the paths of {@link org.twodividedbyzero.idea.findbugs.core.AbstractSettings#excludeBugsFiles}
 * are all expanded. But we want to load/store *always* with marcos.
 * Otherwise we store with marco and after re-open IDEA the FindBugs settings are modified
 * because the paths are changed. So a dirty workaround is that we replace '$' with our '@'.
 */
public final class PathMacroManagerFb {
	@NotNull
	private final List<PathMacroManager> pathMacroManagers;

	private PathMacroManagerFb(@NotNull final List<PathMacroManager> pathMacroManagers) {
		this.pathMacroManagers = pathMacroManagers;
	}

	public String expandPath(String path) {
		if (path != null) {
			path = path.replace("@", "$");
			int length = -1;
			while (length != path.length()) {
				length = path.length();
				for (final PathMacroManager pathMacroManager : pathMacroManagers) {
					path = pathMacroManager.expandPath(path);
				}
			}
			path = path.replace("$", "@");
		}
		return path;
	}

	public String collapsePath(@Nullable String path) {
		if (path != null) {
			path = path.replace("@", "$");
			int length = -1;
			while (length != path.length()) {
				length = path.length();
				for (final PathMacroManager pathMacroManager : pathMacroManagers) {
					path = pathMacroManager.collapsePath(path);
				}
			}
			path = path.replace("$", "@");
		}
		return path;
	}

	@NotNull
	public static PathMacroManagerFb create(
			@NotNull final Project project,
			@Nullable final Module module
	) {
		if (module != null) {
			return new PathMacroManagerFb(Arrays.asList(
					PathMacroManager.getInstance(project),
					PathMacroManager.getInstance(module)
			));
		} else {
			return new PathMacroManagerFb(Collections.singletonList(
					PathMacroManager.getInstance(project)
			));
		}
	}
}
