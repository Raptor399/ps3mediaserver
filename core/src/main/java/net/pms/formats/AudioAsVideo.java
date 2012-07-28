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

import net.pms.PMS;
import net.pms.api.PmsConfiguration;
import net.pms.api.PmsCore;
import net.pms.encoders.Player;
import net.pms.encoders.TsMuxerAudio;

@Singleton
public class AudioAsVideo extends MKV {
	private final PmsCore pmsCore;
	private final PmsConfiguration configuration;

	@Inject
	protected AudioAsVideo(PmsCore pmsCore, PmsConfiguration configuration) {
		super(pmsCore, configuration);
		this.pmsCore = pmsCore;
		this.configuration = configuration;
	}

	/**
	 * {@inheritDoc} 
	 */
	@Override
	public Identifier getIdentifier() {
		return Identifier.AUDIO_AS_VIDEO;
	}

	@Override
	public ArrayList<Class<? extends Player>> getProfiles() {
		ArrayList<Class<? extends Player>> a = new ArrayList<Class<? extends Player>>();

		for (String engine : configuration.getEnginesAsList(pmsCore.getRegistry())) {
			if (engine.equals(TsMuxerAudio.ID)) {
				a.add(TsMuxerAudio.class);
			}
		}
		return a;
	}
}
