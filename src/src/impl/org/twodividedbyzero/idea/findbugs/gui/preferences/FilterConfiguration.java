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
package org.twodividedbyzero.idea.findbugs.gui.preferences;

import com.intellij.openapi.project.Project;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsUtil;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.gui.common.ExtensionFileFilter;
import org.twodividedbyzero.idea.findbugs.gui.common.ListFacade;
import org.twodividedbyzero.idea.findbugs.gui.common.ScrollPaneFacade;
import org.twodividedbyzero.idea.findbugs.gui.preferences.BrowseAction.BrowseActionCallback;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.9
 */
@SuppressWarnings("AnonymousInnerClass")
public class FilterConfiguration implements ConfigurationPage {

	private final FindBugsPreferences _preferences;
	private final ConfigurationPanel _parent;
	private Component _component;
	private JPanel _includePanel;
	private JList _includeList;
	private JPanel _excludePanel;
	private JList _excludeList;
	private JList _baselineList;
	private JPanel _baselinePanel;


	public FilterConfiguration(final ConfigurationPanel parent, final FindBugsPreferences preferences) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_preferences = preferences;
		_parent = parent;
	}


	@Override
	public Component getComponent() {
		if (_component == null) {
			final double border = 5;
			final double rowsGap = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayoutConstants.FILL, border}, // Columns
									 {border, TableLayoutConstants.PREFERRED, rowsGap, TableLayoutConstants.PREFERRED, rowsGap, TableLayoutConstants.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);

			final Container mainPanel = new JPanel(tbl);
			mainPanel.add(getIncludePanel(), "1, 1, 1, 1");
			mainPanel.add(getExcludePanel(), "1, 3, 1, 3");
			mainPanel.add(getBaseLinePanel(), "1, 5, 1, 5");

			_component = mainPanel;
		}
		//updatePreferences();
		return _component;
	}


	@Override
	public void updatePreferences() {
		clearModels();
		syncModels();
	}


	@SuppressWarnings("unchecked")
	private void syncModels() {
		for (final String s : _preferences.getIncludeFilters()) {
			getModel(getIncludeList()).addElement(s);
		}

		for (final String s : _preferences.getExcludeFilters()) {
			getModel(getExcludeList()).addElement(s);
		}

		for (final String s : _preferences.getExcludeBaselineBugs()) {
			getModel(getBaselineList()).addElement(s);
		}
	}


	private void clearModels() {
		getModel(getIncludeList()).clear();
		getModel(getExcludeList()).clear();
		getModel(getBaselineList()).clear();
	}


	JPanel getIncludePanel() {
		if (_includePanel == null) {

			final double border = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayoutConstants.FILL, colsGap, TableLayoutConstants.PREFERRED, border}, // Columns
									 {border, TableLayoutConstants.FILL, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_includePanel = new JPanel(tbl);
			_includePanel.setBorder(BorderFactory.createTitledBorder("Include filter files"));

			final ListModel model = new DefaultListModel();

			_includeList = ListFacade.createList(model);
			_includeList.setVisibleRowCount(7);
			_includeList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			_includeList.setCellRenderer(new ExpandPathMacroListRenderer(_parent.getProject()));

			final Component scrollPane = ScrollPaneFacade.createScrollPane(_includeList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			_includePanel.add(scrollPane, "1, 1, 1, 1"); // col ,row, col, row


			final double rowsGap = 5;
			final double[][] bPanelSize = {{border, TableLayoutConstants.PREFERRED}, // Columns
										   {border, TableLayoutConstants.PREFERRED, rowsGap, TableLayoutConstants.PREFERRED, border}};// Rows
			final TableLayout btbl = new TableLayout(bPanelSize);

			final Container buttonPanel = new JPanel(btbl);
			_includePanel.add(buttonPanel, "3, 1, 3, 1");

			final AbstractButton addButton = new JButton();
			final Action action = new BrowseAction(_parent, "Add...", new ExtensionFileFilter(FindBugsUtil.XML_EXTENSIONS_SET), new BrowseActionCallback() {
				@Override
				public void addSelection(final File selectedFile) {
					final String replacement = IdeaUtilImpl.collapsePathMacro(_parent.getProject(), selectedFile.getAbsolutePath());
					//noinspection unchecked
					((DefaultListModel) _includeList.getModel()).addElement(replacement);
					_preferences.getIncludeFilters().add(replacement);
					_preferences.setModified(true);
				}
			});
			addButton.setAction(action);
			buttonPanel.add(addButton, "1, 1, 1, 1");

			final JButton removeButton = new JButton("Remove") {


				@Override
				public boolean isEnabled() {
					return super.isEnabled() && _includeList.getSelectedIndex() > -1;
				}
			};
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final int index = _includeList.getSelectedIndex();
					getModel(_includeList).remove(index);
					_preferences.removeIncludeFilter(index);
				}
			});
			buttonPanel.add(removeButton, "1, 3, 1, 3");

			_includeList.addListSelectionListener(new IncludeListSelectionListener(removeButton));
		}

		return _includePanel;
	}


	JPanel getExcludePanel() {
		if (_excludePanel == null) {

			final double border = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayoutConstants.FILL, colsGap, TableLayoutConstants.PREFERRED, border}, // Columns
									 {border, TableLayoutConstants.FILL, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_excludePanel = new JPanel(tbl);
			_excludePanel.setBorder(BorderFactory.createTitledBorder("Exclude filter files"));

			final ListModel model = new DefaultListModel();

			_excludeList = ListFacade.createList(model);
			_excludeList.setVisibleRowCount(7);
			_excludeList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			_excludeList.setCellRenderer(new ExpandPathMacroListRenderer(_parent.getProject()));

			final Component scrollPane = ScrollPaneFacade.createScrollPane(_excludeList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			_excludePanel.add(scrollPane, "1, 1, 1, 1"); // col ,row, col, row


			final double rowsGap = 5;
			final double[][] bPanelSize = {{border, TableLayoutConstants.PREFERRED}, // Columns
										   {border, TableLayoutConstants.PREFERRED, rowsGap, TableLayoutConstants.PREFERRED, border}};// Rows
			final TableLayout btbl = new TableLayout(bPanelSize);

			final Container buttonPanel = new JPanel(btbl);
			_excludePanel.add(buttonPanel, "3, 1, 3, 1");

			final AbstractButton addButton = new JButton();
			final Action action = new BrowseAction(_parent, "Add...", new ExtensionFileFilter(FindBugsUtil.XML_EXTENSIONS_SET), new BrowseActionCallback() {
				@Override
				public void addSelection(final File selectedFile) {
					final String replacement = IdeaUtilImpl.collapsePathMacro(_parent.getProject(), selectedFile.getAbsolutePath());
					//noinspection unchecked
					((DefaultListModel) _excludeList.getModel()).addElement(replacement);
					_preferences.getExcludeFilters().add(replacement);
					_preferences.setModified(true);
				}
			});
			addButton.setAction(action);
			buttonPanel.add(addButton, "1, 1, 1, 1");

			final AbstractButton removeButton = new JButton("Remove") {


				@Override
				public boolean isEnabled() {
					return super.isEnabled() && _excludeList.getSelectedIndex() > -1;
				}
			};
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final int index = _excludeList.getSelectedIndex();
					getModel(_excludeList).remove(index);
					_preferences.removeExcludeFilter(index);
				}
			});
			buttonPanel.add(removeButton, "1, 3, 1, 3");

			_excludeList.addListSelectionListener(new ExcludeListSelectionListener(removeButton));
		}

		return _excludePanel;
	}


	JPanel getBaseLinePanel() {
		if (_baselinePanel == null) {

			final double border = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayoutConstants.FILL, colsGap, TableLayoutConstants.PREFERRED, border}, // Columns
									 {border, TableLayoutConstants.FILL, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_baselinePanel = new JPanel(tbl);
			_baselinePanel.setBorder(BorderFactory.createTitledBorder("Exclude baseline bugs"));

			final ListModel model = new DefaultListModel();

			_baselineList = ListFacade.createList(model);
			_baselineList.setVisibleRowCount(7);
			_baselineList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			_baselineList.setCellRenderer(new ExpandPathMacroListRenderer(_parent.getProject()));

			final Component scrollPane = ScrollPaneFacade.createScrollPane(_baselineList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			_baselinePanel.add(scrollPane, "1, 1, 1, 1"); // col ,row, col, row


			final double rowsGap = 5;
			final double[][] bPanelSize = {{border, TableLayoutConstants.PREFERRED}, // Columns
										   {border, TableLayoutConstants.PREFERRED, rowsGap, TableLayoutConstants.PREFERRED, border}};// Rows
			final TableLayout btbl = new TableLayout(bPanelSize);

			final Container buttonPanel = new JPanel(btbl);
			_baselinePanel.add(buttonPanel, "3, 1, 3, 1");

			final AbstractButton addButton = new JButton();
			final Action action = new BrowseAction(_parent, "Add...", new ExtensionFileFilter(FindBugsUtil.XML_EXTENSIONS_SET), new BrowseActionCallback() {
				@Override
				public void addSelection(final File selectedFile) {
					final String replacement = IdeaUtilImpl.collapsePathMacro(_parent.getProject(), selectedFile.getAbsolutePath());
					//noinspection unchecked
					((DefaultListModel) _baselineList.getModel()).addElement(replacement);
					_preferences.getExcludeBaselineBugs().add(replacement);
					_preferences.setModified(true);
				}
			});
			addButton.setAction(action);
			buttonPanel.add(addButton, "1, 1, 1, 1");

			final AbstractButton removeButton = new JButton("Remove") {


				@Override
				public boolean isEnabled() {
					return super.isEnabled() && _baselineList.getSelectedIndex() > -1;
				}
			};
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final int index = _baselineList.getSelectedIndex();
					getModel(_baselineList).remove(index);
					_preferences.removeBaselineExcludeFilter(index);
				}
			});
			buttonPanel.add(removeButton, "1, 3, 1, 3");

			_baselineList.addListSelectionListener(new BaselineListSelectionListener(removeButton));
		}

		return _baselinePanel;
	}


	JList<?> getIncludeList() {
		if (_includeList == null) {
			getIncludePanel();
		}
		return _includeList;
	}


	JList<?> getExcludeList() {
		if (_excludeList == null) {
			getExcludePanel();
		}
		return _excludeList;
	}


	JList<?> getBaselineList() {
		if (_baselineList == null) {
			getBaseLinePanel();
		}
		return _baselineList;
	}


	private static DefaultListModel getModel(final JList<?> list) {
		return (DefaultListModel) list.getModel();
	}


	@Override
	public void setEnabled(final boolean enabled) {
		getIncludePanel().setEnabled(enabled);
		getIncludeList().setEnabled(enabled);
		getExcludePanel().setEnabled(enabled);
		getExcludeList().setEnabled(enabled);
		getBaseLinePanel().setEnabled(enabled);
		getBaselineList().setEnabled(enabled);
		// TODO set state for add- and remove-button
	}


	@Override
	public boolean showInModulePreferences() {
		return true;
	}


	@Override
	public boolean isAdvancedConfig() {
		return false;
	}


	@Override
	public String getTitle() {
		return "Filters";
	}


	@Override
	public void filter(final String filter) {
		// TODO support search
	}


	private static class IncludeListSelectionListener implements ListSelectionListener {

		private final JButton _removeButton;


		private IncludeListSelectionListener(final JButton removeButton) {
			_removeButton = removeButton;
		}


		@Override
		public void valueChanged(final ListSelectionEvent e) {
			_removeButton.setEnabled(!e.getValueIsAdjusting() && e.getFirstIndex() >= 0);

		}
	}

	private static class ExcludeListSelectionListener implements ListSelectionListener {

		private final AbstractButton _removeButton;


		private ExcludeListSelectionListener(final AbstractButton removeButton) {
			_removeButton = removeButton;
		}


		@Override
		public void valueChanged(final ListSelectionEvent e) {
			_removeButton.setEnabled(!e.getValueIsAdjusting() && e.getFirstIndex() >= 0);

		}
	}

	private static class BaselineListSelectionListener implements ListSelectionListener {

		private final AbstractButton _removeButton;


		private BaselineListSelectionListener(final AbstractButton removeButton) {
			_removeButton = removeButton;
		}


		@Override
		public void valueChanged(final ListSelectionEvent e) {
			_removeButton.setEnabled(!e.getValueIsAdjusting() && e.getFirstIndex() >= 0);
		}
	}


	private static class ExpandPathMacroListRenderer extends DefaultListCellRenderer {

		private final Project _project;


		public ExpandPathMacroListRenderer(final Project project) {
			_project = project;
		}


		@Override
		public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
			final Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			setText(IdeaUtilImpl.expandPathMacro(_project, String.valueOf(value)));
			return rendererComponent;
		}
	}
}
