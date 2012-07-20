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
package net.pms.encoders;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JComponent;

import net.pms.api.PmsConfiguration;
import net.pms.api.io.PipeProcessFactory;
import net.pms.api.io.ProcessWrapperFactory;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;

@Singleton
public class MPlayerWebAudio extends MPlayerAudio {
	public static final String ID = "mplayerwebaudio";

	private final PmsConfiguration configuration;
	private final ProcessWrapperFactory processWrapperFactory;
	private final PipeProcessFactory pipeProcessFactory;

	@Inject
	public MPlayerWebAudio(PmsConfiguration configuration,
			ProcessWrapperFactory processWrapperFactory,
			PipeProcessFactory pipeProcessFactory) {
		super(configuration, processWrapperFactory, pipeProcessFactory);
	
		this.configuration = configuration;
		this.processWrapperFactory = processWrapperFactory;
		this.pipeProcessFactory = pipeProcessFactory;
	}

	@Override
	public JComponent config() {
		return null;
	}

	@Override
	public int purpose() {
		return AUDIO_WEBSTREAM_PLAYER;
	}

	@Override
	public String id() {
		return "mplayerwebaudio";
	}

	@Override
	public ProcessWrapper launchTranscode(String fileName, DLNAResource dlna, DLNAMediaInfo media,
		OutputParams params) throws IOException {
		params.minBufferSize = params.minFileSize;
		params.secondread_minsize = 100000;
		params.waitbeforestart = 8000;
		return super.launchTranscode(fileName, dlna, media, params);
	}

	@Override
	public String name() {
		return "MPlayer Web";
	}

	@Override
	public boolean isTimeSeekable() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCompatible(DLNAMediaInfo mediaInfo) {
		if (mediaInfo != null) {
			// TODO: Determine compatibility based on mediaInfo
			return false;
		} else {
			// No information available
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCompatible(Format format) {
		if (format != null && format.getType() == Format.AUDIO) {
			Format.Identifier id = format.getIdentifier();

			if (id.equals(Format.Identifier.WEB)) {
				return true;
			}
		}

		return false;
	}
}
