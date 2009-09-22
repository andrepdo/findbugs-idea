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

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.ProjectStats;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.common.FindBugsPluginConstants;
import org.twodividedbyzero.idea.findbugs.common.VersionManager;
import org.twodividedbyzero.idea.findbugs.common.event.EventListener;
import org.twodividedbyzero.idea.findbugs.common.event.EventManagerImpl;
import org.twodividedbyzero.idea.findbugs.common.event.filters.BugReporterEventFilter;
import org.twodividedbyzero.idea.findbugs.common.event.types.BugReporterEvent;
import org.twodividedbyzero.idea.findbugs.common.ui.ActionToolbarContainer;
import org.twodividedbyzero.idea.findbugs.common.ui.MultiSplitLayout;
import org.twodividedbyzero.idea.findbugs.common.ui.MultiSplitPane;
import org.twodividedbyzero.idea.findbugs.common.ui.NDockLayout;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsUtil;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.gui.common.BalloonTipFactory;

import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class ToolWindowPanel extends JPanel implements EventListener<BugReporterEvent> {

	private static final Logger LOGGER = Logger.getInstance(ToolWindowPanel.class.getName());

	private Project _project;
	private boolean _scrollToSource;
	private ActionToolbar _toolbarLeft;
	private ActionToolbar _toolbarRight;
	private ActionToolbar _toolbarNavigation;
	private ToolWindow _toolWindow;
	private BugTreePanel _bugTreePanel;
	private BugDetailsComponents _bugDetailsComponents;
	private ComponentListener _componentListener;
	private MultiSplitPane _multiSplitPane;


	public ToolWindowPanel(final Project project, final ToolWindow parent) {
		super();
		_project = project;
		_toolWindow = parent;
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
		_toolbarLeft = ActionManager.getInstance().createActionToolbar(IdeaUtilImpl.getPluginComponent(_project).getInternalToolWindowId(), actionGroupLeft, false);

		final ActionGroup actionGroupRight = (ActionGroup) ActionManager.getInstance().getAction(FindBugsPluginConstants.ACTION_GROUP_RIGHT);
		_toolbarRight = ActionManager.getInstance().createActionToolbar(IdeaUtilImpl.getPluginComponent(_project).getInternalToolWindowId(), actionGroupRight, false);


		final ActionGroup actionGroupNavigation = (ActionGroup) ActionManager.getInstance().getAction(FindBugsPluginConstants.ACTION_GROUP_NAVIGATION);
		_toolbarNavigation = ActionManager.getInstance().createActionToolbar(IdeaUtilImpl.getPluginComponent(_project).getInternalToolWindowId(), actionGroupNavigation, false);

		final ActionToolbarContainer toolbarLeft = new ActionToolbarContainer(FindBugsPluginConstants.TOOL_WINDOW_ID + ": Analysis...", SwingConstants.VERTICAL, _toolbarLeft, true); // NON-NLS
		final ActionToolbarContainer toolbarRight = new ActionToolbarContainer(FindBugsPluginConstants.TOOL_WINDOW_ID + ": Grouping...", SwingConstants.VERTICAL, _toolbarRight, true); // NON-NLS
		final ActionToolbarContainer toolbarNavigation = new ActionToolbarContainer(FindBugsPluginConstants.TOOL_WINDOW_ID + ": Navigation...", SwingConstants.VERTICAL, _toolbarNavigation, true); // NON-NLS


		add(toolbarLeft, NDockLayout.WEST);
		add(toolbarRight, NDockLayout.WEST);
		add(toolbarNavigation, NDockLayout.WEST);


		//final String layoutDef = "(ROW (LEAF name=left weight=0.4) (LEAF name=right weight=0.6))";  // NON-NLS
		final String layoutDef = "(ROW (LEAF name=left weight=0.4) (COLUMN weight=0.6 right.top right.bottom) weight=0.6)";  // NON-NLS
		final MultiSplitLayout.Node modelRoot = MultiSplitLayout.parseModel(layoutDef);
		_multiSplitPane = new MultiSplitPane();
		_multiSplitPane.setContinuousLayout(true);
		final MultiSplitLayout multiSplitLayout = _multiSplitPane.getMultiSplitLayout();
		multiSplitLayout.setDividerSize(5);
		multiSplitLayout.setModel(modelRoot);
		multiSplitLayout.setFloatingDividers(true);
		//multiSplitLayout.printModel(modelRoot);


		_bugTreePanel = new BugTreePanel(this, _project);
		//splitPane.setLeftComponent(_bugTreePanel);
		_multiSplitPane.add(_bugTreePanel, "left"); // NON-NLS

		_bugDetailsComponents = new BugDetailsComponents(this);
		//splitPane.setRightComponent(_bugDetailsComponents);
		_multiSplitPane.add(_bugDetailsComponents.getBugDetailsPanel(), "right.top"); // NON-NLS
		_multiSplitPane.add(_bugDetailsComponents.getBugExplanationPanel(), "right.bottom"); // NON-NLS

		add(_multiSplitPane, NDockLayout.CENTER);
	}


	private void checkFindBugsPlugin() {
		final FindBugsPlugin findBugsPlugin = IdeaUtilImpl.getPluginComponent(_project);
		installListeners();
		if (findBugsPlugin == null) {
			throw new IllegalStateException("Couldn't get " + FindBugsPluginConstants.TOOL_WINDOW_ID + " plugin");
		}
	}


	private void installListeners() {
		EventManagerImpl.getInstance().addEventListener(new BugReporterEventFilter(_project.getName()), this);

		if (_componentListener == null) {
			_componentListener = createComponentListener();
		}
		addComponentListener(_componentListener);
	}


	public void onEvent(@NotNull final BugReporterEvent event) {
		///LOGGER.debug(event.toString());

		switch (event.getOperation()) {
			case ANALYSIS_STARTED:
				//_bugDetailsComponents.getBugDetailsPanel().setPreferredSize(new Dimension((int) (getPreferredSize().width * 0.6), getHeight()));
				//_bugDetailsComponents.getBugDetailsPanel().validate();
				_bugTreePanel.clear(true);
				_bugTreePanel.updateRootNode(event.getProjectStats());
				break;
			case ANALYSIS_ABORTED:
				_bugTreePanel.setBugCollection(event.getBugCollection());
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						final String text = "Analysis aborted..."; // NON-NLS
						BalloonTipFactory.showToolWindowInfoNotifier(text);
					}
				});
				break;
			case ANALYSIS_FINISHED:
				final BugCollection bugCollection = event.getBugCollection();
				_bugTreePanel.setBugCollection(bugCollection);
				final ProjectStats projectStats = bugCollection.getProjectStats();
				//final int bugCount = bugCollection.getCollection().size();
				_bugTreePanel.updateRootNode(projectStats);

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						_bugTreePanel.getBugTree().validate();
						final String text = VersionManager.getName() + ": <b>found " + _bugTreePanel.getGroupModel().getBugCount() + " bugs in " + projectStats.getNumClasses() + (projectStats.getNumClasses() > 1 ? " classes" : " class") + "</b><br/>" + "<font size='10px'>using " + VersionManager.getFullVersion() + " with Findbugs version " + FindBugsUtil.getFindBugsFullVersion() + "</font>"; // NON-NLS
						BalloonTipFactory.showToolWindowInfoNotifier(text);
					}
				});

				break;
			case NEW_BUG_INSTANCE:
				_bugTreePanel.addNode(event.getBugInstance());
				//_bugTreePanel.addTreeNode(event.getBugInstance());
				_bugTreePanel.updateRootNode(event.getProjectStats());
				break;
		}
	}


	private ComponentListener createComponentListener() {
		return new ComponentAdapter() {
			@Override
			public void componentShown(final ComponentEvent e) {
				super.componentShown(e);
				final Component component = e.getComponent();
				resizeSplitNodes(component);
				//_multiSplitPane.getMultiSplitLayout().setFloatingDividers(true);
			}


			@Override
			public void componentResized(final ComponentEvent e) {
				super.componentResized(e);
				final Component component = e.getComponent();
				resizeSplitNodes(component);
				//_multiSplitPane.getMultiSplitLayout().setFloatingDividers(true);
			}


			@Override
			public void componentMoved(final ComponentEvent e) {
				super.componentMoved(e);
				final Component component = e.getComponent();
				resizeSplitNodes(component);
				//_multiSplitPane.getMultiSplitLayout().layoutContainer(_bugTreePanel);
				//_multiSplitPane.getMultiSplitLayout().layoutContainer(_bugDetailsComponents.getBugDetailsPanel());
				//_multiSplitPane.getMultiSplitLayout().layoutContainer(_bugDetailsComponents.getBugExplanationPanel());
				//_multiSplitPane.getMultiSplitLayout().setFloatingDividers(true);
				//_multiSplitPane.doLayout();
			}
		};
	}


	private void resizeSplitNodes(final Component component) {
		final int width = component.getWidth();
		final int height = component.getHeight();
		_bugDetailsComponents.resizeComponents(width, height);
		_bugTreePanel.resizeComponent(width, height);
		_multiSplitPane.validate();
	}


	public ActionToolbar getToolbarLeft() {
		return _toolbarLeft;
	}


	public ActionToolbar getToolbarRight() {
		return _toolbarLeft;
	}


	public BugTreePanel getBugTreePanel() {
		return _bugTreePanel;
	}


	public BugDetailsComponents getBugDetailsComponents() {
		return _bugDetailsComponents;
	}
}
