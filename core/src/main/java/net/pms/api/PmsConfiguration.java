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
package net.pms.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.pms.configuration.IpFilter;
import net.pms.io.SystemUtils;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.event.ConfigurationListener;

public interface PmsConfiguration {

	public File getTempFolder() throws IOException;

	public String getVlcPath();

	public void disableVlc();

	public String getEac3toPath();

	public String getMencoderPath();

	public int getMencoderMaxThreads();

	public String getDCRawPath();

	public void disableMEncoder();

	public String getFfmpegPath();

	public void disableFfmpeg();

	public String getMplayerPath();

	public void disableMplayer();

	public String getTsmuxerPath();

	public String getFlacPath();

	/**
	 * If the framerate is not recognized correctly and the video runs too fast or too
	 * slow, tsMuxeR can be forced to parse the fps from FFmpeg. Default value is true.
	 * @return True if tsMuxeR should parse fps from FFmpeg.
	 */
	public boolean isTsmuxerForceFps();

	/**
	 * Force tsMuxeR to mux all audio tracks.
	 * TODO: Remove this redundant code.
	 * @return True
	 */
	public boolean isTsmuxerPreremuxAc3();

	/**
	 * The AC3 audio bitrate determines the quality of digital audio sound. An AV-receiver
	 * or amplifier has to be capable of playing this quality. Default value is 640.
	 * @return The AC3 audio bitrate.
	 */
	public int getAudioBitrate();

	/**
	 * Force tsMuxeR to mux all audio tracks.
	 * TODO: Remove this redundant code; getter always returns true.
	 */
	public void setTsmuxerPreremuxAc3(boolean value);

	/**
	 * If the framerate is not recognized correctly and the video runs too fast or too
	 * slow, tsMuxeR can be forced to parse the fps from FFmpeg.
	 * @param value Set to true if tsMuxeR should parse fps from FFmpeg.
	 */
	public void setTsmuxerForceFps(boolean value);

	/**
	 * The server port where PMS listens for TCP/IP traffic. Default value is 5001.
	 * @return The port number.
	 */
	public int getServerPort();

	/**
	 * Set the server port where PMS must listen for TCP/IP traffic.
	 * @param value The TCP/IP port number.
	 */
	public void setServerPort(int value);

	/**
	 * The hostname of the server.
	 * @return The hostname if it is defined, otherwise <code>null</code>.
	 */
	public String getServerHostname();

	/**
	 * Set the hostname of the server.
	 * @param value The hostname.
	 */
	public void setHostname(String value);

	/**
	 * The TCP/IP port number for a proxy server. Default value is -1.
	 * TODO: Is this still used?
	 * @return The proxy port number.
	 */
	public int getProxyServerPort();

	/**
	 * Get the code of the preferred language for the PMS user interface. Default
	 * is based on the locale.
	 * @return The ISO 639 language code.
	 */
	public String getLanguage();

	/**
	 * Returns the preferred minimum size for the transcoding memory buffer in megabytes.
	 * Default value is 12.
	 * @return The minimum memory buffer size.
	 */
	public int getMinMemoryBufferSize();

	/**
	 * Returns the preferred maximum size for the transcoding memory buffer in megabytes.
	 * The value returned has a top limit of {@link #MAX_MAX_MEMORY_BUFFER_SIZE}. Default
	 * value is 400.
	 *
	 * @return The maximum memory buffer size.
	 */
	public int getMaxMemoryBufferSize();

	/**
	 * Returns the top limit that can be set for the maximum memory buffer size.
	 * @return The top limit.
	 */
	public String getMaxMemoryBufferSizeStr();

	/**
	 * Set the preferred maximum for the transcoding memory buffer in megabytes. The top
	 * limit for the value is {@link #MAX_MAX_MEMORY_BUFFER_SIZE}.
	 *
	 * @param value The maximum buffer size.
	 */
	public void setMaxMemoryBufferSize(int value);

	/**
	 * Returns the font scale used for ASS subtitling. Default value is 1.0.
	 * @return The ASS font scale.
	 */
	public String getMencoderAssScale();

	/**
	 * Some versions of mencoder produce garbled audio because the "ac3" codec is used
	 * instead of the "ac3_fixed" codec. Returns true if "ac3_fixed" should be used.
	 * Default is false.
	 * See https://code.google.com/p/ps3mediaserver/issues/detail?id=1092#c1
	 * @return True if "ac3_fixed" should be used. 
	 */
	public boolean isMencoderAc3Fixed();

