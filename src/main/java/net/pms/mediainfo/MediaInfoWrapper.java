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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.pms.mediainfo.MediaInfo.InfoKind;
import net.pms.mediainfo.MediaInfo.StreamKind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

/**
 * This class adds a level of abstraction to hide the {@link MediaInfo}
 * implementation details.
 * <p>
 * Example usage:
 * <pre>
 * MediaInfoWrapper info = new MediaInfoWrapper("big_buck_bunny.mkv");
 * 
 * for (StreamType streamType : StreamType.values()) {
 *     System.out.println("\n" + streamType.name());
 *
 *     for (int i = 0; i < info.getStreamCount(streamType); i++) {
 *         for (String key : info.getStreamKeys(streamType, i)) {
 *             System.out.println(key + ": '" + info.getStringValue(streamType, i, key) + "'");
 *         }
 *     }
 * }
 * </pre>
 */
public class MediaInfoWrapper {
	
	/** Logger to write messages to the logs. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MediaInfoWrapper.class);

	/** Singleton instance of the JNA wrapper for the MediaInfo DLL. */
	private static MediaInfo myMediaInfo;

	/**
	 * Holds the file information after it has been examined by the MediaInfo
	 * library. This information is supposed to be immutable after it has been
	 * initialized.
	 */
	private Map<StreamType, List<Map<String, String>>> fileInfoMap = null;

	/**
	 * Media files contain one or more streams, each with its own information.
	 * This type denotes every available form of stream.
	 */
	public enum StreamType {
		
		/** The general stream type. */
		GENERAL(StreamKind.General),
		
		/** The video stream type. */
		VIDEO(StreamKind.Video),
		
		/** The audio stream type. */
		AUDIO(StreamKind.Audio),
		
		/** The text stream type. */
		TEXT(StreamKind.Text),
		
		/** The other stream type. */
		OTHER(StreamKind.Other),
		
		/** The image stream type. */
		IMAGE(StreamKind.Image),
		
		/** The menu stream type. */
		MENU(StreamKind.Menu);

		/**
		 * Constructor to hide {@link MediaInfo.StreamKind} type.
		 *
		 * @param streamKind the stream kind
		 */
		StreamType(StreamKind streamKind) {
			this.streamKind = streamKind;
		}

		/** The stream kind. */
		private final StreamKind streamKind;

		/**
		 * Gets the stream kind.
		 *
		 * @return the stream kind
		 */
		public StreamKind getStreamKind() {
			return streamKind;
		}
	}

	/**
	 * Read the file information and stores it for later examination.
	 * 
	 * @param filename
	 *            The path and filename of the file to be examined.
	 */
	public MediaInfoWrapper(String filename) {
		this(null, filename);
	}

	/**
	 * Default constructor. Reads the file information using the provided
	 * {@link MediaInfo} library and stores it for later examination.
	 * 
	 * @param mediaInfo
	 *            The MediaInfo library. If <code>null</code>, a new singleton
	 *            instance will be initialized.
	 * @param filename
	 *            The path and filename of the file to be examined.
	 */
	MediaInfoWrapper(MediaInfo mediaInfo, String filename) {
		if (mediaInfo == null) {
			LOGGER.info("Loading MediaInfo library");

			// Initialize the singleton instance
			try {
				mediaInfo = new MediaInfo();
			} catch (Throwable t) {
				LOGGER.error("Error loading MediaInfo library", t);

				if (!Platform.isWindows() && !Platform.isMac()) {
					LOGGER.info("Make sure you have libmediainfo and libzen installed");
				}

				LOGGER.info("The server will now use the less accurate ffmpeg parsing method");
			}
		}

		myMediaInfo = mediaInfo;

		if (mediaInfo != null) {
			LOGGER.info("Loaded " + mediaInfo.Option("Info_Version"));

			// Set library options
			mediaInfo.Option("Complete", "1");
			mediaInfo.Option("Language", "raw");

			// Analyze the file
			initialize(filename);
		}

	}

	/**
	 * Extract the media information from the filename. This method is
	 * synchronized to ensure that only one file is analyzed at the same time by
	 * the MediaInfo library.
	 * 
	 * @param filename
	 *            The path and file name of the file to be examined.
	 */
	private synchronized void initialize(String filename) {
		if (myMediaInfo.Open(filename) > 0) {
			fileInfoMap = initStreamInfoMap();
			myMediaInfo.Close();
		}
	}

	/**
	 * Returns a map of the available information per available stream. This
	 * method assumes a file has already been opened by the MediaInfo library.
	 * 
	 * @return The stream information map.
	 */
	private Map<StreamType, List<Map<String, String>>> initStreamInfoMap() {
		Map<StreamType, List<Map<String, String>>> streamMap = 
				new EnumMap<StreamType, List<Map<String, String>>>(StreamType.class);

		for (StreamType streamType : StreamType.values()) {
			int streamCount = getStreamCountFromMediaInfo(streamType);

			if (streamCount > 0) {
				List<Map<String, String>> streamInfoList = new ArrayList<Map<String, String>>(streamCount);

				for (int i = 0; i < streamCount; i++) {
					streamInfoList.add(initStreamInfo(streamType, i));
				}

				streamMap.put(streamType, streamInfoList);
			}
		}

		return streamMap;
	}

