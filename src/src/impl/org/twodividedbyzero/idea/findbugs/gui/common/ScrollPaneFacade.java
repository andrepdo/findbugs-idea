/*
 * Copyright 2011 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.twodividedbyzero.idea.findbugs.gui.common;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
public final class ScrollPaneFacade implements ScrollPaneConstants {

	private static final Logger LOGGER = Logger.getInstance(ScrollPaneFacade.class.getName());


	private ScrollPaneFacade() {
	}


	@SuppressWarnings( {"UndesirableClassUsage"})
	public static JScrollPane createScrollPane(@Nullable final Component view, final int vsbPolicy, final int hsbPolicy) {
		if (IdeaUtilImpl.isVersionGreaterThanIdea9()) {
			try {
				final Class<?> clazz = Class.forName("com.intellij.ui.components.JBScrollPane");
				final Constructor<?> constructor = clazz.getConstructor(Component.class, int.class, int.class);
				return (JScrollPane) constructor.newInstance(view, vsbPolicy, hsbPolicy);
			} catch (ClassNotFoundException e) {
				LOGGER.error(e);
			} catch (NoSuchMethodException e) {
				LOGGER.error(e);
			} catch (InvocationTargetException e) {
				LOGGER.error(e.getTargetException());
			} catch (InstantiationException e) {
				LOGGER.error(e);
			} catch (IllegalAccessException e) {
				LOGGER.error(e);
			}
		}

		return new JScrollPane(view, vsbPolicy, hsbPolicy);
	}


	public static JScrollPane createScrollPane(final Component view) {
		return createScrollPane(view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}


	public static JScrollPane createScrollPane(final int vsbPolicy, final int hsbPolicy) {
		return createScrollPane(null, vsbPolicy, hsbPolicy);
	}


	public static JScrollPane createScrollPane() {
		return createScrollPane(null, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}
}
