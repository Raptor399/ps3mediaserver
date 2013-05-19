/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.io;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allows logging of method calls to InputStream objects.
 */
public class LoggingInputStream extends InputStream {

	/** Logger to write messages to the log file */
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingInputStream.class);

	/** Whether or not TRACE messages should be logged. */
	private static final boolean IS_TRACE_ENABLED = LOGGER.isTraceEnabled();

	/** The input stream to wrap */
	private InputStream inputStream;

	LoggingInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public int available() throws IOException {
		int result = inputStream.available();

		if (IS_TRACE_ENABLED) {
			LOGGER.trace("inputstream.available() = " + result + "");
		}

		return result;
	}

	@Override
	public void close() throws IOException {
		if (IS_TRACE_ENABLED) {
			LOGGER.trace("Input stream being closed", new IOException(""));
		}

		inputStream.close();
	}
	
	@Override
	public int read() throws IOException {
		int result = inputStream.read();

		if (IS_TRACE_ENABLED) {
			LOGGER.trace("inputstream.read() = " + result + "");
		}

		return inputStream.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		int result = inputStream.read(b); 

		if (IS_TRACE_ENABLED) {
			LOGGER.trace("inputstream.read(b[" + b.length + "]) = " + result + "");
		}

		return result;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int result = inputStream.read(b, off, len); 

		if (IS_TRACE_ENABLED) {
			LOGGER.trace("inputstream.read(b[" + b.length + "], " + off + ", " + len + ") = " + result + "");
		}

		return result;
	}

	@Override
	public long skip(long n) throws IOException {
		long result = inputStream.skip(n);

		if (IS_TRACE_ENABLED) {
			LOGGER.trace("inputstream.skip(" + n + ") = " + result + "");
		}

		return result;
	}
}
