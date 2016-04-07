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

import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.awt.BorderLayout;

final class FilterTab extends JPanel implements SettingsOwner<AbstractSettings> {
	private FilterPane include;
	private FilterPane exclude;
	private FilterPane bugs;

	FilterTab() {
		super(new BorderLayout());

		include = new FilterPane("filter.include.title");
		exclude = new FilterPane("filter.exclude.title");
		bugs = new FilterPane("filter.exclude.bugs");

		final JPanel filters = new JPanel();
		final BoxLayout filtersLayout = new BoxLayout(filters, BoxLayout.Y_AXIS);
		filters.setLayout(filtersLayout);
		filters.add(include);
		filters.add(exclude);
		filters.add(bugs);
		add(filters);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		include.setEnabled(enabled);
		exclude.setEnabled(enabled);
		bugs.setEnabled(enabled);
	}

	@Override
	public boolean isModified(@NotNull final AbstractSettings settings) {
		return include.isModified(settings.includeFilterFiles) ||
				exclude.isModified(settings.excludeFilterFiles) ||
				bugs.isModified(settings.excludeBugsFiles);
	}

	@Override
	public void apply(@NotNull final AbstractSettings settings) throws ConfigurationException {
		include.apply(settings.includeFilterFiles);
		exclude.apply(settings.excludeFilterFiles);
		bugs.apply(settings.excludeBugsFiles);
	}

	@Override
	public void reset(@NotNull final AbstractSettings settings) {
		include.reset(settings.includeFilterFiles);
		exclude.reset(settings.excludeFilterFiles);
		bugs.reset(settings.excludeBugsFiles);
	}
}
