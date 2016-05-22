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
package org.twodividedbyzero.idea.findbugs.common.util;

import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.TableUtil;
import com.intellij.ui.border.IdeaTitledBorder;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.UIUtil;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.Detector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.ExtendedProblemDescriptor;
import org.twodividedbyzero.idea.findbugs.resources.GuiResources;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public final class GuiUtil {

	private static final String DESKTOP_PROPERTY_AWT_FONT_DESKTOP_HINTS = "awt.font.desktophints";
	public static final boolean HiDPI;
	public static final int SCALE_FACTOR;

	static {
		if (UIUtil.isRetina()) {
			HiDPI = true;
		} else {
			boolean isHiDPI = false;
			try {
				// JBUI was introduced with IDEA 14
				final Class<?> class_JBUI = Class.forName("com.intellij.util.ui.JBUI");
				final Method method_isHiDPI = class_JBUI.getDeclaredMethod("isHiDPI");
				isHiDPI = (Boolean) method_isHiDPI.invoke(null);
			} catch (ClassNotFoundException e) { // ; ignore
			} catch (NoSuchMethodException e) { // ; ignore
			} catch (InvocationTargetException e) { // ; ignore
			} catch (IllegalAccessException e) { // ; ignore
			}
			HiDPI = isHiDPI;
		}
		SCALE_FACTOR = HiDPI ? 2 : 1;
	}

	private GuiUtil() {
		throw new UnsupportedOperationException();
	}

	public static void navigateToElement(final PsiElement psiElement) {
		final PsiElement navigationElement = psiElement.getNavigationElement();
		if (navigationElement != null && navigationElement instanceof Navigatable && ((Navigatable) navigationElement).canNavigate()) {
			((Navigatable) navigationElement).navigate(true);
		}
	}

	@Nullable
	public static Component getScrollPane(final Component innerComponent) {
		Component component = innerComponent;
		if (innerComponent instanceof JScrollPane) {
			return innerComponent;
		}
		if (component.getParent() != null && component.getParent().getParent() != null && component.getParent().getParent() instanceof JScrollPane) {
			component = component.getParent().getParent();
			return component;
		} else {
			return null;
		}
	}

	@SuppressWarnings({"RawUseOfParameterizedType"})
	public static void configureGraphics(final Graphics graphics) {
		if (graphics instanceof Graphics2D) {
			final Graphics2D graphics2D = (Graphics2D) graphics;
			// http://download.oracle.com/docs/cd/E17409_01/javase/6/docs/technotes/guides/2d/flags.html#aaFonts
			// http://download.oracle.com/docs/cd/E17409_01/javase/6/docs/api/java/awt/doc-files/DesktopProperties.html
			final Toolkit tk = Toolkit.getDefaultToolkit();
			final Map map = (Map) tk.getDesktopProperty(DESKTOP_PROPERTY_AWT_FONT_DESKTOP_HINTS);
			if (map != null) {
				graphics2D.addRenderingHints(map);
			} else {
				graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			}
		}
	}

	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"DB_DUPLICATE_SWITCH_CLAUSES"})
	@Nullable
	public static Icon getIcon(final ExtendedProblemDescriptor problemDescriptor) {
		final BugInstance bugInstance = problemDescriptor.getBug().getInstance();
		final int priority = bugInstance.getPriority();
		final Icon icon;
		switch (priority) {
			case Detector.HIGH_PRIORITY:
				icon = GuiResources.PRIORITY_HIGH_ICON;
				break;
			case Detector.NORMAL_PRIORITY:
				icon = GuiResources.PRIORITY_NORMAL_ICON;
				break;
			case Detector.LOW_PRIORITY:
				icon = GuiResources.PRIORITY_LOW_ICON;
				break;
			case Detector.EXP_PRIORITY:
				icon = GuiResources.PRIORITY_EXP_ICON;
				break;
			case Detector.IGNORE_PRIORITY:
			default:
				icon = GuiResources.PRIORITY_HIGH_ICON;
				break;

		}
		return icon;
	}

	@SuppressWarnings("HardcodedFileSeparator")
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"DB_DUPLICATE_SWITCH_CLAUSES"})
	@Nullable
	public static Icon getTinyIcon(final ExtendedProblemDescriptor problemDescriptor) {
		final BugInstance bugInstance = problemDescriptor.getBug().getInstance();
		final int priority = bugInstance.getPriority();
		final Icon icon;
		switch (priority) {
			case Detector.HIGH_PRIORITY:
				icon = ResourcesLoader.loadIcon("priority/bug_high_tiny.png");
				break;
			case Detector.NORMAL_PRIORITY:
				icon = ResourcesLoader.loadIcon("priority/bug_normal_tiny.png");
				break;
			case Detector.LOW_PRIORITY:
				icon = ResourcesLoader.loadIcon("priority/bug_low_tiny.png");
				break;
			case Detector.EXP_PRIORITY:
				icon = ResourcesLoader.loadIcon("priority/bug_exp_tiny.png");
				break;
			case Detector.IGNORE_PRIORITY:
			default:
				icon = ResourcesLoader.loadIcon("priority/bug_high_tiny.png");
				break;

		}
		return icon;
	}

	@SuppressWarnings("HardcodedFileSeparator")
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"DB_DUPLICATE_SWITCH_CLAUSES"})
	@Nullable
	public static Icon getCombinedIcon(final ExtendedProblemDescriptor problemDescriptor) {
		final BugInstance bugInstance = problemDescriptor.getBug().getInstance();
		final int priority = bugInstance.getPriority();
		final Icon icon;
		switch (priority) {
			case Detector.HIGH_PRIORITY:
				icon = ResourcesLoader.loadIcon("intentions/bugBulbHigh.png");
				break;
			case Detector.NORMAL_PRIORITY:
				icon = ResourcesLoader.loadIcon("intentions/bugBulbNormal.png");
				break;
			case Detector.LOW_PRIORITY:
				icon = ResourcesLoader.loadIcon("intentions/bugBulbLow.png");
				break;
			case Detector.EXP_PRIORITY:
				icon = ResourcesLoader.loadIcon("intentions/bugBulbExt.png");
				break;
			case Detector.IGNORE_PRIORITY:
			default:
				icon = ResourcesLoader.loadIcon("intentions/bugBulbHigh.png");
				break;

		}
		return icon;
	}

	@NotNull
	public static Component createVerticalStrut(final int height) {
		return new Box.Filler(new Dimension(0, height), new Dimension(0, height), new Dimension(Short.MAX_VALUE, height));
	}

	@NotNull
	public static JPanel newFlowPane(@NotNull final Component... components) {
		final JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEFT));
		for (final Component component : components) {
			ret.add(component);
		}
		return ret;
	}

	@NotNull
	public static IdeaTitledBorder createTitledBorder(@NotNull final String title) {
		return IdeBorderFactory.createTitledBorder(title, true,
				new Insets(IdeBorderFactory.TITLED_BORDER_TOP_INSET,
						0,
						IdeBorderFactory.TITLED_BORDER_BOTTOM_INSET,
						IdeBorderFactory.TITLED_BORDER_RIGHT_INSET));
	}

	@NotNull
	public static JBTable createCheckboxTable(
			@NotNull final TableModel model,
			final int checkboxColumn,
			@NotNull final ActionListener swapEnabled
	) {
		final JBTable ret = new JBTable() {
			@Override
			public void setRowHeight(final int rowHeight) {
				// do not allow this ; JTable#initializeLocalVars set a default value which will set JBTable#myRowHeightIsExplicitlySet
			}
		};
		ret.setModel(model);
		ret.setShowGrid(false);
		ret.setIntercellSpacing(new Dimension(0, 0));
		ret.setTableHeader(null);
		ret.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		ret.setColumnSelectionAllowed(false);
		final TableColumnModel columnModel = ret.getColumnModel();
		final TableColumn column = columnModel.getColumn(checkboxColumn);
		TableUtil.setupCheckboxColumn(column);
		ret.registerKeyboardAction(
				new ActionListener() {
					@Override
					public void actionPerformed(@NotNull final ActionEvent e) {
						final int[] rows = ret.getSelectedRows();
						if (rows.length > 0) {
							swapEnabled.actionPerformed(e);
							final int lastRow = rows[rows.length - 1];
							ret.setRowSelectionInterval(lastRow, lastRow);
						}
					}
				},
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
				JComponent.WHEN_FOCUSED
		);
		return ret;
	}
}
