/*
 * Copyright 2011 Andre Pfeiler
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

package org.twodividedbyzero.idea.findbugs.gui.common;

import com.intellij.ui.components.JBScrollPane;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Component;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
public final class ScrollPaneFacade implements ScrollPaneConstants {

	@SuppressWarnings( {"UndesirableClassUsage"})
	public static JScrollPane getComponent(final Component view, final int vsbPolicy, final int hsbPolicy) {
		if (IdeaUtilImpl.isVersionGreaterThanIdea9()) {
			return new JBScrollPane(view, vsbPolicy, hsbPolicy);
		} else {
			return new JScrollPane(view, vsbPolicy, hsbPolicy);
		}
	}


	public static JScrollPane getComponent(final Component view) {
		return getComponent(view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}


	public static JScrollPane getComponent(final int vsbPolicy, final int hsbPolicy) {
		return getComponent(null, vsbPolicy, hsbPolicy);
	}


	public static JScrollPane getComponent() {
		return getComponent(null, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}
}
