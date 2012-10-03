/*
 * Copyright 2010 Andre Pfeiler
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
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.containers.SoftHashMap;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
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
	private static final String LOCALE_RESOURCES_PKG = "org.twodividedbyzero.idea.findbugs.resources.i18n.Messages";
	private static final String ICON_RESOURCES_PKG = "/org/twodividedbyzero/idea/findbugs/resources/icons";
	private static final byte[] BYTE = new byte[0];
	private static final Map<String, Object> _iconCache = new SoftHashMap<String, Object>();


	private ResourcesLoader() {
	}


	public static ResourceBundle getResourceBundle() {
		LOGGER.info("Loading locale properties for '" + Locale.getDefault() + ')');

		if (_bundle != null) {
			return _bundle;
		}

		//noinspection UnusedCatchParameter
		try {
			_bundle = ResourceBundle.getBundle(LOCALE_RESOURCES_PKG, Locale.getDefault());
		} catch (MissingResourceException e) {
			throw new MissingResourceException("Missing Resource bundle: " + Locale.getDefault() + ' ', LOCALE_RESOURCES_PKG, "");
		}

		return _bundle;
	}


	public static ResourceBundle getResourceBundle(final String localeResourceFolder) {
		LOGGER.info("Loading locale properties for '" + Locale.getDefault() + '\'');
		//noinspection UnusedCatchParameter
		try {
			_bundle = ResourceBundle.getBundle(localeResourceFolder, Locale.getDefault());
		} catch (MissingResourceException e) {
			throw new MissingResourceException("Missing Resource bundle: " + Locale.getDefault() + ' ', localeResourceFolder, "");
		}

		return _bundle;
	}


	@SuppressWarnings({"UnusedCatchParameter"})
	public static String getString(final String key) {
		try {
			if (_bundle == null) {
				getResourceBundle();
			}
			return _bundle.getString(key);
		} catch (MissingResourceException e) {
			throw new MissingResourceException("Missing Resource: " + Locale.getDefault() + " - key: " + key + "  - resources: " + LOCALE_RESOURCES_PKG, LOCALE_RESOURCES_PKG, key);
		} catch (NullPointerException e) {
			throw new NullPointerException("No bundle set: use ResourcesLoader.getResourceBundle().getString(...)");
		}

	}


	private static InputStream getResourceStream(final String iconResourcePkg, final String filename) {
		String iconResourcePkg1 = iconResourcePkg;
		if (iconResourcePkg1.length() > 0 && iconResourcePkg1.charAt(0) == '/') {
			iconResourcePkg1 = iconResourcePkg1.replaceFirst("/", "");
		}

		final String resourceNname = '/' + iconResourcePkg1.replace('.', '/') + '/' + filename;
		final Class<?> clazz = ResourcesLoader.class;

		return clazz.getResourceAsStream(resourceNname);
	}


	@Nullable
	private static Object queryIconCache(final String pkgName, final String filename) {
		final String key = pkgName + '/' + filename;
		if (_iconCache.containsKey(key)) {
			LOGGER.debug("IconCache (size: " + _iconCache.size() + ") hit for '" + key + '\'');
			return _iconCache.get(key);
		}
		LOGGER.debug("IconCache (size: " + _iconCache.size() + ") miss for '" + key + '\'');

		return null;
	}


	/**
	 * Load string from file from jar or external resources.
	 * <p> <blockquote><pre>
	 * ModuleResources.loadTextResource("core.txt");
	 * </pre></blockquote>
	 *
	 * @param filename the filename string
	 * @return the text <code>String</code>
	 */
	public static String loadTextResource(final String filename) {
		return loadTextResource(LOCALE_RESOURCES_PKG, filename);
	}


	/**
	 * Load string from file from jar or external resources.
	 * <p> <blockquote><pre>
	 * ModuleResources.loadTextResource("de.espirit.firstspirit.opt.vscan.resources.locale", "core.txt");
	 * </pre></blockquote>
	 *
	 * @param pkgName  the java package
	 * @param filename the filename string
	 * @return the text <code>String</code>
	 */
	private static String loadTextResource(final String pkgName, final String filename) {
		String ret = null;
		final InputStream is = getResourceStream(pkgName, filename);

		if (is != null) {
			final StringBuilder sb = new StringBuilder();

			while (true) {
				int c = 0;
				try {
					c = is.read();
				} catch (IOException e) {
					LOGGER.error("InputStream read failure!", e);
				}
				if (c == -1) {
					break;
				}

				sb.append((char) c);
			}

			try {
				is.close();
			} catch (IOException e) {
				LOGGER.error("Couldn't close InputStream.", e);
			}
			ret = sb.toString();
		}

		return ret;
	}


	ImageIcon loadImageIcon(final String filename, final String description) {
		final java.net.URL imgURL = getClass().getResource(ICON_RESOURCES_PKG + '/' + filename);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			LOGGER.info("Couldn't find file: " + filename);
			return null;
		}
	}


	/**
	 * Load an image resource from jar or external locations.
	 * <p> <blockquote><pre>
	 * ModuleResources.loadImageResource("core.png");
	 * </pre></blockquote>
	 *
	 * @param filename the image filename <code>String</code>
	 * @return java.awt.Image
	 */
	public static Image loadImage(final String filename) {
		return loadImage(ICON_RESOURCES_PKG, filename);
	}


	/**
	 * Load an image resource from jar or external locations.
	 *
	 * @param pkgName  the java package
	 * @param filename the image filename <code>String</code>
	 * @return java.awt.Image
	 */
	@SuppressWarnings({"CheckForOutOfMemoryOnLargeArrayAllocation"})
	private static Image loadImage(final String pkgName, final String filename) {
		final Object cacheHit = queryIconCache(pkgName, filename);
		if (cacheHit != null) {
			return (Image) cacheHit;
		}
		Image ret = null;
		final InputStream is = getResourceStream(pkgName, filename);

		if (is != null) {
			byte[] buffer = BYTE;
			final byte[] tmBuf = new byte[1024];

			while (true) {
				int len = 0;
				try {
					len = is.read(tmBuf);
				} catch (IOException e) {
					LOGGER.error("InputStream read failure!", e);
				}

				if (len <= 0) {
					break;
				}

				final byte[] newbuf = new byte[buffer.length + len];
				System.arraycopy(buffer, 0, newbuf, 0, buffer.length);
				System.arraycopy(tmBuf, 0, newbuf, buffer.length, len);
				buffer = newbuf;
			}

			ret = Toolkit.getDefaultToolkit().createImage(buffer);
			addIfAbsent(pkgName, filename, ret);
			try {
				is.close();
			} catch (IOException e) {
				LOGGER.error("Couldn't close InputStream.", e);
			}
		}

		return ret;
	}


	private static void addIfAbsent(final String pkgName, final String filename, final Object ret) {
		synchronized (_iconCache) {
			_iconCache.put(pkgName + '/' + filename, ret);
		}
	}


	/**
	 * Load an image resource from jar or external locations.
	 * <p> <blockquote><pre>
	 * ModuleResources.loadIconResource("core.png");
	 * </pre></blockquote>
	 *
	 * @param filename the image filename <code>String</code>
	 * @return javax.swing.Icon
	 */
	public static Icon loadIcon(final String filename) {
		return loadIcon(ICON_RESOURCES_PKG, filename);
	}


	/**
	 * Load an image resource from jar or external locations.
	 *
	 * @param pkgName  the java package
	 * @param filename the image filename <code>String</code>
	 * @return javax.swing.Icon
	 */
	private static Icon loadIcon(final String pkgName, final String filename) {
		final Object cacheHit = queryIconCache(pkgName, filename);
		if (cacheHit != null) {
			return (Icon) cacheHit;
		}
		Icon ret = null;
		final InputStream is = getResourceStream(pkgName, filename);

		if (is != null) {
			byte[] buffer = BYTE;
			@SuppressWarnings({"CheckForOutOfMemoryOnLargeArrayAllocation"})
			final byte[] tmpBuffer = new byte[1024];

			while (true) {
				int len = 0;
				try {
					len = is.read(tmpBuffer);
				} catch (IOException e) {
					LOGGER.error("InputStream read failure!", e);
				}

				if (len <= 0) {
					break;
				}

				final byte[] newBuf = new byte[buffer.length + len];
				System.arraycopy(buffer, 0, newBuf, 0, buffer.length);
				System.arraycopy(tmpBuffer, 0, newBuf, buffer.length, len);
				buffer = newBuf;
			}

			ret = new ImageIcon(buffer);
			addIfAbsent(pkgName, filename, ret);
			try {
				is.close();
			} catch (IOException e) {
				LOGGER.error("Couldn't close InputStream.", e);
			}
		}

		return ret;
	}


	public static Icon findIcon(final String iconName) {
		return findIcon(ICON_RESOURCES_PKG + '/' + iconName, ResourcesLoader.class);
	}


	public static Icon findIcon(final String path, final Class<?> clazz) {
		final Object cacheHit = queryIconCache(ICON_RESOURCES_PKG, path);
		if (cacheHit != null) {
			return (Icon) cacheHit;
		}


		final Icon icon = IconLoader.findIcon(path, clazz);

		synchronized (_iconCache) {
			_iconCache.put(ICON_RESOURCES_PKG + '!' + path, icon);
		}

		return icon;
	}


	public static Icon findIcon(final String iconName, final ClassLoader classLoader) {
		final Object cacheHit = queryIconCache(ICON_RESOURCES_PKG, iconName);
		if (cacheHit != null) {
			return (Icon) cacheHit;
		}

		final Icon icon = IconLoader.findIcon(ICON_RESOURCES_PKG + '/' + iconName, classLoader);

		synchronized (_iconCache) {
			_iconCache.put(ICON_RESOURCES_PKG + '!' + iconName, icon);
		}

		return icon;
	}

}