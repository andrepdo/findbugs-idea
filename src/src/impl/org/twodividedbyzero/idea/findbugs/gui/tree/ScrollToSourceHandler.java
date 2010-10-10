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
package org.twodividedbyzero.idea.findbugs.gui.tree;

import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.BugTreePanel;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
public class ScrollToSourceHandler extends com.intellij.ui.AutoScrollToSourceHandler {

	private final BugTreePanel _panel;


	public ScrollToSourceHandler(final BugTreePanel panel) {
		_panel = panel;
	}


	@Override
	protected boolean isAutoScrollMode() {
		return _panel.isScrollToSource();
	}


	@Override
	protected void setAutoScrollMode(final boolean flag) {
		_panel.setScrollToSource(flag);
	}


	public void scollToSelectionSource() {
		super.onMouseClicked(_panel.getBugTree());
	}

	// todo: call super.install(JTree tree)


}
