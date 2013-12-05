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
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;

import javax.swing.JList;
import javax.swing.ListModel;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@dev.java.net>
 * @version $Revision$
 * @since 0.9.97
 */
@SuppressWarnings({"UnusedDeclaration"})
public final class ListFacade {

	private static final Logger LOGGER = Logger.getInstance(ListFacade.class.getName());


	private ListFacade() {
	}


	@SuppressWarnings({"UndesirableClassUsage"})
	public static JList createList(final ListModel dataModel) {
		if (IdeaUtilImpl.isVersionGreaterThanIdea9()) {
			try {
				final Class<?> clazz = Class.forName("com.intellij.ui.components.JBList");
				final Constructor<?> constructor = clazz.getConstructor(ListModel.class);
				return (JList) constructor.newInstance(dataModel);
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
		return new JList(dataModel);
	}


	@SuppressWarnings({"UndesirableClassUsage"})
	public static JList createList(final Object[] listData) {
		if (IdeaUtilImpl.isVersionGreaterThanIdea9()) {
			try {
				final Class<?> clazz = Class.forName("com.intellij.ui.components.JBList");
				final Constructor<?> constructor = clazz.getConstructor(Object[].class);
				final Object[] passed = {listData};
				return (JList) constructor.newInstance(passed);
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
		return new JList(listData);
	}


	@SuppressWarnings({"UndesirableClassUsage"})
	public static JList createList() {
		if (IdeaUtilImpl.isVersionGreaterThanIdea9()) {
			try {
				final Class<?> clazz = Class.forName("com.intellij.ui.components.JBList");
				final Constructor<?> constructor = clazz.getConstructor();
				return (JList) constructor.newInstance();
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
		return new JList();
	}

}
