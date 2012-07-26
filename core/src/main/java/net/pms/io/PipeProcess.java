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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import net.pms.api.PmsConfiguration;
import net.pms.api.PmsCore;
import net.pms.api.io.ProcessWrapperFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.sun.jna.Platform;

public class PipeProcess {
	private static final Logger logger = LoggerFactory.getLogger(PipeProcess.class);
	private String linuxPipeName;
	private WindowsNamedPipe mk;
	private boolean forcereconnect;
	private final PmsCore pmsCore;
	private final PmsConfiguration configuration;
	private final ProcessWrapperFactory processWrapperFactory;
	

	@AssistedInject
	public PipeProcess(PmsCore pmsCore, PmsConfiguration configuration,
			ProcessWrapperFactory processWrapperFactory,
			WindowsNamedPipe.Factory windowsNamedPipeFactory,
			@Assisted String pipeName, @Assisted OutputParams params,
			@Assisted String... extras) {

		this.pmsCore = pmsCore;
		this.configuration = configuration;
		this.processWrapperFactory = processWrapperFactory;
		forcereconnect = false;
		boolean in = true;

		if (extras != null && extras.length > 0 && extras[0].equals("out")) {
			in = false;
		}

		if (extras != null) {
			for (int i = 0; i < extras.length; i++) {
				if (extras[i].equals("reconnect")) {
					forcereconnect = true;
				}
			}
		}

		if (pmsCore.isWindows()) {
			mk = windowsNamedPipeFactory.create(pipeName, forcereconnect, in, params);
		} else {
			linuxPipeName = getPipeName(pipeName);
		}
	}

	@AssistedInject
	public PipeProcess(PmsCore pmsCore, PmsConfiguration configuration,
			ProcessWrapperFactory processWrapperFactory,
			WindowsNamedPipe.Factory windowsNamedPipeFactory,
			@Assisted String pipeName, @Assisted String... extras) {
		this(pmsCore, configuration, processWrapperFactory,
				windowsNamedPipeFactory, pipeName, null, extras);
	}

	private String getPipeName(String pipeName) {
		try {
			return configuration.getTempFolder() + "/" + pipeName;
		} catch (IOException e) {
			logger.error("Pipe may not be in temporary directory", e);
			return pipeName;
		}
	}

	public String getInputPipe() {
		if (!pmsCore.isWindows()) {
			return linuxPipeName;
		}
		return mk.getPipeName();
	}

	public String getOutputPipe() {
		if (!pmsCore.isWindows()) {
			return linuxPipeName;
		}
		return mk.getPipeName();
	}

	public ProcessWrapper getPipeProcess() {
		if (!pmsCore.isWindows()) {
			OutputParams mkfifo_vid_params = new OutputParams(configuration);
			mkfifo_vid_params.maxBufferSize = 0.1;
			mkfifo_vid_params.log = true;
			String cmdArray[];

			if (Platform.isMac() || Platform.isFreeBSD() || Platform.isSolaris()) {
				cmdArray = new String[] {"mkfifo", "-m", "777", linuxPipeName};
			} else {
				cmdArray = new String[] {"mkfifo", "--mode=777", linuxPipeName};
			}

			ProcessWrapper mkfifo_vid_process = processWrapperFactory.create(
					cmdArray, mkfifo_vid_params, false, false);
			return mkfifo_vid_process;
		}
		return mk;
	}

	public void deleteLater() {
		if (!pmsCore.isWindows()) {
			File f = new File(linuxPipeName);
			f.deleteOnExit();
		}
	}

	public BufferedOutputFile getDirectBuffer() throws IOException {
		if (!pmsCore.isWindows()) {
			return null;
		}
		return mk.getDirectBuffer();
	}

	public InputStream getInputStream() throws IOException {
		if (!pmsCore.isWindows()) {
			logger.trace("Opening file " + linuxPipeName + " for reading...");
			RandomAccessFile raf = new RandomAccessFile(linuxPipeName, "r");
			return new FileInputStream(raf.getFD());
		}
		return mk.getReadable();
	}

	public OutputStream getOutputStream() throws IOException {
		if (!pmsCore.isWindows()) {
			logger.trace("Opening file " + linuxPipeName + " for writing...");
			RandomAccessFile raf = new RandomAccessFile(linuxPipeName, "rw");
			FileOutputStream fout = new FileOutputStream(raf.getFD());
			return fout;
		}
		return mk.getWritable();
	}
}
