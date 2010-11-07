/*
 * Copyright 2010 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.twodividedbyzero.idea.findbugs.gui.toolwindow.view;

import info.clearthought.layout.TableLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.LayoutManager;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
public class CloudCommentsPane2  extends JPanel {

	private final ToolWindowPanel _toolWindowPanel;
	private JLabel _titleLabel;


	public CloudCommentsPane2(final ToolWindowPanel toolWindowPanel) {
		_toolWindowPanel = toolWindowPanel;

		final double border = 5;
		final double colsGap = 2;
		final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.PREFERRED, colsGap, TableLayout.PREFERRED, border}, // Columns
								 {border, TableLayout.PREFERRED, border}};// Rows
		final LayoutManager tbl = new TableLayout(size);
		setLayout(tbl);

		
	}


	private JLabel getTitleLabel() {
		if (_titleLabel == null) {
			_titleLabel = new JLabel("FindBugs Cloud");
		}
		return _titleLabel;
	}
}