	/**
	 * Returns the margin used for ASS subtitling. Default value is 10.
	 * @return The ASS margin.
	 */
	public String getMencoderAssMargin();

	/**
	 * Returns the outline parameter used for ASS subtitling. Default value is 1.
	 * @return The ASS outline parameter.
	 */
	public String getMencoderAssOutline();

	/**
	 * Returns the shadow parameter used for ASS subtitling. Default value is 1.
	 * @return The ASS shadow parameter.
	 */
	public String getMencoderAssShadow();

	/**
	 * Returns the subfont text scale parameter used for subtitling without ASS.
	 * Default value is 3.
	 * @return The subfont text scale parameter.
	 */
	public String getMencoderNoAssScale();

	/**
	 * Returns the subpos parameter used for subtitling without ASS.
	 * Default value is 2.
	 * @return The subpos parameter.
	 */
	public String getMencoderNoAssSubPos();

	/**
	 * Returns the subfont blur parameter used for subtitling without ASS.
	 * Default value is 1.
	 * @return The subfont blur parameter.
	 */
	public String getMencoderNoAssBlur();

	/**
	 * Returns the subfont outline parameter used for subtitling without ASS.
	 * Default value is 1.
	 * @return The subfont outline parameter.
	 */
	public String getMencoderNoAssOutline();

	/**
	 * Set the subfont outline parameter used for subtitling without ASS.
	 * @param value The subfont outline parameter value to set.
	 */
	public void setMencoderNoAssOutline(String value);

	/**
	 * Some versions of mencoder produce garbled audio because the "ac3" codec is used
	 * instead of the "ac3_fixed" codec.
	 * See https://code.google.com/p/ps3mediaserver/issues/detail?id=1092#c1
	 * @param value Set to true if "ac3_fixed" should be used.
	 */
	public void setMencoderAc3Fixed(boolean value);

	/**
	 * Set the margin used for ASS subtitling.
	 * @param value The ASS margin value to set.
	 */
	public void setMencoderAssMargin(String value);

	/**
	 * Set the outline parameter used for ASS subtitling.
	 * @param value The ASS outline parameter value to set.
	 */
	public void setMencoderAssOutline(String value);

	/**
	 * Set the shadow parameter used for ASS subtitling.
	 * @param value The ASS shadow parameter value to set.
	 */
	public void setMencoderAssShadow(String value);

	/**
	 * Set the font scale used for ASS subtitling.
	 * @param value The ASS font scale value to set.
	 */
	public void setMencoderAssScale(String value);

	/**
	 * Set the subfont text scale parameter used for subtitling without ASS.
	 * @param value The subfont text scale parameter value to set.
	 */
	public void setMencoderNoAssScale(String value);

	/**
	 * Set the subfont blur parameter used for subtitling without ASS.
	 * @param value The subfont blur parameter value to set.
	 */
	public void setMencoderNoAssBlur(String value);

	/**
	 * Set the subpos parameter used for subtitling without ASS.
	 * @param value The subpos parameter value to set.
	 */
	public void setMencoderNoAssSubPos(String value);

	/**
	 * Set the maximum number of concurrent mencoder threads.
	 * XXX Currently unused.
	 * @param value The maximum number of concurrent threads.
	 */
	public void setMencoderMaxThreads(int value);

	/**
	 * Set the preferred language for the PMS user interface.
	 * @param value The ISO 639 language code.
	 */
	public void setLanguage(String value);

	/**
	 * Returns the number of seconds from the start of a video file (the seek
	 * position) where the thumbnail image for the movie should be extracted
	 * from. Default is 1 second.
	 * @return The seek position in seconds.
	 */
	public int getThumbnailSeekPos();

	/**
	 * Sets the number of seconds from the start of a video file (the seek
	 * position) where the thumbnail image for the movie should be extracted
	 * from.
	 * @param value The seek position in seconds.
	 */
	public void setThumbnailSeekPos(int value);

	/**
	 * Older versions of mencoder do not support ASS/SSA subtitles on all
	 * platforms. Returns true if mencoder supports them. Default is true
	 * on Windows and OS X, false otherwise.
	 * See https://code.google.com/p/ps3mediaserver/issues/detail?id=1097
	 * @return True if mencoder supports ASS/SSA subtitles.
	 */
	public boolean isMencoderAss();

