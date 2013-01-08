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

import javax.swing.JTable;
import javax.swing.table.TableModel;
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
public final class TableFacade {

	private static final Logger LOGGER = Logger.getInstance(TableFacade.class.getName());


	private TableFacade() {
	}


	@SuppressWarnings({"UndesirableClassUsage"})
	public static JTable createTable(final TableModel dataModel) {
		if (IdeaUtilImpl.isVersionGreaterThanIdea9()) {
			try {
				final Class<?> clazz = Class.forName("com.intellij.ui.table.JBTable");
				final Constructor<?> constructor = clazz.getConstructor(TableModel.class);
				return (JTable) constructor.newInstance(dataModel);
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
		return new JTable(dataModel);
	}



	@SuppressWarnings({"UndesirableClassUsage"})
	public static JTable createTable() {
		if (IdeaUtilImpl.isVersionGreaterThanIdea9()) {
			try {
				final Class<?> clazz = Class.forName("com.intellij.ui.table.JBTable");
				final Constructor<?> constructor = clazz.getConstructor();
				return (JTable) constructor.newInstance();
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
		return new JTable();
	}

}
