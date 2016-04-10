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
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.gui.tree.GroupBy;

@State(
		name = "FindBugs-IDEA-Workspace",
		storages = {@Storage(StoragePathMacros.WORKSPACE_FILE)}
)
public final class WorkspaceSettings implements PersistentStateComponent<WorkspaceSettings> {

	/** TODO
	 * export settings (path, type etc) should use PropertiesComponent.getInstance().getValue("export.settings.path", DEFAULT_PATH)
	 * like IDEA export settings dialog, see com.intellij.ide.actions.ChooseComponentsToExportDialog
	 */
	public String exportDirectory;

	public boolean exportAsXml = true;

	public boolean exportAsHtml = true;

	public String importFilePath;

	public boolean annotationTextRangeMarkup = true; // TODO UI

	public boolean annotationGutterIcon = true;

	public String suppressWarningsClassName = "edu.umd.cs.findbugs.annotations.SuppressFBWarnings";

	public boolean toolWindowScrollToSource = true;

	public boolean toolWindowEditorPreview = true;

	public String toolWindowGroupBy = GroupBy.BugCategory.name();

	@Nullable
	@Override
	public WorkspaceSettings getState() {
		return this;
	}

	@Override
	public void loadState(final WorkspaceSettings state) {
		XmlSerializerUtil.copyBean(state, this);
	}

	public static WorkspaceSettings getInstance(@NotNull final Project project) {
		return ServiceManager.getService(project, WorkspaceSettings.class);
	}
}
