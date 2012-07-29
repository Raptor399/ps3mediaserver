/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012  I. Sokolov
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
package net.pms.formats.v2;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class AudioPropertiesTest {
	private AudioProperties properties;

	@Before
	public void setUp() {
		properties = new AudioProperties();
	}

	@Test
	public void testDefaultValues() {
		assertThat(properties.getNumberOfChannels()).isEqualTo(2);
		assertThat(properties.getAudioDelay()).isEqualTo(0);
		assertThat(properties.getSampleFrequency()).isEqualTo(48000);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetNumberOfChannels_withIllegalArgument() {
		properties.setNumberOfChannels(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSampleFrequency_withIllegalArgument() {
		properties.setSampleFrequency(0);
	}

	@Test
	public void testGetAttribute_shouldReturnValueForAllPossibleAttributes() {
		for (AudioAttribute attribute : AudioAttribute.values()) {
			properties.getAttribute(attribute);
		}
	}

	@Test
	public void testSetNumberOfChannels() {
		properties.setNumberOfChannels(5);
		assertThat(properties.getNumberOfChannels()).isEqualTo(5);
		properties.setNumberOfChannels("2 channels / 4 channel / 3 channel");
		assertThat(properties.getNumberOfChannels()).isEqualTo(4);
		properties.setNumberOfChannels("-3 channel");
		assertThat(properties.getNumberOfChannels()).isEqualTo(2);
	}

	@Test
	public void testSetAudioDelay() {
		properties.setAudioDelay(5);
		assertThat(properties.getAudioDelay()).isEqualTo(5);
		properties.setAudioDelay("2 ms");
		assertThat(properties.getAudioDelay()).isEqualTo(2);
		properties.setAudioDelay("-3");
		assertThat(properties.getAudioDelay()).isEqualTo(-3);
	}

	@Test
	public void testSetSampleFrequency() {
		properties.setSampleFrequency(22050);
		assertThat(properties.getSampleFrequency()).isEqualTo(22050);
		properties.setSampleFrequency("22050 / 44100");
		assertThat(properties.getSampleFrequency()).isEqualTo(44100);
		properties.setSampleFrequency("-3");
		assertThat(properties.getSampleFrequency()).isEqualTo(48000);
	}

	@Test
	public void testGetChannelsNumberFromLibMediaInfo_withNullEmptyOrNegativeValue() {
		AudioProperties audioProperties = new AudioProperties();
		assertThat(audioProperties.getChannelsNumberFromLibMediaInfo(null)).isEqualTo(2);
		assertThat(audioProperties.getChannelsNumberFromLibMediaInfo("")).isEqualTo(2);
		assertThat(audioProperties.getChannelsNumberFromLibMediaInfo("-2chan")).isEqualTo(2);
		assertThat(audioProperties.getChannelsNumberFromLibMediaInfo("0")).isEqualTo(2);
		assertThat(audioProperties.getChannelsNumberFromLibMediaInfo("zero number")).isEqualTo(2);
	}

	@Test
	public void testGetChannelsNumberFromLibMediaInfo() {
		AudioProperties audioProperties = new AudioProperties();
		assertThat(audioProperties.getChannelsNumberFromLibMediaInfo("1 channel")).isEqualTo(1);
		assertThat(audioProperties.getChannelsNumberFromLibMediaInfo("3 channels")).isEqualTo(3);
		assertThat(audioProperties.getChannelsNumberFromLibMediaInfo("   3 ch ls 21")).isEqualTo(21);
		assertThat(audioProperties.getChannelsNumberFromLibMediaInfo("6 channels")).isEqualTo(6);
		assertThat(audioProperties.getChannelsNumberFromLibMediaInfo("2 channels / 1 channel / 1 channel")).isEqualTo(2);
		assertThat(audioProperties.getChannelsNumberFromLibMediaInfo("2 channels / 4 channel / 3 channel")).isEqualTo(4);
	}

	@Test
	public void testGetAudioDelayFromLibMediaInfo_withNullEmpty() {
		AudioProperties audioProperties = new AudioProperties();
		assertThat(audioProperties.getAudioDelayFromLibMediaInfo(null)).isEqualTo(0);
		assertThat(audioProperties.getAudioDelayFromLibMediaInfo("")).isEqualTo(0);
		assertThat(audioProperties.getAudioDelayFromLibMediaInfo("zero number")).isEqualTo(0);
	}

	@Test
	public void testGetAudioDelayFromLibMediaInfo() {
		AudioProperties audioProperties = new AudioProperties();
		assertThat(audioProperties.getAudioDelayFromLibMediaInfo("1")).isEqualTo(1);
		assertThat(audioProperties.getAudioDelayFromLibMediaInfo("5 msec")).isEqualTo(5);
		assertThat(audioProperties.getAudioDelayFromLibMediaInfo("5.4 milli")).isEqualTo(5);
		assertThat(audioProperties.getAudioDelayFromLibMediaInfo("0")).isEqualTo(0);
		assertThat(audioProperties.getAudioDelayFromLibMediaInfo("-7")).isEqualTo(-7);
		assertThat(audioProperties.getAudioDelayFromLibMediaInfo("delay -15 ms")).isEqualTo(-15);
	}

	@Test
	public void testGetSampleFrequencyFromLibMediaInfo_withNullEmpty() {
		AudioProperties audioProperties = new AudioProperties();
		assertThat(audioProperties.getSampleFrequencyFromLibMediaInfo(null)).isEqualTo(48000);
		assertThat(audioProperties.getSampleFrequencyFromLibMediaInfo("")).isEqualTo(48000);
		assertThat(audioProperties.getSampleFrequencyFromLibMediaInfo("freq unknown")).isEqualTo(48000);
	}

	@Test
	public void testGetSampleFrequencyFromLibMediaInfo() {
		AudioProperties audioProperties = new AudioProperties();
		assertThat(audioProperties.getSampleFrequencyFromLibMediaInfo("1")).isEqualTo(1);
		assertThat(audioProperties.getSampleFrequencyFromLibMediaInfo("5 Hz")).isEqualTo(5);
		assertThat(audioProperties.getSampleFrequencyFromLibMediaInfo("48000")).isEqualTo(48000);
		assertThat(audioProperties.getSampleFrequencyFromLibMediaInfo("44100 Hz")).isEqualTo(44100);
		assertThat(audioProperties.getSampleFrequencyFromLibMediaInfo("44100 / 22050")).isEqualTo(44100);
		assertThat(audioProperties.getSampleFrequencyFromLibMediaInfo("22050 / 44100 Hz")).isEqualTo(44100);
		assertThat(audioProperties.getSampleFrequencyFromLibMediaInfo("-7 kHz")).isEqualTo(48000);
	}
}
