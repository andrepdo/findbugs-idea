/*
 * Copyright 2016 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.core;

import com.intellij.CommonBundle;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.PairConsumer;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.gui.toolwindow.view.ToolWindowPanel;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import java.util.Collection;

public final class CheckinHandlerFactoryImpl extends CheckinHandlerFactory {
	@NotNull
	@Override
	public CheckinHandler createHandler(@NotNull final CheckinProjectPanel panel, @NotNull final CommitContext commitContext) {
		return new CheckinHandler() {
			@Override
			public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
				final JCheckBox run = new JCheckBox(ResourcesLoader.getString("checkin.text"));
				return new RefreshableOnComponent() {
					public JComponent getComponent() {
						return JBUI.Panels.simplePanel().addToLeft(run);
					}

					@Override
					public void refresh() {
					}

					@Override
					public void saveState() {
						WorkspaceSettings.getInstance(panel.getProject()).analyzeBeforeCheckIn = run.isSelected();
					}

					@Override
					public void restoreState() {
						run.setSelected(WorkspaceSettings.getInstance(panel.getProject()).analyzeBeforeCheckIn);
					}
				};
			}

			@Override
			public ReturnResult beforeCheckin(@Nullable final CommitExecutor executor, final PairConsumer<Object, Object> additionalDataConsumer) {
				if (!WorkspaceSettings.getInstance(panel.getProject()).analyzeBeforeCheckIn) {
					return super.beforeCheckin(executor, additionalDataConsumer);
				}

				new FindBugsStarter(
						panel.getProject(),
						"Running FindBugs analysis for affected files...",
						ProgressStartType.Modal
				) {
					@Override
					protected boolean isCompileBeforeAnalyze() {
						return false; // CompilerManager#make start asynchronous task
					}

					@Override
					protected void createCompileScope(@NotNull final CompilerManager compilerManager, @NotNull final Consumer<CompileScope> consumer) {
						throw new UnsupportedOperationException();
					}

					@Override
					protected boolean configure(@NotNull final ProgressIndicator indicator, @NotNull final FindBugsProjects projects, final boolean justCompiled) {
						final Collection<VirtualFile> virtualFiles = panel.getVirtualFiles();
						projects.addFiles(virtualFiles, false, hasTests(virtualFiles));
						return true;
					}
				}.start();
				final ToolWindowPanel toolWindowPanel = ToolWindowPanel.getInstance(panel.getProject());
				if (toolWindowPanel != null && toolWindowPanel.getResult() != null) {
					if (!toolWindowPanel.getResult().isBugCollectionEmpty()) {
						// Based on com.intellij.openapi.vcs.checkin.CodeAnalysisBeforeCheckinHandler#processFoundCodeSmells
						String commitButtonText = executor != null ? executor.getActionText() : panel.getCommitActionName();
						commitButtonText = StringUtil.trimEnd(commitButtonText, "...");
						final int answer = Messages.showYesNoCancelDialog(
								panel.getProject(),
								ResourcesLoader.getString("checkin.problem.text"),
								StringUtil.capitalizeWords(ResourcesLoader.getString("checkin.problem.title"), true),
								ResourcesLoader.getString("checkin.problem.review"),
								commitButtonText,
								CommonBundle.getCancelButtonText(),
								UIUtil.getWarningIcon()
						);
						if (answer == Messages.YES) {
							ToolWindowPanel.showWindow(panel.getProject());
							return ReturnResult.CLOSE_WINDOW;
						} else if (answer == Messages.CANCEL) {
							return ReturnResult.CANCEL;
						}
					}
				}
				return super.beforeCheckin(executor, additionalDataConsumer);
			}
		};
	}
}