	/**
	 * Returns whether or not subtitles should be disabled when using MEncoder
	 * as transcoding engine. Default is false, meaning subtitles should not
	 * be disabled.
	 * @return True if subtitles should be disabled, false otherwise.
	 */
	public boolean isMencoderDisableSubs();

	/**
	 * Returns whether or not the Pulse Code Modulation audio format should be
	 * forced when using MEncoder as transcoding engine. The default is false.
	 * @return True if PCM should be forced, false otherwise.
	 */
	public boolean isMencoderUsePcm();

	/**
	 * Returns whether or not the Pulse Code Modulation audio format should be
	 * used only for HQ audio codecs. The default is false.
	 * @return True if PCM should be used only for HQ audio codecs, false otherwise.
	 */
	public boolean isMencoderUsePcmForHQAudioOnly();

	/**
	 * Returns the name of a TrueType font to use for MEncoder subtitles.
	 * Default is <code>""</code>.
	 * @return The font name.
	 */
	public String getMencoderFont();

	/**
	 * Returns the audio language priority for MEncoder as a comma separated
	 * string. For example: <code>"eng,fre,jpn,ger,und"</code>, where "und"
	 * stands for "undefined". Default value is "loc,eng,fre,jpn,ger,und".
	 *
	 * @return The audio language priority string.
	 */
	public String getMencoderAudioLanguages();

	/**
	 * Returns the subtitle language priority for MEncoder as a comma separated
	 * string. For example: <code>"loc,eng,fre,jpn,ger,und"</code>, where "loc"
	 * stands for the preferred local language and "und" stands for "undefined".
	 * Default value is "loc,eng,fre,jpn,ger,und".
	 *
	 * @return The subtitle language priority string.
	 */
	public String getMencoderSubLanguages();

	/**
	 * Returns the ISO 639 language code for the subtitle language that should
	 * be forced upon MEncoder. 
	 * @return The subtitle language code.
	 */
	public String getMencoderForcedSubLanguage();

	/**
	 * Returns the tag string that identifies the subtitle language that
	 * should be forced upon MEncoder.
	 * @return The tag string.
	 */
	public String getMencoderForcedSubTags();

	/**
	 * Returns a string of audio language and subtitle language pairs
	 * ordered by priority for MEncoder to try to match. Audio language
	 * and subtitle language should be comma separated as a pair,
	 * individual pairs should be semicolon separated. "*" can be used to
	 * match any language, "loc" to match the local language. Subtitle
	 * language can be defined as "off".
	 * Default value is <code>"loc,off;jpn,loc;*,loc;*,*"</code>.
	 *
	 * @return The audio and subtitle languages priority string.
	 */
	public String getMencoderAudioSubLanguages();

	/**
	 * Returns whether or not MEncoder should use FriBiDi mode, which
	 * is needed to display subtitles in languages that read from right to
	 * left, like Arabic, Farsi, Hebrew, Urdu, etc. Default value is false.
	 * @return True if FriBiDi mode should be used, false otherwise.
	 */
	public boolean isMencoderSubFribidi();

	/**
	 * Returns the character encoding (or code page) that MEncoder should use
	 * for displaying subtitles. Default is "cp1252".
	 * @return The character encoding.
	 */
	public String getMencoderSubCp();

	/**
	 * Returns whether or not MEncoder should use fontconfig for displaying
	 * subtitles. Default is false.
	 * @return True if fontconfig should be used, false otherwise.
	 */
	public boolean isMencoderFontConfig();

	/**
	 * Set to true if MEncoder should be forced to use the framerate that is
	 * parsed by FFmpeg.
	 * @param value Set to true if the framerate should be forced, false
	 * 			otherwise.
	 */
	public void setMencoderForceFps(boolean value);

	/**
	 * Returns true if MEncoder should be forced to use the framerate that is
	 * parsed by FFmpeg.
	 * @return True if the framerate should be forced, false otherwise.
	 */
	public boolean isMencoderForceFps();

	/**
	 * Sets the audio language priority for MEncoder as a comma separated
	 * string. For example: <code>"eng,fre,jpn,ger,und"</code>, where "und"
	 * stands for "undefined".
	 * @param value The audio language priority string.
	 */
	public void setMencoderAudioLanguages(String value);

