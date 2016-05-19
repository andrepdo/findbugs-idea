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

import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.FlowLayout;

final class ModuleSettingsPane extends SettingsPane {

	private JBCheckBox test; // TODO support override project settings

	@NotNull
	@Override
	JComponent createHeaderPane() {
		test = new JBCheckBox("Test23");

		final JPanel topPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		topPane.add(test);
		topPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		return topPane;
	}

	@Nullable
	@Override
	GeneralTab createGeneralTab() {
		return null;
	}

	@Nullable
	@Override
	DetectorTab createDetectorTab() {
		return null;
	}

	@Nullable
	@Override
	AnnotateTab createAnnotateTab() {
		return null;
	}

	@Nullable
	@Override
	ShareTab createShareTab() {
		return null;
	}
}
