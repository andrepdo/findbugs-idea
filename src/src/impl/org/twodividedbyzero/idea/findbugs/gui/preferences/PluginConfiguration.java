/*
 * Copyright 2009 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.twodividedbyzero.idea.findbugs.gui.preferences;

import com.intellij.openapi.ui.Messages;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.Plugin;
import edu.umd.cs.findbugs.PluginException;
import edu.umd.cs.findbugs.Project;
import info.clearthought.layout.TableLayout;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsUtil;
import org.twodividedbyzero.idea.findbugs.common.util.GuiUtil;
import org.twodividedbyzero.idea.findbugs.gui.common.CustomLineBorder;
import org.twodividedbyzero.idea.findbugs.gui.common.ExtensionFileFilter;
import org.twodividedbyzero.idea.findbugs.gui.preferences.BrowseAction.BrowseActionCallback;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.ToolWindowPanel;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
@SuppressWarnings({"AnonymousInnerClass"})
public class PluginConfiguration implements ConfigurationPage {

	private final FindBugsPreferences _preferences;
	private final ConfigurationPanel _parent;
	private Component _component;
	private JPanel _pluginsPanel;
	private JPanel _pluginComponentPanel;
	private static final Color PLUGIN_DESCRIPTION_BG_COLOR = UIManager.getColor("Tree.textBackground");


	public PluginConfiguration(final ConfigurationPanel parent, final FindBugsPreferences preferences) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_preferences = preferences;
		_parent = parent;
	}


	public Component getComponent() {
		if (_component == null) {
			final double border = 5;
			final double[][] size = {{border, TableLayout.FILL, border}, // Columns
									 {border, TableLayout.PREFERRED, border}};// Rows
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
		rebuildCheckboxes();
	}


	private void rebuildCheckboxes() {
		if (_pluginComponentPanel == null) {
			return;
		}
		_pluginComponentPanel.removeAll();
		final Project currentProject = getCurrentProject();
		for (final Plugin plugin : Plugin.getAllPlugins()) {
			if (plugin.isCorePlugin()) {
				continue;
			}

			final PluginComponent pluginPanel = new PluginComponent(currentProject, plugin, _preferences);
			final JPanel pluginComponent = pluginPanel.getComponent();
			_pluginComponentPanel.add(pluginComponent);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					pluginComponent.scrollRectToVisible(new Rectangle(0, 0));
				}
			});
		}
	}


	JPanel getPluginPanel() {
		if (_pluginsPanel == null) {

			final double border = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.PREFERRED, border}, // Columns
									 {border, 540, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_pluginsPanel = new JPanel(tbl);

			_pluginComponentPanel = new JPanel();
			_pluginComponentPanel.setLayout(new BoxLayout(_pluginComponentPanel, BoxLayout.Y_AXIS));
			rebuildCheckboxes();

			final Component scrollPane = new JScrollPane(_pluginComponentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			_pluginsPanel.add(scrollPane, "1, 1, 1, 1"); // col ,row, col, row


			final double rowsGap = 5;
			final double[][] bPanelSize = {{border, TableLayout.PREFERRED}, // Columns
										   {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tableLayout = new TableLayout(bPanelSize);

			final Container buttonPanel = new JPanel(tableLayout);
			_pluginsPanel.add(buttonPanel, "3, 1, 3, 1");

			final AbstractButton addButton = new JButton();
			final Action action = new BrowseAction(_parent, "Install New Plugin...", new ExtensionFileFilter(FindBugsUtil.PLUGINS_EXTENSIONS_SET), new BrowseActionCallback() {
				public void addSelection(final File selectedFile) {
					try {
						Plugin.loadPlugin(selectedFile, getCurrentProject());
						try {
							_preferences.addPlugin(selectedFile.toURI().toURL().toExternalForm());
						} catch (MalformedURLException e) {
							Messages.showErrorDialog(e.getMessage(), e.getClass().getSimpleName());
						}
						updatePreferences();
					} catch (PluginException e) {
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


	private Project getCurrentProject() {
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
		_pluginsPanel.setEnabled(enabled);
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


	private static class PluginComponent {

		private final Plugin _plugin;
		private JPanel _component;
		private final Project _currentProject;
		private final FindBugsPreferences _preferences;


		private PluginComponent(final Project currentProject, final Plugin plugin, final FindBugsPreferences preferences) {
			_plugin = plugin;
			_currentProject = currentProject;
			_preferences = preferences;
		}


		JPanel getComponent() {
			if (_component == null) {
				final double border = 5;
				final double[][] size = {{border, TableLayout.PREFERRED, 5, TableLayout.FILL, border}, // Columns
										 {border, TableLayout.PREFERRED, border}};// Rows
				final TableLayout tbl = new TableLayout(size);
				_component = new JPanel(tbl);
				_component.setBorder(new CustomLineBorder(Color.GRAY, 0, 0, 1, 0));
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

				if (website.length() > 0) {
					html.append("<br><p>");
					html.append("<font style='font-weight: bold; color:gray; font-size: 8px'>Website: ").append(website).append("</font>");
					html.append("</p>");
				}
				html.append("</html></body>");

				editorPane.setText(html.toString());
				_component.add(editorPane, "3, 1, 3, 1");
			}
			return _component;
		}


		boolean isEnabled(final Project project, final Plugin plugin) {
			if (project == null) {
				return plugin.isGloballyEnabled();
			}
			return project.getPluginStatus(plugin);
		}


		private static class PluginEditorPane extends JEditorPane {

			@Override
			protected void paintComponent(final Graphics g) {
				GuiUtil.configureGraphics(g);
				super.paintComponent(g);
			}
		}
	}

}