package net.pms.dlna;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jwbroek.cuelib.CueParser;
import jwbroek.cuelib.CueSheet;
import jwbroek.cuelib.FileData;
import jwbroek.cuelib.Position;
import jwbroek.cuelib.TrackData;
import net.pms.PMS;
import net.pms.api.PmsCore;
import net.pms.di.InjectionHelper;
import net.pms.encoders.MEncoderVideo;
import net.pms.encoders.MPlayerAudio;
import net.pms.encoders.Player;
import net.pms.formats.Format;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class CueFolder extends DLNAResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(CueFolder.class);
	private File playlistfile;
	private final PmsCore pmsCore;

	public File getPlaylistfile() {
		return playlistfile;
	}
	private boolean valid = true;

	/**
	 * Constructor for backwards compatibility with plugins.
	 *
	 * @param f
	 */
	public CueFolder(File f) {
		this(InjectionHelper.getInjector().getInstance(PmsCore.class), f);
	}

	@AssistedInject
	public CueFolder(PmsCore pmsCore, @Assisted File f) {
		this.pmsCore = pmsCore;
		playlistfile = f;
		setLastmodified(playlistfile.lastModified());
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getName() {
		return playlistfile.getName();
	}

	@Override
	public String getSystemName() {
		return playlistfile.getName();
	}

	@Override
	public boolean isFolder() {
		return true;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public long length() {
		return 0;
	}

	@Override
	public void resolve() {
		Injector injector = InjectionHelper.getInjector();

		if (playlistfile.length() < 10000000) {
			CueSheet sheet = null;
			try {
				sheet = CueParser.parse(playlistfile);
			} catch (IOException e) {
				LOGGER.info("Error in parsing cue: " + e.getMessage());
				return;
			}

			if (sheet != null) {
				List<FileData> files = sheet.getFileData();
				// only the first one
				if (files.size() > 0) {
					FileData f = files.get(0);
					List<TrackData> tracks = f.getTrackData();
					Player defaultPlayer = null;
					DLNAMediaInfo originalMedia = null;
					ArrayList<DLNAResource> addedResources = new ArrayList<DLNAResource>();
					for (int i = 0; i < tracks.size(); i++) {
						TrackData track = tracks.get(i);
						if (i > 0) {
							double end = getTime(track.getIndices().get(0).getPosition());
							if (addedResources.isEmpty()) {
								// seems the first file was invalid or non existent
								return;
							}
							DLNAResource prec = addedResources.get(i - 1);
							int count = 0;
							while (prec.isFolder() && i + count < addedResources.size()) { // not used anymore
								prec = addedResources.get(i + count);
								count++;
							}
							prec.getSplitRange().setEnd(end);
							prec.getMedia().setDuration(prec.getSplitRange().getDuration());
							LOGGER.debug("Track #" + i + " split range: " + prec.getSplitRange().getStartOrZero() + " - " + prec.getSplitRange().getDuration());
						}
						Position start = track.getIndices().get(0).getPosition();
						RealFile r = new RealFile(new File(playlistfile.getParentFile(), f.getFile()));
						addChild(r);
						addedResources.add(r);
						if (i > 0 && r.getMedia() == null) {
							DLNAMediaInfo mediaInfo = injector.getInstance(DLNAMediaInfo.class);
							r.setMedia(mediaInfo);
							r.getMedia().setMediaparsed(true);
						}
						r.resolve();
						if (i == 0) {
							originalMedia = r.getMedia();
						}
						r.getSplitRange().setStart(getTime(start));
						r.setSplitTrack(i + 1);

						if (r.getPlayer() == null) { // assign a splitter engine if file is natively supported by renderer
							if (defaultPlayer == null) {
								if (r.getFormat() == null) {
									LOGGER.error("No file format known for file \"{}\", assuming it is a video for now.", r.getName());
									defaultPlayer = injector.getInstance(MEncoderVideo.class);
								} else {
									if (r.getFormat().isAudio()) {
										defaultPlayer = injector.getInstance(MPlayerAudio.class);
									} else {
										defaultPlayer = injector.getInstance(MEncoderVideo.class);
									}
								}
							}

							r.setPlayer(defaultPlayer);
						}

						if (r.getMedia() != null) {
							try {
								r.setMedia((DLNAMediaInfo) originalMedia.clone());
							} catch (CloneNotSupportedException e) {
								LOGGER.info("Error in cloning media info: " + e.getMessage());
							}
							if (r.getMedia() != null && r.getMedia().getFirstAudioTrack() != null) {
								if (r.getFormat().isAudio()) {
									r.getMedia().getFirstAudioTrack().setSongname(track.getTitle());
								} else {
									r.getMedia().getFirstAudioTrack().setSongname("Chapter #" + (i + 1));
								}
								r.getMedia().getFirstAudioTrack().setTrack(i + 1);
								r.getMedia().setSize(-1);
								if (StringUtils.isNotBlank(sheet.getTitle())) {
									r.getMedia().getFirstAudioTrack().setAlbum(sheet.getTitle());
								}
								if (StringUtils.isNotBlank(sheet.getPerformer())) {
									r.getMedia().getFirstAudioTrack().setArtist(sheet.getPerformer());
								}
								if (StringUtils.isNotBlank(track.getPerformer())) {
									r.getMedia().getFirstAudioTrack().setArtist(track.getPerformer());
								}
							}

						}

					}

					if (tracks.size() > 0 && addedResources.size() > 0) {
						// last track
						DLNAResource prec = addedResources.get(addedResources.size() - 1);
						prec.getSplitRange().setEnd(prec.getMedia().getDurationInSeconds());
						prec.getMedia().setDuration(prec.getSplitRange().getDuration());
						LOGGER.debug("Track #" + childrenNumber() + " split range: " + prec.getSplitRange().getStartOrZero() + " - " + prec.getSplitRange().getDuration());
					}

					pmsCore.storeFileInCache(playlistfile, Format.PLAYLIST);

				}
			}
		}
	}

	private double getTime(Position p) {
		return p.getMinutes() * 60 + p.getSeconds() + ((double) p.getFrames() / 100);
	}
}
