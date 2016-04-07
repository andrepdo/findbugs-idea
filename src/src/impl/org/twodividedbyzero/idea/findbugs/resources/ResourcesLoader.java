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
package org.twodividedbyzero.idea.findbugs.resources;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.containers.SoftHashMap;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;
import org.twodividedbyzero.idea.findbugs.common.util.ErrorUtil;
import org.twodividedbyzero.idea.findbugs.common.util.IoUtil;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
@SuppressWarnings({"UnusedDeclaration", "HardcodedFileSeparator"})
public final class ResourcesLoader {

	private static final Logger LOGGER = Logger.getInstance(ResourcesLoader.class.getName());

	private static volatile ResourceBundle _bundle;
	public static final String BUNDLE = "org.twodividedbyzero.idea.findbugs.resources.i18n.Messages";
	private static final String ICON_RESOURCES_PKG = "/org/twodividedbyzero/idea/findbugs/resources/icons";
	private static final Map<String, Icon> _iconCache = new SoftHashMap<String, Icon>();


	private ResourcesLoader() {
	}


	@NotNull
	public static ResourceBundle getResourceBundle() {
		LOGGER.info("Loading locale properties for '" + Locale.getDefault() + ')');

		//noinspection StaticVariableUsedBeforeInitialization
		if (_bundle != null) {
			return _bundle;
		}

		//noinspection UnusedCatchParameter
		try {
			_bundle = ResourceBundle.getBundle(BUNDLE, Locale.getDefault());
		} catch (final MissingResourceException e) {
			throw new MissingResourceException("Missing Resource bundle: " + Locale.getDefault() + ' ', BUNDLE, "");
		}

		return _bundle;
	}


	@Nls
	@SuppressWarnings({"UnusedCatchParameter"})
	public static String getString(@NotNull @PropertyKey(resourceBundle = BUNDLE) final String key, @Nullable Object... params) {
		try {
			//noinspection StaticVariableUsedBeforeInitialization
			if (_bundle == null) {
				getResourceBundle();
			}
			String ret = _bundle.getString(key);
			if (params != null && params.length > 0 && ret.indexOf('{') >= 0) {
				return MessageFormat.format(ret, params);
			}
			return ret;
		} catch (final MissingResourceException e) {
			throw new MissingResourceException("Missing Resource: " + Locale.getDefault() + " - key: " + key + "  - resources: " + BUNDLE, BUNDLE, key);
		}
	}


	@Nullable
	private static InputStream getResourceStream(@NotNull final String iconResourcePkg, final String filename) {
		String iconResourcePkg1 = iconResourcePkg;
		if (!iconResourcePkg1.isEmpty() && iconResourcePkg1.charAt(0) == '/') {
			iconResourcePkg1 = iconResourcePkg1.replaceFirst("/", "");
		}

		final String resourceName = '/' + iconResourcePkg1.replace('.', '/') + '/' + filename;
		final Class<?> clazz = ResourcesLoader.class;

		return clazz.getResourceAsStream(resourceName);
	}


	@Nullable
	private static Icon queryIconCache(final String pkgName, final String filename) {
		final String key = pkgName + '/' + filename;
		synchronized (_iconCache) {
			return _iconCache.get(key);
		}
	}


	private static void addIfAbsent(final String pkgName, final String filename, final Icon ret) {
		synchronized (_iconCache) {
			_iconCache.put(pkgName + '/' + filename, ret);
		}
	}


	@NotNull
	public static Icon loadIcon(final String filename) {
		return loadIcon(ICON_RESOURCES_PKG, filename);
	}


	@NotNull
	private static Icon loadIcon(final String pkgName, final String filename) {
		final Object cacheHit = queryIconCache(pkgName, filename);
		if (cacheHit != null) {
			return (Icon) cacheHit;
		}
		final InputStream is = getResourceStream(pkgName, filename);
		if (is == null) {
			throw new IllegalArgumentException("Could not find " + pkgName + filename);
		}

		final Icon ret;
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			IoUtil.copy(is, out);
			ret = new ImageIcon(out.toByteArray());
			addIfAbsent(pkgName, filename, ret);
		} catch (final IOException e) {
			throw ErrorUtil.toUnchecked(pkgName + filename, e);
		} finally {
			IoUtil.safeClose(is);
		}

		if (ret.getIconWidth() == 0 || ret.getIconHeight() == 0) {
			throw new IllegalArgumentException("Could not load " + pkgName + filename);
		}

		return ret;
	}


}