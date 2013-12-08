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

import com.intellij.openapi.module.Module;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.core.ModuleComponentImpl;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


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

		add(getEnableCheckbox(), BorderLayout.NORTH);
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


	private AbstractButton getEnableCheckbox() {
		if (_checkBox == null) {
			_checkBox = new JCheckBox("Override FindBugs-IDEA project settings");
			_checkBox.setFocusable(false);
			_checkBox.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final boolean enabled = _checkBox.isSelected();
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


	private Component getConfigPanel() {
		if (_configPanel == null) {
			_configPanel = new ConfigurationPanel(_plugin);
			updatePreferences();
		}
		return _configPanel;
	}
}
