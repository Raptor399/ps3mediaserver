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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package net.pms.formats;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.pms.di.InjectionHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * This class matches and instantiates formats.
 */
@Singleton
public final class FormatFactory {
	/**
	 * Logger used for all logging.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(FormatFactory.class);

	/**
	 * Initial list of known formats.
	 */
	private final List<Format> formats = new ArrayList<Format>(); 

	@Inject
	FormatFactory() {
		Injector injector = InjectionHelper.getInjector();
		formats.add(injector.getInstance(WEB.class));
		formats.add(injector.getInstance(MKV.class));
		formats.add(injector.getInstance(M4A.class));
		formats.add(injector.getInstance(MP3.class));
		formats.add(injector.getInstance(ISO.class));
		formats.add(injector.getInstance(MPG.class));
		formats.add(injector.getInstance(WAV.class));
		formats.add(injector.getInstance(JPG.class));
		formats.add(injector.getInstance(OGG.class));
		formats.add(injector.getInstance(PNG.class));
		formats.add(injector.getInstance(GIF.class));
		formats.add(injector.getInstance(TIF.class));
		formats.add(injector.getInstance(FLAC.class));
		formats.add(injector.getInstance(DVRMS.class));
		formats.add(injector.getInstance(RAW.class));
	}

	/**
	 * Match a given filename to all known formats and return a fresh instance
	 * of that format. Matching is done by the extension (e.g. ".gif") or
	 * protocol (e.g. "http://") of the filename. Will return <code>null</code>
	 * if no match can be made.
	 * 
	 * @param filename The filename to match.
	 * @return The format.
	 * @see Format#match(String)
	 */
	public Format getAssociatedFormat(final String filename) {
		for (Format ext : formats) {
			if (ext.match(filename)) {
				LOGGER.trace("Matched format " + ext + " to \"" + filename + "\"");

				// Return a fresh instance
				return ext.duplicate();
			}
		}

		LOGGER.trace("Could not match any format to \"" + filename + "\"");
		return null;
	}

	/**
	 * Returns the list of known formats.
	 *
	 * @return The list of known formats.
	 */
	public List<Format> getFormats() {
		return formats;
	}
}