	/**
	 * Sets the subtitle language priority for MEncoder as a comma
	 * separated string. For example: <code>"eng,fre,jpn,ger,und"</code>,
	 * where "und" stands for "undefined".
	 * @param value The subtitle language priority string.
	 */
	public void setMencoderSubLanguages(String value);

	/**
	 * Sets the ISO 639 language code for the subtitle language that should
	 * be forced upon MEncoder. 
	 * @param value The subtitle language code.
	 */
	public void setMencoderForcedSubLanguage(String value);

	/**
	 * Sets the tag string that identifies the subtitle language that
	 * should be forced upon MEncoder.
	 * @param value The tag string.
	 */
	public void setMencoderForcedSubTags(String value);

	/**
	 * Sets a string of audio language and subtitle language pairs
	 * ordered by priority for MEncoder to try to match. Audio language
	 * and subtitle language should be comma separated as a pair,
	 * individual pairs should be semicolon separated. "*" can be used to
	 * match any language. Subtitle language can be defined as "off". For
	 * example: <code>"en,off;jpn,eng;*,eng;*;*"</code>.
	 * @param value The audio and subtitle languages priority string.
	 */
	public void setMencoderAudioSubLanguages(String value);

	/**
	 * Returns custom commandline options to pass on to MEncoder.
	 * @return The custom options string.
	 */
	public String getMencoderCustomOptions();

	/**
	 * Sets custom commandline options to pass on to MEncoder.
	 * @param value The custom options string.
	 */
	public void setMencoderCustomOptions(String value);

	/**
	 * Sets the character encoding (or code page) that MEncoder should use
	 * for displaying subtitles. Default is "cp1252".
	 * @param value The character encoding.
	 */
	public void setMencoderSubCp(String value);

	/**
	 * Sets whether or not MEncoder should use FriBiDi mode, which
	 * is needed to display subtitles in languages that read from right to
	 * left, like Arabic, Farsi, Hebrew, Urdu, etc. Default value is false.
	 * @param value Set to true if FriBiDi mode should be used.
	 */
	public void setMencoderSubFribidi(boolean value);

	/**
	 * Sets the name of a TrueType font to use for MEncoder subtitles.
	 * @param value The font name.
	 */
	public void setMencoderFont(String value);

	/**
	 * Older versions of mencoder do not support ASS/SSA subtitles on all
	 * platforms. Set to true if mencoder supports them. Default should be
	 * true on Windows and OS X, false otherwise.
	 * See https://code.google.com/p/ps3mediaserver/issues/detail?id=1097
	 * @param value Set to true if mencoder supports ASS/SSA subtitles.
	 */
	public void setMencoderAss(boolean value);

	/**
	 * Sets whether or not MEncoder should use fontconfig for displaying
	 * subtitles.
	 * @param value Set to true if fontconfig should be used.
	 */
	public void setMencoderFontConfig(boolean value);

	/**
	 * Set whether or not subtitles should be disabled when using MEncoder
	 * as transcoding engine.
	 * @param value Set to true if subtitles should be disabled.
	 */
	public void setMencoderDisableSubs(boolean value);

	/**
	 * Sets whether or not the Pulse Code Modulation audio format should be
	 * forced when using MEncoder as transcoding engine.
	 * @param value Set to true if PCM should be forced.
	 */
	public void setMencoderUsePcm(boolean value);

	/**
	 * Sets whether or not the Pulse Code Modulation audio format should be
	 * used only for HQ audio codecs.
	 * @param value Set to true if PCM should be used only for HQ audio.
	 */
	public void setMencoderUsePcmForHQAudioOnly(boolean value);

	/**
	 * Returns true if archives (e.g. .zip or .rar) should be browsable by
	 * PMS, false otherwise.
	 * @return True if archives should be browsable.
	 */
	public boolean isArchiveBrowsing();

	/**
	 * Set to true if archives (e.g. .zip or .rar) should be browsable by
	 * PMS, false otherwise.
	 * @param value Set to true if archives should be browsable.
	 */
	public void setArchiveBrowsing(boolean value);

	/**
	 * Returns true if MEncoder should use the deinterlace filter, false
	 * otherwise.
	 * @return True if the deinterlace filter should be used.
	 */
	public boolean isMencoderYadif();

