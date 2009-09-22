/**
 * Copyright 2009 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.common.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
public abstract class AbstractBar extends JToolBar {

	private String _name;

	/** Specifies that the buttons should be aligned along the x axis. */
	public static final int X_AXIS = BoxLayout.X_AXIS;

	/** Specifies that the buttons should be aligned along the y axis. */
	public static final int Y_AXIS = BoxLayout.Y_AXIS;

	private Map<String, Action> _actionComponents;
	//private Map<String, JComponent> _actionComponents;


	public AbstractBar(final int orientation, final boolean floatable) {
		super(orientation);
		installListener();
		setFloatable(floatable);
		_actionComponents = Collections.emptyMap();
	}


	public AbstractBar(final int orientation) {
		this(orientation, false);
	}


	public AbstractBar() {
		this("");
	}


	public AbstractBar(final String name) {
		this(name, Collections.<String, Action>emptyMap(), X_AXIS, true);
	}


	public AbstractBar(final String name, final int axisAlignment) {
		this(name, Collections.<String, Action>emptyMap(), axisAlignment, true);
	}


	public AbstractBar(final String name, final Map<String, Action> actionComponents, final int axisAlignment, final boolean floatable) {
		super(name);
		installListener();
		_actionComponents = new LinkedHashMap<String, Action>(actionComponents);
		setLayout(new BoxLayout(this, axisAlignment));
		//final Box box = Box.createHorizontalBox();
		setFloatable(floatable);
	}


	public void installListener() {
		addPropertyChangeListener(new java.beans.PropertyChangeListener() {
			// This method is called whenever the orientation of the toolbar is changed
			public void propertyChange(final java.beans.PropertyChangeEvent evt) {
				final String propName = evt.getPropertyName();
				if ("orientation".equals(propName)) { // NON-NLS
					// Get the old orientation
					@SuppressWarnings({"UnusedDeclaration"})
					final Integer oldValue = (Integer) evt.getOldValue();

					// Get the new orientation
					final Integer newValue = (Integer) evt.getNewValue();

					if (newValue == JToolBar.HORIZONTAL) {
						// toolbar now has horizontal orientation
					} else {
						// toolbar now has vertical orientation
					}
				}
			}
		});
	}


	@Override
	public String getName() {
		return _name;
	}


	@Override
	public void setName(final String name) {
		_name = name;
	}


	@Override
	public void setOrientation(final int o) {
		super.setOrientation(o);
	}


	@Override
	public void setPreferredSize(final Dimension preferredSize) {
		super.setPreferredSize(preferredSize);
	}


	@Override
	public void setFloatable(final boolean floatable) {
		super.setFloatable(floatable);
	}


	public Map<String, Action> getAllActionComponents() {
		return Collections.unmodifiableMap(_actionComponents);
	}


	public void addAllActionComponents(final Map<String, Action> actionComponents) {
		_actionComponents = actionComponents;
	}


	public void addActionComponent(final String name, final Action actionComponent) {
		_actionComponents.put(name, actionComponent);// NON-NLS
	}


	public void addActionComponent(final Action actionComponent) {
		_actionComponents.put((String) actionComponent.getValue("Name"), actionComponent);// NON-NLS
	}


	public void removeActionComponent(final Action actionComponent) {
		//noinspection SuspiciousMethodCalls
		_actionComponents.remove(actionComponent.getValue("Name"));// NON-NLS
	}


	public Action getActionComponent(final String key) {
		return _actionComponents.get(key);
	}


	@SuppressWarnings({"MethodMayBeStatic"})
	public boolean setActionEnabled(final boolean enable, final Action action) {
		final boolean wasEnabled = action.isEnabled();
		action.setEnabled(enable);
		return wasEnabled;
	}


	public boolean setActionEnabled(final boolean enable, final String key) {
		return setActionEnabled(enable, getActionComponent(key));
	}


	public void addAllComponents() {
		for (final Entry<String, Action> action : getAllActionComponents().entrySet()) {
			if (action.getValue() instanceof Component) {
				add((Component) action.getValue());
			} else {
				add(action.getValue());
			}
		}
	}


	public abstract class AbstractComponentAction extends AbstractAction {

		private static final long serialVersionUID = 0L;


		public AbstractComponentAction(final String name, final String shortDescriptionKey, final String longDescriptionKey, final Icon smallIcon, final Icon largeIcon, final int mnemonicKey, final int modifier, final int menuBarAccelerator) {
			putValue(Action.NAME, name);
			putValue(Action.SHORT_DESCRIPTION, shortDescriptionKey);
			putValue(Action.LONG_DESCRIPTION, longDescriptionKey);
			putValue(Action.SMALL_ICON, smallIcon);
			//putValue(Action.LARGE_ICON_KEY, largeIcon); // NON JDK-1.6
			//putValue(Action.MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonicKey));
			putValue(Action.MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonicKey, modifier).getKeyCode());
			//putValue(Action.MNEMONIC_KEY, mnemonicKey);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(menuBarAccelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

			addActionComponent(this);
		}


		public AbstractComponentAction(final String name, final String shortDescriptionKey, final Icon smallIcon, final int mnemonicKey, final int modifier, final int menuBarAccelerator) {
			this(name, shortDescriptionKey, "", smallIcon, null, mnemonicKey, modifier, menuBarAccelerator);
		}


		public AbstractComponentAction(final String name, final String shortDescriptionKey, final int mnemonicKey, final int modifier, final int menuBarAccelerator) {
			this(name, shortDescriptionKey, null, mnemonicKey, modifier, menuBarAccelerator, null);
		}


		public AbstractComponentAction(final String name, final String shortDescriptionKey, final int mnemonicKey, final int modifier, final int menuBarAccelerator, final JComponent jComponent) {
			this(name, shortDescriptionKey, null, mnemonicKey, modifier, menuBarAccelerator, jComponent);
		}


		public AbstractComponentAction(final String name, final String shortDescriptionKey, final Icon icon, final int mnemonicKey, final int modifier, final int menuBarAccelerator, final JComponent jComponent) {
			//this(name, shortDescriptionKey, "", icon, null, mnemonicKey, menuBarAccelerator);
			putValue(Action.NAME, name);
			putValue(Action.SHORT_DESCRIPTION, shortDescriptionKey);
			//putValue(Action.LONG_DESCRIPTION, longDescriptionKey);
			putValue(Action.SMALL_ICON, icon);
			//putValue(Action.LARGE_ICON_KEY, largeIcon);
			putValue(Action.MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonicKey, modifier).getKeyCode());
			//putValue(Action.MNEMONIC_KEY, mnemonicKey);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(menuBarAccelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

			setFocusable(false);
			addActionComponent(name, this);
		}


		public String getAcceleratorString(final JToolTip tip) {
			final JComponent comp = tip.getComponent();
			if (comp == null) {
				return "";
			}
			final KeyStroke[] keys = comp.getRegisteredKeyStrokes();
			String controlKeyStr = "";
			final KeyStroke postTip = KeyStroke.getKeyStroke(KeyEvent.VK_F1, Event.CTRL_MASK);
			for (final KeyStroke key : keys) {
				// Ignore ToolTipManager postTip action,
				// in swing1.1beta3 and onward
				if (postTip.equals(key)) {
					continue;
				}
				final char c = (char) key.getKeyCode();
				final int mod = key.getModifiers();
				if (mod == InputEvent.CTRL_MASK) {
					controlKeyStr = "Ctrl+" + c;// NON-NLS
					break;
				} else if (mod == InputEvent.ALT_MASK) {
					controlKeyStr = "Alt+" + c;// NON-NLS
					break;
				}
			}
			return controlKeyStr;
		}

	}
}
