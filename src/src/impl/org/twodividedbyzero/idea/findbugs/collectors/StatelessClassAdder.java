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
package org.twodividedbyzero.idea.findbugs.collectors;


import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;

import java.io.File;


/**
 * @author $Author: reto.merz@gmail.com $
 * @version $Revision: 353 $
 * @since 0.9.995
 */
public final class StatelessClassAdder extends AbstractClassAdder {

	private static final Logger LOGGER = Logger.getInstance(StatelessClassAdder.class.getName());
	private final FindBugsProject _findBugsProject;


	public StatelessClassAdder(@NotNull final FindBugsProject findBugsProject, @NotNull final Project project) {
		super(project);
		_findBugsProject = findBugsProject;
	}


	@Override
	void put(@NotNull final String fqp, @NotNull final PsiElement element) {
		final String fqn = fqp + CLASS_FILE_SUFFIX;
		if (new File(fqn).exists()) {
			_findBugsProject.addFile(fqn);
			LOGGER.debug("adding class file: " + fqn);
		} else {
			LOGGER.debug("class file: " + fqn + " does not exists. maybe an inner/anonymous class? try to recompile your sources.");
		}
	}
}