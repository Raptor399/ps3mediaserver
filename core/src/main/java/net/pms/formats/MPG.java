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
package net.pms.formats;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.pms.api.PmsConfiguration;
import net.pms.api.PmsCore;
import net.pms.configuration.RendererConfiguration;
import net.pms.di.InjectionHelper;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.encoders.FFMpegAviSynthVideo;
import net.pms.encoders.FFMpegVideo;
import net.pms.encoders.MEncoderAviSynth;
import net.pms.encoders.MEncoderVideo;
import net.pms.encoders.Player;
import net.pms.encoders.TSMuxerVideo;

@Singleton
public class MPG extends Format {
	private final PmsCore pmsCore;
	private final PmsConfiguration configuration;

	@Inject
	public MPG(PmsCore pmsCore, PmsConfiguration configuration) {
		this.pmsCore = pmsCore;
		this.configuration = configuration;
		type = VIDEO;
	}

	/**
	 * {@inheritDoc} 
	 */
	@Override
	public Identifier getIdentifier() {
		return Identifier.MPG;
	}

	@Override
	public ArrayList<Class<? extends Player>> getProfiles() {
		if (configuration.getEnginesAsList(pmsCore.getRegistry()) == null
				|| configuration.getEnginesAsList(pmsCore.getRegistry()).isEmpty()
				|| configuration.getEnginesAsList(pmsCore.getRegistry()).contains("none")) {
			return null;
		}

		ArrayList<Class<? extends Player>> a = new ArrayList<Class<? extends Player>>();

		for (String engine : configuration.getEnginesAsList(pmsCore.getRegistry())) {
			if (engine.equals(MEncoderVideo.ID)) {
				a.add(MEncoderVideo.class);
			} else if (engine.equals(MEncoderAviSynth.ID) && pmsCore.getRegistry().isAvis()) {
				a.add(MEncoderAviSynth.class);
			} else if (engine.equals(FFMpegVideo.ID)) {
				a.add(FFMpegVideo.class);
			} else if (engine.equals(FFMpegAviSynthVideo.ID) && pmsCore.getRegistry().isAvis()) {
				a.add(FFMpegAviSynthVideo.class);
			} else if (engine.equals(TSMuxerVideo.ID)/* && pmsCore.isWindows()*/) {
				a.add(TSMuxerVideo.class);
			}
		}
		return a;
	}

	@Override
	public boolean transcodable() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getId() {
		return new String[] { "mpg", "mpeg", "mpe", "mod", "tivo", "ty", "tmf",
				"ts", "tp", "m2t", "m2ts", "m2p", "mts", "mp4", "m4v", "avi",
				"wmv", "wm", "vob", "divx", "div", "vdr" };
	}

	/**
	 * @deprecated Use {@link #isCompatible(DLNAMediaInfo, RendererConfiguration)} instead.
	 * <p>
	 * Returns whether or not a format can be handled by the PS3 natively.
	 * This means the format can be streamed to PS3 instead of having to be
	 * transcoded.
	 * 
	 * @return True if the format can be handled by PS3, false otherwise.
	 */
	@Deprecated
	@Override
	public boolean ps3compatible() {
		return true;
	}
}
