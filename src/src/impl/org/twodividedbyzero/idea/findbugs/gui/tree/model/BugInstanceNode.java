/*
 * Copyright 2008-2016 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.gui.tree.model;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.Bug;
import org.twodividedbyzero.idea.findbugs.gui.tree.NodeVisitor;
import org.twodividedbyzero.idea.findbugs.gui.tree.view.MaskIcon;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BugInstanceNode extends AbstractTreeNode<VisitableTreeNode> implements VisitableTreeNode {

	private PsiFile _file;
	private ProblemDescriptor _problem;
	private String _description;

	@NotNull
	private final Bug bug;

	@NotNull
	private final BugInstance _bugInstance;

	private final List<VisitableTreeNode> _childs;
	private final Project _project;

	private static final Icon _expandedIcon = new MaskIcon(AllIcons.Nodes.Class, JBColor.BLACK);
	private static final Icon _collapsedIcon = _expandedIcon;


	public BugInstanceNode(@NotNull final Bug bug, @Nullable final VisitableTreeNode parent, final Project project) {
		this.bug = bug;
		_project = project;
		//_parent = parent;
		setParent(parent);
		_childs = new ArrayList<VisitableTreeNode>();
		_bugInstance = bug.getInstance();
		_simpleName = _bugInstance.getMessageWithoutPrefix();

		setTooltip(_simpleName);
		setCollapsedIcon(_collapsedIcon);
		setExpandedIcon(_expandedIcon);
	}


	public ProblemDescriptor getProblem() {
		return _problem;
	}

	/**
	 * Note that this method could be invoked outside EDT.
	 * So in worst case the {@code _file} reference is resolved multiple times
	 * (because of thread visibility, _file is not volatile)
	 * but this is legal since other used references (f. e. {@code _project}) are final.
	 */
	@Nullable
	public PsiFile getPsiFile() {
		if (_file == null) {
			final PsiClass psiClass = IdeaUtilImpl.findJavaPsiClass(_project, getBug().getModule(), getSourcePath());
			if (psiClass != null) {
				_file = psiClass.getContainingFile();
			}
		}
		return _file;
	}

	@NotNull
	public Bug getBug() {
		return bug;
	}

	@NotNull
	public BugInstance getBugInstance() {
		return _bugInstance;
	}


	@Override
	public void accept(final NodeVisitor visitor) {
	}


	@Override
	public List<VisitableTreeNode> getChildsList() {
		return _childs;
	}


	public BugInstanceNode getTreeNode() {
		return this;
	}


	public static TreePath getPath(TreeNode node) {
		final List<TreeNode> list = new ArrayList<TreeNode>();

		while (node != null) {
			list.add(node);
			//noinspection AssignmentToMethodParameter
			node = node.getParent();
		}
		Collections.reverse(list);

		return new TreePath(list.toArray());
	}


	public String[] getPath() {
		final List<String> path = _getPath();
		return getPath(path.size());
	}


	public String[] getPath(final int depth) {
		final List<String> path = _getPath();
		final String[] result = new String[depth];

		for (int i = 0; i < depth; i++) {
			final String str = path.get(i);
			result[i] = str;
		}

		return result;
	}


	List<String> _getPath() {
		final List<String> list = new ArrayList<String>();

		TreeNode node = getParent();
		while (node != null) {
			if (node instanceof BugInstanceGroupNode) {
				list.add(((BugInstanceGroupNode) node).getGroupName());
				node = node.getParent();
			}
		}
		//Collections.reverse(list);

		return list;
	}


	public String getSourceFilename() {
		return _bugInstance.getPrimaryClass().getSourceFileName();
	}


	public String getSourceFile() {
		return _bugInstance.getPrimaryClass().getSourceLines().getSourceFile();
	}


	public String getSourcePath() {
		return _bugInstance.getPrimaryClass().getSourceLines().getSourcePath();
	}


	/**
	 * @return start line and end line
	 */
	@SuppressWarnings("ConstantConditions")
	@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
	@NotNull
	public int[] getSourceLines() {
		final int[] lines = new int[2];
		final SourceLineAnnotation annotation = _bugInstance.getPrimarySourceLineAnnotation();

		if (annotation != null) {
			lines[0] = annotation.getStartLine();
			lines[1] = annotation.getEndLine();
		} else {
			lines[0] = 1;
			lines[1] = 1;
		}

		return lines;
	}

	public static boolean isAnonymousClass(@NotNull final int lines[]) {
		return lines[0] == -1 && lines[1] == -1;
	}

	public boolean isAnonymousClass() {
		return isAnonymousClass(getSourceLines());
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("BugInstanceNode");
		sb.append("{_file=").append(_file);
		sb.append(", _problem=").append(_problem);
		sb.append(", _description='").append(_description).append('\'');
		sb.append(", _bugInstance=").append(_bugInstance);
		sb.append(", _childs=").append(_childs);
		sb.append('}');
		return sb.toString();
	}


	public boolean getAllowsChildren() {
		return false;
	}


	public boolean isLeaf() {
		return _childs.isEmpty();
	}
}
