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

package org.twodividedbyzero.idea.findbugs.gui.preferences;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.Plugin;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsCustomPluginUtil;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsUtil;
import org.twodividedbyzero.idea.findbugs.common.util.GuiUtil;
import org.twodividedbyzero.idea.findbugs.gui.common.CustomLineBorder;
import org.twodividedbyzero.idea.findbugs.gui.common.ExtensionFileFilter;
import org.twodividedbyzero.idea.findbugs.gui.common.ScrollPaneFacade;
import org.twodividedbyzero.idea.findbugs.gui.common.VerticalFlowLayout;
import org.twodividedbyzero.idea.findbugs.gui.preferences.BrowseAction.BrowseActionCallback;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.ToolWindowPanel;
import org.twodividedbyzero.idea.findbugs.plugins.AbstractPluginLoaderLegacy;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
import org.twodividedbyzero.idea.findbugs.preferences.PersistencePreferencesBean;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @since 0.9.97
 */
@SuppressWarnings({"AnonymousInnerClass", "HardcodedLineSeparator"})
public class PluginConfiguration implements ConfigurationPage {

	/**
	 * False because:
	 * - Its not yet possible to clear/eject current bug collection
	 * - Needs some UI visualization to make clear plugins states comes from bug collection
	 */
	private static final boolean RESPECT_PROJECT_PLUGIN_STATE = false;

	private static final Color PLUGIN_DESCRIPTION_BG_COLOR = UIManager.getColor("Tree.textBackground");

	private final FindBugsPreferences _preferences;
	private final ConfigurationPanel _parent;
	private JPanel _component;
	private JPanel _pluginComponentPanel;
	private AbstractButton _addButton;


