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

import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

@SuppressWarnings("unused")
public final class DebugUtil {
	private DebugUtil() {
	}

	public static void dumpClasses(@NotNull final Class clazz) {
		final StringBuilder s = new StringBuilder();
		s.append("Begin dump: ").append(clazz).append("\n");

		final ClassLoader threadCl = Thread.currentThread().getContextClassLoader();
		s.append("Current Context CL: ").append(threadCl.getClass()).append(" [").append(System.identityHashCode(threadCl)).append("]\n");

		final Set<ClassLoader> cls = New.set();
		collectClassLoaders(cls, clazz.getClassLoader());
		collectClassLoaders(cls, threadCl);

		final Field classesField;
		try {
			classesField = ClassLoader.class.getDeclaredField("classes");
			classesField.setAccessible(true);
		} catch (final NoSuchFieldException e) {
			e.printStackTrace();
			return;
		}
		for (final ClassLoader cl : cls) {
			s.append("\n\nCL: ").append(cl.getClass()).append(" [").append(System.identityHashCode(cl)).append("]\n");
			try {
				@SuppressWarnings("unchecked") final Vector<Class<?>> classes = (Vector<Class<?>>) classesField.get(cl);
				final List<Class<?>> sorted = new ArrayList<Class<?>>(classes);
				Collections.sort(sorted, new Comparator<Class<?>>() {
					@Override
					public int compare(Class<?> o1, Class<?> o2) {
						return o1.getName().compareToIgnoreCase(o2.getName());
					}
				});
				for (final Class<?> c : sorted) {
					final URL url = cl.getResource("/" + c.getName().replace(".", "/") + ".class");
					s.append("    ").append(c.getName()).append(" -> ").append(url).append("\n");
				}
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		try {
			FileUtil.writeToFile(new File("C:\\classLoaderDump.txt"), s.toString());
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private static void collectClassLoaders(
			@NotNull final Set<ClassLoader> cls,
			@Nullable final ClassLoader cl
	) {
		if (cl == null) {
			return;
		}
		if (cls.add(cl)) {
			if (cl instanceof PluginClassLoader) {
				try {
					final Field myParentsField = PluginClassLoader.class.getDeclaredField("myParents");
					final ClassLoader[] parents = (ClassLoader[]) myParentsField.get(cl);
					for (final ClassLoader parent : parents) {
						collectClassLoaders(cls, parent);
					}
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			collectClassLoaders(cls, cl.getParent());
		}
	}
}