	/**
	 * Set to true if MEncoder should use the deinterlace filter, false
	 * otherwise.
	 * @param value Set ot true if the deinterlace filter should be used.
	 */
	public void setMencoderYadif(boolean value);

	/**
	 * Returns true if MEncoder should be used to upscale the video to an
	 * optimal resolution. Default value is false, meaning the renderer will
	 * upscale the video itself.
	 *
	 * @return True if MEncoder should be used, false otherwise. 
	 * @see {@link #getMencoderScaleX(int)}, {@link #getMencoderScaleY(int)}
	 */
	public boolean isMencoderScaler();

	/**
	 * Set to true if MEncoder should be used to upscale the video to an
	 * optimal resolution. Set to false to leave upscaling to the renderer.
	 *
	 * @param value Set to true if MEncoder should be used to upscale.
	 * @see {@link #setMencoderScaleX(int)}, {@link #setMencoderScaleY(int)}
	 */
	public void setMencoderScaler(boolean value);

	/**
	 * Returns the width in pixels to which a video should be scaled when
	 * {@link #isMencoderScaler()} returns true.
	 *
	 * @return The width in pixels.
	 */
	public int getMencoderScaleX();

	/**
	 * Sets the width in pixels to which a video should be scaled when
	 * {@link #isMencoderScaler()} returns true.
	 *
	 * @param value The width in pixels.
	 */
	public void setMencoderScaleX(int value);

	/**
	 * Returns the height in pixels to which a video should be scaled when
	 * {@link #isMencoderScaler()} returns true.
	 *
	 * @return The height in pixels.
	 */
	public int getMencoderScaleY();

	/**
	 * Sets the height in pixels to which a video should be scaled when
	 * {@link #isMencoderScaler()} returns true.
	 *
	 * @param value The height in pixels.
	 */
	public void setMencoderScaleY(int value);

	/**
	 * Returns the number of audio channels that MEncoder should use for
	 * transcoding. Default value is 6 (for 5.1 audio).
	 *
	 * @return The number of audio channels.
	 */
	public int getAudioChannelCount();

	/**
	 * Sets the number of audio channels that MEncoder should use for
	 * transcoding.
	 *
	 * @param value The number of audio channels.
	 */
	public void setAudioChannelCount(int value);

	/**
	 * Sets the AC3 audio bitrate, which determines the quality of digital
	 * audio sound. An AV-receiver or amplifier has to be capable of playing
	 * this quality.
	 * 
	 * @param value The AC3 audio bitrate.
	 */
	public void setAudioBitrate(int value);

	/**
	 * Returns the maximum video bitrate to be used by MEncoder. The default
	 * value is 110.
	 *
	 * @return The maximum video bitrate.
	 */
	public String getMaximumBitrate();

	/**
	 * Sets the maximum video bitrate to be used by MEncoder.
	 *
	 * @param value The maximum video bitrate.
	 */
	public void setMaximumBitrate(String value);

	/**
	 * Returns true if thumbnail generation is enabled, false otherwise.
	 *
	 * @return boolean indicating whether thumbnail generation is enabled.
	 */
	public boolean isThumbnailGenerationEnabled();

	/**
	 * @deprecated Use {@link #setThumbnailGenerationEnabled(boolean)} instead.
	 * <p>
	 * Sets the thumbnail generation option.
	 * This only determines whether a thumbnailer (e.g. dcraw, MPlayer)
	 * is used to generate thumbnails. It does not reflect whether
	 * thumbnails should be displayed or not.
	 *
	 * @return boolean indicating whether thumbnail generation is enabled.
	 */
	@Deprecated
	public void setThumbnailsEnabled(boolean value);

	/**
	 * Sets the thumbnail generation option.
	 */
	public void setThumbnailGenerationEnabled(boolean value);

	/**
	 * Returns true if PMS should generate thumbnails for images. Default value
	 * is true.
	 *
	 * @return True if image thumbnails should be generated.
	 */
	public boolean getImageThumbnailsEnabled();

	/**
	 * Set to true if PMS should generate thumbnails for images.
	 *
	 * @param value True if image thumbnails should be generated.
	 */
	public void setImageThumbnailsEnabled(boolean value);

	/**
	 * Returns the number of CPU cores that should be used for transcoding.
	 * 
	 * @return The number of CPU cores.
	 */
	public int getNumberOfCpuCores();

