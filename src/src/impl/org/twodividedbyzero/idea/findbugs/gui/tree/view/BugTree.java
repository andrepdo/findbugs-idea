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
package org.twodividedbyzero.idea.findbugs.gui.tree.view;

import com.intellij.ide.OccurenceNavigator;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.PopupHandler;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.ui.UIUtil;
import edu.umd.cs.findbugs.BugInstance;
import org.jetbrains.annotations.NonNls;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.BugTreePanel;
import org.twodividedbyzero.idea.findbugs.gui.tree.BugTreeHelper;
import org.twodividedbyzero.idea.findbugs.gui.tree.ScrollToSourceHandler;
import org.twodividedbyzero.idea.findbugs.gui.tree.TreeOccurenceNavigator;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.AbstractNodeDescriptor;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.AbstractTreeNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.VisitableTreeNode;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.8-dev
 */
public class BugTree extends Tree implements DataProvider, OccurenceNavigator {

	private static final Logger LOGGER = Logger.getInstance(BugTree.class.getName());

	private final BugTreePanel _bugTreePanel;
	private transient TreeMouseListener _treeMouseListener;
	private transient TreeSelectionListener _treeSelectionListener;
	private final Project _project;
	private transient BugTreeHelper _treeHelper;
	private KeyAdapter _treeKeyAdapter;
	private transient ScrollToSourceHandler _scrollToSourceHandler;
	private final transient TreeOccurenceNavigator _occurenceNavigator;


	public BugTree(final TreeModel treeModel, final BugTreePanel bugTreePanel, final Project project) {
		super(treeModel);
		_project = project;
		_bugTreePanel = bugTreePanel;
		_occurenceNavigator = new TreeOccurenceNavigator(this);
		init();
	}


	public final void init() {

		_treeHelper = BugTreeHelper.create(this, _project);

		_treeMouseListener = new TreeMouseListener();
		_treeSelectionListener = new TreeSelectionListener();

		addTreeSelectionListener(_treeSelectionListener);
		addMouseListener(_treeMouseListener);

		UIUtil.setLineStyleAngled(this);
		setScrollsOnExpand(true);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setRootVisible(true);
		setShowsRootHandles(true);
		setCellRenderer(new TreeNodeCellRenderer2());

		installHandlers();
	}


	private void installHandlers() {

		final DefaultActionGroup defaultactiongroup = new DefaultActionGroup();
		defaultactiongroup.add(ActionManager.getInstance().getAction("EditSource"));
		defaultactiongroup.addSeparator();
		defaultactiongroup.add(ActionManager.getInstance().getAction("VersionControlsGroup"));
		PopupHandler.installPopupHandler(this, defaultactiongroup, "FoudBugsViewPopup", ActionManager.getInstance());
		_treeKeyAdapter = createKeyAdapter();
		addKeyListener(_treeKeyAdapter);

		EditSourceOnDoubleClickHandler.install(this);
		_scrollToSourceHandler = new ScrollToSourceHandler(_bugTreePanel);
		_scrollToSourceHandler.install(this);
	}


	private KeyAdapter createKeyAdapter() {
		return new KeyAdapter() {

			@Override
			public void keyPressed(final KeyEvent keyevent) {

				if (!keyevent.isConsumed() && 10 == keyevent.getKeyCode()) {
					final TreePath treepath = getSelectionPath();
					if (treepath == null) {
						return;
					}

					@SuppressWarnings({"unchecked"})
					final AbstractNodeDescriptor<VisitableTreeNode> nodedescriptor = (AbstractNodeDescriptor<VisitableTreeNode>) treepath.getLastPathComponent();
					if (!(nodedescriptor instanceof BugInstanceNode)) {
						return;
					}
					OpenSourceUtil.openSourcesFrom(BugTree.this, true);
				}
			}

		};
	}


