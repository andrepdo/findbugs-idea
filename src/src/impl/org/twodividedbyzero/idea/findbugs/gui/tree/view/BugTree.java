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
package org.twodividedbyzero.idea.findbugs.gui.tree.view;

import com.intellij.ide.OccurenceNavigator;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.PopupHandler;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.ui.UIUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.Bug;
import org.twodividedbyzero.idea.findbugs.gui.common.AnalysisRunDetailsDialog;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.BugTreePanel;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.ToolWindowPanel;
import org.twodividedbyzero.idea.findbugs.gui.tree.BugTreeHelper;
import org.twodividedbyzero.idea.findbugs.gui.tree.ScrollToSourceHandler;
import org.twodividedbyzero.idea.findbugs.gui.tree.TreeOccurenceNavigator;
import org.twodividedbyzero.idea.findbugs.gui.tree.model.AbstractNodeDescriptor;
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

@SuppressFBWarnings("SE_BAD_FIELD")
public class BugTree extends Tree implements DataProvider, OccurenceNavigator {

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
		_treeHelper = new BugTreeHelper(this);

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

	@Override
	public Object getData(@NonNls final String dataId) {
		if (CommonDataKeys.VIRTUAL_FILE.is(dataId)) {
			return getSelectedVirtualFile();
		}
		if (CommonDataKeys.NAVIGATABLE.is(dataId)) {
			return getNavigatableData();
		}
		if (CommonDataKeys.VIRTUAL_FILE_ARRAY.is(dataId)) {
			final VirtualFile virtualFile = getSelectedVirtualFile();
			return virtualFile != null ? new VirtualFile[]{virtualFile} : VirtualFile.EMPTY_ARRAY;
		}
		if (CommonDataKeys.PSI_ELEMENT.is(dataId)) {
			final BugInstanceNode node = _treeHelper.getSelectedBugInstanceNode();
			if (node == null) {
				return null;
			}
			final PsiFile psiFile = node.getPsiFile();
			if (node.isAnonymousClass()) {
				final PsiElement psiElement = IdeaUtilImpl.findAnonymousClassPsiElement(psiFile, node.getBugInstance(), _project);
				if (psiElement != null) {
					return psiElement;
				}
			}
			return psiFile;
		}
		return null;
	}

	@Nullable
	private PsiFile getSelectedPsiFile() {
		return _treeHelper.getSelectedPsiFile();
	}

	@Nullable
	private VirtualFile getSelectedVirtualFile() {
		final PsiFile psiFile = getSelectedPsiFile();
		return psiFile != null ? psiFile.getVirtualFile() : null;
	}

	@Nullable
	private Object getNavigatableData() {
		final BugInstanceNode node = _treeHelper.getSelectedBugInstanceNode();
		if (node == null) {
			return null;
		}
		final int[] lines = node.getSourceLines();
		if (BugInstanceNode.isAnonymousClass(lines)) {
			return IdeaUtilImpl.findAnonymousClassPsiElement(node.getPsiFile(), node.getBugInstance(), _project);
		}
		final PsiFile psiFile = node.getPsiFile();
		if (psiFile == null) {
			return null;
		}
		final VirtualFile virtualFile = psiFile.getVirtualFile();
		if (virtualFile == null) {
			return null;
		}
		return new OpenFileDescriptor(_project, virtualFile, lines[0] - 1, 0);
	}

	public ScrollToSourceHandler getScrollToSourceHandler() {
		return _scrollToSourceHandler;
	}

	public void gotoNode(final Bug bug) {
		_treeHelper.gotoNode(bug);
	}

	public Project getProject() {
		return _project;
	}

	public BugTreeHelper getTreeHelper() {
		return _treeHelper;
	}

	@Override
	public boolean hasNextOccurence() {
		return _occurenceNavigator.hasNextOccurence();
	}

	@Override
	public boolean hasPreviousOccurence() {
		return _occurenceNavigator.hasPreviousOccurence();
	}

	@Override
	public OccurenceInfo goNextOccurence() {
		return _occurenceNavigator.goNextOccurence();
	}

	@Override
	public OccurenceInfo goPreviousOccurence() {
		return _occurenceNavigator.goPreviousOccurence();
	}

	@Override
	public String getNextOccurenceActionName() {
		return _occurenceNavigator.getNextOccurenceActionName();
	}

	@Override
	public String getPreviousOccurenceActionName() {
		return _occurenceNavigator.getPreviousOccurenceActionName();
	}

	private class MouseMotionListenerImpl extends MouseMotionAdapter {
		@Override
		public void mouseMoved(final MouseEvent e) {
			if (_bugTreePanel.getResult() != null && getRowForLocation(e.getX(), e.getY()) != 0) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} else if (_bugTreePanel.getResult() != null) {
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

	/**
	 * Listen for clicks and scroll to the error's source as necessary.
	 */
	private class MouseListenerImpl extends MouseAdapter {

		@Override
		public void mouseClicked(final MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() < 2) {
				if (_bugTreePanel.getResult() != null && getRowForLocation(e.getX(), e.getY()) == 0) {
					final Object root = getModel().getRoot();
					final Component rendererComponent = getCellRenderer().getTreeCellRendererComponent(BugTree.this, root, true, true, false, 0, true);
					final int width = rendererComponent.getPreferredSize().width;
					final int mouseX = e.getX();
					if (mouseX <= width + 10 && mouseX >= width - 40) {
						final ToolWindowPanel panel = ToolWindowPanel.getInstance(_project);
						if (panel != null) {
							final DialogBuilder dialog = AnalysisRunDetailsDialog.create(
									panel.getProject(),
									panel.getBugTreePanel().getGroupModel().getBugCount(),
									panel.getResult().getAnalyzedClassCountSafe(),
									panel.getResult()
							);
							dialog.showModal(false);
						}
					}
				}
			}
		}

		@Override
		public void mouseExited(final MouseEvent e) {
			if (_bugTreePanel.getResult() != null) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	/**
	 * Listen for tree selection events and scroll to the error's source as necessary.
	 */
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