	/**
	 * Sets the number of CPU cores that should be used for transcoding. The
	 * maximum value depends on the physical available count of "real processor
	 * cores". That means hyperthreading virtual CPU cores do not count! If you
	 * are not sure, analyze your CPU with the free tool CPU-z on Windows
	 * systems. On Linux have a look at the virtual proc-filesystem: in the
	 * file "/proc/cpuinfo" you will find more details about your CPU. You also
	 * get much information about CPUs from AMD and Intel from their Wikipedia
	 * articles.
	 * <p>
	 * PMS will detect and set the correct amount of cores as the default value.
	 *
	 * @param value The number of CPU cores.
	 */
	public void setNumberOfCpuCores(int value);

	/**
	 * @deprecated This method is not used anywhere.
	 */
	@Deprecated
	public boolean isTurboModeEnabled();

	/**
	 * Returns true if PMS should start minimized, i.e. without its window
	 * opened. Default value false: to start with a window.
	 *
	 * @return True if PMS should start minimized, false otherwise.
	 */
	public boolean isMinimized();

	/**
	 * Set to true if PMS should start minimized, i.e. without its window
	 * opened.
	 *
	 * @param value True if PMS should start minimized, false otherwise.
	 */
	public void setMinimized(boolean value);

	/**
	 * Returns true when PMS should check for external subtitle files with the
	 * same name as the media (*.srt, *.sub, *.ass, etc.). The default value is
	 * true.
	 *
	 * @return True if PMS should check for external subtitle files, false if
	 * 		they should be ignored.
	 */
	public boolean getUseSubtitles();

	/**
	 * Set to true if PMS should check for external subtitle files with the
	 * same name as the media (*.srt, *.sub, *.ass etc.).
	 *
	 * @param value True if PMS should check for external subtitle files.
	 */
	public void setUseSubtitles(boolean value);

	/**
	 * Returns true if PMS should hide the "# Videosettings #" folder on the
	 * DLNA device. The default value is false: PMS will display the folder.
	 *
	 * @return True if PMS should hide the folder, false othewise.
	 */
	public boolean getHideVideoSettings();

	/**
	 * Set to true if PMS should hide the "# Videosettings #" folder on the
	 * DLNA device, or set to false to make PMS display the folder.
	 *
	 * @param value True if PMS should hide the folder.
	 */
	public void setHideVideoSettings(boolean value);

	/**
	 * Returns true if PMS should cache scanned media in its internal database,
	 * speeding up later retrieval. When false is returned, PMS will not use
	 * cache and media will have to be rescanned.
	 *
	 * @return True if PMS should cache media.
	 */
	public boolean getUseCache();

	/**
	 * Set to true if PMS should cache scanned media in its internal database,
	 * speeding up later retrieval.
	 *
	 * @param value True if PMS should cache media.
	 */
	public void setUseCache(boolean value);

	/**
	 * Set to true if PMS should pass the flag "convertfps=true" to AviSynth.
	 *
	 * @param value True if PMS should pass the flag.
	 */
	public void setAvisynthConvertFps(boolean value);

	/**
	 * Returns true if PMS should pass the flag "convertfps=true" to AviSynth.
	 *
	 * @return True if PMS should pass the flag.
	 */
	public boolean getAvisynthConvertFps();

	/**
	 * Returns the template for the AviSynth script. The script string can
	 * contain the character "\u0001", which should be treated as the newline
	 * separator character.
	 *
	 * @return The AviSynth script template.
	 */
	public String getAvisynthScript();

	/**
	 * Sets the template for the AviSynth script. The script string may contain
	 * the character "\u0001", which will be treated as newline character.
	 *
	 * @param value The AviSynth script template.
	 */
	public void setAvisynthScript(String value);

	/**
	 * Returns additional codec specific configuration options for MEncoder.
	 *
	 * @return The configuration options.
	 */
	public String getCodecSpecificConfig();

	/**
	 * Sets additional codec specific configuration options for MEncoder.
	 *
	 * @param value The additional configuration options.
	 */
	public void setCodecSpecificConfig(String value);

	/**
	 * Returns the maximum size (in MB) that PMS should use for buffering
	 * audio.
	 *
	 * @return The maximum buffer size.
	 */
	public int getMaxAudioBuffer();

	/**
	 * Returns the minimum size (in MB) that PMS should use for the buffer used
	 * for streaming media.
	 *
	 * @return The minimum buffer size.
	 */
	public int getMinStreamBuffer();

