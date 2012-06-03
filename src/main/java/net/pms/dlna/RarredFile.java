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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.formats.Format;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teleal.cling.support.model.DIDLAttribute;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.DIDLObject.Property;
import org.teleal.cling.support.model.PersonWithRole;
import org.teleal.cling.support.model.Protocol;
import org.teleal.cling.support.model.ProtocolInfo;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.WriteStatus;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.ImageItem;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.cling.support.model.item.PlaylistItem;
import org.teleal.cling.support.model.item.VideoItem;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

public class RarredFile extends DLNAResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(RarredFile.class);
	private File f;
	private Archive rarFile;

	public RarredFile(File f) {
		this.f = f;
		setLastmodified(f.lastModified());
		try {
			rarFile = new Archive(f);
			List<FileHeader> headers = rarFile.getFileHeaders();
			for (FileHeader fh : headers) {
				//if (fh.getFullUnpackSize() < MAX_ARCHIVE_ENTRY_SIZE && fh.getFullPackSize() < MAX_ARCHIVE_ENTRY_SIZE)
				addChild(new RarredEntry(fh.getFileNameString(), f, fh.getFileNameString(), fh.getFullUnpackSize()));
			}
			rarFile.close();
		} catch (RarException e) {
			LOGGER.error(null, e);
		} catch (IOException e) {
			LOGGER.error(null, e);
		}
	}

	public InputStream getInputStream() throws IOException {
		return new FileInputStream(f);
	}

	public String getName() {
		return f.getName();
	}

	public long length() {
		return f.length();
	}

	public boolean isFolder() {
		return true;
	}

	public long lastModified() {
		return 0;
	}

	@Override
	public String getSystemName() {
		return f.getAbsolutePath();
	}

	@Override
	public boolean isValid() {
		boolean t = false;
		try {
			t = f.exists() && !rarFile.isEncrypted();
		} catch (Throwable th) {
			LOGGER.debug("Caught exception", th);
		}
		return t;
	}

	/**
	 * Returns the item detail information for this resource as per the UPnP spec.
	 * 
	 * @param renderer
	 *            Media Renderer for which to represent this information. Useful
	 *            for some hacks.
	 * @param includeChildren
	 *            Set to true if the resource should include its immediate
	 *            children as container items.
	 * @return The {@link org.teleal.cling.support.model.item.Item Item}.
	 */
	public DIDLObject getDidlObject(RendererConfiguration renderer, boolean includeChildren) {
		DIDLObject result;

		if (isFolder()) {
			result = new Container();
		} else {
			// Determine the item type first
			switch (getType()) {
				case Format.AUDIO:
					result = new MusicTrack();
					// Missing: AudioBook and AudioBroadcast
					break;

				case Format.IMAGE:
					result = new ImageItem();
					// Missing: Photo
					break;

				case Format.VIDEO:
					result = new VideoItem();
					// Missing: Movie, MusicVideoClip and VideoBroadcast
					break;

				case Format.PLAYLIST:
					result = new PlaylistItem();
					break;

				default:
					result = new Item();
					break;
			}
		}

		result.setId(getResourceId());

		if (isFolder()) {
			if (!isDiscovered() && childrenNumber() == 0) {
				//  When a folder has not been scanned for resources, it will automatically have zero children.
				//  Some renderers like XBMC will assume a folder is empty when encountering childCount="0" and
				//  will not display the folder. By returning childCount="1" these renderers will still display
				//  the folder. When it is opened, its children will be discovered and childrenNumber() will be
				//  set to the right value.
				((Container) result).setChildCount(1);
			} else {
				((Container) result).setChildCount(childrenNumber());
				
				if (includeChildren) {
					for (DLNAResource child : getChildren()) {
						DIDLObject object = child.getDidlObject(renderer, false);
	
						if (object instanceof Container) {
							((Container) result).addContainer((Container) object);
						} else {
							((Container) result).addItem((Item) object);
						}
					}
				}
			}
		}

		result.setParentID(getParentId());
		result.setRestricted(true);

		final DLNAMediaAudio firstAudioTrack = getMedia() != null ? getMedia().getFirstAudioTrack() : null;
		String title;

		if (firstAudioTrack != null && StringUtils.isNotBlank(firstAudioTrack.getSongname())) {
			String playerName = "";

			if (getPlayer() != null && !PMS.getConfiguration().isHideEngineNames()) {
				playerName = " [" + getPlayer().name() + "]";
			}

			title = firstAudioTrack.getSongname() + playerName;
		} else {
			if (isFolder() || getPlayer() == null) {
				title = getDisplayName();
			} else {
				title = renderer.getUseSameExtension(getDisplayName(renderer));
			}
		}

		result.setTitle(title);

		if (firstAudioTrack != null) {
			if (StringUtils.isNotBlank(firstAudioTrack.getAlbum())) {
				((MusicTrack) result).setAlbum(firstAudioTrack.getAlbum());
			}

			if (StringUtils.isNotBlank(firstAudioTrack.getArtist())) {
				PersonWithRole artist = new PersonWithRole(firstAudioTrack.getArtist());
				((MusicTrack) result).setArtists(new PersonWithRole[] { artist });
				((MusicTrack) result).setCreator(firstAudioTrack.getArtist());
			}

			if (StringUtils.isNotBlank(firstAudioTrack.getGenre())) {
				((MusicTrack) result).setGenres(new String[] { firstAudioTrack.getGenre() });
			}

			if (firstAudioTrack.getTrack() > 0) {
				((MusicTrack) result).setOriginalTrackNumber(firstAudioTrack.getTrack());
			}
		}

		String thumbURL = getThumbnailURL();

		if (!isFolder()) {
			int indexCount = 1;
			String flags = getDlnaOrgOp(renderer); 

			// FIXME: Setting global variable to keep DLNAResource.getDlnaContentFeatures()
			// happy. Remove this when the transition to Cling is complete.
			setFlags(flags);

			if (renderer.isDLNALocalizationRequired()) {
				indexCount = getDLNALocalesCount();
			}

			// Determine the renderer specific mime type
			String mimeType = getRendererMimeType(mimeType(), renderer);

			if (mimeType == null) {
				// Type couldn't be determined; assume video/mpeg
				mimeType = "video/mpeg";
			}

			for (int c = 0; c < indexCount; c++) {
				String dlnaspec = getDlnaOrgPn(renderer, mimeType, c);

				// FIXME: We're in a for loop setting a global variable. Smells fishy!
				// Doing it nevertheless to keep DLNAResource.getDlnaContentFeatures() happy.
				// Remove this when the transition to Cling is complete.
				setDlnaSpec(dlnaspec);

				// Construct protocol info
				ProtocolInfo protocolInfo = new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD,
						mimeType, (dlnaspec != null ? (dlnaspec + ";") : "") + flags);

				// Construct resource info
				Res res = new Res();
				res.setProtocolInfo(protocolInfo);


				if (getExt() != null && getExt().isVideo() && getMedia() != null && getMedia().isMediaparsed()) {
					if (getPlayer() == null && getMedia() != null) {
						res.setSize(getMedia().getSize());
					} else {
						long transcoded_size = renderer.getTranscodedSize();

						if (transcoded_size != 0) {
							res.setSize(transcoded_size);
						}
					}

					if (getMedia().getDuration() != null) {
						if (getSplitRange().isEndLimitAvailable()) {
							res.setDuration(DLNAMediaInfo.getDurationString(getSplitRange().getDuration()));
						} else {
							res.setDuration(getMedia().getDurationString());
						}
					}

					if (getMedia().getResolution() != null) {
						res.setResolution(getMedia().getResolution());
					}

					res.setBitrate((long) getMedia().getRealVideoBitrate());

					if (firstAudioTrack != null) {
						if (firstAudioTrack.getNrAudioChannels() > 0) {
							res.setNrAudioChannels((long) firstAudioTrack.getNrAudioChannels());
						}
						if (firstAudioTrack.getSampleFrequency() != null) {
							try {
								res.setSampleFrequency(Long.parseLong(firstAudioTrack.getSampleFrequency()));
							} catch (NumberFormatException e) {
								LOGGER.debug("Cannot parse audio track sample frequency " + firstAudioTrack.getSampleFrequency());
							}
						}
					}
				} else if (getExt() != null && getExt().isImage()) {
					if (getMedia() != null && getMedia().isMediaparsed()) {
						res.setSize(getMedia().getSize());

						if (getMedia().getResolution() != null) {
							res.setResolution(getMedia().getResolution());
						}
					} else {
						res.setSize(length());
					}
				} else if (getExt() != null && getExt().isAudio()) {
					if (getMedia() != null && getMedia().isMediaparsed()) {
						res.setBitrate((long) getMedia().getBitrate());

						if (getMedia().getDuration() != null) {
							res.setDuration(DLNAMediaInfo.getDurationString(getMedia().getDuration()));
						}

						if (firstAudioTrack != null && firstAudioTrack.getSampleFrequency() != null) {
							try {
								res.setSampleFrequency(Long.parseLong(firstAudioTrack.getSampleFrequency()));
							} catch (NumberFormatException e) {
								LOGGER.debug("Cannot parse audio track sample frequency " + firstAudioTrack.getSampleFrequency());
							}
						}

						if (firstAudioTrack != null) {
							res.setNrAudioChannels((long) firstAudioTrack.getNrAudioChannels());
						}

						if (getPlayer() == null) {
							res.setSize(getMedia().getSize());
						} else {
							// calcul taille wav
							if (firstAudioTrack != null) {
								int defaultFrequency = renderer.isTranscodeAudioTo441() ? 44100 : 48000;

								if (!PMS.getConfiguration().isAudioResample()) {
									try {
										// FIXME: Which exception could be thrown here?
										defaultFrequency = firstAudioTrack.getSampleRate();
									} catch (Exception e) {
										LOGGER.debug("Caught exception", e);
									}
								}

								int na = firstAudioTrack.getNrAudioChannels();

								if (na > 2) { 
									// no 5.1 dump in mplayer
									na = 2;
								}

								long finalsize = (long) (getMedia().getDurationInSeconds() * defaultFrequency * 2 * na);
								LOGGER.debug("Calculated size: " + finalsize);
								res.setSize(finalsize);
							}
						}
					} else {
						res.setSize(length());
					}
				} else {
					// Video without media info; set some defaults
					res.setSize(DLNAMediaInfo.TRANS_SIZE);
					res.setDuration("09:59:59");
					res.setBitrate(1000000L);
				}

				URI itemUri = getItemURI();
				
				if (itemUri != null) {
					res.setImportUri(itemUri);
				} else {
					LOGGER.debug("Cannot determine import URI for " + getName());
				}
				
				String fileUrl = getFileURL();
				
				if (fileUrl != null) {
					res.setValue(fileUrl);
					result.addResource(res);
				} else {
					LOGGER.debug("Cannot determine file URL for " + getName() + ", skipping.");
				}
			}
		}

		if (!isFolder() && (getExt() == null || (getExt() != null && thumbURL != null))) {
			String typeName = "JPEG_TN";

			if (getThumbnailContentType().equals(PNG_TYPEMIME) && !renderer.isForceJPGThumbnails()) {
				typeName = "PNG_TN";
			}

			DIDLAttribute profileAttribute = new DIDLAttribute(Property.DLNA.NAMESPACE.URI, "dlna", typeName);
			Property<DIDLAttribute> attribute = new Property.DLNA.PROFILE_ID(profileAttribute);

			try {
				URI thumbUri = new URI(thumbURL);
				Property<URI> albumArt = new Property.UPNP.ALBUM_ART_URI(thumbUri);
				albumArt.addAttribute(attribute);
				result.addProperty(albumArt);
			} catch (URISyntaxException e) {
				LOGGER.debug("Error in URI syntax for album art \"" + thumbURL + "\"");
			}
		}

		if ((isFolder() || renderer.isForceJPGThumbnails()) && thumbURL != null) {
			String contentFormat = "image/jpeg";
			String additionalInfo = "DLNA.ORG_PN=JPEG_TN";
			
			if (getThumbnailContentType().equals(PNG_TYPEMIME) && !renderer.isForceJPGThumbnails()) {
				contentFormat = "image/png";
				additionalInfo = "DLNA.ORG_PN=PNG_TN";
			}

			ProtocolInfo protocolInfo = new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD,
					contentFormat, additionalInfo);
			Res res = new Res();
			
			res.setProtocolInfo(protocolInfo);
			URI itemUri = getItemURI();
			
			if (itemUri != null) {
				res.setImportUri(itemUri);
			} else {
				LOGGER.debug("Cannot determine import URI for " + getName());
			}

			res.setValue(thumbURL);
			result.addResource(res);
		}

		if (getLastmodified() > 0) {
			SimpleDateFormat SDF_DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
			Property<String> lastmodified = new Property.DC.DATE(SDF_DATE.format(new Date(getLastmodified())));
			result.addProperty(lastmodified);
		}

		String uclass = null;

		if (getFirst() != null && getMedia() != null && !getMedia().isSecondaryFormatValid()) {
			uclass = "dummy";
		} else {
			if (isFolder()) {
				uclass = "object.container.storageFolder";
				boolean xbox = renderer.isXBOX();
				if (xbox && getFakeParentId() != null && getFakeParentId().equals("7")) {
					uclass = "object.container.album.musicAlbum";
				} else if (xbox && getFakeParentId() != null && getFakeParentId().equals("6")) {
					uclass = "object.container.person.musicArtist";
				} else if (xbox && getFakeParentId() != null && getFakeParentId().equals("5")) {
					uclass = "object.container.genre.musicGenre";
				} else if (xbox && getFakeParentId() != null && getFakeParentId().equals("F")) {
					uclass = "object.container.playlistContainer";
				}
			} else if (getExt() != null && getExt().isVideo()) {
				uclass = "object.item.videoItem";
			} else if (getExt() != null && getExt().isImage()) {
				uclass = "object.item.imageItem.photo";
			} else if (getExt() != null && getExt().isAudio()) {
				uclass = "object.item.audioItem.musicTrack";
			} else {
				uclass = "object.item.videoItem";
			}
		}

		if (uclass != null) {
			result.setClazz(new DIDLObject.Class(uclass));
		}

		result.setWriteStatus(WriteStatus.NOT_WRITABLE);
		result.setCreator("System");
		return result;
	}
}
