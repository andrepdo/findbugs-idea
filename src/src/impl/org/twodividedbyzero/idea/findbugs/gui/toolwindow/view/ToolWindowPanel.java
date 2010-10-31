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
package org.twodividedbyzero.idea.findbugs.gui.toolwindow.view;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiFile;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.ProjectStats;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.VersionManager;
import org.twodividedbyzero.idea.findbugs.common.event.EventListener;
import org.twodividedbyzero.idea.findbugs.common.event.EventManagerImpl;
import org.twodividedbyzero.idea.findbugs.common.event.filters.BugReporterEventFilter;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsUtil;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.gui.common.ActionToolbarContainer;
import org.twodividedbyzero.idea.findbugs.gui.common.BalloonTipFactory;
import org.twodividedbyzero.idea.findbugs.gui.common.MultiSplitLayout;
import org.twodividedbyzero.idea.findbugs.gui.common.MultiSplitPane;
import org.twodividedbyzero.idea.findbugs.gui.common.NDockLayout;

import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;
import java.util.Map;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
@SuppressWarnings({"HardCodedStringLiteral", "AnonymousInnerClass", "AnonymousInnerClassMayBeStatic"})
@edu.umd.cs.findbugs.annotations.SuppressWarnings({"SE_BAD_FIELD"})
public class ToolWindowPanel extends JPanel implements EventListener<BugReporterEvent> {

	private static final Logger LOGGER = Logger.getInstance(ToolWindowPanel.class.getName());

	private static final String DEFAULT_LAYOUT_DEF = "(ROW (LEAF name=left weight=0.4) (COLUMN weight=0.6 right.top right.bottom) wight=0.6)";
	private static final String PREVIEW_LAYOUT_DEF = "(ROW (LEAF name=left weight=0.3) (LEAF name=middle weight=0.4) (COLUMN weight=0.3 right.top right.bottom))";
	//private static final String PREVIEW_LAYOUT_DEF = "(ROW (LEAF name=left weight=0.4) (COLUMN weight=0.6 right.top right.bottom)  (LEAF name=right))";

	private final Project _project;
	private BugTreePanel _bugTreePanel;
	private transient BugDetailsComponents _bugDetailsComponents;
	private ComponentListener _componentListener;
	private MultiSplitPane _multiSplitPane;

	private boolean _previewEnabled;
	private boolean _isPreviewLayoutEnabled;
	private transient PreviewPanel _previewPanel;
	private final transient ToolWindow _parent;


	public ToolWindowPanel(final Project project, final ToolWindow parent) {
		_project = project;
		_parent = parent;
		checkFindBugsPlugin();
		initGui();
	}


	private void initGui() {
		setLayout(new NDockLayout());
		setBorder(new EmptyBorder(1, 1, 1, 1));

		// Create the toolbars
		//final DefaultActionGroup actionGroupLeft = new DefaultActionGroup();
		//actionGroupLeft.add(new AnalyzeCurrentEditorFile());
		final ActionGroup actionGroupLeft = (ActionGroup) ActionManager.getInstance().getAction(FindBugsPluginConstants.ACTION_GROUP_LEFT);
		final ActionToolbar toolbarLeft1 = ActionManager.getInstance().createActionToolbar(IdeaUtilImpl.getPluginComponent(_project).getInternalToolWindowId(), actionGroupLeft, false);

		final ActionGroup actionGroupRight = (ActionGroup) ActionManager.getInstance().getAction(FindBugsPluginConstants.ACTION_GROUP_RIGHT);
		final ActionToolbar toolbarRight1 = ActionManager.getInstance().createActionToolbar(IdeaUtilImpl.getPluginComponent(_project).getInternalToolWindowId(), actionGroupRight, false);

		final ActionGroup actionGroupNavigation = (ActionGroup) ActionManager.getInstance().getAction(FindBugsPluginConstants.ACTION_GROUP_NAVIGATION);
		final ActionToolbar toolbarNavigation1 = ActionManager.getInstance().createActionToolbar(IdeaUtilImpl.getPluginComponent(_project).getInternalToolWindowId(), actionGroupNavigation, false);

		final ActionGroup actionGroupUtils = (ActionGroup) ActionManager.getInstance().getAction(FindBugsPluginConstants.ACTION_GROUP_UTILS);
		final ActionToolbar toolbarUtils1 = ActionManager.getInstance().createActionToolbar(IdeaUtilImpl.getPluginComponent(_project).getInternalToolWindowId(), actionGroupUtils, false);


		final Component toolbarLeft = new ActionToolbarContainer(FindBugsPluginConstants.TOOL_WINDOW_ID + ": Analysis...", SwingConstants.VERTICAL, toolbarLeft1, true);
		final Component toolbarRight = new ActionToolbarContainer(FindBugsPluginConstants.TOOL_WINDOW_ID + ": Grouping...", SwingConstants.VERTICAL, toolbarRight1, true);
		final Component toolbarNavigation = new ActionToolbarContainer(FindBugsPluginConstants.TOOL_WINDOW_ID + ": Navigation...", SwingConstants.VERTICAL, toolbarNavigation1, true);
		final Component toolbarUtils = new ActionToolbarContainer(FindBugsPluginConstants.TOOL_WINDOW_ID + ": Utils...", SwingConstants.VERTICAL, toolbarUtils1, true);


		add(toolbarLeft, NDockLayout.WEST);
		add(toolbarRight, NDockLayout.WEST);
		add(toolbarNavigation, NDockLayout.WEST);
		add(toolbarUtils, NDockLayout.WEST);

		updateLayout(false);

		add(getMultiSplitPane(), NDockLayout.CENTER);
	}


