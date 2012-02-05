/*
 * Copyright 2012 Andre Pfeiler
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
