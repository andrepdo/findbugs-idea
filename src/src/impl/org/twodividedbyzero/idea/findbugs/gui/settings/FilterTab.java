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

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.ErrorUtil;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.io.File;

final class FilterTab extends JPanel implements SettingsOwner<AbstractSettings> {
	private FilterPane include;
	private FilterPane exclude;
	private FilterPane bugs;

	FilterTab() {
		super(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

		include = new FilterPane("filter.include.title", "filter.choose.filterXml");
		exclude = new FilterPane("filter.exclude.title", "filter.choose.filterXml");
		bugs = new FilterPane("filter.exclude.bugs", "filter.choose.baselineBugsXml");

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

	void addRFilerFilter() {
		final VirtualFileWrapper wrapper = FileChooserFactory.getInstance().createSaveFileDialog(
				new FileSaverDescriptor(
						StringUtil.capitalizeWords(ResourcesLoader.getString("filter.rFile.save.title"), true),
						ResourcesLoader.getString("filter.rFile.save.text"),
						XmlFileType.DEFAULT_EXTENSION
				), this).save(null, "findbugs-android-exclude");
		if (wrapper == null) {
			return;
		}

		final File file = wrapper.getFile();
		try {
			FileUtil.writeToFile(file, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<FindBugsFilter>\n" +
					"    <!-- http://stackoverflow.com/questions/7568579/eclipsefindbugs-exclude-filter-files-doesnt-work -->\n" +
					"    <Match>\n" +
					"        <Or>\n" +
					"            <Class name=\"~.*\\.R\\$.*\"/>\n" +
					"            <Class name=\"~.*\\.Manifest\\$.*\"/>\n" +
					"        </Or>\n" +
					"    </Match>\n" +
					"</FindBugsFilter>");

			exclude.addFile(file);
		} catch (final Exception e) {
			throw ErrorUtil.toUnchecked(e);
		}
	}

	@NotNull
	static String getSearchPath() {
		return ResourcesLoader.getString("settings.filter");
	}

	@NotNull
	static String[] getSearchResourceKey() {
		return new String[]{
				"filter.include.title",
				"filter.exclude.title",
				"filter.exclude.bugs"
		};
	}
}
