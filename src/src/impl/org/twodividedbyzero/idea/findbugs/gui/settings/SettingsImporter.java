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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.xmlb.SmartSerializer;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.gui.common.BalloonTipFactory;
import org.twodividedbyzero.idea.findbugs.gui.preferences.importer.SonarProfileImporter;
import org.twodividedbyzero.idea.findbugs.preferences.PersistencePreferencesBean;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import java.io.IOException;
import java.io.InputStream;

public class SettingsImporter {

	@NotNull
	private final Project project;

	public SettingsImporter(@NotNull final Project project) {
		this.project = project;
	}

	public final boolean doImport(@NotNull final InputStream input, @NotNull final ProjectSettings settings) throws JDOMException, IOException {
		final Element root = JDOMUtil.load(input);
		boolean success;
		if (SonarProfileImporter.isValid(root)) {
			success = new SonarProfileImporter() {
				@Override
				protected void handleError(@NotNull final String message) {
					SettingsImporter.this.handleError(ResourcesLoader.getString("sonar.import.error.title"), message);
				}
			}.doImport(root, settings);
		} else {
			if (isLegacy(root)) {
				final PersistencePreferencesBean prefs = XmlSerializer.deserialize(root, PersistencePreferencesBean.class);
				if (prefs.getBasePreferences().isEmpty()) {
					// TODO ask continue;
				}
				System.out.println("TODO convert it"); // TODO convert it
			} else {
				new SmartSerializer().readExternal(settings, root);
			}
			success = true;
		}
		return success;
	}

	private static boolean isLegacy(@NotNull final Element element) {
		if ("option".equalsIgnoreCase(element.getName()) && "_basePreferences".equalsIgnoreCase(element.getAttributeValue("name"))) {
			return true;
		}
		for (final Element child : element.getChildren()) {
			if (isLegacy(child)) {
				return true;
			}
		}
		return false;
	}

	public void handleError(@NotNull final String title, @NotNull final String message) {
		// TODO make HTML with title
		BalloonTipFactory.showToolWindowErrorNotifier(project, message);
	}
}