	public boolean isFileBuffer();

	public void setFfmpegSettings(String value);

	public String getFfmpegSettings();

	public boolean isMencoderNoOutOfSync();

	public void setMencoderNoOutOfSync(boolean value);

	public boolean getTrancodeBlocksMultipleConnections();

	public void setTranscodeBlocksMultipleConnections(boolean value);

	public boolean getTrancodeKeepFirstConnections();

	public void setTrancodeKeepFirstConnections(boolean value);

	public String getCharsetEncoding();

	public void setCharsetEncoding(String value);

	public boolean isMencoderIntelligentSync();

	public void setMencoderIntelligentSync(boolean value);

	public String getFfmpegAlternativePath();

	public void setFfmpegAlternativePath(String value);

	public boolean getSkipLoopFilterEnabled();

	/**
	 * The list of network interfaces that should be skipped when checking
	 * for an available network interface. Entries should be comma separated
	 * and typically exclude the number at the end of the interface name.
	 * <p>
	 * Default is to skip the interfaces created by Virtualbox, OpenVPN and
	 * Parallels: "tap,vmnet,vnic".
	 * @return The string of network interface names to skip.
	 */
	public List<String> getSkipNetworkInterfaces();

	public void setSkipLoopFilterEnabled(boolean value);

	public String getMencoderMainSettings();

	public void setMencoderMainSettings(String value);

	public String getMencoderVobsubSubtitleQuality();

	public void setMencoderVobsubSubtitleQuality(String value);

	public String getMencoderOverscanCompensationWidth();

	public void setMencoderOverscanCompensationWidth(String value);

	public String getMencoderOverscanCompensationHeight();

	public void setMencoderOverscanCompensationHeight(String value);

	public void setEnginesAsList(ArrayList<String> enginesAsList);

	public List<String> getEnginesAsList(SystemUtils registry);

	public void save() throws ConfigurationException;

	public String getFolders();

	public void setFolders(String value);

	public String getNetworkInterface();

	public void setNetworkInterface(String value);

	public boolean isHideEngineNames();

	public void setHideEngineNames(boolean value);

	public boolean isHideExtensions();

	public void setHideExtensions(boolean value);

	public String getShares();

	public void setShares(String value);

	public String getNoTranscode();

	public void setNoTranscode(String value);

	public String getForceTranscode();

	public void setForceTranscode(String value);

	public void setMencoderMT(boolean value);

	public boolean getMencoderMT();

	public void setRemuxAC3(boolean value);

	public boolean isRemuxAC3();

	public void setMencoderRemuxMPEG2(boolean value);

	public boolean isMencoderRemuxMPEG2();

	public void setDisableFakeSize(boolean value);

	public boolean isDisableFakeSize();

	public void setMencoderAssDefaultStyle(boolean value);

	public boolean isMencoderAssDefaultStyle();

	public int getMEncoderOverscan();

	public void setMEncoderOverscan(int value);

	/**
	 * Returns sort method to use for ordering lists of files. One of the
	 * following values is returned:
	 * <ul>
	 * <li>0: Locale-sensitive A-Z</li>
	 * <li>1: Sort by modified date, newest first</li>
	 * <li>2: Sort by modified date, oldest first</li>
	 * <li>3: Case-insensitive ASCIIbetical sort</li>
	 * <li>4: Locale-sensitive natural sort</li>
	 * </ul>
	 * Default value is 0.
	 * @return The sort method
	 */
	public int getSortMethod();

	/**
	 * Set the sort method to use for ordering lists of files. The following
	 * values are recognized:
	 * <ul>
	 * <li>0: Locale-sensitive A-Z</li>
	 * <li>1: Sort by modified date, newest first</li>
	 * <li>2: Sort by modified date, oldest first</li>
	 * <li>3: Case-insensitive ASCIIbetical sort</li>
	 * <li>4: Locale-sensitive natural sort</li>
	 * </ul>
	 * @param value The sort method to use
	 */
	public void setSortMethod(int value);

	public int getAudioThumbnailMethod();

	public void setAudioThumbnailMethod(int value);

	public String getAlternateThumbFolder();

	public void setAlternateThumbFolder(String value);

	public String getAlternateSubsFolder();

	public void setAlternateSubsFolder(String value);

	public void setDTSEmbedInPCM(boolean value);

