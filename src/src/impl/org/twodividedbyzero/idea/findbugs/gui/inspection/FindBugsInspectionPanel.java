/*
 * Copyright 2010 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.gui.inspection;

import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ResourceBundle;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class FindBugsInspectionPanel extends JPanel {

	/** A text label for a description blurb. */
	private final JTextArea _descriptionLabel = new JTextArea();


	public FindBugsInspectionPanel() {
		super(new GridBagLayout());

		initialize();
	}


	void initialize() {
		final ResourceBundle i18n = ResourcesLoader.getResourceBundle();


		// fake a multi-line label with a text area
		_descriptionLabel.setText(i18n.getString("findbugs.inspection.description.config"));
		_descriptionLabel.setEditable(false);
		_descriptionLabel.setEnabled(false);
		_descriptionLabel.setWrapStyleWord(true);
		_descriptionLabel.setLineWrap(true);
		_descriptionLabel.setOpaque(false);
		_descriptionLabel.setDisabledTextColor(_descriptionLabel.getForeground());

		final GridBagConstraints descLabelConstraints = new GridBagConstraints(0, 0, 3, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0);
		add(_descriptionLabel, descLabelConstraints);


	}
}