	/**
	 * Returns a map of the available information for the provided stream. This
	 * method assumes a file has already been opened by the MediaInfo library.
	 *
	 * @param streamType the stream type
	 * @param streamNumber the stream number
	 * @return The information map for the stream.
	 */
	private Map<String, String> initStreamInfo(StreamType streamType, int streamNumber) {
		Map<String, String> streamInfo = new LinkedHashMap<String, String>();
		int infoCount = myMediaInfo.Count_Get(streamType.getStreamKind(), streamNumber);

		for (int i = 0; i < infoCount; i++) {
			String value = myMediaInfo.Get(streamType.getStreamKind(), streamNumber, i, InfoKind.Text);

			if (value.length() > 0) {
				streamInfo.put(myMediaInfo.Get(streamType.getStreamKind(), streamNumber, i, InfoKind.Name), value);
			}
		}

		return streamInfo;
	}

	/**
	 * Returns the number of streams of the given type as reported by the
	 * mediaInfo object.
	 * 
	 * @param streamType
	 *            The stream type
	 * @return The number of streams.
	 */
	private int getStreamCountFromMediaInfo(StreamType streamType) {
		try {
			return Integer.parseInt(myMediaInfo.Get(streamType.getStreamKind(), 0, "StreamCount"));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Returns the number of streams for a particular stream type.
	 * 
	 * @param streamType
	 *            The {@link StreamType} of the stream.
	 * @return The number of available streams of that type.
	 */
	public int getStreamCount(StreamType streamType) {
		if (fileInfoMap != null && fileInfoMap.containsKey(streamType)) {
			return fileInfoMap.get(streamType).size();
		}

		return 0;
	}

	/**
	 * Returns the available key names for the given stream.
	 * 
	 * @param streamType
	 *            The {@link StreamType} of the stream.
	 * @param streamNumber
	 *            A file can contain multiple streams of the same type. This
	 *            indicates which of the streams of the indicated type should be
	 *            used.
	 * @return A set of all key strings, or <code>null</code> if the information
	 *         is not available.
	 */
	public Set<String> getStreamKeys(StreamType streamType, int streamNumber) {
		if (fileInfoMap != null && fileInfoMap.containsKey(streamType)) {
			// Single out the information lists for the indicated stream type
			List<Map<String, String>> streamInfoList = fileInfoMap.get(streamType);

			if (streamInfoList != null && streamNumber >= 0 && streamNumber < streamInfoList.size()) {
				return streamInfoList.get(streamNumber).keySet();
			}
		}

		return null;
	}

	/**
	 * Determine whether a key value pair exists for the given stream type and
	 * index.
	 * 
	 * @param streamType
	 *            The {@link StreamType} of the stream.
	 * @param streamNumber
	 *            A file can contain multiple streams of the same type. This
	 *            indicates which of the streams of the indicated type should be
	 *            used.
	 * @param key
	 *            The name of the key that holds the value.
	 * @return True if the key exists, false otherwise.
	 */
	public boolean keyExists(StreamType streamType, int streamNumber, String key) {
		if (fileInfoMap != null && fileInfoMap.containsKey(streamType)) {
			// Single out the information lists for the indicated stream type
			List<Map<String, String>> streamInfoList = fileInfoMap.get(streamType);

			if (streamInfoList != null && streamNumber >= 0 && streamNumber < streamInfoList.size()) {
				// Single out the information for the indicated list
				Map<String, String> streamInfo = streamInfoList.get(streamNumber);

				return streamInfo.containsKey(key);
			}
		}

		return false;
	}

	/**
	 * Returns the string value of a key, based on the type of stream the
	 * number of the stream.
	 * 
	 * @param streamType
	 *            The {@link StreamType} of the stream.
	 * @param streamNumber
	 *            A file can contain multiple streams of the same type. This
	 *            indicates which of the streams of the indicated type should be
	 *            used.
	 * @param key
	 *            The name of the key that holds the value.
	 * @return The string value for the key if it exists. Otherwise
	 *         <code>null</code> is returned.
	 */
	public String getStringValue(StreamType streamType, int streamNumber, String key) {
		if (fileInfoMap != null && fileInfoMap.containsKey(streamType)) {
			// Single out the information lists for the indicated stream type
			List<Map<String, String>> streamInfoList = fileInfoMap.get(streamType);

			if (streamInfoList != null && streamNumber >= 0 && streamNumber < streamInfoList.size()) {
				// Single out the information for the indicated list
				Map<String, String> streamInfo = streamInfoList.get(streamNumber);

				if (key != null) {
					return streamInfo.get(key);
				}
			}
		}

		return null;
	}

	/**
	 * Returns the int value of a key, based on the type of stream and the
	 * number of the stream.
	 * 
	 * @param streamType
	 *            The {@link StreamType} of the stream.
	 * @param streamNumber
	 *            A file can contain multiple streams of the same type. This
	 *            indicates which of the streams of the indicated type should be
	 *            used.
	 * @param key
	 *            The name of the key that holds the value.
	 * @return The int value for the key if it exists and can be parsed.
	 *         Otherwise 0 is returned.
	 */
	public int getIntValue(StreamType streamType, int streamNumber, String key) {
		String stringValue = getStringValue(streamType, streamNumber, key);

		if (stringValue != null) {
			try {
				int result = Integer.parseInt(stringValue);
				return result;
			} catch (NumberFormatException e) {
				// Do nothing.
			}
		}

		return 0;
	}

	@Override
	public String toString() {
		if (fileInfoMap == null) {
			return "{}";
		}

		return fileInfoMap.toString();
	}
}
