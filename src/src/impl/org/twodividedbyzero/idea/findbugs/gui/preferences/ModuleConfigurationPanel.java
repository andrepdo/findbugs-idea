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

import com.intellij.openapi.module.Module;
import org.twodividedbyzero.idea.findbugs.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.ModuleComponentImpl;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.9
 */
public class ModuleConfigurationPanel extends JPanel {

	private final FindBugsPlugin _plugin;
	private ConfigurationPanel _configPanel;
	private JCheckBox _checkBox;


	public ModuleConfigurationPanel(final FindBugsPlugin plugin) {
		super(new BorderLayout());

		if (plugin == null) {
			throw new IllegalArgumentException("Plugin may not be null.");
		}

		_plugin = plugin;

		add(getEnableCheckbox(), BorderLayout.NORTH);  // NON-NLS
		add(getConfigPanel(), BorderLayout.CENTER);
	}


	private FindBugsPreferences getProjectPreferences() {
		return ((ModuleComponentImpl) _plugin).getProjectPreferences();
	}


	private void updatePreferences() {
		final Module module = _plugin.getModule();
		if (module != null) {
			final boolean enabled = getProjectPreferences().isModuleConfigEnabled(module.getName());
			getEnableCheckbox().setSelected(enabled);
			_configPanel.setEnabled(enabled);
		}
	}


	private JCheckBox getEnableCheckbox() {
		if (_checkBox == null) {
			_checkBox = new JCheckBox("Override FindBugs-IDEA project settings");
			_checkBox.setFocusable(false);
			_checkBox.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					final boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
					getConfigPanel().setEnabled(enabled);
					final Module module = _plugin.getModule();
					if (module != null) {
						getProjectPreferences().enabledModuleConfig(module.getName(), enabled);
						getConfigPanel().setEnabled(enabled);
					}
				}
			});
			_checkBox.setBorder(BorderFactory.createEmptyBorder(20, 5, 20, 20));
		}
		return _checkBox;
	}


	private ConfigurationPanel getConfigPanel() {
		if (_configPanel == null) {
			_configPanel = new ConfigurationPanel(_plugin);
			updatePreferences();
		}
		return _configPanel;
	}
}
