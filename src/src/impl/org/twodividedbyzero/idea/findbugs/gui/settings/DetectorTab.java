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
package org.twodividedbyzero.idea.findbugs.gui.settings;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import edu.umd.cs.findbugs.DetectorFactory;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * Some code here is based on {@link com.intellij.profile.codeInspection.ui.SingleInspectionProfilePanel}.
 */
final class DetectorTab extends JPanel implements Disposable {
	private JLabel hintLabel;
	private DetectorTableHeaderPane tableHeaderPane;
	private DetectorTablePane tablePane;
	private JBSplitter splitter;
	private DetectorDetailsPane details;

	DetectorTab() {
		super(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

		final JPanel hintPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		hintPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
		hintPane.add(getHintLabel());

		final JPanel topPane = new JPanel(new BorderLayout());
		topPane.add(hintPane, BorderLayout.NORTH);
		topPane.add(getTableHeaderPane(), BorderLayout.SOUTH);

		splitter = new JBSplitter(false, 0.5f, 0.01f, 0.99f);
		splitter.setSplitterProportionKey("DetectorTab.VERTICAL_DIVIDER_PROPORTION");
		splitter.setFirstComponent(getTablePane());
		splitter.setSecondComponent(getDetails());
		splitter.setHonorComponentsMinimumSize(false);

		add(topPane, BorderLayout.NORTH);
		add(splitter);
	}

	@NotNull
	private JLabel getHintLabel() {
		if (hintLabel == null) {
			// LATER: use HyperlinkLabel.setHyperlinkText("because the ", "bug category", " is disabled")
			hintLabel = new JLabel("<html>" + ResourcesLoader.getString("detector.description.line1") + "<br>"
					+ ResourcesLoader.getString("detector.description.line2") + "</html>");
			hintLabel.setIcon(MessageType.INFO.getDefaultIcon());
			hintLabel.setDisabledIcon(MessageType.INFO.getDefaultIcon());
		}
		return hintLabel;
	}

	@NotNull
	private DetectorTableHeaderPane getTableHeaderPane() {
		getTablePane();
		return tableHeaderPane;
	}

	@NotNull
	DetectorTablePane getTablePane() {
		if (tablePane == null) {
			tablePane = new DetectorTablePane();
			final TreeTableTree tree = tablePane.getTable().getTree();
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(@NotNull final TreeSelectionEvent e) {
					// LATER: optimize this, valueChanged is invoked multiple time
					final Object selected = tree.getLastSelectedPathComponent();
					DetectorFactory detector = null;
					if (selected != null && !((AbstractDetectorNode) selected).isGroup()) {
						detector = ((DetectorNode) selected).getDetector();
					}
					getDetails().load(detector);
				}
			});
			details = new DetectorDetailsPane();
			tableHeaderPane = new DetectorTableHeaderPane(tablePane, details);
			tablePane.setHeaderPane(tableHeaderPane);
			details.setHeaderPane(tableHeaderPane);
		}
		return tablePane;
	}

	@NotNull
	private DetectorDetailsPane getDetails() {
		getTablePane();
		return details;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		getHintLabel().setEnabled(enabled);
		getTableHeaderPane().setEnabled(enabled);
		getTablePane().setEnabled(enabled);
		splitter.setEnabled(enabled);
		getDetails().setEnabled(enabled);
	}

	boolean isModified(@NotNull final AbstractSettings settings) {
		return getTablePane().isModified(settings);
	}

	void apply(@NotNull final AbstractSettings settings) throws ConfigurationException {
		getTablePane().apply(settings);
	}

	void reset(@NotNull final AbstractSettings settings) {
		getTablePane().reset(settings);
	}

	void setFilter(String filter) {
		if (tableHeaderPane != null) {
			tableHeaderPane.setFilter(filter);
		}
	}

	@Override
	public void dispose() {
		if (tableHeaderPane != null) {
			Disposer.dispose(tableHeaderPane);
			tableHeaderPane = null;
		}
	}

	@NotNull
	static String getSearchPath() {
		return ResourcesLoader.getString("settings.detector");
	}

	@NotNull
	static String[] getSearchResourceKey() {
		return new String[]{
				"detector.description.line1",
				"detector.description.line2"
		};
	}
}
