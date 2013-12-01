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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.ui.FilterComponent;
import com.intellij.util.xmlb.XmlSerializer;
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.color.ColorUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.gui.common.ScrollPaneFacade;
import org.twodividedbyzero.idea.findbugs.preferences.AnalysisEffort;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;
import org.twodividedbyzero.idea.findbugs.preferences.PersistencePreferencesBean;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.84-dev
 */
@SuppressWarnings({"AnonymousInnerClass"})
public class ConfigurationPanel extends JPanel {

	private static final Logger LOGGER = Logger.getInstance(ConfigurationPanel.class.getName());

	private final FindBugsPlugin _plugin;
	private JCheckBox _compileBeforeAnalyseChkb;
	private JCheckBox _analyzeAfterCompileChkb;
	private JCheckBox _runInBackgroundChkb;
	private JCheckBox _toolwindowToFront;


	private JComboBox _effortLevelCombobox;
	private JPanel _mainPanel;
	private JCheckBox _detectorThresholdChkb;
	private JPanel _effortPanel;
	private JTabbedPane _tabbedPane;
	private transient DetectorConfiguration _detectorConfig;
	private transient ReportConfiguration _reporterConfig;
	private JButton _restoreDefaultsButton;
	private JSlider _effortSlider;
	private transient FilterConfiguration _filterConfig;
	private transient PluginConfiguration _pluginConfig;
	private transient ImportExportConfiguration _importExportConfig;
	private transient AnnotationConfiguration _annotationConfig;
	private List<ConfigurationPage> _configPagesRegistry;
	private JToggleButton _showAdvancedConfigsButton;
	private JButton _exportButton;
	private JButton _importButton;
	private MyFilter _myFilter;


	public ConfigurationPanel(final FindBugsPlugin plugin) {
		super(new BorderLayout());

		if (plugin == null) {
			throw new IllegalArgumentException("Plugin may not be null.");
		}

		_plugin = plugin;

		initGui();

		final FindBugsPreferences preferences = getPreferences();
		if (!preferences.getBugCategories().containsValue("true") && !preferences.getDetectors().containsValue("true") || (preferences.getBugCategories().isEmpty() && preferences.getDetectors().isEmpty())) {
			restoreDefaultPreferences();
		}
	}


	Project getProject() {
		return _plugin.getProject();
	}


	public FindBugsPlugin getFindBugsPlugin() {
		return _plugin;
	}


	private void initGui() {
		add( ScrollPaneFacade.createScrollPane(getMainPanel()), BorderLayout.CENTER );
	}


