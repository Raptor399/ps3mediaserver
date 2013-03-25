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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package net.pms.mediainfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNoException;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Set;

import net.pms.mediainfo.MediaInfo.InfoKind;
import net.pms.mediainfo.MediaInfoWrapper.StreamType;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

public class MediaInfoWrapperTest {
	/**
	 * Set up testing conditions before running the tests.
	 */
	@Before
	public final void setUp() {
		// Silence all log messages from the PMS code that is being tested
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.reset();
	}

	/**
	 * Only testing the constructor with a mock MediaInfo library, as testing
	 * with the real thing would require the library to be available at test
	 * time. On Linux, the test would fail. 
	 */
	@Test
	public void testConstructorSetup() {
		MediaInfo mediaInfo = mock(MediaInfo.class);
		String filename = "testfile.avi";

		when(mediaInfo.Option("Info_Version")).thenReturn("0.7.58");
		when(mediaInfo.Open(filename)).thenReturn(1);

		// Initialize the info
		MediaInfoWrapper info = new MediaInfoWrapper(mediaInfo, filename);

		verify(mediaInfo).Option("Complete", "1");
		verify(mediaInfo).Option("Language", "raw");
		verify(mediaInfo).Open(filename);
		verify(mediaInfo).Close();
	}

	/**
	 * Test what happens when the library somehow does not initialize
	 * correctly.
	 */
	@Test
	public void testUninitializedLibrary() {
		MediaInfo mediaInfo = mock(MediaInfo.class);

		when(mediaInfo.Option("Info_Version")).thenReturn("0.7.58");

		// Initialize the info.
		MediaInfoWrapper info = new MediaInfoWrapper(mediaInfo, "testfile.avi");

		assertNull(info.getStringValue(null,  0, "unknown_key"));
		assertNull(info.getStringValue(StreamType.GENERAL, 0, null));
		assertNull(info.getStringValue(StreamType.GENERAL, 0, "unknown_key"));
		assertNull(info.getStringValue(StreamType.GENERAL, 1, "unknown_key"));

		assertEquals(0, info.getStreamCount(StreamType.AUDIO));
		assertEquals(0, info.getStreamCount(StreamType.GENERAL));
		assertEquals(0, info.getStreamCount(StreamType.IMAGE));
		assertEquals(0, info.getStreamCount(StreamType.MENU));
		assertEquals(0, info.getStreamCount(StreamType.OTHER));
		assertEquals(0, info.getStreamCount(StreamType.TEXT));
		assertEquals(0, info.getStreamCount(StreamType.VIDEO));

		assertNull(info.getStreamKeys(StreamType.AUDIO, 0));
		assertNull(info.getStreamKeys(StreamType.GENERAL, 0));
		assertNull(info.getStreamKeys(StreamType.IMAGE, 0));
		assertNull(info.getStreamKeys(StreamType.MENU, 0));
		assertNull(info.getStreamKeys(StreamType.OTHER, 0));
		assertNull(info.getStreamKeys(StreamType.TEXT, 0));
		assertNull(info.getStreamKeys(StreamType.VIDEO, 0));
	}

	@Test
	public void testGetStringValue() {
		StreamType streamType = StreamType.GENERAL;
		int streamNumber = 0;
		String key = "key";
		String value = "value";

		MediaInfo mediaInfo = createSimpleKeyValueMock(streamType, streamNumber, key, value);

		// Initialize the info.
		MediaInfoWrapper info = new MediaInfoWrapper(mediaInfo, "testfile.avi");

		assertEquals(value, info.getStringValue(streamType, streamNumber, key));

		// Edge cases
		assertNull(info.getStringValue(StreamType.AUDIO, streamNumber, key));
		assertNull(info.getStringValue(streamType, streamNumber, "unknown_key"));
		assertNull(info.getStringValue(streamType, streamNumber + 1, key));
		assertNull(info.getStringValue(streamType, -1, key));
		assertNull(info.getStringValue(null, streamNumber, key));
		assertNull(info.getStringValue(null, streamNumber, key));
		assertNull(info.getStringValue(streamType, streamNumber, null));
		assertNull(info.getStringValue(streamType, streamNumber, ""));
	}

