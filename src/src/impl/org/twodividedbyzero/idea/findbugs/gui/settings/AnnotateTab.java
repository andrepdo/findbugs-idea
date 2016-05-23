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
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.core.WorkspaceSettings;
import org.twodividedbyzero.idea.findbugs.gui.common.HAlignment;
import org.twodividedbyzero.idea.findbugs.gui.common.VAlignment;
import org.twodividedbyzero.idea.findbugs.gui.common.VerticalFlowLayout;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

final class AnnotateTab extends JPanel {

	private final Project project;
	private LabeledComponent<EditorTextFieldWithBrowseButton> annotationClassField;
	private JBCheckBox gutterIconCheckbox;
	private JBCheckBox textRangeMarkupCheckbox;

	AnnotateTab(@NotNull final Project project) {
		super(new VerticalFlowLayout(HAlignment.Left, VAlignment.Top, 0, UIUtil.DEFAULT_HGAP, true, false));
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
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
		add(annotationClassField);

		gutterIconCheckbox = new JBCheckBox(ResourcesLoader.getString("annotate.gutterIcon.text"));
		add(gutterIconCheckbox);

		textRangeMarkupCheckbox = new JBCheckBox(ResourcesLoader.getString("annotate.textRange.text"));
		add(textRangeMarkupCheckbox);
	}

	void setProjectSettingsEnabled(final boolean enabled) {
		annotationClassField.setEnabled(enabled);
	}

	boolean isModified(@NotNull final AbstractSettings settings) {
		return !StringUtil.equals(settings.suppressWarningsClassName, annotationClassField.getComponent().getText());
	}

	boolean isModifiedWorkspace(@NotNull final WorkspaceSettings settings) {
		return settings.annotationGutterIcon != gutterIconCheckbox.isSelected() ||
				settings.annotationTextRangeMarkup != textRangeMarkupCheckbox.isSelected();
	}

	@SuppressWarnings("UnnecessaryLocalVariable")
	void apply(@NotNull final AbstractSettings settings) throws ConfigurationException {
		final String suppressWarningsClassName = annotationClassField.getComponent().getText();
		settings.suppressWarningsClassName = suppressWarningsClassName;
		// Do not throw any configuration exception at the moment because it
		// is impossible to save settings otherwise.
		//if (IdeaUtilImpl.findJavaPsiClass(project, suppressWarningsClassName) == null) {
		//	throw new RuntimeConfigurationWarning(ResourcesLoader.getString("annotate.suppressClass.error", suppressWarningsClassName));
		//}
	}

	void applyWorkspace(@NotNull final WorkspaceSettings settings) throws ConfigurationException {
		settings.annotationGutterIcon = gutterIconCheckbox.isSelected();
		settings.annotationTextRangeMarkup = textRangeMarkupCheckbox.isSelected();
	}

	void reset(@NotNull final AbstractSettings settings) {
		annotationClassField.getComponent().setText(settings.suppressWarningsClassName);
	}

	void resetWorkspace(@NotNull final WorkspaceSettings settings) {
		gutterIconCheckbox.setSelected(settings.annotationGutterIcon);
		textRangeMarkupCheckbox.setSelected(settings.annotationTextRangeMarkup);
	}

	@NotNull
	static String getSearchPath() {
		return ResourcesLoader.getString("settings.annotate");
	}

	@NotNull
	static String[] getSearchResourceKey() {
		return new String[]{
				"annotate.suppressClass.text",
				"annotate.gutterIcon.text",
				"annotate.textRange.text"
		};
	}

	@NotNull
	static String[] getSearchTexts() {
		return new String[]{
				"edu.umd.cs.findbugs.annotations.SuppressWarnings",
				"edu.umd.cs.findbugs.annotations.SuppressFBWarnings"
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