	private Component getMainPanel() {
		if (_mainPanel == null) {

			final double border = 5;
			final double rowsGap = 0;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.FILL, border}, // Columns
									 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, 10, TableLayout.FILL, 10, TableLayout.PREFERRED, border}};// Rows
			final LayoutManager tbl = new TableLayout(size);

			_mainPanel = new JPanel();
			_mainPanel.setLayout(tbl);
			//panel.setBorder(BorderFactory.createTitledBorder("Inspections"));
			_mainPanel.setBorder(BorderFactory.createTitledBorder("Configuration options"));
			//_compileBeforeAnalyseChkb = new JCheckBox("Compile before inspect");

			updatePreferences();

			//_detectorThresholdChkb = new JCheckBox("Set DetectorThreshold");

			//_mainPanel.add(_compileBeforeAnalyseChkb);
			final Container checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			checkboxPanel.add(getRunInBgCheckbox());
			checkboxPanel.add(getAnalyzeAfterCompileCheckbox());
			checkboxPanel.add(getToolwindowToFrontCheckbox());
			_mainPanel.add(checkboxPanel, "1, 1, 3, 1");
			//_mainPanel.add(_detectorThresholdChkb);
			////_mainPanel.add(getEffortPanel(), "1, 3, 3, 3");

			final Container tabPanel = new JPanel(new BorderLayout());
			tabPanel.add(getTabbedPane(), BorderLayout.CENTER);
			_mainPanel.add(tabPanel, "1, 5, 3, 5");

			final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			buttonsPanel.add(getMyFilter());
			buttonsPanel.add(getExportButton());
			buttonsPanel.add(getImportButton());
			buttonsPanel.add(getShowAdvancedConfigsButton());
			buttonsPanel.add(getRestoreDefaultsButton());
			_mainPanel.add(buttonsPanel, "3, 7, 3, 7, R, T");

		}

		return _mainPanel;
	}


	private void updatePreferences() {
		getEffortSlider().setValue(AnalysisEffort.valueOfLevel(getPreferences().getProperty(FindBugsPreferences.ANALYSIS_EFFORT_LEVEL, AnalysisEffort.DEFAULT.getEffortLevel())).getValue());
		getRunInBgCheckbox().setSelected(getPreferences().getBooleanProperty(FindBugsPreferences.RUN_ANALYSIS_IN_BACKGROUND, false));
		getAnalyzeAfterCompileCheckbox().setSelected(getPreferences().getBooleanProperty(FindBugsPreferences.ANALYZE_AFTER_COMPILE, false));
		getToolwindowToFrontCheckbox().setSelected(getPreferences().getBooleanProperty(FindBugsPreferences.TOOLWINDOW_TO_FRONT, true));
		getEffortLevelCombobox().setSelectedItem(AnalysisEffort.valueOfLevel(getPreferences().getProperty(FindBugsPreferences.ANALYSIS_EFFORT_LEVEL, AnalysisEffort.DEFAULT.getEffortLevel())));
		//((FindBugsPluginImpl) _plugin).setPreferences(FindBugsPreferences.createDefaultPreferences());
		getReporterConfig().updatePreferences();
		getDetectorConfig().updatePreferences();
		getFilterConfig().updatePreferences();
		if (!_plugin.isModuleComponent()) {
			getPluginConfig().updatePreferences();
			getImportExportConfig().updatePreferences();
			getAnnotationConfig().updatePreferences();
		}
	}


	private FindBugsPreferences getPreferences() {
		return _plugin.getPreferences();
	}


	private AbstractButton getRunInBgCheckbox() {
		if (_runInBackgroundChkb == null) {
			_runInBackgroundChkb = new JCheckBox("Run analysis in background");
			_runInBackgroundChkb.setFocusable(false);
			_runInBackgroundChkb.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					getPreferences().setProperty(FindBugsPreferences.RUN_ANALYSIS_IN_BACKGROUND, e.getStateChange() == ItemEvent.SELECTED);
				}
			});
		}
		return _runInBackgroundChkb;
	}


	private AbstractButton getAnalyzeAfterCompileCheckbox() {
		if (_analyzeAfterCompileChkb == null) {
			_analyzeAfterCompileChkb = new JCheckBox("Analyze affected files after compile");
			_analyzeAfterCompileChkb.setFocusable( false );
			_analyzeAfterCompileChkb.addItemListener( new ItemListener() {
				public void itemStateChanged( final ItemEvent e ) {
					getPreferences().setProperty( FindBugsPreferences.ANALYZE_AFTER_COMPILE, e.getStateChange() == ItemEvent.SELECTED );
				}
			} );
		}
		return _analyzeAfterCompileChkb;
	}


	private AbstractButton getToolwindowToFrontCheckbox() {
		if (_toolwindowToFront == null) {
			_toolwindowToFront = new JCheckBox("Activate toolwindow on run");
			_toolwindowToFront.setFocusable(false);
			_toolwindowToFront.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					getPreferences().setProperty(FindBugsPreferences.TOOLWINDOW_TO_FRONT, e.getStateChange() == ItemEvent.SELECTED);
				}
			});
		}
		return _toolwindowToFront;
	}


	private Component getEffortPanel() {
		if (_effortPanel == null) {

			final double border = 5;
			final double rowsGap = 0;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.PREFERRED, border}, // Columns
									 {border, TableLayout.PREFERRED, border}};// Rows
			final LayoutManager tbl = new TableLayout(size);

			_effortPanel = new JPanel(tbl);
			_effortPanel.add(new JLabel("Analysis effort"), "1, 1, 1, 1");
			_effortPanel.setBorder(null);
			_effortPanel.add( getEffortSlider(), "3, 1, 3, 1, l, t" );
		}
		return _effortPanel;
	}


	@SuppressWarnings({"UseOfObsoleteCollectionType"})
	private JSlider getEffortSlider() {
		if (_effortSlider == null) {
			_effortSlider = new JSlider(JSlider.HORIZONTAL, 10, 30, 20);
			_effortSlider.setBackground(GuiResources.HIGHLIGHT_COLOR_DARKER);
			_effortSlider.setMajorTickSpacing(10);
			_effortSlider.setPaintTicks(true);
			_effortSlider.setSnapToTicks(true);
			_effortSlider.setBorder(null);
			_effortSlider.setFocusable(false);

			final Dictionary<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
			labelTable.put(10, new JLabel(AnalysisEffort.MIN.getMessage()));
			labelTable.put(20, new JLabel(AnalysisEffort.DEFAULT.getMessage()));
			labelTable.put(30, new JLabel(AnalysisEffort.MAX.getMessage()));
			_effortSlider.setLabelTable(labelTable);
			_effortSlider.setPaintLabels(true);

			_effortSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(final ChangeEvent e) {
					final JSlider source = (JSlider) e.getSource();
					if (!source.getValueIsAdjusting()) {
						final int value = source.getValue();
						getPreferences().setProperty(FindBugsPreferences.ANALYSIS_EFFORT_LEVEL, AnalysisEffort.getEffortLevelByValue(value));
					}
				}
			});

		}
		return _effortSlider;
	}


	private JComboBox getEffortLevelCombobox() {
		if (_effortLevelCombobox == null) {
			_effortLevelCombobox = new JComboBox(new DefaultComboBoxModel(AnalysisEffort.values()));
			_effortLevelCombobox.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					getPreferences().setProperty(FindBugsPreferences.ANALYSIS_EFFORT_LEVEL, ((AnalysisEffort) e.getItem()).getEffortLevel());
				}
			});
		}
		return _effortLevelCombobox;
	}


	private Component getTabbedPane() {
		if (_tabbedPane == null) {
			_tabbedPane = new JTabbedPane();

			final List<ConfigurationPage> configPages = getConfigPages();
			for (final ConfigurationPage configPage : configPages) {
				if (!configPage.isAdvancedConfig()) {
					if (!_plugin.isModuleComponent()) {
						_tabbedPane.addTab(configPage.getTitle(), configPage.getComponent());
					} else if (configPage.showInModulePreferences()) {
						_tabbedPane.addTab(configPage.getTitle(), configPage.getComponent());
					}
				}
			}
		}
		return _tabbedPane;
	}

	List<ConfigurationPage> getConfigPages() {
		if (_configPagesRegistry == null) {
			_configPagesRegistry = new ArrayList<ConfigurationPage>();
			_configPagesRegistry.add(getDetectorConfig());
			_configPagesRegistry.add(getReporterConfig());
			_configPagesRegistry.add(getFilterConfig());
			if (!_plugin.isModuleComponent()) {
				_configPagesRegistry.add(getPluginConfig());
				_configPagesRegistry.add(getImportExportConfig());
				_configPagesRegistry.add(getAnnotationConfig());
			}
		}
		return _configPagesRegistry;
	}


	DetectorConfiguration getDetectorConfig() {
		if (_detectorConfig == null) {
			_detectorConfig = new DetectorConfiguration(this, getPreferences());
		}
		return _detectorConfig;
	}


	ReportConfiguration getReporterConfig() {
		if (_reporterConfig == null) {
			_reporterConfig = new ReportConfiguration(this, getPreferences());
		}
		return _reporterConfig;
	}


	FilterConfiguration getFilterConfig() {
		if (_filterConfig == null) {
			_filterConfig = new FilterConfiguration(this, getPreferences());
		}
		return _filterConfig;
	}


	PluginConfiguration getPluginConfig() {
		if (_pluginConfig == null) {
			_pluginConfig = new PluginConfiguration(this, getPreferences());
		}
		return _pluginConfig;
	}


	ImportExportConfiguration getImportExportConfig() {
		if (_importExportConfig == null) {
			_importExportConfig = new ImportExportConfiguration(this, getPreferences());
		}
		return _importExportConfig;
	}


	AnnotationConfiguration getAnnotationConfig() {
		if (_annotationConfig == null) {
			_annotationConfig = new AnnotationConfiguration(this, getPreferences());
		}
		return _annotationConfig;
	}


	private Component getRestoreDefaultsButton() {
		if (_restoreDefaultsButton == null) {
			_restoreDefaultsButton = new JButton("Restore defaults");
			_restoreDefaultsButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					restoreDefaultPreferences();
				}
			});
		}
		return _restoreDefaultsButton;
	}


	private Component getShowAdvancedConfigsButton() {
		if (_showAdvancedConfigsButton == null) {
			_showAdvancedConfigsButton = new JCheckBox("Advanced");
			_showAdvancedConfigsButton.setBackground(GuiResources.HIGHLIGHT_COLOR_DARKER);
			_showAdvancedConfigsButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					if(_showAdvancedConfigsButton.isSelected()) {
						showAdvancedConfigs(true);
					} else {
						showAdvancedConfigs(false);
					}
				}
			});
		}
		return _showAdvancedConfigsButton;
	}


	private Component getExportButton() {
		if (_exportButton == null) {
			_exportButton = new JButton("Export");
			_exportButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					exportPreferences();
				}
			});
		}
		return _exportButton;
	}


	private Component getImportButton() {
		if (_importButton == null) {
			_importButton = new JButton("Import");
			_importButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					importPreferences();
				}
			});
		}
		return _importButton;
	}


	private MyFilter getMyFilter() {
		if (_myFilter == null) {
			_myFilter = new MyFilter();
		}
		return _myFilter;
	}


	private void showAdvancedConfigs(final boolean show) {
		if (show) {
			_mainPanel.add(getEffortPanel(), "1, 3, 3, 3");
			_mainPanel.validate();
			_mainPanel.repaint();
		} else {
			_mainPanel.remove(getEffortPanel());
			_mainPanel.validate();
			_mainPanel.repaint();
		}

		final String fontColorHex = ColorUtil.toHexString(GuiResources.HIGHLIGHT_COLOR_DARKER);

		final List<ConfigurationPage> configPages = getConfigPages();
		Component firstAdvancedPage = null;
		for (int i = 0, configPagesSize = configPages.size(); i < configPagesSize; i++) {
			final ConfigurationPage configPage = configPages.get(i);
			if (configPage.isAdvancedConfig()) {
				if (!_plugin.isModuleComponent()) {
					if (show) {
						if (firstAdvancedPage == null) {
							firstAdvancedPage = configPage.getComponent();
						}
						_tabbedPane.insertTab("<html><b><font color='" + fontColorHex + "'>" + configPage.getTitle(), null, configPage.getComponent(), configPage.getTitle(), i);
						_tabbedPane.setForegroundAt(i, GuiResources.HIGHLIGHT_COLOR_DARKER);
					} else {
						_tabbedPane.remove(configPage.getComponent());
					}
				} else if (configPage.showInModulePreferences()) {
					if (show) {
						if (firstAdvancedPage == null) {
							firstAdvancedPage = configPage.getComponent();
						}
						_tabbedPane.insertTab("<html><b><font color='" + fontColorHex + "'>" + configPage.getTitle(), null, configPage.getComponent(), configPage.getTitle(), i);
						_tabbedPane.setForegroundAt(i, GuiResources.HIGHLIGHT_COLOR_DARKER);
					} else {
						_tabbedPane.remove(configPage.getComponent());
					}
				}
			}
		}
		if (firstAdvancedPage != null) {
			_tabbedPane.setSelectedComponent(firstAdvancedPage);
		}
	}


	private void restoreDefaultPreferences() {
		final FindBugsPreferences bugsPreferences = getPreferences();
		bugsPreferences.setDefaults(FindBugsPreferences.createDefault());
		updatePreferences();
		bugsPreferences.setModified(true);
	}


	private void exportPreferences() {
		final PersistencePreferencesBean prefs = _plugin.getState();
		final VirtualFileWrapper wrapper = FileChooserFactory.getInstance().createSaveFileDialog(
				new FileSaverDescriptor("Export FindBugs Preferences to File...", "", "xml"), this).save( null, null );
		if (wrapper == null) return;
		final Element el= XmlSerializer.serialize(prefs);
		el.setName("findbugs"); // rename "PersistencePreferencesBean"
		final Document document = new Document(el);
		try {
			JDOMUtil.writeDocument(document, wrapper.getFile(), "\n");
		} catch (IOException ex) {
			LOGGER.error(ex);
			final String msg = ex.getLocalizedMessage();
			//noinspection DialogTitleCapitalization
			Messages.showErrorDialog(this, msg != null && msg.length() > 0 ? msg : ex.toString(), "Export failed");
		}
	}


	private void importPreferences() {
		final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, false, true, false) {
			@Override
			public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
				return super.isFileVisible(file, showHiddenFiles) &&
						(file.isDirectory() || "xml".equals(file.getExtension()) || file.getFileType() == StdFileTypes.ARCHIVE);
			}

			@Override
			public boolean isFileSelectable(VirtualFile file) {
				return file.getFileType() == StdFileTypes.XML;
			}
		};
		descriptor.setDescription( "Please select the configuration file (usually named findbugs.xml) to import." );
		descriptor.setTitle( "Import Configuration" );

		final VirtualFile [] files = FileChooser.chooseFiles(descriptor, this, getProject(), null);
		if (files.length != 1) {
			return;
		}
		final PersistencePreferencesBean prefs;
		try {
			final Document document = JDOMUtil.loadDocument(files[0].getInputStream());
			prefs = XmlSerializer.deserialize(document, PersistencePreferencesBean.class);
		} catch (Exception ex) {
			LOGGER.warn(ex);
			final String msg = ex.getLocalizedMessage();
			//noinspection DialogTitleCapitalization
			Messages.showErrorDialog(this, msg != null && msg.length() > 0 ? msg : ex.toString(), "Import failed");
			return;
		}
		_pluginConfig.importPreferences( getProject(), prefs, new PluginConfiguration.ImportCallback() {
			public void validated( PersistencePreferencesBean prefs ) {
				_plugin.loadState( prefs );
				updatePreferences();
				if ( !prefs.getPlugins().isEmpty() ) {
					PluginConfiguration.showRestartHint( ConfigurationPanel.this );
				}
			}
		} );
	}


	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		final List<ConfigurationPage> configPages = getConfigPages();
		for (final ConfigurationPage configPage : configPages) {
			configPage.setEnabled(enabled);
		}
	}


	public void setFilter(String filter) {
		_myFilter.setSelectedItem(filter);
	}


	public class MyFilter extends FilterComponent {

		public MyFilter() {
			super("FILTER", 5);
		}

		public void filter() {
			if (!_showAdvancedConfigsButton.isSelected()) {
				showAdvancedConfigs(true);
			}
			final String filter = getFilter().toLowerCase();
			for (ConfigurationPage page : _configPagesRegistry) {
				page.filter(filter);
			}
		}
	}
}
