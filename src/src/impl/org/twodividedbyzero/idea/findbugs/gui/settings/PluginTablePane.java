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

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import edu.umd.cs.findbugs.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsCustomPluginUtil;
import org.twodividedbyzero.idea.findbugs.common.util.GuiUtil;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.core.PluginSettings;
import org.twodividedbyzero.idea.findbugs.plugins.AbstractPluginLoader;
import org.twodividedbyzero.idea.findbugs.plugins.PluginInfo;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

final class PluginTablePane extends JPanel {
	private static final Logger LOGGER = Logger.getInstance(PluginTablePane.class);

	private JBTable table;
	private String lastPluginError;
	private DetectorTablePane detectorTablePane;
	private List<PluginInfo> bundled;

	PluginTablePane() {
		super(new BorderLayout());
		setBorder(GuiUtil.createTitledBorder(ResourcesLoader.getString("plugins.title")));
		table = GuiUtil.createCheckboxTable(
				new Model(New.<PluginInfo>arrayList()),
				Model.IS_ENABLED_COLUMN,
				new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						swapEnabled();
					}
				}
		);
		table.getColumnModel().getColumn(Model.NAME_COLUMN).setCellRenderer(new TableCellRenderer() {
			private PluginPane pane;
			private PluginErrorPane errorPane;

			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
				final PluginInfo pluginInfo = (PluginInfo) value;
				if (pluginInfo.errorMessage != null) {
					if (errorPane == null) {
						errorPane = new PluginErrorPane();
					}
					errorPane.load(pluginInfo);
					return errorPane;
				} else {
					if (pane == null) {
						pane = new PluginPane();
					}
					pane.load(pluginInfo);
					return pane;
				}
			}
		});

		final JPanel tablePane = ToolbarDecorator.createDecorator(table)
				.setAddAction(new AnActionButtonRunnable() {
					@Override
					public void run(@NotNull final AnActionButton anActionButton) {
						doAdd(anActionButton);
					}
				})
				.setRemoveAction(new AnActionButtonRunnable() {
					@Override
					public void run(@NotNull final AnActionButton anActionButton) {
						doRemove();
					}
				})
				.setAsUsualTopToolbar().createPanel();

		add(tablePane);
	}

	@NotNull
	private Model getModel() {
		return (Model) table.getModel();
	}

	private void swapEnabled() {
		final int rows[] = table.getSelectedRows();
		for (final int row : rows) {
			getModel().rows.get(row).settings.enabled = !getModel().rows.get(row).settings.enabled;
		}
		getModel().fireTableDataChanged();
	}

	private void doAdd(@NotNull final AnActionButton anActionButton) {
		final Project project = IdeaUtilImpl.getProject(anActionButton.getDataContext());
		if (bundled.isEmpty()) {
			doAddWithFileChooser(project);
		} else {
			// like com.intellij.codeInsight.template.impl.TemplateListPanel#addTemplateOrGroup
			final DefaultActionGroup group = new DefaultActionGroup();
			for (final PluginInfo pluginInfo : bundled) {
				group.add(new DumbAwareAction(ResourcesLoader.getString("plugins.add", pluginInfo.shortDescription)) {
					@Override
					public void actionPerformed(@NotNull final AnActionEvent e) {
						final Set<PluginSettings> settings = New.set();
						pluginInfo.settings.enabled = true;
						for (final PluginInfo other : getModel().rows) {
							settings.add(other.settings);
							if (other.settings.enabled) {
								if (other.settings.id.equals(pluginInfo.settings.id)) {
									pluginInfo.settings.enabled = false;
									break;
								}
							}
						}
						bundled.remove(pluginInfo);
						settings.add(pluginInfo.settings);
						load(settings);
					}
				});
			}
			group.add(new DumbAwareAction(ResourcesLoader.getString("plugins.addFromDisk")) {
				@Override
				public void actionPerformed(@NotNull AnActionEvent e) {
					doAddWithFileChooser(project);
				}
			});
			final DataContext context = DataManager.getInstance().getDataContext(anActionButton.getContextComponent());
			final ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
					null,
					group,
					context,
					JBPopupFactory.ActionSelectionAid.ALPHA_NUMBERING,
					true,
					null
			);
			popup.show(anActionButton.getPreferredPopupPoint());
		}
	}

	private void doAddWithFileChooser(@Nullable final Project project) {
		final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, true, false, true);
		descriptor.setTitle(ResourcesLoader.getString("plugins.choose.title"));
		descriptor.setDescription(ResourcesLoader.getString("plugins.choose.description"));
		descriptor.withFileFilter(new Condition<VirtualFile>() {
			@Override
			public boolean value(final VirtualFile virtualFile) {
				return "jar".equalsIgnoreCase(virtualFile.getExtension());
			}
		});

		final VirtualFile[] files = FileChooser.chooseFiles(descriptor, this, project, null);
		if (files.length > 0) {

			// collect current plugin settings
			final Set<PluginSettings> settings = New.set();
			for (final PluginInfo plugin : getModel().rows) {
				settings.add(plugin.settings);
			}

			// unload all plugin
			for (final Plugin plugin : Plugin.getAllPlugins()) {
				if (!plugin.isCorePlugin()) {
					FindBugsCustomPluginUtil.unload(plugin);
				}
			}

			// load choosed plugins
			StringBuilder errors = new StringBuilder();
			for (final VirtualFile virtualFile : files) {
				final File file = VfsUtilCore.virtualToIoFile(virtualFile);
				Plugin plugin = null;
				try {
					plugin = FindBugsCustomPluginUtil.loadTemporary(file);
					final PluginSettings pluginSettings = new PluginSettings();
					pluginSettings.id = plugin.getPluginId();
					pluginSettings.bundled = false;
					pluginSettings.enabled = true; // enable ; do not use plugin.isEnabledByDefault();
					pluginSettings.url = FindBugsCustomPluginUtil.getAsString(plugin);
					for (final PluginSettings other : settings) {
						if (other.id.equals(pluginSettings.id)) {
							pluginSettings.enabled = false;
						}
					}
					settings.add(pluginSettings);
				} catch (final Exception e) {
					LOGGER.warn(String.valueOf(file), e);
					errors.append("\n    - ").append(e.getMessage());
				} finally {
					if (plugin != null) {
						FindBugsCustomPluginUtil.unload(plugin);
					}
				}
			}

			// reload all plugins
			load(settings);
			if (errors.length() > 0) {
				Messages.showErrorDialog(
						this,
						ResourcesLoader.getString("plugins.load.error.text") + errors.toString(),
						StringUtil.capitalizeWords(ResourcesLoader.getString("plugins.load.error.title"), true)
				);
			}
		}
	}

	private void doRemove() {
		final int[] index = table.getSelectedRows();
		if (index != null && index.length > 0) {
			final Set<PluginSettings> settings = New.set();
			for (final PluginInfo plugin : getModel().rows) {
				settings.add(plugin.settings);
			}
			for (final int idx : index) {
				settings.remove(getModel().rows.get(idx).settings);
			}
			load(settings);
		}
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		table.setEnabled(enabled);
	}

	boolean isModified(@NotNull final AbstractSettings settings) {
		final Set<PluginSettings> plugins = New.set();
		for (final PluginInfo pluginInfo : getModel().rows) {
			plugins.add(pluginInfo.settings);
		}
		return !settings.plugins.equals(plugins);
	}

	void apply(@NotNull final AbstractSettings settings) throws ConfigurationException {
		if (lastPluginError != null) {
			throw new ConfigurationException(lastPluginError, ResourcesLoader.getString("plugins.error.title"));
		}
		settings.plugins.clear();
		for (final PluginInfo pluginInfo : getModel().rows) {
			settings.plugins.add(pluginInfo.settings);
		}
	}

	void reset(@NotNull final AbstractSettings settings) {
		lastPluginError = null;
		load(settings.plugins);
	}

	private void load(@NotNull final Set<PluginSettings> settings) {
		getModel().rows.clear();
		final PluginLoaderImpl pluginLoader = new PluginLoaderImpl();
		pluginLoader.load(settings);
		Collections.sort(pluginLoader.configured, PluginInfo.ByShortDescription);
		Collections.sort(pluginLoader.bundled, PluginInfo.ByShortDescription);
		bundled = pluginLoader.bundled;
		getModel().rows.addAll(pluginLoader.configured);
		getModel().fireTableDataChanged();
		if (detectorTablePane != null) {
			detectorTablePane.reload(false);
		}
	}

	void setDetectorTablePane(@Nullable final DetectorTablePane detectorTablePane) {
		this.detectorTablePane = detectorTablePane;
	}

	private class Model extends AbstractTableModel {
		private static final int IS_ENABLED_COLUMN = 0;
		private static final int NAME_COLUMN = 1;
		@NotNull
		private final List<PluginInfo> rows;

		private Model(@NotNull final List<PluginInfo> rows) {
			this.rows = rows;
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return columnIndex == IS_ENABLED_COLUMN;
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			switch (columnIndex) {
				case IS_ENABLED_COLUMN:
					return Boolean.class;
				case NAME_COLUMN:
					return Component.class;
				default:
					throw new IllegalArgumentException("Column " + columnIndex);
			}
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case IS_ENABLED_COLUMN:
					return rows.get(rowIndex).settings.enabled;
				case NAME_COLUMN:
					return rows.get(rowIndex);
				default:
					throw new IllegalArgumentException("Column " + columnIndex);
			}
		}

		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
			if (columnIndex == IS_ENABLED_COLUMN) {
				final PluginSettings settings = rows.get(rowIndex).settings;
				boolean enabled = (Boolean) aValue;
				if (enabled) {
					for (final PluginInfo other : rows) {
						if (other.settings != settings) {
							if (other.settings.id.equals(settings.id) && other.settings.enabled) {
								Messages.showInfoMessage(
										PluginTablePane.this,
										ResourcesLoader.getString("plugins.duplicate.text"),
										StringUtil.capitalizeWords(ResourcesLoader.getString("plugins.duplicate.title"), true)
								);
								enabled = false;
							}
						}
					}
				}
				rows.get(rowIndex).settings.enabled = enabled;
			}
		}
	}

	private class PluginLoaderImpl extends AbstractPluginLoader {

		@NotNull
		private final List<PluginInfo> configured;

		@NotNull
		private final List<PluginInfo> bundled;

		PluginLoaderImpl() {
			super(false);
			configured = New.arrayList();
			bundled = New.arrayList();
		}

		@Override
		protected void seenBundledPlugin(@NotNull final PluginInfo plugin) {
			bundled.add(plugin);
		}

		@Override
		protected void seenConfiguredPlugin(@NotNull final PluginInfo plugin) {
			configured.add(plugin);
		}

		@Override
		protected void handleError(@NotNull String message) {
			// do not log ; PluginErrorPane is used in this case
		}
	}
}