	@Test
	public void testGetIntValue() {
		StreamType streamType = StreamType.GENERAL;
		int streamNumber = 0;
		String key = "key";
		String value = "17";

		MediaInfo mediaInfo = createSimpleKeyValueMock(streamType, streamNumber, key, value);

		// Initialize the info.
		MediaInfoWrapper info = new MediaInfoWrapper(mediaInfo, "testfile.avi");

		assertEquals(Integer.parseInt(value), info.getIntValue(streamType, streamNumber, key));

		// Edge cases
		assertEquals(0, info.getIntValue(StreamType.AUDIO, streamNumber, key));
		assertEquals(0, info.getIntValue(streamType, streamNumber, "unknown_key"));
		assertEquals(0, info.getIntValue(streamType, streamNumber + 1, key));
		assertEquals(0, info.getIntValue(streamType, -1, key));
		assertEquals(0, info.getIntValue(null, streamNumber, key));
		assertEquals(0, info.getIntValue(null, streamNumber, key));
		assertEquals(0, info.getIntValue(streamType, streamNumber, null));
		assertEquals(0, info.getIntValue(streamType, streamNumber, ""));
	}

	@Test
	public void testGetBrokenIntValue() {
		MediaInfo mediaInfo = createSimpleKeyValueMock(StreamType.GENERAL, 0, "key", "not_an_int");

		// Initialize the info.
		MediaInfoWrapper info = new MediaInfoWrapper(mediaInfo, "testfile.avi");

		// Cannot parse an int from the key value.
		assertEquals(0, info.getIntValue(StreamType.GENERAL, 0, "key"));
	}

	@Test
	public void testGetStreamKeys() {
		MediaInfo mediaInfo = createSimpleKeyValueMock(StreamType.GENERAL, 0, "key", "value");

		// Initialize the info.
		MediaInfoWrapper info = new MediaInfoWrapper(mediaInfo, "testfile.avi");
		
		// There should be exactly one key for StreamType.GENERAL.
		Set<String> keys = info.getStreamKeys(StreamType.GENERAL, 0);
		assertNotNull(keys);
		assertEquals(1, keys.size());
		assertEquals("key", keys.iterator().next());

		// Edge cases
		assertNull(info.getStreamKeys(StreamType.GENERAL, 1));
		assertNull(info.getStreamKeys(StreamType.GENERAL, -1));
		assertNull(info.getStreamKeys(StreamType.AUDIO, 0));
		assertNull(info.getStreamKeys(StreamType.IMAGE, 0));
		assertNull(info.getStreamKeys(StreamType.MENU, 0));
		assertNull(info.getStreamKeys(StreamType.OTHER, 0));
		assertNull(info.getStreamKeys(StreamType.TEXT, 0));
		assertNull(info.getStreamKeys(StreamType.VIDEO, 0));
		assertNull(info.getStreamKeys(null, 0));
	}

	@Test
	public void testGetStreamCount() {
		MediaInfo mediaInfo = createSimpleKeyValueMock(StreamType.VIDEO, 3, "key", "value");

		// Initialize the info.
		MediaInfoWrapper info = new MediaInfoWrapper(mediaInfo, "testfile.avi");
		
		assertEquals(4, info.getStreamCount(StreamType.VIDEO));

		// Edge cases
		assertEquals(0, info.getStreamCount(null));
		assertEquals(0, info.getStreamCount(StreamType.AUDIO));
		assertEquals(0, info.getStreamCount(StreamType.GENERAL));
		assertEquals(0, info.getStreamCount(StreamType.IMAGE));
		assertEquals(0, info.getStreamCount(StreamType.MENU));
		assertEquals(0, info.getStreamCount(StreamType.OTHER));
		assertEquals(0, info.getStreamCount(StreamType.TEXT));
	}

