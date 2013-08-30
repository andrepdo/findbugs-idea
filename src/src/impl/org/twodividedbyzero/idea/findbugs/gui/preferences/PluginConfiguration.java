/*
 * Copyright 2008-2013 Andre Pfeiler
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
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsUtil;
import org.twodividedbyzero.idea.findbugs.common.util.GuiUtil;
import org.twodividedbyzero.idea.findbugs.gui.common.CustomLineBorder;
import org.twodividedbyzero.idea.findbugs.gui.common.ExtensionFileFilter;
import org.twodividedbyzero.idea.findbugs.gui.common.ScrollPaneFacade;
import org.twodividedbyzero.idea.findbugs.gui.preferences.BrowseAction.BrowseActionCallback;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.ToolWindowPanel;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
import org.twodividedbyzero.idea.findbugs.preferences.PersistencePreferencesBean;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
@SuppressWarnings({"AnonymousInnerClass", "HardcodedLineSeparator"})
public class PluginConfiguration implements ConfigurationPage {

	private static final Color PLUGIN_DESCRIPTION_BG_COLOR = UIManager.getColor("Tree.textBackground");

	private final FindBugsPreferences _preferences;
	private final ConfigurationPanel _parent;
	private Component _component;
	private JPanel _pluginsPanel;
	private JPanel _pluginComponentPanel;


	public PluginConfiguration(final ConfigurationPanel parent, final FindBugsPreferences preferences) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_preferences = preferences;
		_parent = parent;
	}


	public Component getComponent() {
		if (_component == null) {
			final double border = 5;
			final double[][] size = {{border, TableLayoutConstants.FILL, border}, // Columns
									 {border, TableLayoutConstants.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);

			final JComponent mainPanel = new JPanel(tbl);
			mainPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder("Installed Plugins")));
			_component = mainPanel;
			mainPanel.add(getPluginPanel(), "1, 1, 1, 1");

		}
		//updatePreferences();
		return _component;
	}


	public void updatePreferences() {
		rebuildPluginComponents();
	}


	@edu.umd.cs.findbugs.annotations.SuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
	@SuppressWarnings({"ObjectAllocationInLoop"})
	private void rebuildPluginComponents() {
		if (_pluginComponentPanel == null) {
			return;
		}
		_pluginComponentPanel.removeAll();
		final Project currentProject = getCurrentFbProject();
		JPanel pluginComponent = null;
		for (final Plugin plugin : Plugin.getAllPlugins()) {
			plugin.setGloballyEnabled(true);
			if (plugin.isCorePlugin()) {
				//continue;
			}

			final PluginComponent pluginPanel = new PluginComponent(currentProject, plugin, _preferences);
			pluginComponent = pluginPanel.getComponent();
			_pluginComponentPanel.add(pluginComponent);
		}
		if (pluginComponent != null) {
			final JPanel scrollToThis = pluginComponent;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					scrollToThis.scrollRectToVisible(new Rectangle(0, 0));
				}
			});
		}
	}


	JPanel getPluginPanel() {
		if (_pluginsPanel == null) {

			final double border = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayoutConstants.PREFERRED, colsGap, TableLayoutConstants.PREFERRED, border}, // Columns
									 {border, TableLayoutConstants.FILL, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_pluginsPanel = new JPanel(tbl);

			_pluginComponentPanel = new JPanel();
			_pluginComponentPanel.setLayout(new BoxLayout(_pluginComponentPanel, BoxLayout.Y_AXIS));
			rebuildPluginComponents();

			final Component scrollPane = ScrollPaneFacade.createScrollPane(_pluginComponentPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			_pluginsPanel.add(scrollPane, "1, 1, 1, 1"); // col ,row, col, row


			final double rowsGap = 5;
			final double[][] bPanelSize = {{border, TableLayoutConstants.PREFERRED}, // Columns
										   {border, TableLayoutConstants.PREFERRED, rowsGap, TableLayoutConstants.PREFERRED, border}};// Rows
			final TableLayout tableLayout = new TableLayout(bPanelSize);

			final Container buttonPanel = new JPanel(tableLayout);
			_pluginsPanel.add(buttonPanel, "3, 1, 3, 1");

			final AbstractButton addButton = new JButton();
			final Action action = new BrowseAction(_parent, "Install New Plugin...", new ExtensionFileFilter(FindBugsUtil.PLUGINS_EXTENSIONS_SET), new BrowseActionCallback() {
				public void addSelection(final File selectedFile) {
					try {
						Plugin.loadCustomPlugin(selectedFile, getCurrentFbProject());
						try {
							_preferences.addPlugin(FindBugsPreferences.getPluginAsString(selectedFile));
							showRestartHint(_parent);
						} catch (MalformedURLException e) {
							Messages.showErrorDialog(e.getMessage(), e.getClass().getSimpleName());
						}
						updatePreferences();
					} catch (Throwable e) {
						//noinspection DialogTitleCapitalization
						Messages.showErrorDialog(_parent, "Error loading " + selectedFile.getPath() + ":\n\n" + e.getClass().getSimpleName() + ": " + e.getMessage(), "Error loading plugin");
					}
					_preferences.setModified(true);
				}
			});
			addButton.setAction(action);
			buttonPanel.add(addButton, "1, 1, 1, 1");

		}

		return _pluginsPanel;
	}


	static void showRestartHint(Component parent) {
		Messages.showInfoMessage(parent, "Restart Intellij IDEA to load custom findbugs plugins.", "Intellij Needs to Be Restarted");
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


	public void setEnabled(final boolean enabled) {
		getPluginPanel().setEnabled(enabled);
	}


	public boolean showInModulePreferences() {
		return false;
	}


	public boolean isAdvancedConfig() {
		return false;
	}


	public String getTitle() {
		return "Plugins";
	}


	public void filter(String filter) {
		// TODO support search
	}


	public void importPreferences(com.intellij.openapi.project.Project project, PersistencePreferencesBean prefs, ImportCallback callback) {
		final List<String> invalid = FindBugsPreferences.collectInvalidPlugins(prefs.getPlugins());
		if (invalid.isEmpty()) {
			callback.validated(prefs);
		} else {
			showImportDialog(project, prefs, callback, invalid);
		}
	}


	private void showImportDialog(com.intellij.openapi.project.Project project, PersistencePreferencesBean prefs, ImportCallback callback, List<String> invalidPlugins) {
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


	private static class PluginComponent {

		private final Plugin _plugin;
		private JPanel _component;
		private final Project _currentProject;
		private final FindBugsPreferences _preferences;
		private static final Border SELECTION_BORDER = BorderFactory.createLineBorder(GuiResources.HIGHLIGHT_COLOR_DARKER);


		private PluginComponent(final Project currentProject, final Plugin plugin, final FindBugsPreferences preferences) {
			_plugin = plugin;
			_currentProject = currentProject;
			_preferences = preferences;
		}


		JPanel getComponent() {
			if (_component == null) {
				final double border = 5;
				final double[][] size = {{border, TableLayoutConstants.PREFERRED, 5, TableLayoutConstants.FILL, border}, // Columns
										 {border, TableLayoutConstants.PREFERRED, border}};// Rows
				final TableLayout tbl = new TableLayout(size);
				_component = new JPanel(tbl);
				_component.setBorder(new CustomLineBorder(JBColor.GRAY, 0, 0, 1, 0));
				_component.setBackground(PLUGIN_DESCRIPTION_BG_COLOR);

				String text = _plugin.getShortDescription();
				final String id = _plugin.getPluginId();
				if (text == null) {
					text = id;
				}

				final String pluginUrl = _plugin.getPluginLoader().getURL().toExternalForm();
				final boolean enabled = isEnabled(_currentProject, _plugin);
				final AbstractButton checkbox = new JCheckBox();
				checkbox.setEnabled(enabled);
				checkbox.setBackground(PLUGIN_DESCRIPTION_BG_COLOR);
				_component.add(checkbox,"1, 1, 1, 1, L, T");

				final String longText = _plugin.getDetailedDescription();
				if (longText != null) {
					checkbox.setToolTipText("<html>" + longText + "</html>");
				}
				checkbox.setSelected(!_preferences.isPluginDisabled(_plugin.getPluginId()));
				checkbox.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						_preferences.enablePlugin(_plugin.getPluginId(), checkbox.isSelected());
						_preferences.setModified(true);
					}
				});

				final JEditorPane editorPane = new PluginEditorPane();
				editorPane.setEditable(false);
				editorPane.setEditorKit(new HTMLEditorKit());
				editorPane.setFocusable(false);
				editorPane.setContentType("text/html");

				final String website = _plugin.getWebsite();
				final StringBuilder html = new StringBuilder("<html><body width='300px'>");
				html.append("<p><b>").append(text).append("</b> <font style='color:gray; font-size: 8px'>(").append(_plugin.getPluginId()).append(")</font> </p>");
				html.append("<p><font style='color: gray; font-weight: normal; font-style:italic'>");
				html.append(pluginUrl);
				html.append("</font>");
				html.append(longText);
				html.append("</p>");

				if (website != null && website.length() > 0) {
					html.append("<br><p>");
					html.append("<font style='font-weight: bold; color:gray; font-size: 8px'>Website: ").append(website).append("</font>");
					html.append("</p>");
				}
				html.append("</html></body>");

				editorPane.setText(html.toString());
				_component.add(editorPane, "3, 1, 3, 1");

				_component.addMouseListener(new PluginComponentMouseAdapter(editorPane, checkbox));
				editorPane.addMouseListener(new PluginComponentMouseAdapter(editorPane, checkbox));
			}
			return _component;
		}


		static boolean isEnabled(final Project project, final Plugin plugin) {
			if (plugin.isCorePlugin()) {
				return false;
			}
			if (project == null) {
				return plugin.isGloballyEnabled();
			}
			final Boolean pluginStatus = project.getPluginStatus(plugin);
			return pluginStatus != null;
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


	public static interface ImportCallback {
		void validated(PersistencePreferencesBean prefs);
	}
}
