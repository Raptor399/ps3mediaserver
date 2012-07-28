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
package net.pms.encoders;

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Arrays;

import javax.inject.Inject;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.api.PmsConfiguration;
import net.pms.api.io.PipeProcessFactory;
import net.pms.api.io.ProcessWrapperFactory;
import net.pms.di.InjectionHelper;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.network.HTTPResource;

import com.google.inject.Injector;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MPlayerAudio extends Player {
	public static final String ID = "mplayeraudio";
	private final PmsConfiguration configuration;
	private final ProcessWrapperFactory processWrapperFactory;
	private final PipeProcessFactory pipeProcessFactory;

	@Inject
	public MPlayerAudio(PmsConfiguration configuration,
			ProcessWrapperFactory processWrapperFactory,
			PipeProcessFactory pipeProcessFactory) {
		this.configuration = configuration;
		this.processWrapperFactory = processWrapperFactory;
		this.pipeProcessFactory = pipeProcessFactory;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public int purpose() {
		return AUDIO_SIMPLEFILE_PLAYER;
	}

	@Override
	public String[] args() {
		return new String[]{};
	}

	@Override
	public String executable() {
		return configuration.getMplayerPath();
	}

	@Override
	public ProcessWrapper launchTranscode(
		String fileName,
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params) throws IOException {
		if (!(this instanceof MPlayerWebAudio) && !(this instanceof MPlayerWebVideoDump)) {
			params.waitbeforestart = 2000;
		}

		params.manageFastStart();

		if (params.mediaRenderer.isTranscodeToMP3()) {
			// FFmpeg handles audio transcoding to MP3.
			Injector injector = InjectionHelper.getInjector();
			FFMpegAudio player = injector.getInstance(FFMpegAudio.class);
			return player.launchTranscode(fileName, dlna, media, params);
		}

		params.maxBufferSize = configuration.getMaxAudioBuffer();
		
		PipeProcess audioP = pipeProcessFactory.create("mplayer_aud" + System.currentTimeMillis());

		String mPlayerdefaultAudioArgs[] = new String[]{configuration.getMplayerPath(), fileName, "-prefer-ipv4", "-nocache", "-af", "channels=2", "-srate", "48000", "-vo", "null", "-ao", "pcm:nowaveheader:fast:file=" + audioP.getInputPipe(), "-quiet", "-format", "s16be"};
		if (params.mediaRenderer.isTranscodeToWAV()) {
			mPlayerdefaultAudioArgs[11] = "pcm:waveheader:fast:file=" + audioP.getInputPipe();
			mPlayerdefaultAudioArgs[13] = "-quiet";
			mPlayerdefaultAudioArgs[14] = "-quiet";
		}
		if (params.mediaRenderer.isTranscodeAudioTo441()) {
			mPlayerdefaultAudioArgs[7] = "44100";
		}
		if (!configuration.isAudioResample()) {
			mPlayerdefaultAudioArgs[6] = "-quiet";
			mPlayerdefaultAudioArgs[7] = "-quiet";
		}
		params.input_pipes[0] = audioP;

		if (params.timeseek > 0 || params.timeend > 0) {
			mPlayerdefaultAudioArgs = Arrays.copyOf(mPlayerdefaultAudioArgs, mPlayerdefaultAudioArgs.length + 4);
			mPlayerdefaultAudioArgs[mPlayerdefaultAudioArgs.length - 4] = "-ss";
			mPlayerdefaultAudioArgs[mPlayerdefaultAudioArgs.length - 3] = "" + params.timeseek;
			if (params.timeend > 0) {
				mPlayerdefaultAudioArgs[mPlayerdefaultAudioArgs.length - 2] = "-endpos";
				mPlayerdefaultAudioArgs[mPlayerdefaultAudioArgs.length - 1] = "" + params.timeend;
			} else {
				mPlayerdefaultAudioArgs[mPlayerdefaultAudioArgs.length - 2] = "-quiet";
				mPlayerdefaultAudioArgs[mPlayerdefaultAudioArgs.length - 1] = "-quiet";
			}
		}

		ProcessWrapper mkfifo_process = audioP.getPipeProcess();

		mPlayerdefaultAudioArgs = finalizeTranscoderArgs(
			this,
			fileName,
			dlna,
			media,
			params,
			mPlayerdefaultAudioArgs);
		ProcessWrapper pw = processWrapperFactory.create(mPlayerdefaultAudioArgs, params);
		pw.attachProcess(mkfifo_process);
		mkfifo_process.runInNewThread();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}

		audioP.deleteLater();
		pw.runInNewThread();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		return pw;
	}

	@Override
	public String mimeType() {
		return HTTPResource.AUDIO_TRANSCODE;
	}

	@Override
	public String name() {
		return "MPlayer Audio";
	}

	@Override
	public int type() {
		return Format.AUDIO;
	}
	JCheckBox noresample;

	@Override
	public JComponent config() {
		FormLayout layout = new FormLayout(
			"left:pref, 0:grow",
			"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, 0:grow");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(Borders.EMPTY_BORDER);
		builder.setOpaque(false);

		CellConstraints cc = new CellConstraints();

		JComponent cmp = builder.addSeparator("Audio settings", cc.xyw(2, 1, 1));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		noresample = new JCheckBox(Messages.getString("TrTab2.22"));
		noresample.setContentAreaFilled(false);
		noresample.setSelected(configuration.isAudioResample());
		noresample.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				configuration.setAudioResample(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		builder.add(noresample, cc.xy(2, 3));

		return builder.getPanel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCompatible(DLNAMediaInfo mediaInfo) {
		if (mediaInfo != null) {
			// TODO: Determine compatibility based on mediaInfo
			return false;
		} else {
			// No information available
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCompatible(Format format) {
		if (format != null) {
			Format.Identifier id = format.getIdentifier();

			if (id.equals(Format.Identifier.FLAC)
					|| id.equals(Format.Identifier.M4A)
					|| id.equals(Format.Identifier.OGG)
					|| id.equals(Format.Identifier.WAV)) {
				return true;
			}
		}

		return false;
	}

}
