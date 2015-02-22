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

import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Displays pseudo-tooltips for tree and list views which don't have enough
 * space.  This class is not NB specific, and can be used with any
 * JTree or JList.
 *
 * @author Tim Boudreau
 * @author Andre Pfeiler - made some tiny modification on positioning the buffered image rect
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"RI_REDUNDANT_INTERFACES"})
@SuppressWarnings({"AssignmentToNull", "RedundantInterfaceDeclaration"})
public final class ViewTooltips extends MouseAdapter implements MouseMotionListener {

	/** The default instance, reference counted */
	private static ViewTooltips INSTANCE;
	/** A reference count for number of comps listened to */
	private int refcount;
	/** The last known component we were invoked against, nulled on hide() */
	private JComponent inner;
	/** The last row we were invoked against */
	private int row = -1;
	/** An array of currently visible popups */
	private final Popup[] popups = new Popup[2];
	/** A component we'll reuse to paint into the popups */

	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
			value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
			justification = "because I know better")
	private final ImgComp painter = new ImgComp();


	/** Nobody should instantiate this */
	private ViewTooltips() {
	}


	/**
	 * Register a child of a JScrollPane (only JList or JTree supported
	 * for now) which should show helper tooltips.  Should be called
	 * from the component's addNotify() method.
	 *
	 * @param comp
	 */
	public static void register(final JComponent comp) {
		if (INSTANCE == null) {
			INSTANCE = new ViewTooltips();
		}
		INSTANCE.attachTo(comp);
	}


	/**
	 * Unregister a child of a JScrollPane (only JList or JTree supported
	 * for now) which should show helper tooltips. Should be called
	 * from the component's removeNotify() method.
	 *
	 * @param comp
	 */
	public static void unregister(final JComponent comp) {
		assert INSTANCE != null : "Unregister asymmetrically called";
		if (INSTANCE.detachFrom(comp) == 0) {
			INSTANCE.hide();
			INSTANCE = null;
		}
	}


	/**
	 * Start listening to mouse motion on the passed component
	 *
	 * @param comp ..
	 */
	private void attachTo(final JComponent comp) {
		assert comp instanceof JTree || comp instanceof JList;
		comp.removeMouseListener(this);
		comp.removeMouseMotionListener(this);
		comp.addMouseListener(this);
		comp.addMouseMotionListener(this);
		refcount++;
	}


	/**
	 * Stop listening to mouse motion on the passed component
	 *
	 * @param comp
	 * @return
	 */
	private int detachFrom(final JComponent comp) {
		assert comp instanceof JTree || comp instanceof JList;
		comp.removeMouseMotionListener(this);
		comp.removeMouseListener(this);
		return refcount--;
	}


	//@Override
	public void mouseMoved(final MouseEvent e) {
		Point p = e.getPoint();
		final JComponent comp = (JComponent) e.getSource();
		final JScrollPane jsp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, comp);
		if (jsp != null) {
			p = SwingUtilities.convertPoint(comp, p, jsp);
			show(jsp, p);
		}
	}


	//@Override
	public void mouseDragged(final MouseEvent e) {
		hide();
	}


	@Override
	public void mouseEntered(final MouseEvent e) {
		hide();
	}


	@Override
	public void mouseExited(final MouseEvent e) {
		hide();
	}


	/**
	 * Shows the appropriate popups given the state of the scroll pane and
	 * its view.
	 *
	 * @param view The scroll pane owning the component the event happened on
	 * @param pt   The point at which the mouse event happened, in the coordinate
	 *             space of the scroll pane.
	 */
	void show(final JScrollPane view, final Point pt) {
		if (view.getViewport().getView() instanceof JTree) {
			showJTree(view, pt);
		} else if (view.getViewport().getView() instanceof JList) {
			showJList(view, pt);
		} else {
			assert false : "Bad component type registered: " + view.getViewport().getView();
		}
	}


	private void showJList(final JScrollPane view, final Point pt) {
		final JList list = (JList) view.getViewport().getView();
		final Point p = SwingUtilities.convertPoint(view, pt.x, pt.y, list);
		final int row = list.locationToIndex(p);
		if (row == -1) {
			hide();
			return;
		}
		final Rectangle bds = list.getCellBounds(row, row);
		//GetCellBounds returns a width that is the
		//full component width;  we want only what
		//the renderer really needs.
		final ListCellRenderer ren = list.getCellRenderer();
		final Dimension rendererSize = ren.getListCellRendererComponent(list, list.getModel().getElementAt(row), row, false, false).getPreferredSize();

		bds.width = rendererSize.width;
		if (!bds.contains(p)) {
			hide();
			return;
		}
		if (setCompAndRow(list, row)) {
			final Rectangle visible = getShowingRect(view);
			final Rectangle[] rects = getRects(bds, visible);
			if (rects.length > 0) {
				ensureOldPopupsHidden();
				painter.configure(list.getModel().getElementAt(row), view, list, row);
				showPopups(rects, bds, visible, list, view);
			} else {
				hide();
			}
		}
	}


	private void showJTree(final JScrollPane view, final Point pt) {
		final JTree tree = (JTree) view.getViewport().getView();
		final Point p = SwingUtilities.convertPoint(view, pt.x, pt.y, tree);

		final int row = tree.getClosestRowForLocation(p.x, p.y);

		final TreePath path = tree.getClosestPathForLocation(p.x, p.y);

		final Rectangle bds = tree.getPathBounds(path);
		if (bds == null || !bds.contains(p)) {
			hide();
			return;
		}
		if (setCompAndRow(tree, row)) {
			final Rectangle visible = getShowingRect(view);
			final Rectangle[] rects = getRects(bds, visible);
			if (rects.length > 0) {
				ensureOldPopupsHidden();
				painter.configure(path.getLastPathComponent(), view, tree, path, row);
				showPopups(rects, bds, visible, tree, view);
			} else {
				hide();
			}
		}
	}


	/**
	 * Set the currently shown component and row, returning true if they are
	 * not the same as the last known values.
	 *
	 * @param inner
	 * @param row
	 * @return
	 */
	private boolean setCompAndRow(final JComponent inner, final int row) {
		final boolean rowChanged = row != this.row;
		final boolean compChanged = inner != this.inner;
		this.inner = inner;
		this.row = row;
		return rowChanged || compChanged;
	}


	/**
	 * Hide all popups and discard any references to the components the
	 * popups were showing for.
	 */
	void hide() {
		ensureOldPopupsHidden();
		if (painter != null) {
			painter.clear();
		}
		setHideComponent(null, null);
		inner = null;
		row = -1;
	}


	private void ensureOldPopupsHidden() {
		for (int i = 0; i < popups.length; i++) {
			if (popups[i] != null) {
				popups[i].hide();
				popups[i] = null;
			}
		}
	}


	/**
	 * Gets the sub-rectangle of a JScrollPane's area that
	 * is actually showing the view
	 *
	 * @param pane
	 * @return
	 */
	private Rectangle getShowingRect(final JScrollPane pane) {
		final Insets ins1 = pane.getViewport().getInsets();
		final Border inner = pane.getViewportBorder();
		final Insets ins2;
		if (inner != null) {
			ins2 = inner.getBorderInsets(pane);
		} else {
			ins2 = new Insets(0, 0, 0, 0);
		}
		Insets ins3 = new Insets(0, 0, 0, 0);
		if (pane.getBorder() != null) {
			ins3 = pane.getBorder().getBorderInsets(pane);
		}

		Rectangle r = pane.getViewportBorderBounds();
		r.translate(-r.x, -r.y);
		r.width -= ins1.left + ins1.right;
		r.width -= ins2.left + ins2.right;
		r.height -= ins1.top + ins1.bottom;
		r.height -= ins2.top + ins2.bottom;
		r.x -= ins2.left;
		r.x -= ins3.left;
		final Point p = pane.getViewport().getViewPosition();
		r.translate(p.x, p.y);
		r = SwingUtilities.convertRectangle(pane.getViewport(), r, pane);
		return r;
	}


	/**
	 * Fetches an array or rectangles representing the non-overlapping
	 * portions of a cell rect against the visible portion of the component.
	 *
	 * @param bds
	 * @param vis
	 * @return
	 * @bds The cell's bounds, in the coordinate space of the tree or list
	 * @vis The visible area of the tree or list, in the tree or list's coordinate space
	 */
	private static Rectangle[] getRects(final Rectangle bds, final Rectangle vis) {
		final Rectangle[] result;
		if (vis.contains(bds)) {
			result = new Rectangle[0];
		} else {
			if (bds.x < vis.x && bds.x + bds.width > vis.x + vis.width) {
				final Rectangle a = new Rectangle(bds.x, bds.y, vis.x - bds.x, bds.height);
				final Rectangle b = new Rectangle(vis.x + vis.width, bds.y, bds.x + bds.width - (vis.x + vis.width), bds.height);
				result = new Rectangle[] {a, b};
			} else if (bds.x < vis.x) {
				result = new Rectangle[] {new Rectangle(bds.x, bds.y, vis.x - bds.x, bds.height)};
			} else if (bds.x + bds.width > vis.x + vis.width) {
				result = new Rectangle[] {new Rectangle(vis.x + vis.width, bds.y, bds.x + bds.width - (vis.x + vis.width), bds.height)};
			} else {
				result = new Rectangle[0];
			}
		}
		return result;
	}


	/**
	 * Show popups for each rectangle, using the now configured painter.
	 *
	 * @param rects
	 * @param bds
	 * @param visible
	 * @param comp
	 * @param view
	 */
	private void showPopups(final Rectangle[] rects, final Rectangle bds, final Rectangle visible, final JComponent comp, final JScrollPane view) {
		boolean shown = false;
		for (int i = 0; i < rects.length; i++) {
			final Rectangle sect = rects[i];
			sect.translate(-bds.x, -bds.y);
			final ImgComp part = painter.getPartial(sect, bds.x + rects[i].x < visible.x);
			final Point pos = new Point(bds.x + rects[i].x, bds.y + rects[i].y);
			SwingUtilities.convertPointToScreen(pos, comp);
			if (comp instanceof JList) {
				//XXX off by one somewhere, only with JLists - where?
				pos.y--;
			}
			if (pos.x > 0) { //Mac OS will reposition off-screen popups to x=0,
				//so don't try to show them
				popups[i] = getPopupFactory().getPopup(view, part, pos.x, pos.y);
				popups[i].show();
				shown = true;
			}
		}
		if (shown) {
			setHideComponent(comp, view);
		} else {
			setHideComponent(null, null); //clear references
		}
	}


	private static PopupFactory getPopupFactory() {
		/*if (Utilities.isMac()) {

					// See ide/applemenu/src/org/netbeans/modules/applemenu/ApplePopupFactory
					// We have a custom PopupFactory that will consistently use
					// lightweight popups on Mac OS, since HW popups get a drop
					// shadow.  By default, popups returned when a heavyweight popup
					// is needed (SDI mode) are no-op popups, since some hacks
					// are necessary to make it really work.

					// To enable heavyweight popups which have no drop shadow
					// *most* of the time on mac os, run with
					// -J-Dnb.explorer.hw.completions=true

					// To enable heavyweight popups which have no drop shadow
					// *ever* on mac os, you need to put the cocoa classes on the
					// classpath - modify netbeans.conf to add
					// System/Library/Java on the bootclasspath.  *Then*
					// run with the above line switch and
					// -J-Dnb.explorer.hw.cocoahack=true

					PopupFactory result = (PopupFactory) Lookup.getDefault().lookup (
							PopupFactory.class);
					return result == null ? PopupFactory.getSharedInstance() : result;
				} else {*/
		return PopupFactory.getSharedInstance();
		//}
	}


	private Hider hider;


	/**
	 * Set a component (JList or JTree) which should be listened to, such that if
	 * a model, selection or scroll event occurs, all currently open popups
	 * should be hidden.
	 *
	 * @param comp
	 * @param parent
	 */
	private void setHideComponent(final JComponent comp, final JScrollPane parent) {
		if (hider != null) {
			if (hider.isListeningTo(comp)) {
				return;
			}
		}
		if (hider != null) {
			hider.detach();
		}
		if (comp != null) {
			hider = new Hider(comp, parent);
		} else {
			hider = null;
		}
	}


	/**
	 * A JComponent which creates a BufferedImage of a cell renderer and can
	 * produce clones of itself that display subrectangles of that cell
	 * renderer.
	 */
	@SuppressWarnings("SimplifiableConditionalExpression")
	private static final class ImgComp extends JComponent {

		private transient BufferedImage img;
		private Dimension d;

		private Color bg = JBColor.WHITE;
		private JScrollPane comp;

		private Object node;

		private AffineTransform at = AffineTransform.getTranslateInstance(0d, 0d);
		boolean isRight;


		ImgComp() {
		}


		/**
		 * Create a clone with a specified backing image
		 *
		 * @param img
		 * @param off
		 * @param right
		 */
		ImgComp(final BufferedImage img, final Rectangle off, final boolean right) {
			this.img = img;
			at = AffineTransform.getTranslateInstance(-off.x, 0);
			d = new Dimension(off.width, off.height);
			isRight = right;
		}


		public ImgComp getPartial(final Rectangle bds, final boolean right) {
			assert img != null;
			return new ImgComp(img, bds, right);
		}


		/**
		 * Configures a tree cell renderer and sets up sizing and the
		 * backing image from it
		 *
		 * @param nd
		 * @param tv
		 * @param tree
		 * @param path
		 * @param row
		 * @return
		 */
		public boolean configure(final Object nd, final JScrollPane tv, final JTree tree, final TreePath path, final int row) {
			//final boolean sameVn = setLastRendereredObject(nd);
			//final boolean sameComp = setLastRenderedScrollPane(tv);
			bg = tree.getBackground();
			final boolean sel = tree.isSelectionEmpty() ? false : tree.getSelectionModel().isPathSelected(path);
			final boolean exp = tree.isExpanded(path);
			final boolean leaf = !exp && tree.getModel().isLeaf(nd);
			final boolean lead = path.equals(tree.getSelectionModel().getLeadSelectionPath());
			final Component renderer = tree.getCellRenderer().getTreeCellRendererComponent(tree, nd, sel, exp, leaf, row, lead);
			if (renderer != null) {
				setComponent(renderer);
			}
			return true;
		}


		/**
		 * Configures a list cell renderer and sets up sizing and the
		 * backing image from it
		 *
		 * @param nd
		 * @param tv
		 * @param list
		 * @param row
		 * @return
		 */
		public boolean configure(final Object nd, final JScrollPane tv, final JList list, final int row) {
			//final boolean sameVn = setLastRendereredObject(nd);
			//final boolean sameComp = setLastRenderedScrollPane(tv);
			bg = list.getBackground();
			final boolean sel = list.isSelectionEmpty() ? false : list.getSelectionModel().isSelectedIndex(row);
			final Component renderer = list.getCellRenderer().getListCellRendererComponent(list, nd, row, sel, false);
			if (renderer != null) {
				setComponent(renderer);
			}
			return true;
		}


		void clear() {
			comp = null;
			node = null;
		}


		/**
		 * Set the cell renderer we will proxy.
		 *
		 * @param jc
		 */
		public void setComponent(final Component jc) {
			final Dimension d = jc.getPreferredSize();
			final BufferedImage nue = UIUtil.createImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB_PRE);// was height +2
			SwingUtilities.paintComponent(nue.getGraphics(), jc, this, 0, 0, d.width, d.height); // was height +2
			setImage(nue);
		}


		@Override
		public Rectangle getBounds() {
			final Dimension dd = getPreferredSize();
			return new Rectangle(0, 0, dd.width, dd.height);
		}


		private void setImage(final BufferedImage img) {
			this.img = img;
			d = null;
		}


		@Override
		public Dimension getPreferredSize() {
			if (d == null) {
				d = new Dimension(img.getWidth(), img.getHeight());
			}
			return d;
		}


		@Override
		public Dimension getSize() {
			return getPreferredSize();
		}


		@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
				value = "BC_UNCONFIRMED_CAST",
				justification = "")
		@Override
		public void paint(final Graphics g) {
			g.setColor(bg);
			g.fillRect(0, 0, d.width, d.height);
			final Graphics2D g2d = (Graphics2D) g;
			g2d.drawRenderedImage(img, at);
			g.setColor(JBColor.BLACK);
			g.drawLine(0, 0, d.width, 0);
			g.drawLine(0, d.height - 1, d.width, d.height - 1);
			if (isRight) {
				g.drawLine(0, 0, 0, d.height - 1);
			} else {
				g.drawLine(d.width - 1, 0, d.width - 1, d.height - 1);
			}
		}


		@Override
		public void firePropertyChange(final String s, final Object a, final Object b) {
		}


		@Override
		public void invalidate() {
		}


		@Override
		public void validate() {
		}


		@Override
		public void revalidate() {
		}
	}

	/**
	 * A listener that listens to just about everything in the known universe
	 * and hides all currently displayed popups if anything happens.
	 */
	private static final class Hider implements ChangeListener, PropertyChangeListener, TreeModelListener, TreeSelectionListener, HierarchyListener, HierarchyBoundsListener, ListSelectionListener, ListDataListener, ComponentListener {

		private final JTree tree;

		private final JScrollPane pane;
		private final JList list;


		@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
				value = "BC_UNCONFIRMED_CAST",
				justification = "")
		private Hider(final JComponent comp, final JScrollPane pane) {
			if (comp instanceof JTree) {
				tree = (JTree) comp;
				list = null;
			} else {
				list = (JList) comp;
				tree = null;
			}
			assert tree != null || list != null;
			this.pane = pane;
			attach();
		}


		private boolean isListeningTo(final JComponent comp) {
			return !detached && (comp == list || comp == tree);
		}


		private void attach() {
			if (tree != null) {
				tree.getModel().addTreeModelListener(this);
				tree.getSelectionModel().addTreeSelectionListener(this);
				tree.addHierarchyBoundsListener(this);
				tree.addHierarchyListener(this);
				tree.addComponentListener(this);
			} else {
				list.getSelectionModel().addListSelectionListener(this);
				list.getModel().addListDataListener(this);
				list.addHierarchyBoundsListener(this);
				list.addHierarchyListener(this);
				list.addComponentListener(this);
			}
			pane.getHorizontalScrollBar().getModel().addChangeListener(this);
			pane.getVerticalScrollBar().getModel().addChangeListener(this);
			KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(this);
		}


		private boolean detached;


		private void detach() {
			KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(this);
			if (tree != null) {
				tree.getSelectionModel().removeTreeSelectionListener(this);
				tree.getModel().removeTreeModelListener(this);
				tree.removeHierarchyBoundsListener(this);
				tree.removeHierarchyListener(this);
				tree.removeComponentListener(this);
			} else {
				list.getSelectionModel().removeListSelectionListener(this);
				list.getModel().removeListDataListener(this);
				list.removeHierarchyBoundsListener(this);
				list.removeHierarchyListener(this);
				list.removeComponentListener(this);
			}
			pane.getHorizontalScrollBar().getModel().removeChangeListener(this);
			pane.getVerticalScrollBar().getModel().removeChangeListener(this);
			detached = true;
		}


		private void change() {
			if (ViewTooltips.INSTANCE != null) {
				ViewTooltips.INSTANCE.hide();
			}
			detach();
		}


		public void propertyChange(final PropertyChangeEvent evt) {
			change();
		}


		public void treeNodesChanged(final TreeModelEvent e) {
			change();
		}


		public void treeNodesInserted(final TreeModelEvent e) {
			change();
		}


		public void treeNodesRemoved(final TreeModelEvent e) {
			change();
		}


		public void treeStructureChanged(final TreeModelEvent e) {
			change();
		}


		public void hierarchyChanged(final HierarchyEvent e) {
			change();
		}


		public void valueChanged(final TreeSelectionEvent e) {
			change();
		}


		public void ancestorMoved(final HierarchyEvent e) {
			change();
		}


		public void ancestorResized(final HierarchyEvent e) {
			change();
		}


		public void stateChanged(final ChangeEvent e) {
			change();
		}


		public void valueChanged(final ListSelectionEvent e) {
			change();
		}


		public void intervalAdded(final ListDataEvent e) {
			change();
		}


		public void intervalRemoved(final ListDataEvent e) {
			change();
		}


		public void contentsChanged(final ListDataEvent e) {
			change();
		}


		public void componentResized(final ComponentEvent e) {
			change();
		}


		public void componentMoved(final ComponentEvent e) {
			change();
		}


		public void componentShown(final ComponentEvent e) {
			change();
		}


		public void componentHidden(final ComponentEvent e) {
			change();
		}
	}
}