	@Test
	public void testKeyExists() {
		MediaInfo mediaInfo = createSimpleKeyValueMock(StreamType.GENERAL, 0, "key", "value");

		// Initialize the info.
		MediaInfoWrapper info = new MediaInfoWrapper(mediaInfo, "testfile.avi");
		
		assertTrue(info.keyExists(StreamType.GENERAL, 0, "key"));

		// Edge cases
		assertFalse(info.keyExists(StreamType.GENERAL, 0, null));
		assertFalse(info.keyExists(StreamType.GENERAL, 0, ""));
		assertFalse(info.keyExists(StreamType.GENERAL, 0, "unknown_key"));
		assertFalse(info.keyExists(StreamType.GENERAL, 1, "key"));
		assertFalse(info.keyExists(StreamType.AUDIO, 0, "key"));
	}

	/**
	 * The MediaInfo library is an external library that is regularly updated
	 * and that might change its behavior over different versions. To be aware
	 * of relevant changes, test interesting properties of known files.
	 */
	@Test
	public void testKnownVideoFile() {
		MediaInfoWrapper info = null;
		String filename = "src/main/resources/videos/action_success-512.mpg";

		// Make sure the file exists
		File file = new File(filename);
		assertEquals(true, file.exists());

		try {
			info = new MediaInfoWrapper(filename);
		} catch (NoClassDefFoundError e) {
			// Halt and ignore this test if the MediaInfo library cannot be loaded. 
			assumeNoException(e);
		}

		// To see the properties, outcomment the following. 
		//System.out.println(info.toString());

		assertEquals(0, info.getStreamCount(StreamType.AUDIO));
		assertEquals(1, info.getStreamCount(StreamType.GENERAL));
		assertEquals(0, info.getStreamCount(StreamType.IMAGE));
		assertEquals(0, info.getStreamCount(StreamType.MENU));
		assertEquals(0, info.getStreamCount(StreamType.OTHER));
		assertEquals(0, info.getStreamCount(StreamType.TEXT));
		assertEquals(1, info.getStreamCount(StreamType.VIDEO));
		assertEquals("VBR", info.getStringValue(StreamType.GENERAL, 0, "OverallBitRate_Mode"));
		assertEquals(507904, info.getIntValue(StreamType.GENERAL, 0, "OverallBitRate"));
		assertEquals("MPEG Video", info.getStringValue(StreamType.VIDEO, 0, "Format"));
		assertEquals(1000, info.getIntValue(StreamType.VIDEO, 0, "Duration"));
		assertEquals(512, info.getIntValue(StreamType.VIDEO, 0, "Width"));
		assertEquals(512, info.getIntValue(StreamType.VIDEO, 0, "Height"));
		assertEquals("24.000", info.getStringValue(StreamType.VIDEO, 0, "FrameRate"));
	}

	/**
	 * Create a mock {@link MediaInfo} library object containing one key value
	 * pair for the given stream.
	 * 
	 * @param streamType The {@link StreamType} of the stream.
	 * @param streamNumber The stream number for the stream.
	 * @param key
	 * @param value
	 * @return
	 */
	private MediaInfo createSimpleKeyValueMock(StreamType streamType, int streamNumber, String key, String value) {
		MediaInfo mediaInfo = mock(MediaInfo.class);

		when(mediaInfo.Option("Info_Version")).thenReturn("0.7.58");
		when(mediaInfo.Open(anyString())).thenReturn(1);

		if (key != null && value != null) {
			when(mediaInfo.Get(streamType.getStreamKind(), 0, "StreamCount")).thenReturn("" + (streamNumber + 1));
			when(mediaInfo.Count_Get(streamType.getStreamKind(), streamNumber)).thenReturn(1);
			when(mediaInfo.Get(streamType.getStreamKind(), streamNumber, 0, InfoKind.Name)).thenReturn(key);
			when(mediaInfo.Get(streamType.getStreamKind(), streamNumber, 0, InfoKind.Text)).thenReturn(value);
		}

		return mediaInfo;
	}
}
