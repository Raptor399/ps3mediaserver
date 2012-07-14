package net.pms.medialibrary.scanner.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import net.pms.dlna.DLNAMediaAudio;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.dlna.RealFile;
import net.pms.medialibrary.commons.dataobjects.DOAudioFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOImageFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOManagedFile;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.scanner.FileScannerDlnaResource;
import net.pms.medialibrary.scanner.FileInfoCollector;
import net.pms.medialibrary.scanner.MediaInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class DefaultFileInfoCollector implements FileInfoCollector {

	private static final Logger log = LoggerFactory.getLogger(DefaultFileInfoCollector.class);

	private final FileScannerDlnaResource dummyParent = new FileScannerDlnaResource();

	@Inject
	private MediaInfo mediaInfo;


	@Override
	public Optional<DOFileInfo> analyze(DOManagedFile mf) {
		final File f = new File(mf.getPath());
		DOFileInfo newSourceFileInfo = null;

		final FileType ft = mediaInfo.analyzeMediaType(f);
		switch (ft) {
		case VIDEO:
			newSourceFileInfo = new DOVideoFileInfo();
			break;
			
		case AUDIO:
			newSourceFileInfo = new DOAudioFileInfo();
			break;
			
		case PICTURES:
			newSourceFileInfo = new DOImageFileInfo();
			break;
			
		default:
			break;
		}
		
		if(analyzeAndUpdate(mf, newSourceFileInfo)) {
			return Optional.of((DOFileInfo) newSourceFileInfo);
		} else {
			return Optional.absent();
		}
	}

	@Override
	public boolean analyzeAndUpdate(final DOManagedFile mf, final DOFileInfo sourceFileInfo) {
		final File f = new File(mf.getPath());

		if (!f.exists()) {
			return false;
		}

		Optional<DOFileInfo> updatedFileFinfo = Optional.absent();

		final FileType ft = mediaInfo.analyzeMediaType(f);
		switch (ft) {
		case VIDEO:
			if(sourceFileInfo instanceof DOVideoFileInfo) {
				updatedFileFinfo = analyzeVideo(mf, f, (DOVideoFileInfo) sourceFileInfo);
			} else {
				log.warn("A file info of type DOVideoFileInfo has to be used to update a video file");
			}
			break;

		case AUDIO:
			if(sourceFileInfo instanceof DOAudioFileInfo) {
				updatedFileFinfo = analyzeAudio(mf, f, (DOAudioFileInfo) sourceFileInfo);
			} else {
				log.warn("A file info of type DOAudioFileInfo has to be used to update a audio file");
			}
			break;

		case PICTURES:
			if(sourceFileInfo instanceof DOImageFileInfo) {
				updatedFileFinfo = analyzePicture(mf, f, (DOImageFileInfo) sourceFileInfo);
			} else {
				log.warn("A file info of type DOImageFileInfo has to be used to update an image file");
			}
			break;

		default:
			break;
		}

		if (updatedFileFinfo.isPresent()) {
			final DOFileInfo info = updatedFileFinfo.get();
			info.setActive(true);
			info.setSize(f.length());
			info.setDateModifiedOs(new Date(f.lastModified()));
			return true;
		}

		return false;
	}

	private Optional<DOFileInfo> analyzePicture(final DOManagedFile mf,
			final File f, DOImageFileInfo sourceFileInfo) {
		if (mf.isPicturesEnabled()) {
			sourceFileInfo.setFolderPath(f.getParent());
			sourceFileInfo.setFileName(f.getName());
			sourceFileInfo.setType(FileType.PICTURES);
			// TODO: Implement
			return Optional.of((DOFileInfo) sourceFileInfo);
		}
		return Optional.absent();
	}

	private Optional<DOFileInfo> analyzeAudio(final DOManagedFile mf,
			final File f, DOAudioFileInfo sourceFileInfo) {
		if (mf.isAudioEnabled()) {
			sourceFileInfo.setFolderPath(f.getParent());
			sourceFileInfo.setFileName(f.getName());
			sourceFileInfo.setType(FileType.AUDIO);
			// TODO: Implement
			return Optional.of((DOFileInfo)sourceFileInfo);
		}
		return Optional.absent();
	}

	private Optional<DOFileInfo> analyzeVideo(final DOManagedFile mf,
			final File f, DOVideoFileInfo sourceFileInfo) {
		if (mf.isVideoEnabled()) {
			sourceFileInfo.setFolderPath(f.getParent());
			sourceFileInfo.setFileName(f.getName());
			sourceFileInfo.setType(FileType.VIDEO);
			// get the information from pms internal util (mediainfo or ffmpeg)
			populateMovieInfo(sourceFileInfo);

			if (sourceFileInfo.getName().equals("")) {
				sourceFileInfo.setName(sourceFileInfo.getFileName(false));
			}
			if (sourceFileInfo.getSortName().equals("")) {
				sourceFileInfo.setSortName(sourceFileInfo.getName());
			}

			return Optional.of((DOFileInfo) sourceFileInfo);
		}
		return Optional.absent();
	}

	private void populateMovieInfo(final DOVideoFileInfo fi) {

		final File inFile = new File(fi.getFilePath());
		if (!inFile.exists() && !inFile.canRead()) {
			log.error("File "
					+ fi.getFilePath()
					+ " doesn't exist or couldn't be opened as a file for reading");
			return;
		}
		final RealFile rf = new RealFile(inFile);
		// add the parent to avoid a null pointer exception when calling
		// isValid
		rf.setParent(dummyParent);
		if (!rf.isValid()) {
			return;
		}
		rf.resolve();
		final DLNAMediaInfo mi = rf.getMedia();

		try {
			fi.setAspectRatio(mi.getAspect());
			fi.setBitrate(mi.getBitrate());
			fi.setBitsPerPixel(mi.getBitsPerPixel());
			if (mi.getCodecV() != null) {
				fi.setCodecV(mi.getCodecV());
			}
			if (mi.getContainer() != null) {
				fi.setContainer(mi.getContainer());
			}
			fi.setDurationSec(mi.getDurationInSeconds());
			fi.setDvdtrack(mi.getDvdtrack());
			if (mi.getFrameRate() != null) {
				fi.setFrameRate(mi.getFrameRate());
			}
			if (mi.getH264AnnexB() != null) {
				fi.setH264_annexB(mi.getH264AnnexB());
			}
			fi.setHeight(mi.getHeight());
			if (mi.getMimeType() != null) {
				fi.setMimeType(mi.getMimeType());
			}
			if (mi.getModel() != null) {
				fi.setModel(mi.getModel());
			}
			fi.setSize(mi.getSize());
			fi.setWidth(mi.getWidth());
			fi.setMuxingMode(mi.getMuxingMode());
			
			//reset subtitles and add new ones
			fi.setSubtitlesCodes(new ArrayList<DLNAMediaSubtitle>());
			if (mi.getSubtitleTracksList() != null) {
				fi.setSubtitlesCodes(mi.getSubtitleTracksList());
			}

			//reset audio tracks and add new ones
			fi.setAudioCodes(new ArrayList<DLNAMediaAudio>());
			if (mi.getAudioCodes() != null) {
				fi.setAudioCodes(mi.getAudioCodes());
			}
		} catch (final Exception ex) {
			log.error("Failed to parse file info", ex);
		}
	}
}
