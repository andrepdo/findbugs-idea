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
package org.twodividedbyzero.idea.findbugs.core;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.New;

import java.util.Set;

@State(
		name = "FindBugs-IDEA",
		storages = @Storage(value = "findbugs-31.xml", roamingType = RoamingType.DEFAULT) // TODO kick -26
)
public final class ProjectSettings extends AbstractSettings implements PersistentStateComponent<ProjectSettings> {

	public Set<PluginSettings> plugins = New.set();

	@Nullable
	@Override
	public ProjectSettings getState() {
		return this;
	}

	@Override
	public void loadState(final ProjectSettings state) {
		XmlSerializerUtil.copyBean(state, this);
	}

	public static ProjectSettings getInstance(@NotNull final Project project) {
		return ServiceManager.getService(project, ProjectSettings.class);
	}
}
