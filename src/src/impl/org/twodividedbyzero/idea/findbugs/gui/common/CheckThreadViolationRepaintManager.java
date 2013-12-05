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

/*
 * This is a modified copy of https://swinghelper.dev.java.net/files/documents/4699/131797/CheckThreadViolationRepaintManager.java
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.twodividedbyzero.idea.findbugs.gui.common;


import com.intellij.openapi.diagnostic.Logger;

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * <p>This class is used to detect Event Dispatch Thread rule violations<br> See <a
 * href="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How to Use Threads</a> for more info</p>
 * <p/> <p>This is a modification of original idea of Scott Delap<br> Initial version of ThreadCheckingRepaintManager
 * can be found here<br> <a href="http://www.clientjava.com/blog/2004/08/20/1093059428000.html">Easily Find Swing
 * Threading Mistakes</a> </p>
 *
 * @author Scott Delap
 * @author Alexander Potochkin
 *         <p/>
 *         https://swinghelper.dev.java.net/
 */
public class CheckThreadViolationRepaintManager extends RepaintManager {

	private static final Logger LOGGER = Logger.getInstance(CheckThreadViolationRepaintManager.class.getName());

	private static final boolean DEFAULT_MODE = false;
	private static final Set<StackTraceElement> FALSE_POSITIVE;

	private static boolean _installed;


	static {
		FALSE_POSITIVE = new HashSet<StackTraceElement>();
		final StackTraceElement[] ignore_these = {new StackTraceElement("javax.swing.JLabel", "setText", null, -1)};
		FALSE_POSITIVE.addAll(Arrays.asList(ignore_these));
	}


	// it is recommended to pass the complete check
	private final boolean _completeCheck;
	private WeakReference<JComponent> _lastComponent;


	public static synchronized void install() {
		if (_installed) {
			LOGGER.warn("ignoring duplicate installation of EDT violation checker");
		} else {
			RepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager(DEFAULT_MODE));
			LOGGER.info("installed EDT violation checker, complete check = " + DEFAULT_MODE);
			_installed = true;
		}
	}


	public CheckThreadViolationRepaintManager(final boolean completeCheck) {
		_completeCheck = completeCheck;
	}


	public CheckThreadViolationRepaintManager() {
		this(true);
	}


	public boolean isCompleteCheck() {
		return _completeCheck;
	}


	@Override
	public synchronized void addInvalidComponent(final JComponent component) {
		checkThreadViolations(component);
		super.addInvalidComponent(component);
	}


	@Override
	public void addDirtyRegion(final JComponent component, final int x, final int y, final int w, final int h) {
		checkThreadViolations(component);
		super.addDirtyRegion(component, x, y, w, h);
	}


	private void checkThreadViolations(final JComponent c) {
		if (!SwingUtilities.isEventDispatchThread() && (_completeCheck || c.isShowing())) {
			boolean repaint = false;
			boolean fromSwing = false;
			final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			for (final StackTraceElement st : stackTrace) {
				if (FALSE_POSITIVE.contains(st)) {
					return;
				}
				if ("javax.swing.JLabel".equals(st.getClassName()) && "setText".equals(st.getMethodName())) {
					//JLabel.setText is ok
					return;
				}
				if ("repaint".equals(st.getMethodName())) {
					repaint = true;
					fromSwing = false;
				}
				if (repaint && st.getClassName().startsWith("javax.swing.")) {
					fromSwing = true;
				}
				if (repaint && "imageUpdate".equals(st.getMethodName())) {
					//assuming it is java.awt.image.ImageObserver.imageUpdate(...)
					//image was asynchronously updated, that's ok
					return;
				}
			}
			if (repaint && !fromSwing) {
				//no problems here, since repaint() is thread safe
				return;
			}
			//ignore the last processed component
			if ((_lastComponent != null) && (c == _lastComponent.get())) {
				return;
			}
			_lastComponent = new WeakReference<JComponent>(c);
			violationFound(c);
		}
	}


	@SuppressWarnings({"ThrowableInstanceNeverThrown"})
	protected void violationFound(final JComponent c) {
		LOGGER.warn("EDT violation detected for " + c.getClass() + ' ' + c, new RuntimeException());
	}

}