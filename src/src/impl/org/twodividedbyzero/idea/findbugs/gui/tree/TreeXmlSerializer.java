/**
 * Copyright 2008 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.twodividedbyzero.idea.findbugs.gui.tree;

import javax.swing.tree.DefaultTreeModel;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.29-dev
 */
public class TreeXmlSerializer {

	public static void write(final DefaultTreeModel model, final String filename) {
		XMLEncoder encoder = null;
		FileOutputStream fileOutputStream = null;
		BufferedOutputStream stream = null;

		try {
			fileOutputStream = new FileOutputStream(filename);
			stream = new BufferedOutputStream(fileOutputStream);
			encoder = new XMLEncoder(stream);
			encoder.writeObject(model);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (encoder != null) {
				encoder.close();
			}
		}
	}

	// -------------------------
	// loads JTree


	public static DefaultTreeModel read(final String filename) {
		XMLDecoder decoder = null;
		BufferedInputStream stream = null;
		try {
			final FileInputStream fis = new FileInputStream(filename);
			stream = new BufferedInputStream(fis);
			decoder = new XMLDecoder(stream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				assert stream != null;
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		DefaultTreeModel model = null;
		try {
			model = (DefaultTreeModel) decoder.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			decoder.close();
		}

		return model;
	}

}
