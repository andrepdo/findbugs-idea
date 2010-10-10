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
package org.twodividedbyzero.idea.findbugs.common.ui;

import javax.swing.border.LineBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.7-dev
 */
public class DashedBorder extends LineBorder {

	private static final long serialVersionUID = 0L;

	private static final BasicStroke _stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[] {5, 5}, 0);


	public DashedBorder(final Color color, final int thickness) {
		super(color, thickness);
	}


	@Override
	public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width, final int height) {
		final Graphics2D g2d = (Graphics2D) g.create();
		g2d.setStroke(_stroke);
		super.paintBorder(c, g2d, x, y, width, height);
		g2d.dispose();
	}


	public static BasicStroke getStroke() {
		return _stroke;
	}
}