	public Object getData(@NonNls final String s) {
		final TreePath treepath = getSelectionPath();
		if (treepath == null) {
			return null;
		}

		@SuppressWarnings({"unchecked"})
		final AbstractTreeNode<VisitableTreeNode> treeNode = (AbstractTreeNode<VisitableTreeNode>) treepath.getLastPathComponent();
		if (!(treeNode instanceof BugInstanceNode)) {
			return null;
		}
		final BugInstanceNode node = (BugInstanceNode) treeNode;

		if ("virtualFile".equals(s)) {
			final PsiFile psiFile = _treeHelper.getSelectedFile();
			return psiFile == null ? null : psiFile.getVirtualFile();
		}
		if ("Navigatable".equals(s)) {
			final PsiFile psiFile = _treeHelper.getSelectedFile();
			if (psiFile != null) {
				final VirtualFile virtualFile = psiFile.getVirtualFile();
				//LOGGER.debug("PsiFile: " + psiFile + " VirtualFile: " + virtualFile.getName() + " - Line: " + node.getSourceLines()[0]);
				if (virtualFile != null) {
					return new OpenFileDescriptor(_project, virtualFile, node.getSourceLines()[0] - 1, 0);
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
		if ("psi.Element".equals(s)) {
			return _treeHelper.getSelectedElement();
		}
		if ("virtualFileArray".equals(s)) {
			final PsiFile psiFile = _treeHelper.getSelectedFile();
			if (psiFile != null) {
				LOGGER.debug("PsiFile: " + psiFile);
				return new VirtualFile[] {psiFile.getVirtualFile()};
			} else {
				return VirtualFile.EMPTY_ARRAY;
			}
		}
		/*if ("helpId".equals(s)) {
			return "find.todoList";
		} else {
			return null;
		}*/
		return null;
	}


	public KeyAdapter getTreeKeyAdapter() {
		return _treeKeyAdapter;
	}


	public ScrollToSourceHandler getScrollToSourceHandler() {
		return _scrollToSourceHandler;
	}


	public void gotoNode(final BugInstanceNode node) {
		gotoNode(node.getBugInstance());
	}


	public void gotoNode(final BugInstance bugInstance) {
		_treeHelper.gotoNode(bugInstance);
	}


	public Project getProject() {
		return _project;
	}


	public BugTreeHelper getTreeHelper() {
		return _treeHelper;
	}


	public boolean hasNextOccurence() {
		return _occurenceNavigator.hasNextOccurence();
	}


	public boolean hasPreviousOccurence() {
		return _occurenceNavigator.hasPreviousOccurence();
	}


	public OccurenceInfo goNextOccurence() {
		return _occurenceNavigator.goNextOccurence();
	}


	public OccurenceInfo goPreviousOccurence() {
		return _occurenceNavigator.goPreviousOccurence();
	}


	public String getNextOccurenceActionName() {
		return _occurenceNavigator.getNextOccurenceActionName();
	}


	public String getPreviousOccurenceActionName() {
		return _occurenceNavigator.getPreviousOccurenceActionName();
	}


	/** Listen for clicks and scroll to the error's source as necessary. */
	protected class TreeMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(final MouseEvent e) {
			if (!_bugTreePanel.isScrollToSource() && e.getClickCount() < 2) {
				final TreePath treePath = getPathForLocation(e.getX(), e.getY());
				if (_bugTreePanel.isPreviewEnabled() && treePath != null) {
					_bugTreePanel.setPreview(treePath);
				}
				return;
			}

			final TreePath treePath = getPathForLocation(e.getX(), e.getY());
			//_bugTree.setHighlightPath(treePath);

			if (treePath != null) {
				_scrollToSourceHandler.scollToSelectionSource();
				//_bugTreePanel.scrollToSource(treePath);
			}
		}

	}


	/** Listen for tree selection events and scroll to the error's source as necessary. */
	protected class TreeSelectionListener implements javax.swing.event.TreeSelectionListener {

		public void valueChanged(final TreeSelectionEvent e) {
			final TreePath path = e.getPath();

			_bugTreePanel.setDetailText(path);
			_bugTreePanel.setDetailHtml(path);

			if (_bugTreePanel.isScrollToSource() && path != null) {
				_scrollToSourceHandler.scollToSelectionSource();
				//_bugTreePanel.scrollToSource(path);
			}
			if (_bugTreePanel.isPreviewEnabled() && path != null) {
				_bugTreePanel.setPreview(path);
			}
		}
	}


}
