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
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.gui.tree.GroupBy;

import java.util.Map;

@State(
		name = "FindBugs-IDEA-Workspace",
		storages = {@Storage(file /*IDEA 2016: switch to value()*/ = StoragePathMacros.WORKSPACE_FILE)}
)
public final class WorkspaceSettings implements PersistentStateComponent<WorkspaceSettings> {

	@Tag
	public boolean compileBeforeAnalyze = true;

	@Tag
	public boolean analyzeAfterCompile = false;

	@Tag
	public boolean analyzeAfterAutoMake = false;

	@Tag
	public boolean runInBackground = false;

	/**
	 * Last used export directory for "Export Bug Collection".
	 */
	@Tag
	public String exportBugCollectionDirectory;

	/**
	 * Last used setting for "Export Bug Collection".
	 */
	@Tag
	public boolean exportBugCollectionAsXml = true;

	/**
	 * Last used setting for "Export Bug Collection".
	 */
	@Tag
	public boolean exportBugCollectionAsHtml = true;

	/**
	 * Last used setting for "Export Bug Collection".
	 */
	@Tag
	public boolean exportBugCollectionCreateSubDirectory = false;

	/**
	 * Last used setting for "Export Bug Collection".
	 */
	@Tag
	public boolean openExportedHtmlBugCollectionInBrowser = true;

	public static final String PROJECT_IMPORT_FILE_PATH_KEY = "";

	/**
	 * This settings file will be used for analysis.
	 * Key is the module name, {@code PROJECT_IMPORT_FILE_PATH_KEY} is for project scope.
	 */
	@Tag(value = "importFilePaths")
	@MapAnnotation(
			surroundWithTag = false,
			surroundValueWithTag = false,
			surroundKeyWithTag = false,
			entryTagName = "importFilePath",
			keyAttributeName = "module",
			valueAttributeName = "path"
	)
	public Map<String, String> importFilePath = New.map();

	@Tag
	public boolean annotationTextRangeMarkup = true;

	@Tag
	public boolean annotationGutterIcon = true;

	@Tag
	public boolean toolWindowToFront = true;

	@Tag
	public boolean toolWindowScrollToSource = true;

	@Tag
	public boolean toolWindowEditorPreview = true;

	@Tag
	public String toolWindowGroupBy = GroupBy.BugCategory.name();

	@Tag
	public boolean analyzeBeforeCheckIn = false;

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
