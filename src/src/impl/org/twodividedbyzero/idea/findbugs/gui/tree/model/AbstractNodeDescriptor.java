/**
 * Copyright 2008 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.gui.tree.model;

import org.twodividedbyzero.idea.findbugs.gui.tree.GroupBy;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;

import javax.swing.Icon;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
public abstract class AbstractNodeDescriptor<E> {

	protected GroupBy _groupBy;
	protected int _depthFromRoot;
	protected String _simpleName;
	protected String _groupName;
	protected int _memberCount; // todo: ??? needed ???
	protected String _tooltip;

	protected Icon _expandedIcon = GuiResources.TREENODE_OPEN_ICON;
	protected Icon _collapsedIcon = GuiResources.TREENODE_CLOSED_ICON;


	public final GroupBy getGroupBy() {
		return _groupBy;
	}


	public final int getDepth() {
		return _depthFromRoot;
	}


	public final String getSimpleName() {
		return _simpleName;
	}


	public final String getGroupName() {
		return _groupName;
	}


	public int getMemberCount() {
		return _memberCount;
	}


	public String getTooltip() {
		return _tooltip;
	}


	public Icon getExpandedIcon() {
		return _expandedIcon;
	}


	public Icon getCollapsedIcon() {
		return _collapsedIcon;
	}


	public AbstractNodeDescriptor<E> getElement() {
		return this;
	}


}
