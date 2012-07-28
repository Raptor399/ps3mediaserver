/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2010  A.Brochard
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
package net.pms.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.inject.Inject;

import net.pms.api.PmsCore;
import net.pms.gui.IFrame;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.status.ErrorStatus;

/**
 * Special Logback appender to 'print' log messages on the PMS GUI.
 * 
 * @author thomas@innot.de
 * 
 */
public class FrameAppender<E> extends UnsynchronizedAppenderBase<E> {
	private Encoder<E> encoder;
	private final ByteArrayOutputStream outputstream = new ByteArrayOutputStream(
		256);
	private final Object lock = new Object();

	@Inject
	private final PmsCore pmsCore;

	@Inject
	FrameAppender(PmsCore pmsCore) {
		this.pmsCore = pmsCore;
	}

	/**
	 * Checks that the required parameters are set and if everything is in
	 * order, activates this appender.
	 */
	@Override
	public void start() {
		int error = 0;
		if (this.encoder == null) {
			addStatus(new ErrorStatus(
				"No encoder set for the appender named \"" + name + "\".",
				this));
			error++;
		} else {
			try {
				encoder.init(outputstream);
			} catch (IOException ioe) {
				addStatus(new ErrorStatus(
					"Failed to initialize encoder for appender named ["
					+ name + "].", this, ioe));
				error++;
			}
		}

		if (error == 0) {
			super.start();
		}
	}

	/* (non-Javadoc)
	 * @see ch.qos.logback.core.UnsynchronizedAppenderBase#append(java.lang.Object)
	 */
	@Override
	protected void append(E eventObject) {
		IFrame frame = pmsCore.getFrame();

		if (frame != null) {
			try {
				synchronized (lock) {
					this.encoder.doEncode(eventObject);
					String msg = outputstream.toString("UTF-8");
					frame.append(msg);
					outputstream.reset();
				}
			} catch (IOException ioe) {
				addStatus(new ErrorStatus("IO failure in appender", this, ioe));
			}
		}
	}

	/**
	 * @return The encoder associated with this appender, or <code>null</code>
	 *         if no encoder has been set.
	 */
	public Encoder<E> getEncoder() {
		return encoder;
	}

	/**
	 * Set the logback Encoder for the appender.
	 * 
	 * Needs to be called (via the <encoder class="..."> element in the
	 * logback.xml config file) before the appender can be started.
	 * 
	 * @param encoder
	 */
	public void setEncoder(Encoder<E> encoder) {
		this.encoder = encoder;
	}
}
