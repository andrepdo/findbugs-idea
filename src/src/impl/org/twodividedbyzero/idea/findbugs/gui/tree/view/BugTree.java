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
package org.twodividedbyzero.idea.findbugs.gui.tree.view;

import com.intellij.ide.OccurenceNavigator;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.content.Content;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.ui.UIUtil;
import edu.umd.cs.findbugs.BugInstance;
import org.jetbrains.annotations.NonNls;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.gui.common.AnalysisRunDetailsDialog;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.BugTreePanel;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.ToolWindowPanel;
import org.twodividedbyzero.idea.findbugs.gui.tree.BugTreeHelper;
import org.twodividedbyzero.idea.findbugs.gui.tree.ScrollToSourceHandler;
import org.twodividedbyzero.idea.findbugs.gui.tree.TreeOccurenceNavigator;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.AbstractNodeDescriptor;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.AbstractTreeNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.BugInstanceNode;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.VisitableTreeNode;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;


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
	private final Project _project;
	private BugTreeHelper _treeHelper;
	private ScrollToSourceHandler _scrollToSourceHandler;
	private final TreeOccurenceNavigator _occurenceNavigator;


	public BugTree(final TreeModel treeModel, final BugTreePanel bugTreePanel, final Project project) {
		super(treeModel);
		_project = project;
		_bugTreePanel = bugTreePanel;
		_occurenceNavigator = new TreeOccurenceNavigator(this);
		init();
	}


	public final void init() {
		_treeHelper = BugTreeHelper.create(this, _project);

		addTreeSelectionListener(new SelectionListenerImpl());
		addMouseMotionListener(new MouseMotionListenerImpl());
		addMouseListener(new MouseListenerImpl());

		if (UIUtil.isUnderDarcula()) {
			putClientProperty("JTree.lineStyle", "None");
		} else {
			UIUtil.setLineStyleAngled(this);
		}
		setScrollsOnExpand(true);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setRootVisible(true);
		setShowsRootHandles(true);
		setCellRenderer(new TreeNodeCellRenderer());

		installHandlers();
	}


	private void installHandlers() {

		final DefaultActionGroup defaultactiongroup = new DefaultActionGroup();
		defaultactiongroup.add(ActionManager.getInstance().getAction("EditSource"));
		defaultactiongroup.addSeparator();
		defaultactiongroup.add(ActionManager.getInstance().getAction("VersionControlsGroup"));
		PopupHandler.installPopupHandler(this, defaultactiongroup, "FoundBugsViewPopup", ActionManager.getInstance());
		addKeyListener(createKeyAdapter());

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
				final int[] lines = node.getSourceLines();
				if (lines[0] == -1 && lines[1] == -1) {  // find anonymous classes
					final PsiElement psiElement = IdeaUtilImpl.findAnonymousClassPsiElement(psiFile, node, _project);
					if (psiElement != null) {
						return psiElement;
					}
				} else if (virtualFile != null) {
					return new OpenFileDescriptor(_project, virtualFile, node.getSourceLines()[0] - 1, 0);
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
		if ("psi.Element".equals(s)) {
			final int[] lines = node.getSourceLines();
			if (lines[0] == -1 && lines[1] == -1) {  // find anonymous classes
				final PsiFile psiFile = _treeHelper.getSelectedFile();
				final PsiElement psiElement = IdeaUtilImpl.findAnonymousClassPsiElement(psiFile, node, _project);
				if (psiElement != null) {
					return psiElement;
				}
			}
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


	public ScrollToSourceHandler getScrollToSourceHandler() {
		return _scrollToSourceHandler;
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


	private class MouseMotionListenerImpl extends MouseMotionAdapter {


		@Override
		public void mouseMoved(final MouseEvent e) {
			if (_bugTreePanel.getBugCollection() != null && getRowForLocation(e.getX(), e.getY()) != 0) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} else if (_bugTreePanel.getBugCollection() != null) {
				final Object root = getModel().getRoot();
				final Component rendererComponent = getCellRenderer().getTreeCellRendererComponent(BugTree.this, root, true, true, false, 0, true);
				final int width = rendererComponent.getPreferredSize().width;
				final int mouseX = e.getX();
				if (mouseX <= width && mouseX >= width - 100) {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
			}
		}
	}


	/** Listen for clicks and scroll to the error's source as necessary. */
	private class MouseListenerImpl extends MouseAdapter {

		@Override
		public void mouseClicked(final MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() < 2) {
				if (_bugTreePanel.getBugCollection() != null && getRowForLocation(e.getX(), e.getY()) == 0) {
					final Object root = getModel().getRoot();
					final Component rendererComponent = getCellRenderer().getTreeCellRendererComponent(BugTree.this, root, true, true, false, 0, true);
					final int width = rendererComponent.getPreferredSize().width;
					final int mouseX = e.getX();
					if (mouseX <= width + 10 && mouseX >= width - 40) {
						final ToolWindow toolWindow = ToolWindowManager.getInstance(_project).getToolWindow(IdeaUtilImpl.getPluginComponent(_project).getInternalToolWindowId());
						final Content content = toolWindow.getContentManager().getContent(0);
						if (content != null) {
							final ToolWindowPanel panel = (ToolWindowPanel) content.getComponent();
							final DialogBuilder dialog = AnalysisRunDetailsDialog.create(panel.getProject(), panel.getBugTreePanel().getGroupModel().getBugCount(), panel.getBugCollection().getProjectStats(), panel.getBugsProject());
							dialog.showModal(false);
						}
					}
				}
			}
		}


		@Override
		public void mouseExited(final MouseEvent e) {
			if (_bugTreePanel.getBugCollection() != null) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}


	/** Listen for tree selection events and scroll to the error's source as necessary. */
	private class SelectionListenerImpl implements TreeSelectionListener {
		@Override
		public void valueChanged(final TreeSelectionEvent e) {
			final TreePath path = BugTree.this.getSelectionPath();
			setHighlightPath(path);

			_bugTreePanel.setDetails(path);

			if (_bugTreePanel.isScrollToSource() && path != null) {
				_scrollToSourceHandler.scollToSelectionSource();
			}
			if (_bugTreePanel.isPreviewEnabled()) {
				_bugTreePanel.setPreview(path);
			}
		}
	}
}
