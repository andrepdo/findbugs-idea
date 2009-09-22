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

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.psi.PsiFile;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.gui.tree.NodeVisitor;
import org.twodividedbyzero.idea.findbugs.gui.tree.view.MaskIcon;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.Color;
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
	private ArrayList<VisitableTreeNode> _childs;

	private static final Icon _expandedIcon = new MaskIcon(ResourcesLoader.findIcon("/nodes/class.png", BugInstanceNode.class), Color.BLACK); // NON-NLS
	private static final Icon _collapsedIcon = _expandedIcon;


	/**
	 * Construct a node with the given display text.
	 *
	 * @param simpleName the text of the node.
	 * @param parent	 the parent tree node
	 */
	public BugInstanceNode(final String simpleName, @Nullable final VisitableTreeNode parent) {
		super();

		//_parent = parent;
		setParent(parent);
		_childs = new ArrayList<VisitableTreeNode>();
		_simpleName = simpleName;

		setTooltip(_simpleName);
		setCollapsedIcon(_collapsedIcon);
		setExpandedIcon(_expandedIcon);
	}


	public BugInstanceNode(@NotNull final BugInstance bugInstance, @Nullable final VisitableTreeNode parent) {
		this(null, bugInstance, parent);
	}


	public BugInstanceNode(@Nullable final String simpleName, @NotNull final BugInstance bugInstance, @Nullable final VisitableTreeNode parent) {
		super();

		//_parent = parent;
		setParent(parent);
		_childs = new ArrayList<VisitableTreeNode>();
		_bugInstance = bugInstance;
		_simpleName = (simpleName == null ? bugInstance.getMessageWithoutPrefix() : simpleName);

		setTooltip(_simpleName);
		setCollapsedIcon(_collapsedIcon);
		setExpandedIcon(_expandedIcon);
	}


	public BugInstanceNode(final PsiFile file, final ProblemDescriptor problem, @NotNull final BugInstance bugInstance, @Nullable final VisitableTreeNode parent) {
		if (file == null) {
			throw new IllegalArgumentException("File may not be null");
		}
		if (problem == null) {
			throw new IllegalArgumentException("Problem may not be null");
		}

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
		return "abstract|default";  // NON-NLS // todo: ... icon 
	}


	public ProblemDescriptor getProblem() {
		return _problem;
	}


	public void setSimpleName(final String simpleName) {
		if (simpleName == null || simpleName.trim().length() == 0) {
			throw new IllegalArgumentException("simpleName may not be null/empty");
		}
		_simpleName = simpleName;
	}


	public PsiFile getPsiFile() {
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
		final StringBuilder buf = new StringBuilder();
		buf.append("<html><body>");  // NON-NLS
		buf.append(_simpleName);
		buf.append("</html></body>");  // NON-NLS


		if (_simpleName != null && _problem == null) {
			return buf.toString();
		}

		//final ResourceBundle resources = ResourceBundle.getBundle(FindBugsPluginConstants.RESOURCE_BUNDLE);
		//final MessageFormat stringFormat = new MessageFormat(resources.getString("plugin.results.file-result"));

		final String column;

		if (_problem instanceof ExtendedProblemDescriptor) {
			//column = Integer.toString(((ExtendedProblemDescriptor) _problem).getColumn());
		} else {
			//column = "?";
		}

		final int line;
		if (_problem instanceof ExtendedProblemDescriptor) {
			//line = ((ExtendedProblemDescriptor) _problem).getLine();
		} else {
			//line = _problem.getLineNumber();
		}

		//return stringFormat.format(new Object[] {_file.getName(), _problem.getDescriptionTemplate(), line, column});
		return _simpleName == null ? "null" : _simpleName;
	}


	public boolean getAllowsChildren() {
		return false;
	}


	public boolean isLeaf() {
		return _childs.isEmpty();
	}
}
