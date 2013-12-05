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
			} catch (final ClassNotFoundException e) {
				LOGGER.error(e);
			} catch (final NoSuchMethodException e) {
				LOGGER.error(e);
			} catch (final InvocationTargetException e) {
				LOGGER.error(e.getTargetException());
			} catch (final InstantiationException e) {
				LOGGER.error(e);
			} catch (final IllegalAccessException e) {
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
