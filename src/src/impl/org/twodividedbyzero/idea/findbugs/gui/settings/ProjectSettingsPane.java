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
package org.twodividedbyzero.idea.findbugs.gui.settings;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.FilterComponent;
import com.intellij.ui.TabbedPaneWrapper;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.core.WorkspaceSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

final class ProjectSettingsPane extends JPanel implements Disposable {

	private GeneralTab generalTab;
	private ReportTab reportTab;
	private FilterTab filterTab;
	private DetectorTab detectorTab;
	private ShareTab shareTab;

	ProjectSettingsPane() {
		super(new BorderLayout());

		final JPanel topPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		topPane.add(new FilterComponentImpl());
		topPane.add(createToolbar().getComponent());
		topPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		add(topPane, BorderLayout.NORTH);

		final TabbedPaneWrapper tabs = new TabbedPaneWrapper(this);
		tabs.addTab(ResourcesLoader.getString("settings.general"), getGeneralTab());
		tabs.addTab(ResourcesLoader.getString("settings.report"), getReportTab());
		tabs.addTab(ResourcesLoader.getString("settings.filter"), getFilterTab());
		tabs.addTab(ResourcesLoader.getString("settings.detector"), getDetectorTab());
		tabs.addTab(ResourcesLoader.getString("settings.share"), getShareTab());
		add(tabs.getComponent());

		getDetectorTab().getTablePane().getTable().setBugCategory(getReportTab().getBugCategory());
	}

	@NotNull
	private ActionToolbar createToolbar() {
		final DefaultActionGroup actions = new DefaultActionGroup();
		actions.add(new AdvancedSettingsAction());

		final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, true);
		actionToolbar.setTargetComponent(this);
		return actionToolbar;
	}

	@NotNull
	private GeneralTab getGeneralTab() {
		if (generalTab == null) {
			generalTab = new GeneralTab();
		}
		return generalTab;
	}

	@NotNull
	private ReportTab getReportTab() {
		if (reportTab == null) {
			reportTab = new ReportTab();
		}
		return reportTab;
	}

	@NotNull
	private FilterTab getFilterTab() {
		if (filterTab == null) {
			filterTab = new FilterTab();
		}
		return filterTab;
	}

	@NotNull
	private DetectorTab getDetectorTab() {
		if (detectorTab == null) {
			detectorTab = new DetectorTab();
		}
		return detectorTab;
	}

	@NotNull
	private ShareTab getShareTab() {
		if (shareTab == null) {
			shareTab = new ShareTab();
		}
		return shareTab;
	}

	boolean isModified(
			@NotNull final ProjectSettings settings,
			@NotNull final WorkspaceSettings workspaceSettings
	) {
		return getGeneralTab().isModified(settings) ||
				getReportTab().isModified(settings) ||
				getFilterTab().isModified(settings) ||
				getDetectorTab().isModified(settings) ||
				getShareTab().isModified(workspaceSettings);
	}

	void apply(
			@NotNull final ProjectSettings settings,
			@NotNull final WorkspaceSettings workspaceSettings
	) throws ConfigurationException {
		getGeneralTab().apply(settings);
		getReportTab().apply(settings);
		getFilterTab().apply(settings);
		getDetectorTab().apply(settings);
		getShareTab().apply(workspaceSettings);
	}

	void reset(
			@NotNull final ProjectSettings settings,
			@NotNull final WorkspaceSettings workspaceSettings
	) {
		getGeneralTab().reset(settings);
		getReportTab().reset(settings);
		getFilterTab().reset(settings);
		getDetectorTab().reset(settings);
		getShareTab().reset(workspaceSettings);
	}

	@Override
	public void dispose() {
		if (shareTab != null) {
			Disposer.dispose(shareTab);
			shareTab = null;
		}
	}

	private class FilterComponentImpl extends FilterComponent {
		private FilterComponentImpl() {
			super("FINDBUGS_SETTINGS_SEARCH_HISTORY", 10);
		}

		@Override
		public void filter() {
			final String filter = getFilter();
			System.out.println("TODO: " + filter); // TODO
		}

		@Override
		protected void onlineFilter() {
			final String filter = getFilter();
			System.out.println("TODO: " + filter); // TODO
		}
	}
}
