/*
 * Copyright 2010 Andre Pfeiler
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
public final class FileUtil {

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
