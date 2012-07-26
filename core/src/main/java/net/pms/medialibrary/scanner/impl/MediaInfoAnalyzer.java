package net.pms.medialibrary.scanner.impl;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.pms.di.InjectionHelper;
import net.pms.formats.Format;
import net.pms.formats.FormatFactory;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.scanner.MediaInfo;

import com.google.common.io.Files;
import com.google.inject.Injector;

public class MediaInfoAnalyzer implements MediaInfo {

	private final Set<String> audioFileExtensions;
	private final Set<String> videoFileExtensions;
	private final Set<String> imageFileExtensions;

	MediaInfoAnalyzer() {
		Injector injector = InjectionHelper.getInjector();
		FormatFactory formatFactory = injector.getInstance(FormatFactory.class);

		audioFileExtensions = new HashSet<String>();
		videoFileExtensions = new HashSet<String>();
		imageFileExtensions = new HashSet<String>();

		for (Format format : formatFactory.getFormats()) {
			if (format.isAudio()) {
				audioFileExtensions.addAll(Arrays.asList(format.getId()));
			}

			if (format.isVideo()) {
				videoFileExtensions.addAll(Arrays.asList(format.getId()));
			}

			if (format.isImage()) {
				imageFileExtensions.addAll(Arrays.asList(format.getId()));
			}
		}
	}

	@Override
	public FileType analyzeMediaType(final String fileName) {
		FileType retVal = FileType.UNKNOWN;

		final String extension = Files.getFileExtension(fileName).toLowerCase();
		if (this.videoFileExtensions.contains(extension)) {
			retVal = FileType.VIDEO;
		} else if (this.audioFileExtensions.contains(extension)) {
			retVal = FileType.AUDIO;
		} else if (this.imageFileExtensions.contains(extension)) {
			retVal = FileType.PICTURES;
		}

		return retVal;
	}

	@Override
	public FileType analyzeMediaType(final File file) {

		FileType retVal = FileType.UNKNOWN;

		if (file.isFile()) {
			retVal = analyzeMediaType(file.getName());
		}

		return retVal;
	}

}
