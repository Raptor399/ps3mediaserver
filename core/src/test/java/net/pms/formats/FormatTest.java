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
		assertEquals("DVRMS matches \"test.dvr\"", true, injector.getInstance(DVRMS.class).match("test.dvr"));
		assertEquals("FLAC matches \"test.flac\"", true, injector.getInstance(FLAC.class).match("test.flac"));
		assertEquals("GIF matches \"test.gif\"", true, injector.getInstance(GIF.class).match("test.gif"));
		assertEquals("ISO matches \"test.iso\"", true, injector.getInstance(ISO.class).match("test.iso"));
		assertEquals("JPG matches \"test.jpg\"", true, injector.getInstance(JPG.class).match("test.jpg"));
		assertEquals("M4A matches \"test.wma\"", true, injector.getInstance(M4A.class).match("test.wma"));
		assertEquals("MKV matches \"test.mkv\"", true, injector.getInstance(MKV.class).match("test.mkv"));
		assertEquals("MP3 matches \"test.mp3\"", true, injector.getInstance(MP3.class).match("test.mp3"));
		assertEquals("MPG matches \"test.mpg\"", true, injector.getInstance(MPG.class).match("test.mpg"));
		assertEquals("OGG matches \"test.ogg\"", true, injector.getInstance(OGG.class).match("test.ogg"));
		assertEquals("PNG matches \"test.png\"", true, injector.getInstance(PNG.class).match("test.png"));
		assertEquals("RAW matches \"test.arw\"", true, injector.getInstance(RAW.class).match("test.arw"));
		assertEquals("TIF matches \"test.tiff\"", true, injector.getInstance(TIF.class).match("test.tiff"));
		assertEquals("WAV matches \"test.wav\"", true, injector.getInstance(WAV.class).match("test.wav"));
		assertEquals("WEB matches \"http\"", true, injector.getInstance(WEB.class).match("http://test.org/"));
	}
}
