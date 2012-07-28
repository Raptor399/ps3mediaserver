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
package net.pms.dlna;

import java.io.File;
import java.util.List;

import net.pms.api.PmsConfiguration;
import net.pms.api.PmsCore;
import net.pms.api.io.ProcessWrapperFactory;
import net.pms.di.InjectionHelper;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.formats.Format;
import net.pms.formats.FormatFactory;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;
import net.pms.util.ProcessUtil;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class DVDISOFile extends VirtualFolder {
	public static final String PREFIX = "[DVD ISO] ";
	private final File f;
	private final ProcessWrapperFactory processWrapperFactory;
	private final PmsCore pmsCore;
	private final PmsConfiguration configuration;
	private final DVDISOTitle.Factory dvdIsoTitleFactory;

	/**
	 * Interface for a factory to be generated by Guice, using <a
	 * href="https://code.google.com/p/google-guice/wiki/AssistedInject">
	 * AssistedInject</a>.
	 */
	public interface Factory {
		public DVDISOFile create(File f);
	}


	/**
	 * Temporary constructor to help with transition to DI.
	 * @deprecated Use {@link #DVDISOFile(PmsCore, ProcessWrapperFactory, File)} instead.
	 */
	@Deprecated
	public DVDISOFile(File f) {
		this(InjectionHelper.getInjector().getInstance(PmsCore.class),
				InjectionHelper.getInjector().getInstance(PmsConfiguration.class),
				InjectionHelper.getInjector().getInstance(ProcessWrapperFactory.class),
				InjectionHelper.getInjector().getInstance(DVDISOTitle.Factory.class),
				InjectionHelper.getInjector().getInstance(FormatFactory.class),
				f);
	}
	
	@AssistedInject
	public DVDISOFile(PmsCore pmsCore, PmsConfiguration configuration,
			ProcessWrapperFactory processWrapperFactory, 
			DVDISOTitle.Factory dvdIsoTitleFactory,
			FormatFactory formatFactory, @Assisted File f) {
		super(pmsCore, configuration, formatFactory, PREFIX + (f.isFile() ? f.getName() : "VIDEO_TS"), null);
		this.pmsCore = pmsCore;
		this.configuration = configuration;
		this.processWrapperFactory = processWrapperFactory;
		this.dvdIsoTitleFactory = dvdIsoTitleFactory;
		this.f = f;
		setLastmodified(f.lastModified());
	}

	@Override
	public void resolve() {
		double titles[] = new double[100];
		String cmd[] = new String[]{configuration.getMplayerPath(), "-identify", "-endpos", "0", "-v", "-ao", "null", "-vc", "null", "-vo", "null", "-dvd-device", ProcessUtil.getShortFileNameIfWideChars(f.getAbsolutePath()), "dvd://1"};
		OutputParams params = new OutputParams(configuration);
		params.maxBufferSize = 1;
		params.log = true;
		final ProcessWrapper pw = processWrapperFactory.create(cmd, params, true, false);
		Runnable r = new Runnable() {
			public void run() {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
				}
				pw.stopProcess();
			}
		};
		Thread failsafe = new Thread(r, "DVDISO Failsafe");
		failsafe.start();
		pw.runInSameThread();
		List<String> lines = pw.getOtherResults();
		if (lines != null) {
			for (String line : lines) {
				if (line.startsWith("ID_DVD_TITLE_") && line.contains("_LENGTH")) {
					int rank = Integer.parseInt(line.substring(13, line.indexOf("_LENGT")));
					double duration = Double.parseDouble(line.substring(line.lastIndexOf("LENGTH=") + 7));
					titles[rank] = duration;
				}
			}
		}

		double oldduration = -1;

		for (int i = 1; i < 99; i++) {
			// don't take into account titles less than 10 seconds
			// also, workaround for the mplayer bug which reports several times an unique title with the same length
			// The "maybe wrong" title is taken into account only if his length is smaller than 1 hour.
			// Common sense is a single video track on a DVD is usually greater than 1h
			if (titles[i] > 10 && (titles[i] != oldduration || oldduration < 3600)) {
				DVDISOTitle dvd = dvdIsoTitleFactory.create(f, i);
				addChild(dvd);
				oldduration = titles[i];
			}
		}

		if (childrenNumber() > 0) {
			pmsCore.storeFileInCache(f, Format.ISO);
		}

	}

	@Override
	public String getDisplayName() {
		String s = super.getDisplayName();
		if (f.getName().toUpperCase().equals("VIDEO_TS")) {
			s += " {" + f.getParentFile().getName() + "}";
		}
		return s;
	}
}
