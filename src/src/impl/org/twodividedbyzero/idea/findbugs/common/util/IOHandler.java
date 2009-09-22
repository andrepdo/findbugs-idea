/**
 * Copyright 2009 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.common.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.4-dev
 */
public class IOHandler extends InputStream {

	private InputStream _delegate;
	private String _name;
	private int _bufferSize = 8096;
	private OutputStream _outputStream;


	public IOHandler(final InputStream stream) {
		this("no_name_file", stream);
	}


	public IOHandler(final String name, final InputStream delegate) {
		_name = name;
		_delegate = delegate;
	}


	public IOHandler(final File src) throws FileNotFoundException {
		_name = src.getName();
		//noinspection IOResourceOpenedButNotSafelyClosed
		_delegate = new BufferedInputStream(new FileInputStream(src));
	}


	@Override
	public int read() throws IOException {
		return _delegate.read();
	}


	@Override
	public int read(final byte[] b) throws IOException {
		return _delegate.read(b);
	}


	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		return _delegate.read(b, off, len);
	}


	@Override
	public long skip(final long n) throws IOException {
		return _delegate.skip(n);
	}


	@Override
	public int available() throws IOException {
		return _delegate.available();
	}


	@Override
	public void close() throws IOException {
		_delegate.close();
	}


	@Override
	public synchronized void mark(final int readlimit) {
		_delegate.mark(readlimit);
	}


	@Override
	public synchronized void reset() throws IOException {
		_delegate.reset();
	}


	public long copyTo(final File dest) throws IOException {
		File dest1 = dest;
		if (dest1.isDirectory()) {
			dest1 = new File(dest1, _name);
		}
		final FileOutputStream out = new FileOutputStream(dest1);
		try {
			return copyTo(out);
		} finally {
			out.close();
		}
	}


	public long copyTo(final OutputStream out) throws IOException {
		_outputStream = out;
		long total = 0;
		final byte[] buffer = new byte[_bufferSize];
		int len;
		//noinspection NestedAssignment
		while ((len = read(buffer)) >= 0) {
			out.write(buffer, 0, len);
			total += len;
		}
		out.flush();
		return total;
	}


	public void closeQuietly() {
		try {
			close();
		} catch (IOException ignore) {
			// ignore
		}
	}


	public void copyAndClose(final File file) throws IOException {
		try {
			copyTo(file);
		} finally {
			close();
		}
	}


	public void copyAndClose(final OutputStream out) throws IOException {
		try {
			copyTo(out);
		} finally {
			close();
		}
	}


	public void copyAndCloseBoth() {
		closeInput();
		closeOutput();
	}


	public void closeInput() {
		try {
			close();
		} catch (IOException ignore) {
		}
	}


	public void closeOutput() {
		try {
			_outputStream.close();
		} catch (IOException ignore) {
		}
	}


	public IOHandler bufferSize(final int size) {
		_bufferSize = size;
		return this;
	}


	public IOHandler name(final String newName) {
		_name = newName;
		return this;
	}


	public static void main(final String args[]) {
		/*try {
			new IOHandler(new URL("/tmp/test1").openStream()).copyTo(new BufferedOutputStream(new FileOutputStream("/tmp/testOut"))).closeOutput().closeInput();
		} catch (IOException ignore) {
		}*/
		//new IOHandler( new URL("/tmp/test2").openStream() ).copyToAndCloseBoth( new FooOutputStream() );
	}
}