	public PluginConfiguration(final ConfigurationPanel parent, final FindBugsPreferences preferences) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_preferences = preferences;
		_parent = parent;
	}


	@NotNull
	public Component getComponent() {
		if (_component == null) {
			_component = new JPanel(new BorderLayout());
			_component.setBorder(BorderFactory.createTitledBorder("Installed Plugins"));

			_pluginComponentPanel = new JPanel(new VerticalFlowLayout());
			rebuildPluginComponents();

			_component.add(ScrollPaneFacade.createScrollPane(_pluginComponentPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));

			final JPanel buttonPanel = new JPanel(new BorderLayout());
			buttonPanel.add(getAddButton(), BorderLayout.NORTH);
			_component.add(buttonPanel, BorderLayout.EAST);
		}
		return _component;
	}


	@NotNull
	private AbstractButton getAddButton() {
		if (_addButton == null) {
			_addButton = new JButton();
			final Action action = new BrowseAction(_parent, "Add Plugin...", new ExtensionFileFilter(FindBugsUtil.PLUGINS_EXTENSIONS_SET), new BrowseActionCallback() {
				public void addSelection(final File selectedFile) {
					doAddPlugin(selectedFile);
				}
			});
			_addButton.setAction(action);
		}
		return _addButton;
	}


	@Override
	public void updatePreferences() {
		rebuildPluginComponents();
	}


	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
	@SuppressWarnings({"ObjectAllocationInLoop"})
	private void rebuildPluginComponents() {
		if (_pluginComponentPanel == null) {
			return;
		}
		_pluginComponentPanel.removeAll();

		final PluginLoaderImpl pluginLoader = new PluginLoaderImpl(getCurrentFbProject());
		pluginLoader.load(_preferences.getPlugins(), _preferences.getDisabledUserPluginIds(), _preferences.getEnabledBundledPluginIds(), _preferences.getDisabledBundledPluginIds());
		pluginLoader.showErrorBalloonIfNecessary(_parent.getProject()); // LATER: check can we display errors in the bottom of the settings dialog?

		final JPanel pluginComponent = pluginLoader.getLastPluginComponent();
		if (pluginComponent != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					pluginComponent.scrollRectToVisible(new Rectangle(0, 0));
				}
			});
		}
	}


	private void doAddPlugin(final File selectedFile) {
		try {
			// TODO CUSTOM_PLUGIN: check what happen when a plugin with same pluginId is already loaded ?
			final Plugin plugin = FindBugsCustomPluginUtil.loadTemporary(selectedFile);
			if (plugin == null) {
				Messages.showErrorDialog(_parent, "Can not load plugin " + selectedFile.getPath(), "Plugin Loading");
				return;
			}
			try {
				final int answer = Messages.showYesNoDialog(_parent, "Add plugin '" + plugin.getPluginId() + "'?", "Add Plugin", null);
				if (answer == Messages.YES) {
					_preferences.addUserPlugin(FindBugsCustomPluginUtil.getAsString(plugin), plugin.getPluginId(), true);
					updatePreferences();
					_preferences.setModified(true);
				}
			} finally {
				FindBugsCustomPluginUtil.unload(plugin);
			}
		} catch (final Throwable e) {
			//noinspection DialogTitleCapitalization
			Messages.showErrorDialog(_parent, "Error loading " + selectedFile.getPath() + ":\n\n" + e.getClass().getSimpleName() + ": " + e.getMessage(), "Error loading plugin");
		}
	}


	@Nullable
	private Project getCurrentFbProject() {
		final ToolWindowPanel panel = _parent.getFindBugsPlugin().getToolWindowPanel();
		if (panel == null) {
			return null;
		}
		final BugCollection bugCollection = panel.getBugCollection();
		if (bugCollection == null) {
			return null;
		}
		return bugCollection.getProject();
	}


	@Override
	public void setEnabled(final boolean enabled) {
		getAddButton().setEnabled(enabled);
		// TODO Plugin checkboxes
	}


	@Override
	public boolean showInModulePreferences() {
		return false;
	}


	@Override
	public boolean isAdvancedConfig() {
		return false;
	}


	@Override
	public String getTitle() {
		return "Plugins";
	}


	@Override
	public void filter(final String filter) {
		// TODO support search
	}


	public void importPreferences(final com.intellij.openapi.project.Project project, final PersistencePreferencesBean prefs, final ImportCallback callback) {
		final List<String> invalid = FindBugsPreferences.collectInvalidPlugins(prefs.getPlugins());
		if (invalid.isEmpty()) {
			callback.validated(prefs);
		} else {
			showImportDialog(project, prefs, callback, invalid);
		}
	}


	private void showImportDialog(final com.intellij.openapi.project.Project project, final PersistencePreferencesBean prefs, final ImportCallback callback, final List<String> invalidPlugins) {
		final ImportPluginsDialog dialog = new ImportPluginsDialog(project, prefs, invalidPlugins);
		dialog.setModal(true);
		dialog.pack();
		dialog.show();
		if(DialogWrapper.OK_EXIT_CODE == dialog.getExitCode()) {
			prefs.getPlugins().clear();
			prefs.getPlugins().addAll(dialog.getPlugins());
			callback.validated(prefs);
		} // else do not import
	}


	private class PluginLoaderImpl extends AbstractPluginLoaderLegacy {

		private Project _currentProject;
		private JPanel _lastPluginComponent;


		protected PluginLoaderImpl(final Project currentProject) {
			super(true);
			_currentProject = currentProject;
		}


		@Override
		protected void seenCorePlugin(final Plugin plugin) {
			seenPlugin(plugin, false);
		}


		@Override
		protected void seenBundledPlugin(final Plugin plugin) {
			seenPlugin(plugin, false);
		}


		@Override
		protected void seenUserPlugin(@NotNull final String pluginUrl, final Plugin plugin) {
			seenPlugin(plugin, true);
		}


		private void seenPlugin(final Plugin plugin, final boolean userPlugin) {
			final PluginComponent pluginPanel = new PluginComponent(_currentProject, plugin, userPlugin, _preferences);
			_lastPluginComponent = pluginPanel.getComponent();
			_pluginComponentPanel.add(_lastPluginComponent);
		}


		public JPanel getLastPluginComponent() {
			return _lastPluginComponent;
		}
	}


	private static class PluginComponent {

		private JPanel _component;
		private final FindBugsPreferences _preferences;
		private static final Border SELECTION_BORDER = BorderFactory.createLineBorder(GuiResources.HIGHLIGHT_COLOR_DARKER);


		private PluginComponent(@Nullable final Project currentProject, final Plugin plugin, final boolean userPlugin, final FindBugsPreferences preferences) {
			/**
			 * Do not hold reference to currentProject nor plugin
			 */
			_preferences = preferences;
			init(currentProject, plugin, userPlugin);
		}


		void init(@Nullable final Project currentProject, final Plugin plugin, final boolean userPlugin) {
			final double border = GuiUtil.SCALE_FACTOR*5;
			final double[][] size = {{border, TableLayoutConstants.PREFERRED, 5, TableLayoutConstants.FILL, border}, // Columns
									 {border, TableLayoutConstants.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_component = new JPanel(tbl);
			_component.setBorder(new CustomLineBorder(JBColor.GRAY, 0, 0, GuiUtil.SCALE_FACTOR*1, 0));
			_component.setBackground(PLUGIN_DESCRIPTION_BG_COLOR);

			String text = plugin.getShortDescription();
			final String pluginId = plugin.getPluginId();
			if (text == null) {
				text = pluginId;
			}

			final String pluginUrl = FindBugsCustomPluginUtil.getAsString(plugin);
			final boolean enabled = isEnabled(currentProject, plugin);
			final AbstractButton checkbox = new JCheckBox();
			checkbox.setEnabled(enabled);
			checkbox.setBackground(PLUGIN_DESCRIPTION_BG_COLOR);
			_component.add(checkbox,"1, 1, 1, 1, L, T");

			String longText = plugin.getDetailedDescription();
			if (longText != null) {
				if (!longText.toLowerCase().startsWith("<p>")) {
					longText = "<p>" + longText + "</p>";
				}
				checkbox.setToolTipText("<html>" + longText + "</html>");
			}
			checkbox.setSelected(isSelected(currentProject, plugin, userPlugin));
			checkbox.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					if (checkbox.isSelected()) {
						if (userPlugin) {
							_preferences.addUserPlugin(pluginUrl, pluginId, true);
						} else {
							_preferences.addBundledPlugin(pluginId, true);
						}
					} else {
						if (userPlugin) {
							final String[] options = {
									"Disable",
									"Remove",
									"Cancel"
							};
							final int answer = Messages.showDialog(checkbox, "Would you like to disable or remove the plugin?", "Disable or Remove", options, 0, Messages.getQuestionIcon());
							if (0 == answer) {
								_preferences.addUserPlugin(pluginUrl, pluginId, false);
							} else if (1 == answer) {
								_preferences.removeUserPlugin(pluginUrl);
							} else {
								// cancel ; restore
								checkbox.setSelected(true);
							}
						} else {
							_preferences.addBundledPlugin(pluginId, false);
						}
					}
				}
			});

			final JEditorPane editorPane = new PluginEditorPane();
			editorPane.setEditable(false);
			editorPane.setEditorKit(new HTMLEditorKit());
			editorPane.setFocusable(false);
			editorPane.setContentType("text/html");
			editorPane.setBackground(PLUGIN_DESCRIPTION_BG_COLOR);

			final String website = plugin.getWebsite();
			final StringBuilder html = new StringBuilder("<html><body width='300px'>");
			html.append("<p><b>").append(text).append("</b> <font style='color:gray; font-size: x-small'>(").append(pluginId).append(")</font> </p>");
			html.append("<p><font style='color: gray; font-weight: normal; font-style:italic; font-size: x-small'>");
			html.append(pluginUrl);
			html.append("</font>");
			html.append(longText);
			html.append("</p>");

			if (website != null && !website.isEmpty()) {
				html.append("<br><p>");
				html.append("<font style='font-weight: bold; color:gray; font-size: small'>Website: ").append(website).append("</font>");
				html.append("</p>");
			}
			html.append("</html></body>");

			editorPane.setText(html.toString());
			_component.add(editorPane, "3, 1, 3, 1");

			_component.addMouseListener(new PluginComponentMouseAdapter(editorPane, checkbox));
			editorPane.addMouseListener(new PluginComponentMouseAdapter(editorPane, checkbox));
		}


		JPanel getComponent() {
			return _component;
		}


		@SuppressFBWarnings(value = "UCF_USELESS_CONTROL_FLOW_NEXT_LINE", justification = "See javadoc of RESPECT_PROJECT_PLUGIN_STATE")
		@SuppressWarnings({"PointlessBooleanExpression", "SimplifiableIfStatement"})
		static boolean isEnabled(@Nullable final Project project, final Plugin plugin) {
			if (plugin.isCorePlugin()) {
				return false;
			}
			if (project == null || !RESPECT_PROJECT_PLUGIN_STATE) {
				return !plugin.cannotDisable();
			}
			return false;
		}


		@SuppressFBWarnings(value = "UCF_USELESS_CONTROL_FLOW_NEXT_LINE", justification = "See javadoc of RESPECT_PROJECT_PLUGIN_STATE")
		@SuppressWarnings("PointlessBooleanExpression")
		boolean isSelected(@Nullable final Project project, final Plugin plugin, final boolean userPlugin) {
			if (plugin.isCorePlugin()) {
				return true;
			}
			if (project == null || !RESPECT_PROJECT_PLUGIN_STATE) {
				return _preferences.isPluginEnabled(plugin.getPluginId(), userPlugin);
			}
			final Boolean pluginStatus = project.getPluginStatus(plugin);
			return pluginStatus != null && pluginStatus; // need tristate checkbox for better visual state representation
		}


		private static class PluginEditorPane extends JEditorPane {

			@Override
			protected void paintComponent(final Graphics g) {
				GuiUtil.configureGraphics(g);
				super.paintComponent(g);
			}
		}

		private class PluginComponentMouseAdapter extends MouseAdapter {

			private final JEditorPane _editorPane;
			private final AbstractButton _checkbox;


			private PluginComponentMouseAdapter(final JEditorPane editorPane, final AbstractButton checkbox) {
				_editorPane = editorPane;
				_checkbox = checkbox;
			}


			@Override
			public void mouseReleased(final MouseEvent e) {
				_component.setBackground(GuiResources.HIGHLIGHT_COLOR_LIGHTER);
				//_component.setBorder(SELECTION_BORDER);
				_editorPane.setBackground(GuiResources.HIGHLIGHT_COLOR_LIGHTER);
				_checkbox.setBackground(GuiResources.HIGHLIGHT_COLOR_LIGHTER);
			}


			@Override
			public void mouseEntered(final MouseEvent e) {
				_component.setBackground(GuiResources.HIGHLIGHT_COLOR_LIGHTER);
				_editorPane.setBackground(GuiResources.HIGHLIGHT_COLOR_LIGHTER);
				_checkbox.setBackground(GuiResources.HIGHLIGHT_COLOR_LIGHTER);
			}


			@Override
			public void mouseExited(final MouseEvent e) {
				_component.setBackground(PLUGIN_DESCRIPTION_BG_COLOR);
				_editorPane.setBackground(PLUGIN_DESCRIPTION_BG_COLOR);
				_checkbox.setBackground(PLUGIN_DESCRIPTION_BG_COLOR);
			}
		}
	}


	public interface ImportCallback {
		void validated(PersistencePreferencesBean prefs);
	}
}
