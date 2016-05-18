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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.core.ModuleSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JComponent;

public final class ModuleConfigurableImpl implements Configurable {

	@NotNull
	private final Module module;

	@NotNull
	private final ModuleSettings settings;

	private SettingsPane pane;

	public ModuleConfigurableImpl(@NotNull final Module module) {
		this.module = module;
		settings = ModuleSettings.getInstance(module);
	}

	@Nls
	@Override
	public String getDisplayName() {
		return ResourcesLoader.getString("findbugs.plugin.configuration.name");
	}

	@Nullable
	@Override
	public String getHelpTopic() {
		return null;
	}

	@Nullable
	@Override
	public JComponent createComponent() {
		if (pane == null) {
			pane = new SettingsPane() {
				@NotNull
				@Override
				GeneralTab createGeneralTab() {
					return new GeneralTab(false);
				}

				@Nullable
				@Override
				ShareTab createShareTab() {
					return null;
				}

				@NotNull
				@Override
				AbstractSettings createSettings() {
					return new ModuleSettings();
				}
			};
		}
		return pane;
	}

	@Override
	public boolean isModified() {
		return pane.isModified(settings);
	}

	@Override
	public void apply() throws ConfigurationException {
		pane.apply(settings);
	}

	@Override
	public void reset() {
		pane.reset(settings);
	}

	@Override
	public void disposeUIResources() {
		if (pane != null) {
			Disposer.dispose(pane);
			pane = null;
		}
	}
}
