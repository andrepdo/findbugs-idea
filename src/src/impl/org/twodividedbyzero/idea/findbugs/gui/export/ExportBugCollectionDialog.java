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
package org.twodividedbyzero.idea.findbugs.gui.export;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileTextField;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.OptionGroup;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.JBDimension;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.WorkspaceSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class ExportBugCollectionDialog extends DialogWrapper {

	@NonNls
	private static final String DEFAULT_PATH = FileUtil.toSystemDependentName(System.getProperty("user.home"));

	@NotNull
	private final Project project;

	private TextFieldWithBrowseButton targetDirectoryField;
	private JBCheckBox htmlCheckbox;
	private JBCheckBox xmlCheckbox;
	private JBCheckBox createSubDirectoryCheckbox;
	private JBCheckBox openInBrowserCheckbox;

	public ExportBugCollectionDialog(@NotNull final Project project) {
		super(project);
		this.project = project;
		setTitle(StringUtil.capitalizeWords(ResourcesLoader.getString("export.title"), true));
		setOKButtonText(ResourcesLoader.getString("export.button.ok"));
		setCancelButtonText(ResourcesLoader.getString("export.button.cancel"));
		init();
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {

		final FileTextField field = FileChooserFactory.getInstance().createFileTextField(FileChooserDescriptorFactory.createSingleFolderDescriptor(), myDisposable);
		targetDirectoryField = new TextFieldWithBrowseButton(field.getField());
		targetDirectoryField.addBrowseFolderListener(
				StringUtil.capitalizeWords(ResourcesLoader.getString("export.directory.choose.title"), true),
				ResourcesLoader.getString("export.directory.choose.text"),
				project,
				FileChooserDescriptorFactory.createSingleFolderDescriptor()
		);

		final LabeledComponent<TextFieldWithBrowseButton> targetDirectoryLabeled = new LabeledComponent<TextFieldWithBrowseButton>();
		targetDirectoryLabeled.setText(ResourcesLoader.getString("export.directory.label"));
		targetDirectoryLabeled.setComponent(targetDirectoryField);

		final ActionListener updateControlsAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateControls();
			}
		};

		htmlCheckbox = new JBCheckBox(ResourcesLoader.getString("export.options.html"));
		htmlCheckbox.addActionListener(updateControlsAction);

		xmlCheckbox = new JBCheckBox(ResourcesLoader.getString("export.options.xml"));
		xmlCheckbox.addActionListener(updateControlsAction);

		createSubDirectoryCheckbox = new JBCheckBox(ResourcesLoader.getString("export.options.createSubDir"));
		createSubDirectoryCheckbox.addActionListener(updateControlsAction);

		openInBrowserCheckbox = new JBCheckBox(ResourcesLoader.getString("export.options.openInBrowser"));
		openInBrowserCheckbox.addActionListener(updateControlsAction);

		final OptionGroup optionGroup = new OptionGroup(ResourcesLoader.getString("export.options.title"));
		optionGroup.add(htmlCheckbox);
		optionGroup.add(xmlCheckbox);
		optionGroup.add(createSubDirectoryCheckbox);
		optionGroup.add(openInBrowserCheckbox);

		final JPanel pane = new JPanel(new BorderLayout());
		pane.add(targetDirectoryLabeled, BorderLayout.NORTH);
		pane.add(optionGroup.createPanel());
		pane.setPreferredSize(new JBDimension(400, 0));
		return pane;
	}

	public void reset() {
		final WorkspaceSettings workspaceSettings = WorkspaceSettings.getInstance(project);

		String initialDirectory = DEFAULT_PATH;
		if (!StringUtil.isEmptyOrSpaces(workspaceSettings.exportBugCollectionDirectory)) {
			initialDirectory = workspaceSettings.exportBugCollectionDirectory;
		}
		targetDirectoryField.setText(initialDirectory);

		htmlCheckbox.setSelected(workspaceSettings.exportBugCollectionAsHtml);
		xmlCheckbox.setSelected(workspaceSettings.exportBugCollectionAsXml);
		createSubDirectoryCheckbox.setSelected(workspaceSettings.exportBugCollectionCreateSubDirectory);
		openInBrowserCheckbox.setSelected(workspaceSettings.openExportedHtmlBugCollectionInBrowser);
		updateControls();
	}

	public void apply() {
		final WorkspaceSettings workspaceSettings = WorkspaceSettings.getInstance(project);

		workspaceSettings.exportBugCollectionDirectory = targetDirectoryField.getText();
		workspaceSettings.exportBugCollectionAsHtml = htmlCheckbox.isSelected();
		workspaceSettings.exportBugCollectionAsXml = xmlCheckbox.isSelected();
		workspaceSettings.exportBugCollectionCreateSubDirectory = createSubDirectoryCheckbox.isSelected();
		workspaceSettings.openExportedHtmlBugCollectionInBrowser = openInBrowserCheckbox.isSelected();
	}

	private void updateControls() {
		setOKActionEnabled(htmlCheckbox.isSelected() || xmlCheckbox.isSelected());
		openInBrowserCheckbox.setEnabled(htmlCheckbox.isSelected());
	}
}
