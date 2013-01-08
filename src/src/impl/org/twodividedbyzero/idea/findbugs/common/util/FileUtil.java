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
package org.twodividedbyzero.idea.findbugs.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.96
 */
final class FileUtil {

	private FileUtil() {
	}


	@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "HardCodedStringLiteral"})
	public static String fileToString(final File file) throws IOException {
		FileInputStream in = null;
		int size = 0;
		if (file.exists()) {
			in = new FileInputStream(file);
			size = (int) file.length();
		}

		try {
			int fRead = 0;
			final byte[] fBuffer = new byte[size];
			while (fRead < size) {
				if (in != null) {
					fRead += in.read(fBuffer, fRead, size - fRead);
				}
			}
			if (in != null) {
				in.close();
			}
			return new String(fBuffer, "UTF-8");
		} catch (IOException ex) {
			throw new IOException("could not read configuration file '" + file.getPath() + "': " + ex.getMessage());
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}


}
