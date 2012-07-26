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
 */package net.pms.formats;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.pms.api.PmsConfiguration;
import net.pms.api.PmsCore;
import net.pms.di.InjectionHelper;

import com.google.inject.Injector;

@Singleton
public class FLAC extends OGG {
	/**
	 * {@inheritDoc} 
	 */
	@Override
	public Identifier getIdentifier() {
		return Identifier.FLAC;
	}

	@Inject
	public FLAC(PmsCore pmsCore, PmsConfiguration configuration) {
		super(pmsCore, configuration);
		type = AUDIO;
		Injector injector = InjectionHelper.getInjector();
		secondaryFormat = injector.getInstance(AudioAsVideo.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getId() {
		return new String[] { "flac", "mlp", "fla" };
	}
}
