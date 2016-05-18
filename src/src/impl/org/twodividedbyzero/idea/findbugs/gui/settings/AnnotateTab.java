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

import com.intellij.execution.ui.ClassBrowser;
import com.intellij.ide.util.ClassFilter;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaCodeFragment;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.EditorTextFieldWithBrowseButton;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JPanel;
import java.awt.BorderLayout;

final class AnnotateTab extends JPanel implements SettingsOwner<ProjectSettings> {

	private final Project project;
	private LabeledComponent<EditorTextFieldWithBrowseButton> annotationClassField;

	AnnotateTab(@NotNull final Project project) {
		super(new BorderLayout());
		this.project = project;


		final EditorTextFieldWithBrowseButton field = new EditorTextFieldWithBrowseButton(project, true, new JavaCodeFragment.VisibilityChecker() {
			@Override
			public Visibility isDeclarationVisible(PsiElement declaration, PsiElement place) {
				if (declaration instanceof PsiClass) {
					final PsiClass aClass = (PsiClass) declaration;
					if (aClass.isAnnotationType()) {
						return Visibility.VISIBLE;
					}
				}
				return Visibility.NOT_VISIBLE;
			}
		});
		annotationClassField = new LabeledComponent<EditorTextFieldWithBrowseButton>();
		annotationClassField.setText(ResourcesLoader.getString("annotate.suppressClass.text"));
		annotationClassField.setComponent(field);
		//noinspection unchecked
		new Suppress(project, ResourcesLoader.getString("annotate.suppressClass.text")).setField(annotationClassField.getComponent());
		add(annotationClassField, BorderLayout.NORTH);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		annotationClassField.setEnabled(enabled);
	}

	@Override
	public boolean isModified(@NotNull final ProjectSettings settings) {
		return !StringUtil.equals(settings.suppressWarningsClassName, annotationClassField.getComponent().getText());
	}

	@Override
	public void apply(@NotNull final ProjectSettings settings) throws ConfigurationException {
		final String suppressWarningsClassName = annotationClassField.getComponent().getText();
		if (IdeaUtilImpl.findJavaPsiClass(project, suppressWarningsClassName) == null) {
			throw new ConfigurationException(ResourcesLoader.getString("annotate.suppressClass.error", suppressWarningsClassName));
		}
		settings.suppressWarningsClassName = suppressWarningsClassName;
	}

	@Override
	public void reset(@NotNull final ProjectSettings settings) {
		annotationClassField.getComponent().setText(settings.suppressWarningsClassName);
	}

	@NotNull
	static String getSearchPath() {
		return ResourcesLoader.getString("settings.annotate");
	}

	@NotNull
	static String[] getSearchResourceKey() {
		return new String[]{
				"annotate.suppressClass.text"
		};
	}

	private class Suppress extends ClassBrowser {
		public Suppress(final Project project, final String title) {
			super(project, title);
		}

		@Override
		protected ClassFilter.ClassFilterWithScope getFilter() throws NoFilterException {
			return new ClassFilter.ClassFilterWithScope() {
				@Override
				public GlobalSearchScope getScope() {
					return GlobalSearchScope.allScope(project);
				}

				@Override
				public boolean isAccepted(final PsiClass aClass) {
					return aClass.isAnnotationType();
				}
			};
		}

		@Override
		protected PsiClass findClass(final String className) {
			return IdeaUtilImpl.findJavaPsiClass(project, className);
		}
	}
}
