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
@SuppressWarnings("CallToPrintStackTrace")
class TreeXmlSerializer {

	public static void write(final DefaultTreeModel model, final String filename) {
		XMLEncoder encoder = null;
		FileOutputStream fileOutputStream = null;
		BufferedOutputStream stream = null;

		try {
			fileOutputStream = new FileOutputStream(filename);
			stream = new BufferedOutputStream(fileOutputStream);
			encoder = new XMLEncoder(stream);
			encoder.writeObject(model);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
				if (stream != null) {
					stream.close();
				}
			} catch (final IOException e) {
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
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				assert stream != null;
				stream.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		DefaultTreeModel model = null;
		try {
			model = (DefaultTreeModel) decoder.readObject();
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			decoder.close();
		}

		return model;
	}

}
