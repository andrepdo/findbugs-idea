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
import com.intellij.util.xmlb.Constants;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsCustomPluginUtil;
import org.twodividedbyzero.idea.findbugs.common.util.New;

import java.util.Map;
import java.util.Set;

@State(
		name = "FindBugs-IDEA",
		storages = @Storage(value = "findbugs-66.xml", roamingType = RoamingType.DEFAULT) // TODO kick -XX number
)
public final class ProjectSettings extends AbstractSettings implements PersistentStateComponent<ProjectSettings> {

	@Tag
	public String suppressWarningsClassName = "edu.umd.cs.findbugs.annotations.SuppressFBWarnings";

	/**
	 * Additional findbugs plugins.
	 *
	 * @see FindBugsCustomPluginUtil
	 */
	@Tag(value = "plugins")
	@AbstractCollection(surroundWithTag = false, elementTag = Constants.SET)
	public Set<PluginSettings> plugins = New.set();

	/**
	 * Note that the map only contains detectors from the core plugin and
	 * only enabled state which are not equal to the default enable state
	 * {@link edu.umd.cs.findbugs.DetectorFactory#isDefaultEnabled()}.
	 * <p>
	 * Key = {@link edu.umd.cs.findbugs.DetectorFactory#getShortName()}
	 * (like {@link edu.umd.cs.findbugs.config.UserPreferences#detectorEnablementMap})
	 * <p>
	 * Value = Enabled state
	 */
	@Tag(value = "detectors")
	@MapAnnotation(
			surroundWithTag = false,
			surroundValueWithTag = false,
			surroundKeyWithTag = false,
			entryTagName = "detector",
			keyAttributeName = "name",
			valueAttributeName = "enabled"
	)
	public Map<String, Boolean> detectors = New.map();

	@Nullable
	@Override
	public ProjectSettings getState() {
		return this;
	}

	@Override
	public void loadState(final ProjectSettings state) {
		XmlSerializerUtil.copyBean(state, this);
	}

	@NotNull
	public static ProjectSettings getInstance(@NotNull final Project project) {
		return ServiceManager.getService(project, ProjectSettings.class);
	}
}
