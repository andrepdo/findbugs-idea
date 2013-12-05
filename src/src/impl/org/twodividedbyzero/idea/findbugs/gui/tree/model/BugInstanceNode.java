/*
 * Copyright 2008-2013 Andre Pfeiler
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
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.DoneCallback;
import org.twodividedbyzero.idea.findbugs.common.util.BugInstanceUtil;
import org.twodividedbyzero.idea.findbugs.gui.tree.NodeVisitor;
import org.twodividedbyzero.idea.findbugs.gui.tree.view.MaskIcon;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class BugInstanceNode extends AbstractTreeNode<VisitableTreeNode> implements VisitableTreeNode {

	private PsiFile _file;
	private ProblemDescriptor _problem;
	private String _description;
	private BugInstance _bugInstance;
	private final List<VisitableTreeNode> _childs;
	private final Project _project;

	private static final Icon _expandedIcon = new MaskIcon(ResourcesLoader.findIcon("/nodes/class.png", BugInstanceNode.class), JBColor.BLACK);
	private static final Icon _collapsedIcon = _expandedIcon;


	/**
	 * Construct a node with the given display text.
	 *
	 * @param simpleName the text of the node.
	 * @param parent	 the parent tree node
	 */
	public BugInstanceNode(final String simpleName, @Nullable final VisitableTreeNode parent, final Project project) {

		_project = project;
		//_parent = parent;
		setParent(parent);
		_childs = new ArrayList<VisitableTreeNode>();
		_simpleName = simpleName;

		setTooltip(_simpleName);
		setCollapsedIcon(_collapsedIcon);
		setExpandedIcon(_expandedIcon);
	}


	public BugInstanceNode(@NotNull final BugInstance bugInstance, @Nullable final VisitableTreeNode parent, final Project project) {
		this(null, bugInstance, parent, project);
	}


	public BugInstanceNode(@Nullable final String simpleName, @NotNull final BugInstance bugInstance, @Nullable final VisitableTreeNode parent, final Project project) {

		_project = project;
		//_parent = parent;
		setParent(parent);
		_childs = new ArrayList<VisitableTreeNode>();
		_bugInstance = bugInstance;
		_simpleName = simpleName == null ? bugInstance.getMessageWithoutPrefix() : simpleName;

		setTooltip(_simpleName);
		setCollapsedIcon(_collapsedIcon);
		setExpandedIcon(_expandedIcon);
	}


	public BugInstanceNode(final PsiFile file, final ProblemDescriptor problem, @NotNull final BugInstance bugInstance, @Nullable final VisitableTreeNode parent, final Project project) {
		if (file == null) {
			throw new IllegalArgumentException("File may not be null");
		}
		if (problem == null) {
			throw new IllegalArgumentException("Problem may not be null");
		}

		_project = project;
		//_parent = parent;
		setParent(parent);
		_childs = new ArrayList<VisitableTreeNode>();
		_file = file;
		_problem = problem;
		_bugInstance = bugInstance;
		_simpleName = bugInstance.getMessageWithoutPrefix();

		setTooltip(_simpleName);
		setCollapsedIcon(_collapsedIcon);
		setExpandedIcon(_expandedIcon);
	}


	public static String getJavaClassType(final BugInstance bugInstance) {
		return "abstract|default";   // todo: ... icon
	}


	public ProblemDescriptor getProblem() {
		return _problem;
	}


	public void setSimpleName(final String simpleName) {
		if (simpleName == null || simpleName.trim().isEmpty()) {
			throw new IllegalArgumentException("simpleName may not be null/empty");
		}
		_simpleName = simpleName;
	}


	@Nullable
	public PsiFile getPsiFile() {
		if(_file == null) {
			_file = BugInstanceUtil.getPsiElement(_project, this);
		}
		return _file;
	}


	@SuppressWarnings({"AnonymousInnerClass"})
	@Nullable
	public PsiFile getPsiFile(final DoneCallback<PsiFile> doneCallback) {
		if(_file == null) {
			BugInstanceUtil.findPsiElement(_project, this, doneCallback, new DoneCallback<PsiFile>() {
				public void onDone(final PsiFile value) {
					_file = value;
				}
			});
		}
		return _file;
	}


	public void setPsiFile(final PsiFile file) {
		_file = file;
	}


	public BugInstance getBugInstance() {
		return _bugInstance;
	}


	public void setBugInstance(final BugInstance bugInstance) {
		_bugInstance = bugInstance;
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


	/** @return start line and end line */
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