	public boolean isDTSEmbedInPCM();

	public void setMencoderMuxWhenCompatible(boolean value);

	public boolean isMencoderMuxWhenCompatible();

	public void setMuxAllAudioTracks(boolean value);

	public boolean isMuxAllAudioTracks();

	public void setUseMplayerForVideoThumbs(boolean value);

	public boolean isUseMplayerForVideoThumbs();

	public String getIpFilter();

	public IpFilter getIpFiltering();

	public void setIpFilter(String value);

	public void setPreventsSleep(boolean value);

	public boolean isPreventsSleep();

	public void setHTTPEngineV2(boolean value);

	public boolean isHTTPEngineV2();

	public boolean getIphotoEnabled();

	public void setIphotoEnabled(boolean value);

	public boolean getApertureEnabled();

	public void setApertureEnabled(boolean value);

	public boolean getItunesEnabled();

	public void setItunesEnabled(boolean value);

	public boolean isHideEmptyFolders();

	public void setHideEmptyFolders(boolean value);

	public boolean isHideMediaLibraryFolder();

	public void setHideMediaLibraryFolder(boolean value);

	public boolean getHideTranscodeEnabled();

	public void setHideTranscodeEnabled(boolean value);

	public boolean isDvdIsoThumbnails();

	public void setDvdIsoThumbnails(boolean value);

	public Object getCustomProperty(String property);

	public void setCustomProperty(String property, Object value);

	public boolean isChapterSupport();

	public void setChapterSupport(boolean value);

	public int getChapterInterval();

	public void setChapterInterval(int value);

	public int getSubsColor();

	public void setSubsColor(int value);

	public boolean isFix25FPSAvMismatch();

	public void setFix25FPSAvMismatch(boolean value);

	public int getVideoTranscodeStartDelay();

	public void setVideoTranscodeStartDelay(int value);

	public boolean isAudioResample();

	public void setAudioResample(boolean value);

	/**
	 * Returns the name of the renderer to fall back on when header matching
	 * fails. PMS will recognize the configured renderer instead of "Unknown
	 * renderer". Default value is "", which means PMS will return the unknown
	 * renderer when no match can be made.
	 *
	 * @return The name of the renderer PMS should fall back on when header
	 * 			matching fails.
	 * @see #isRendererForceDefault()
	 */
	public String getRendererDefault();

	/**
	 * Sets the name of the renderer to fall back on when header matching
	 * fails. PMS will recognize the configured renderer instead of "Unknown
	 * renderer". Set to "" to make PMS return the unknown renderer when no
	 * match can be made.
	 *
	 * @param value The name of the renderer to fall back on. This has to be
	 * 				<code>""</code> or a case insensitive match with the name
	 * 				used in any render configuration file.
	 * @see #setRendererForceDefault(boolean)
	 */
	public void setRendererDefault(String value);

	/**
	 * Returns true when PMS should not try to guess connecting renderers
	 * and instead force picking the defined fallback renderer. Default
	 * value is false, which means PMS will attempt to recognize connecting
	 * renderers by their headers.
	 *
	 * @return True when the fallback renderer should always be picked.
	 * @see #getRendererDefault()
	 */
	public boolean isRendererForceDefault();

	/**
	 * Set to true when PMS should not try to guess connecting renderers
	 * and instead force picking the defined fallback renderer. Set to false
	 * to make PMS attempt to recognize connecting renderers by their headers.
	 *
	 * @param value Set to true when the fallback renderer should always be
	 *				picked.
	 * @see #setRendererDefault(String)
	 */
	public void setRendererForceDefault(boolean value);

	public String getVirtualFolders();

	public String getProfilePath();

	public String getProfileDirectory();

	public String getPluginDirectory();

	public void setPluginDirectory(String value);

	public String getProfileName();

	public boolean isAutoUpdate();

	public void setAutoUpdate(boolean value);

	public String getIMConvertPath();

	public int getUpnpPort();

	public String getUuid();

	public void setUuid(String value);

	public void addConfigurationListener(ConfigurationListener l);

	public void removeConfigurationListener(ConfigurationListener l);

	public boolean initBufferMax();

	/**
	 * Returns the set of the keys defining when the HTTP server has to
	 * restarted due to a configuration change.
	 *
	 * @return The flags
	 */
	public Set<String> getNeedReloadFlags();
}