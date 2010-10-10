/**
 * Copyright 2009 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
