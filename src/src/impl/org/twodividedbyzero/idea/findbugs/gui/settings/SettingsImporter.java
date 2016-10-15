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
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.core.WorkspaceSettings;
import org.twodividedbyzero.idea.findbugs.gui.preferences.LegacyAbstractSettingsConverter;
import org.twodividedbyzero.idea.findbugs.preferences.PersistencePreferencesBean;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import java.io.IOException;
import java.io.InputStream;

public abstract class SettingsImporter {

	@NotNull
	private final Project project;

	protected SettingsImporter(@NotNull final Project project) {
		this.project = project;
	}

	public final boolean doImport(
			@NotNull final InputStream input,
			@NotNull final AbstractSettings settings,
			@NotNull final String importFilePathKey
	) throws JDOMException, IOException {

		final Element root = JDOMUtil.load(input);
		boolean success = false;
		if (SonarProfileImporter.isValid(root)) {
			success = new SonarProfileImporter() {
				@Override
				protected void handleError(@NotNull final String message) {
					SettingsImporter.this.handleError(ResourcesLoader.getString("sonar.import.error.title"), message);
				}
			}.doImport(root, settings);
		} else {
			boolean legacy = false;
			PersistencePreferencesBean legacyPrefs = null;
			Element legacyIdea = getLegacyIdea(root);
			if (legacyIdea != null) {
				legacy = true;
				legacyPrefs = XmlSerializer.deserialize(legacyIdea, PersistencePreferencesBean.class);
			} else if (isLegacyExport(root)) {
				legacy = true;
				legacyPrefs = XmlSerializer.deserialize(root, PersistencePreferencesBean.class);
			}
			if (legacy) {
				if (legacyPrefs != null) {
					LegacyAbstractSettingsConverter.applyTo(
							legacyPrefs,
							settings,
							WorkspaceSettings.getInstance(project),
							importFilePathKey
					);
					success = true;
				} else {
					handleError(ResourcesLoader.getString("sonar.import.error.legacyInvalid.title"), ResourcesLoader.getString("sonar.import.error.legacyInvalid.text"));
				}
			} else {
				Element findBugsRoot = root;
				if (!"findbugs".equalsIgnoreCase(root.getName())) {
					findBugsRoot = getIdea(root);
				}
				if (findBugsRoot != null) {
					new SmartSerializer().readExternal(settings, root);
					success = true;
				} else {
					handleError(ResourcesLoader.getString("sonar.import.error.invalid.title"), ResourcesLoader.getString("sonar.import.error.invalid.text"));
				}

			}
		}
		return success;
	}

	@Nullable
	private static Element getIdea(@NotNull final Element root) {
		if ("project".equalsIgnoreCase(root.getName())) {
			for (final Element component : root.getChildren()) {
				if ("component".equalsIgnoreCase(component.getName())) {
					if ("FindBugs-IDEA".equalsIgnoreCase(component.getAttributeValue("name"))) {
						return component;
					}
				}
			}
		}
		return null;
	}

	@Nullable
	private static Element getLegacyIdea(@NotNull final Element root) {
		if ("project".equalsIgnoreCase(root.getName())) {
			for (final Element component : root.getChildren()) {
				if ("component".equalsIgnoreCase(component.getName())) {
					if ("org.twodividedbyzero.idea.findbugs".equalsIgnoreCase(component.getAttributeValue("name"))) {
						if (hasLegacyBasePreferencesChild(component)) {
							return component;
						}
					}
				}
			}
		}
		return null;
	}

	private static boolean isLegacyExport(@NotNull final Element root) {
		return "findbugs".equalsIgnoreCase(root.getName()) && hasLegacyBasePreferencesChild(root);
	}

	private static boolean hasLegacyBasePreferencesChild(@NotNull final Element parent) {
		for (final Element child : parent.getChildren()) {
			if ("option".equalsIgnoreCase(child.getName())) {
				if ("_basePreferences".equalsIgnoreCase(child.getAttributeValue("name"))) {
					return true;
				}
			}
		}
		return false;
	}

	protected abstract void handleError(@NotNull String title, @NotNull String message);
}
