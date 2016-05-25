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
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.core.ModuleSettings;

public final class ModuleConfigurableImpl extends AbstractConfigurableImpl<ModuleSettings> implements Configurable.NoScroll {

	@NotNull
	private final Module module;

	public ModuleConfigurableImpl(@NotNull final Module module) {
		super(module.getProject(), ModuleSettings.getInstance(module));
		this.module = module;
	}

	@NotNull
	@Override
	SettingsPane createSettingsPane() {
		return new ModuleSettingsPane(module.getProject(), module);
	}

	@Override
	public boolean isModified() {
		return super.isModified() || pane.isModifiedModule(settings);
	}

	@Override
	public void apply() throws ConfigurationException {
		super.apply();
		pane.applyModule(settings);
	}

	@Override
	public void reset() {
		super.reset();
		pane.resetModule(settings);
	}

	public static void show(@NotNull final Module module) {
		/**
		 * It is correct to create a configurable instance,
		 * see java doc of ShowSettingsUtil#findProjectConfigurable (deprecated).
		 */
		showImpl(module.getProject(), new ModuleConfigurableImpl(module), null);
	}

	public static void showShare(@NotNull final Module module) {
		/**
		 * It is correct to create a configurable instance,
		 * see java doc of ShowSettingsUtil#findProjectConfigurable (deprecated).
		 */
		showShareImpl(module.getProject(), new ModuleConfigurableImpl(module));
	}

	public static void showFileFilterAndAddRFilerFilter(@NotNull final Module module) {
		showFileFilterAndAddRFilerFilterImpl(module.getProject(), new ModuleConfigurableImpl(module));
	}
}