	private MultiSplitPane getMultiSplitPane() {
		if(_multiSplitPane == null) {
			_multiSplitPane = new MultiSplitPane();
			_multiSplitPane.setContinuousLayout(true);
		}
		return _multiSplitPane;
	}


	private void _updateMultiSplitLayout(final String layoutDef) {
		final MultiSplitLayout.Node modelRoot = MultiSplitLayout.parseModel(layoutDef);
		final MultiSplitLayout multiSplitLayout = getMultiSplitPane().getMultiSplitLayout();
		multiSplitLayout.setDividerSize(5);
		multiSplitLayout.setModel(modelRoot);
		multiSplitLayout.setFloatingDividers(true);
	}


	private void updateLayout(final boolean enablePreviewLayout) {
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			public void run() {

				if (!_isPreviewLayoutEnabled && enablePreviewLayout) {
					_updateMultiSplitLayout(PREVIEW_LAYOUT_DEF);
					getMultiSplitPane().add(getBugTreePanel(), "left");
					_multiSplitPane.add(getPreviewPanel().getComponent(), "middle");
					getMultiSplitPane().add(getBugDetailsComponents().getBugDetailsPanel(), "right.top");
					getMultiSplitPane().add(getBugDetailsComponents().getBugExplanationPanel(), "right.bottom");
					_isPreviewLayoutEnabled = true;

				} else if (!enablePreviewLayout) {
					getPreviewPanel().release();

					_updateMultiSplitLayout(DEFAULT_LAYOUT_DEF);
					getMultiSplitPane().add(getBugTreePanel(), "left");
					getMultiSplitPane().add(getBugDetailsComponents().getBugDetailsPanel(), "right.top");
					getMultiSplitPane().add(getBugDetailsComponents().getBugExplanationPanel(), "right.bottom");

					if (getPreviewPanel().getEditor() != null) {
						resizeSplitNodes(ToolWindowPanel.this);
					}
					_previewPanel = null;
					_isPreviewLayoutEnabled = false;
				}
			}
		});
	}


	private PreviewPanel getPreviewPanel() {
		if(_previewPanel == null) {
			_previewPanel = new PreviewPanel();
		}
		return _previewPanel;
	}

	
	public void setPreviewEditor(@Nullable final Editor editor, final PsiFile psiFile) {
		if(editor != null) {
			//getPreviewPanel().releaseEditor();
			updateLayout(true);
			getPreviewPanel().add(editor, psiFile);
			resizeSplitNodes(this);
		}
	}


	public void setPreviewEnabled(final boolean enabled) {
		updateLayout(false);
		_previewEnabled = enabled;
		getBugTreePanel().setPreviewEnabled(enabled);
	}


	public boolean isPreviewEnabled() {
		return _previewEnabled;
	}


	private void checkFindBugsPlugin() {
		final FindBugsPlugin findBugsPlugin = IdeaUtilImpl.getPluginComponent(_project);
		installListeners();
		if (findBugsPlugin == null) {
			throw new IllegalStateException("Couldn't get " + FindBugsPluginConstants.TOOL_WINDOW_ID + " plugin");
		}
	}


	public BugCollection getBugCollection() {
		return _bugTreePanel.getBugCollection();
	}


	public Map<PsiFile, List<ExtendedProblemDescriptor>> getProblems() {
		return _bugTreePanel.getProblems();
	}


	private void installListeners() {
		EventManagerImpl.getInstance().addEventListener(new BugReporterEventFilter(_project.getName()), this);

		if (_componentListener == null) {
			_componentListener = createComponentListener();
		}
		addComponentListener(_componentListener);
	}


	@edu.umd.cs.findbugs.annotations.SuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
	public void onEvent(@NotNull final BugReporterEvent event) {
		///LOGGER.debug(event.toString());

		switch (event.getOperation()) {
			case ANALYSIS_STARTED:
				EventDispatchThreadHelper.invokeLater(new Runnable() {
					public void run() {
										EditorFactory.getInstance().refreshAllEditors();
						DaemonCodeAnalyzer.getInstance(_project).restart();
					}
				});
				updateLayout(false);

				_bugTreePanel.clear(true);
				_bugTreePanel.updateRootNode(event.getProjectStats());
				break;
			case ANALYSIS_ABORTED:
				_bugTreePanel.setBugCollection(event.getBugCollection());
				EventDispatchThreadHelper.invokeLater(new Runnable() {
					public void run() {
						final String text = "Analysis aborted..."; 
						BalloonTipFactory.showToolWindowInfoNotifier(text);
					}
				});
				break;
			case ANALYSIS_FINISHED:
				final BugCollection bugCollection = event.getBugCollection();
				_bugTreePanel.setBugCollection(bugCollection);
				final ProjectStats projectStats = bugCollection.getProjectStats();
				_bugTreePanel.updateRootNode(projectStats);

				EventDispatchThreadHelper.invokeLater(new Runnable() {
					public void run() {
						_bugTreePanel.getBugTree().validate();
						final String text = VersionManager.getName() + ": <b>found " + _bugTreePanel.getGroupModel().getBugCount() + " bugs in " + projectStats.getNumClasses() + (projectStats.getNumClasses() > 1 ? " classes" : " class") + "</b><br/>" + "<font size='10px'>using " + VersionManager.getFullVersion() + " with Findbugs version " + FindBugsUtil.getFindBugsFullVersion() + "</font>";
						BalloonTipFactory.showToolWindowInfoNotifier(text);
						EditorFactory.getInstance().refreshAllEditors();
						DaemonCodeAnalyzer.getInstance(_project).restart();
					}
				});

				break;
			case NEW_BUG_INSTANCE:
				EventDispatchThreadHelper.invokeLater(new Runnable() {
					public void run() {
						_bugTreePanel.addNode(event.getBugInstance());
						_bugTreePanel.updateRootNode(event.getProjectStats());
					}
				});
				break;
			default:
		}
	}


	private ComponentListener createComponentListener() {
		return new ToolWindowComponentAdapter(this);
	}


	private void resizeSplitNodes(final Component component) {
		final int width = component.getWidth();
		final int height = component.getHeight();
		if(isPreviewEnabled() && getPreviewPanel().getEditor() != null) {
			_bugDetailsComponents.adaptSize((int) (width * 0.3), height);
			_bugTreePanel.adaptSize((int) (width * 0.3), height);
		} else {
			_bugDetailsComponents.adaptSize((int) (width * 0.6), height);
			_bugTreePanel.adaptSize((int) (width * 0.4), height);
		}
		getPreviewPanel().adaptSize((int) (width * 0.4), height);
		_multiSplitPane.validate();
	}


	public BugTreePanel getBugTreePanel() {
		if(_bugTreePanel == null) {
			_bugTreePanel = new BugTreePanel(this, _project);
		}
		return _bugTreePanel;
	}


	public BugDetailsComponents getBugDetailsComponents() {
		if(_bugDetailsComponents == null) {
			_bugDetailsComponents = new BugDetailsComponents(this);
		}
		return _bugDetailsComponents;
	}


	private static class ToolWindowComponentAdapter extends ComponentAdapter {

		private final ToolWindowPanel _toolWindowPanel;


		private ToolWindowComponentAdapter(final ToolWindowPanel toolWindowPanel) {
			_toolWindowPanel = toolWindowPanel;
		}


		@Override
		public void componentShown(final ComponentEvent e) {
			super.componentShown(e);
			final Component component = e.getComponent();
			_toolWindowPanel.resizeSplitNodes(component);
		}


		@Override
		public void componentResized(final ComponentEvent e) {
			super.componentResized(e);
			final Component component = e.getComponent();
			_toolWindowPanel.resizeSplitNodes(component);
		}


		@Override
		public void componentMoved(final ComponentEvent e) {
			super.componentMoved(e);
			final Component component = e.getComponent();
			_toolWindowPanel.resizeSplitNodes(component);
		}
	}
}
