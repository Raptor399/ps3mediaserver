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

import static org.junit.Assert.assertEquals;
import net.pms.di.PmsGuice;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

import com.google.inject.Injector;


/**
 * Test basic functionality of {@link Format}.
 */
public class FormatTest {
	private Injector injector;

	@Before
	public void setUp() {
		// Silence all log messages from the PMS code that is being tested
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

		// Instantiate Guice because some classes use InjectionHelper.getInjector()
		injector = new PmsGuice().getInjector();
	}

    /**
     * Test edge cases for {@link Format#match(String)}.
     */
    @Test
	public void testFormatEdgeCases() {
    	MP3 mp3 = injector.getInstance(MP3.class);
    
    	// Empty string
		assertEquals("MP3 does not match \"\"", false, mp3.match(""));

    	// Null string
		assertEquals("MP3 does not match null", false, mp3.match(null));

    	TIF tif = injector.getInstance(TIF.class);

		// Mixed case
		assertEquals("TIFF matches \"tEsT.TiFf\"", true, tif.match("tEsT.TiFf"));

		// Starting with identifier instead of ending
		assertEquals("TIFF does not match \"tiff.test\"", false, tif.match("tiff.test"));

		// Substring
		assertEquals("TIFF does not match \"not.tiff.but.mp3\"", false, tif.match("not.tiff.but.mp3"));
    }
    
    /**
     * Test if {@link Format#match(String)} manages to match the identifiers
     * specified in each format with getId().
     */
    @Test
	public void testFormatIdentifiers() {
		// Identifier tests based on the identifiers defined in getId() of each class
    	DVRMS dvrms = injector.getInstance(DVRMS.class);
		assertEquals("DVRMS matches \"test.dvr\"", true, dvrms.match("test.dvr"));

		FLAC flac = injector.getInstance(FLAC.class);
		assertEquals("FLAC matches \"test.flac\"", true, flac.match("test.flac"));

		GIF gif = injector.getInstance(GIF.class);
		assertEquals("GIF matches \"test.gif\"", true, gif.match("test.gif"));
		assertEquals("ISO matches \"test.iso\"", true, new ISO().match("test.iso"));
		assertEquals("JPG matches \"test.jpg\"", true, new JPG().match("test.jpg"));
		assertEquals("M4A matches \"test.wma\"", true, new M4A().match("test.wma"));
		assertEquals("MKV matches \"test.mkv\"", true, new MKV().match("test.mkv"));
		assertEquals("MP3 matches \"test.mp3\"", true, new MP3().match("test.mp3"));
		assertEquals("MPG matches \"test.mpg\"", true, injector.getInstance(MPG.class).match("test.mpg"));
		assertEquals("OGG matches \"test.ogg\"", true, new OGG().match("test.ogg"));
		assertEquals("PNG matches \"test.png\"", true, new PNG().match("test.png"));
		assertEquals("RAW matches \"test.arw\"", true, new RAW().match("test.arw"));
		assertEquals("TIF matches \"test.tiff\"", true, new TIF().match("test.tiff"));
		assertEquals("WAV matches \"test.wav\"", true, new WAV().match("test.wav"));
		assertEquals("WEB matches \"http\"", true, new WEB().match("http://test.org/"));
	}
}