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
