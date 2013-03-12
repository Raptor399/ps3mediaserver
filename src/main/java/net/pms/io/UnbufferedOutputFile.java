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
import java.io.OutputStream;


/**
 * Utility class to transport bytes from a transcoding process to a DLNA client. Unlike
 * {@link BufferedOutputFileImpl}, no attempt is made to buffer the output. Instead,
 * {@link java.io.PipedOutputStream PipedOutputStream} and {@link java.io.PipedInputStream
 * PipedInputStream} are used to pump data from the transcoder to the client. The idea is
 * to have as little interference as possible in the piping process, allowing PMS to be
 * agnostic of the transcoded data and focus on steering the process and handling requests
 * instead.
 * <p>
 * TODO: Since no data is buffered, seek requests should be not be delegated to this
 * class. This class can only be used for straightforward streaming. Instead, the
 * current process should be stopped (killed) and a new process should be started.
 * This has not been implemented yet, so seeking is not an option right now. 
 * <p>
 * Because of the missing feature, this class is currently not used anywhere in PMS. If
 * you want to experiment with it, search for "new BufferedOutputFileImpl(" and replace it
 * with "new UnbufferedOutputFile(" in the classes {@link OutputBufferConsumer} and
 * {@link WindowsNamedPipe}.
 */
public class UnbufferedOutputFile implements BufferedOutputFile {
	/**
	 * Size of the circular byte buffer. The circular buffer will be filled
	 * from the pipe buffer, so its size should be greater than that of the
	 * pipe buffer, and preferably a multiple of that size. This way the
	 * output stream can be written by one thread while the input stream is
	 * being read in a different tempo by another.  
	 */
	private final int BUFFER_SIZE = OutputBufferConsumer.PIPE_BUFFER_SIZE * 4;

	/** Stream to capture the output of a process that needs to be piped */
	private OutputStream outputStream;

	/** Stream to send the piped contents to */
	private InputStream inputStream;

	public UnbufferedOutputFile(OutputParams params) {
		CircularByteBuffer buffer = new CircularByteBuffer(BUFFER_SIZE);
		outputStream = buffer.getOutputStream();
		inputStream = buffer.getInputStream();
	}
	
	/**
	 * Do not use this method to close the streams of this object.
	 * Instead, call {@link #closeInputStream()} or {@link #closeOutputStream()}
	 * when a stream can be closed.
	 */
	@Override
	public void close() throws IOException {
		// RequestV2, line 862 calls "inputStream.close()" after every handled request
		// of max 8k bytes. We have much more bytes in the pipe, so don't close the
		// streams just yet when requested to do so.
	}

	/**
	 * Close the output stream of the buffered output. This signifies that no
	 * more data will be written to the buffered output.
	 *
	 * @throws IOException When closing the output stream fails.
	 */
	@Override
	public void closeOutputStream() throws IOException {
		outputStream.close();
	}

	/**
	 * Close the input stream of the buffered output. This signifies
	 * that no more data will be read from the buffered output.
	 *
	 * @throws IOException When closing the input stream fails.
	 */
	@Override
	public void closeInputStream() throws IOException {
		inputStream.close();
	}

	/**
	 * Returns the {@link java.io.PipedInputStream PipedInputStream} connected to the
	 * transcoding output stream as is, ignoring the newReadposition parameter.
	 * <p>
	 * Note that is it very well possible that the input stream has no available
	 * bytes. E.g. this can happen when the output process is still starting up
	 * and has not produced any output yet. An input stream with no available
	 * bytes does not mean there is nothing left to output!
	 *
	 * @param newReadPosition This parameter is ignored.
	 * @return The piped input stream
	 */
	@Override
	public InputStream getInputStream(long newReadPosition) {
		return inputStream;
	}

	/**
	 * Writes len bytes from the specified byte array starting at offset off to
	 * the piped output stream.
	 *  
	 * @param b The data
	 * @param off The start offset in the data
	 * @param len The number of bytes to write
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		outputStream.write(b, off, len);
	}
	
	/**
	 * Writes the specified byte to the piped output stream.
	 * 
	 * @param b The byte to write
	 */
	@Override
	public void write(int b) throws IOException {
		outputStream.write(b);
	}

	/**
	 * Writes b.length bytes from the specified byte array to this output stream. The
	 * general contract for <code>write(b)</code> is that it should have exactly the
	 * same effect as the call <code>write(b, 0, b.length)</code>.
	 * @param byteArray
	 */
	@Override
	public void write(byte[] byteArray) throws IOException {
		outputStream.write(byteArray);
	}
	
	/**
	 * @deprecated Unused method from interface.
	 * @return null
	 */
	@Deprecated
	public WaitBufferedInputStream getCurrentInputStream() {
		return null;
	}
	
	/**
	 * @deprecated Unused method from interface.
	 * @return 0
	 */
	@Deprecated
	public long getWriteCount() {
		return 0;
	}
	
	/**
	 * @deprecated Unused method from interface.
	 * @return 0
	 */
	@Deprecated
	public int read(boolean firstRead, long readCount) {
		return 0;
	}

	/**
	 * @deprecated Unused method from interface.
	 * @return 0
	 */
	@Deprecated
	public int read(boolean firstRead, long readCount, byte[] b, int off, int len) {
		return 0;
	}
	
	/**
	 * @deprecated Unused method from interface.
	 */
	@Deprecated
	public void attachThread(ProcessWrapper thread) {
	}
	
	/**
	 * @deprecated Unused method from interface.
	 */
	@Deprecated
	public void reset() {
	}
	
	/**
	 * @deprecated Unused method from interface.
	 */
	@Deprecated
	public void removeInputStream(WaitBufferedInputStream waitBufferedInputStream) {
	}

	
	/**
	 * @deprecated Unused method from interface.
	 */
	@Deprecated
	public void detachInputStream() {
	}

}
