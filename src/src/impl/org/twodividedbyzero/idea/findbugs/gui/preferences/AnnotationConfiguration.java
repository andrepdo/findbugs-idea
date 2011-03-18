/*
 * Copyright 2010 Andre Pfeiler
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

import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.ui.ColorChooser;
import com.intellij.ui.UIBundle;
import com.intellij.util.ui.UIUtil;
import info.clearthought.layout.TableLayout;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings({"SE_TRANSIENT_FIELD_NOT_RESTORED", "SE_BAD_FIELD", "SIC_INNER_SHOULD_BE_STATIC_ANON"})
public class AnnotationConfiguration implements ConfigurationPage {

	private AnnotationTypePanel _annotationTypePanel;

	enum AnnotationType {

		HighPriority(HighlightSeverity.ERROR, Color.RED, Color.WHITE, Color.RED, EffectType.WAVE_UNDERSCORE, Font.BOLD),
		NormalPriority(HighlightSeverity.WARNING, Color.BLACK, Color.WHITE, Color.YELLOW.darker(), EffectType.WAVE_UNDERSCORE, Font.ITALIC),
		ExpPriority(HighlightSeverity.INFO, Color.BLACK, Color.WHITE, Color.GRAY, EffectType.WAVE_UNDERSCORE, Font.PLAIN),
		LowPriority(HighlightSeverity.INFO, Color.BLACK, Color.WHITE, Color.GREEN, EffectType.BOXED, Font.PLAIN),
		IgnorePriority(HighlightSeverity.INFO, Color.BLACK, Color.WHITE, Color.MAGENTA.darker().darker(), EffectType.WAVE_UNDERSCORE, Font.PLAIN);

		private final HighlightSeverity _severity;
		private final Color _foregroundColor;
		private final Color _backgroundColor;
		private final Color _effectColor;
		private final EffectType _effectType;
		private final int _font;


		AnnotationType(final HighlightSeverity severity, final Color foregroundColor, final Color backgroundColor, final Color effectColor, final EffectType effectType, final int font) {
			_severity = severity;
			_foregroundColor = foregroundColor;
			_backgroundColor = backgroundColor;
			_effectColor = effectColor;
			_effectType = effectType;
			_font = font;
		}


		public HighlightSeverity getSeverity() {
			return _severity;
		}


		public Color getForegroundColor() {
			return _foregroundColor;
		}


		public Color getBackgroundColor() {
			return _backgroundColor;
		}


		public Color getEffectColor() {
			return _effectColor;
		}


		public EffectType getEffectType() {
			return _effectType;
		}


		public int getFont() {
			return _font;
		}


		public static List<EffectType> getEffectTypes() {
			final List<EffectType> result = new ArrayList<EffectType>();
			for (final AnnotationType annotationType : values()) {
				result.add(annotationType.getEffectType());
			}
			return result;
		}
	}


	private final FindBugsPreferences _preferences;
	private final ConfigurationPanel _parent;
	private Component _component;
	private JPanel _mainPanel;
	private JTextField _annotationPathField;
	private JCheckBox _enableGutterIcon;
	private JCheckBox _enableTextRangeMarkUp;
	private JPanel _annotationPathPanel;
	private JPanel _markUpPanel;
	private JPanel _typeSettingsPanel;
	private JList _annotationTypeList;


	public AnnotationConfiguration(final ConfigurationPanel parent, final FindBugsPreferences preferences) {
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_preferences = preferences;
		_parent = parent;
	}


	public Component getComponent() {
		if (_component == null) {
			final double border = 5;
			final double rowsGap = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayout.FILL, border}, // Columns
									 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);

			final Container mainPanel = new JPanel(tbl);
			mainPanel.add(getMarkUpPanel(), "1, 1, 1, 1");
			mainPanel.add(getAnnotationPathPanel(), "1, 3, 1, 3");

			_component = mainPanel;
		}
		//updatePreferences();
		return _component;
	}


	public void updatePreferences() {
		clearModels();
		syncModels();
	}


	private void syncModels() {
		final String annotationSuppressWarningName = _preferences.getProperty(FindBugsPreferences.ANNOTATION_SUPPRESS_WARNING_CLASS);
		getAnnotationPathField().setText(annotationSuppressWarningName);
		/*for (final String s : _preferences.getPlugins()) {
			getModel(getPluginList()).addElement(s);
		}*/
	}


	private void clearModels() {
		getAnnotationPathField().setText("");
		//getModel(getPluginList()).clear();
	}


	JPanel getAnnotationPathPanel() {
		if (_annotationPathPanel == null) {

			final double border = 5;
			final double colsGap = 10;
			final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.PREFERRED, border}, // Columns
									 {border, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_annotationPathPanel = new JPanel(tbl);
			_annotationPathPanel.setBorder(BorderFactory.createTitledBorder("FindBugs Annotation class (@SuppressWarning) need to be on the classpath"));

			_annotationPathPanel.add(getAnnotationPathField(), "1, 1, 1, 1"); // col ,row, col, row


			final double rowsGap = 5;
			final double[][] bPanelSize = {{border, TableLayout.PREFERRED}, // Columns
										   {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tableLayout = new TableLayout(bPanelSize);

			final Container buttonPanel = new JPanel(tableLayout);
			_annotationPathPanel.add(buttonPanel, "3, 1, 3, 1");


			/*final AbstractButton addButton = new JButton();
			final Action action = new BrowseAction(_parent, "Browse...", new ExtensionFileFilter(Collections.singletonMap(".java")), new BrowseActionCallback() {
				public void addSelection(final File selectedFile) {

					_preferences.setModified(true);
				}
			});*/
			/*addButton.setAction(action);
			buttonPanel.add(addButton, "1, 1, 1, 1");*/


		}

		return _annotationPathPanel;
	}


	private JTextField getAnnotationPathField() {
		if (_annotationPathField == null) {
			_annotationPathField = new JTextField(30);
			_annotationPathField.setEditable(true);
			_annotationPathField.getDocument().addDocumentListener(new DocumentListener() {
				public void insertUpdate(final DocumentEvent e) {
					_preferences.setProperty(FindBugsPreferences.ANNOTATION_SUPPRESS_WARNING_CLASS, _annotationPathField.getText());
				}


				public void removeUpdate(final DocumentEvent e) {
				}


				public void changedUpdate(final DocumentEvent e) {
					_preferences.setProperty(FindBugsPreferences.ANNOTATION_SUPPRESS_WARNING_CLASS, _annotationPathField.getText());
				}
			});
		}
		return _annotationPathField;
	}


	JPanel getMarkUpPanel() {
		if (_markUpPanel == null) {
			final double border = 5;
			final double colsGap = 10;
			final double rowsGap = 15;
			final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.PREFERRED, border}, // Columns
									 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_markUpPanel = new JPanel(tbl);
			_markUpPanel.setBorder(BorderFactory.createTitledBorder("Annotation/MarkUp Settings"));

			_markUpPanel.add(getGutterIconCheckbox(), "1, 1, 1, 1"); // col ,row, col, row
			//_markUpPanel.add(getTextRangeMarkupCheckbox(), "1, 3, 1, 3"); // col ,row, col, row
			_markUpPanel.add(getAnnotationTypeSettingsPanel(), "1, 3, 1, 3"); // col ,row, col, row

		}

		return _markUpPanel;
	}


	private JPanel getAnnotationTypeSettingsPanel() {
		if (_typeSettingsPanel == null) {
			final double border = 5;
			final double colsGap = 10;
			final double rowsGap = 5;
			final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.PREFERRED, border}, // Columns
									 {border, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			_typeSettingsPanel = new JPanel(tbl);
			_typeSettingsPanel.setBorder(BorderFactory.createTitledBorder("Annotation type settings"));

			_typeSettingsPanel.add(getTextRangeMarkupCheckbox(), "1, 1, 3, 1"); // col ,row, col, row
			_typeSettingsPanel.add(getAnnotationTypeList(), "1, 3, 1, 3");
			_typeSettingsPanel.add(getAnnotationTypePanel() , "3, 3, 3, 3");
			getAnnotationTypeList().setSelectedIndex(0);

		}
		return _typeSettingsPanel;
	}

	private JList getAnnotationTypeList() {
		if (_annotationTypeList == null) {
			_annotationTypeList = new JList(AnnotationType.values());
			_annotationTypeList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(final ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
						getAnnotationTypePanel().setAnnotationType((AnnotationType) _annotationTypeList.getSelectedValue());
					}
				}
			});
		}
		return _annotationTypeList;
	}


	private JCheckBox getTextRangeMarkupCheckbox() {
		if (_enableTextRangeMarkUp == null) {
			_enableTextRangeMarkUp = new JCheckBox("Enable editor TextRange markup");
			_enableTextRangeMarkUp.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					final boolean selected = e.getStateChange() == ItemEvent.SELECTED;
					getAnnotationTypePanel().setEnabled(selected);
					getAnnotationTypeList().setEnabled(selected);
					//_preferences.setProperty(FindBugsPreferences.EXPORT_AS_XML, e.getStateChange() == ItemEvent.SELECTED);
				}
			});
		}
		return _enableTextRangeMarkUp;
	}


	private JCheckBox getGutterIconCheckbox() {
		if (_enableGutterIcon == null) {
			_enableGutterIcon = new JCheckBox("Enable editor GutterIcon markup");
			_enableGutterIcon.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					//_preferences.setProperty(FindBugsPreferences.EXPORT_AS_XML, e.getStateChange() == ItemEvent.SELECTED);
				}
			});
		}
		return _enableGutterIcon;
	}


	public void setEnabled(final boolean enabled) {
		getAnnotationTypePanel().setEnabled(enabled);
		getAnnotationTypeList().setEnabled(enabled);
		getMarkUpPanel().setEnabled(enabled);
		getAnnotationPathPanel().setEnabled(enabled);
		getAnnotationPathField().setEditable(enabled);

	}


	public boolean showInModulePreferences() {
		return false;
	}


	public boolean isAdvancedConfig() {
		return true;
	}


	public String getTitle() {
		return "Annotations";
	}


	private AnnotationTypePanel getAnnotationTypePanel() {
		if (_annotationTypePanel == null) {
			_annotationTypePanel = new AnnotationTypePanel(this, AnnotationType.HighPriority);
		}
		return _annotationTypePanel;
	}


	private static class AnnotationTypePanel extends JPanel {

		private final AnnotationConfiguration _configuration;

		private JCheckBox _plainBox;
		private JCheckBox _italicBox;
		private JCheckBox _boldBox;
		private ColorBox _foreground;
		private ColorBox _background;
		private ColorBox _effectTypeColor;
		private JComboBox _typeComboBox;
		private AnnotationType _annotationType;


		private AnnotationTypePanel(final AnnotationConfiguration configuration, final AnnotationType annotationType) {
			_configuration = configuration;
			_annotationType = annotationType;
			initGui();
		}


		private void initGui() {
			final double border = 5;
			final double colsGap = 10;
			final double rowsGap = 5;
			final double[][] size = {{border, TableLayout.PREFERRED, colsGap, TableLayout.FILL,  border}, // Columns
									 {border, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, rowsGap, TableLayout.PREFERRED, border}};// Rows
			final TableLayout tbl = new TableLayout(size);
			setLayout(tbl);

			final JPanel fontPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			_plainBox = new JCheckBox("plain");
			_plainBox.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					//TODO: implement
				}
			});
			fontPanel.add(_plainBox);

			_italicBox = new JCheckBox("italic");
			_italicBox.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					//TODO: implement
				}
			});
			fontPanel.add(_italicBox);

			_boldBox = new JCheckBox("bold");
			_boldBox.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					//TODO: implement
				}
			});
			fontPanel.add(_boldBox);

			final ButtonGroup group = new ButtonGroup();
			group.add(_plainBox);
			group.add(_italicBox);
			group.add(_boldBox);

			add(fontPanel, "1, 1, 3, 1");

			_foreground = new ColorBox(this, _annotationType.getForegroundColor(), 24, true);
			add(_foreground, "1, 3, 1, 3");
			add(new JLabel("Foreground"), "3, 3, 3, 3");

			_background = new ColorBox(this, _annotationType.getBackgroundColor(), 24, true);
			add(_background, "1, 5, 1, 5");
			add(new JLabel("Background"), "3, 5, 3, 5");


			final JPanel effectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			_typeComboBox = new JComboBox(EffectType.values());
			_typeComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					//TODO: implement
				}
			});
			_typeComboBox.setSelectedItem(_annotationType);

			_effectTypeColor = new ColorBox(this, _annotationType.getEffectColor(), 24, true);
			effectPanel.add(_effectTypeColor);
			effectPanel.add(new JLabel(" Effect"));
			effectPanel.add(_typeComboBox);

			add(effectPanel, "1, 7, 3, 7");
		}


		public void setAnnotationType(final AnnotationType annotationType) {
			_annotationType = annotationType;
			_foreground.setColor(annotationType.getForegroundColor());
			_background.setColor(annotationType.getBackgroundColor());
			_effectTypeColor.setColor(annotationType.getEffectColor());
			_typeComboBox.setSelectedItem(annotationType.getEffectType());
			final int fontType = annotationType.getFont();
			switch (fontType) {
				case Font.BOLD :
					_boldBox.setSelected(true);
					_italicBox.setSelected(false);
					_plainBox.setSelected(false);
					break;
				case Font.ITALIC :
					_boldBox.setSelected(false);
					_italicBox.setSelected(true);
					_plainBox.setSelected(false);
					break;
				case Font.PLAIN :
				default:
					_boldBox.setSelected(false);
					_italicBox.setSelected(false);
					_plainBox.setSelected(true);
			}

		}


		@Override
		public void setEnabled(final boolean enabled) {
			super.setEnabled(enabled);
			_foreground.setEnabled(enabled);
			_background.setEnabled(enabled);
			_effectTypeColor.setEnabled(enabled);
			_typeComboBox.setEnabled(enabled);
			_plainBox.setEnabled(enabled);
			_boldBox.setEnabled(enabled);
			_italicBox.setEnabled(enabled);
		}
	}


	private static class ColorBox extends JComponent {

		public static final String RGB = "RGB";
		public static final Color DISABLED_COLOR = UIUtil.getPanelBackgound();

		private final Dimension _size;
		private final boolean _isSelectable;
		private Runnable _selectColorAction;
		private Color _color;
		private final JComponent _parent;


		private ColorBox(final JComponent parent, final Color color, final int size, final boolean isSelectable) {
			_parent = parent;
			_size = new Dimension(size, size);
			_isSelectable = isSelectable;
			_color = color;
			setBorder(BorderFactory.createLineBorder(Color.GRAY));

			updateToolTip();
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(final MouseEvent mouseevent) {
					if (!isEnabled()) {
						return;
					}
					if (mouseevent.isPopupTrigger()) {
						selectColor();
					}
				}


				@Override
				public void mousePressed(final MouseEvent mouseevent) {
					if (!isEnabled()) {
						return;
					}
					if (mouseevent.getClickCount() == 2) {
						selectColor();
					} else {
						if (SwingUtilities.isLeftMouseButton(mouseevent)) {
							//setSelectedColor(myColor);
							//fireActionEvent();
						} else {
							if (mouseevent.isPopupTrigger()) {
								selectColor();
							}
						}
					}
				}
			});
		}


		public void setSelectColorAction(final Runnable selectColorAction) {
			_selectColorAction = selectColorAction;
		}


		private void selectColor() {
			if (_isSelectable) {
				final Color color = ColorChooser.chooseColor(_parent, UIBundle.message("color.panel.select.color.dialog.description"), _color);
				if (color != null) {
					setColor(color);
					if (_selectColorAction != null) {
						_selectColorAction.run();
					}
				}
			}
		}


		@Override
		public Dimension getMinimumSize() {
			return _size;
		}


		@Override
		public Dimension getMaximumSize() {
			return _size;
		}


		@Override
		public Dimension getPreferredSize() {
			return _size;
		}


		@Override
		public void paintComponent(final Graphics g) {
			if (isEnabled()) {
				g.setColor(_color);
			} else {
				g.setColor(DISABLED_COLOR);
			}
			g.fillRect(0, 0, getWidth(), getHeight());
		}


		private void updateToolTip() {
			if (_color == null) {
				return;
			}
			final StringBuilder buffer = new StringBuilder(64);
			buffer.append(RGB + ": ");
			buffer.append(_color.getRed());
			buffer.append(", ");
			buffer.append(_color.getGreen());
			buffer.append(", ");
			buffer.append(_color.getBlue());

			if (_isSelectable) {
				buffer.append(" (").append(UIBundle.message("color.panel.right.click.to.customize.tooltip.suffix")).append(")");
			}
			setToolTipText(buffer.toString());
		}


		public void setColor(final Color color) {
			_color = color;
			updateToolTip();
			repaint();
		}


		public Color getColor() {
			return _color;
		}
	}

}
