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
package net.pms.dlna.virtual;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaAudio;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.formats.FormatFactory;
import net.pms.network.HTTPResource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.fourthline.cling.support.model.DIDLAttribute;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.DIDLObject.Property;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Protocol;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.fourthline.cling.support.model.item.PlaylistItem;
import org.fourthline.cling.support.model.item.VideoItem;

/**
 * Implements a container that when browsed, an action will be performed.
 * The class assumes that the action to be performed is to toggle a boolean value.
 * Because of this, the thumbnail is either a green tick mark or a red cross. Equivalent
 * videos are shown after the value is toggled.<p> 
 * However this is just cosmetic. Any action can be performed.
 */
public abstract class VirtualVideoAction extends DLNAResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(VirtualVideoAction.class);

	private boolean enabled;
	protected String name;
	private String thumbnailIconOK;
	private String thumbnailIconKO;
	private String thumbnailContentType;
	private String videoOk;
	private String videoKo;
	private long timer1;

	/**Constructor for this class. Recommended instantation includes overriding the {@link #enable()} function (example shown in the link).
	 * @param name Name that is shown via the UPNP ContentBrowser service. This field cannot be changed after the instantiation.
	 * @param enabled If true, a green tick mark is shown as thumbnail. If false, a red cross is shown. This initial value
	 * is usually changed via the {@link #enable()} function.
	 */
	public VirtualVideoAction(String name, boolean enabled) {
		this.name = name;
		thumbnailContentType = HTTPResource.PNG_TYPEMIME;
		thumbnailIconOK = "images/apply-256.png";
		thumbnailIconKO = "images/button_cancel-256.png";
		this.videoOk = "videos/action_success-512.mpg";
		this.videoKo = "videos/button_cancel-512.mpg";
		timer1 = -1;
		this.enabled = enabled;

		// Create correct mediaInfo for the embedded .mpg videos
		// This is needed by Format.isCompatible()
		DLNAMediaInfo mediaInfo = new DLNAMediaInfo();
		mediaInfo.setContainer("mpegps");
		ArrayList<DLNAMediaAudio> audioCodes = new ArrayList<DLNAMediaAudio>();
		mediaInfo.setAudioCodes(audioCodes);
		mediaInfo.setMimeType("video/mpeg");
		mediaInfo.setCodecV("mpeg2");
		mediaInfo.setMediaparsed(true);
		
		setMedia(mediaInfo);
	}

	/**
	 * Returns <code>false</code> because this virtual video action should not
	 * appear in the transcode folder.
	 *
	 * @return Always returns <code>false</code>
	 */
	@Override
	public boolean isTranscodeFolderAvailable() {
		return false;
	}

	/**This function is called as an action from the UPNP client when
	 * the user tries to play this item. This function calls instead the enable()
	 * function in order to execute an action.
	 * As the client expects to play an item, a really short video (less than 1s) is shown with 
	 * the results of the action. 
	 * @see #enable()
	 * @see net.pms.dlna.DLNAResource#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		if (timer1 == -1) {
			timer1 = System.currentTimeMillis();
		} else if (System.currentTimeMillis() - timer1 < 2000) {
			timer1 = -1;
		}
		if (timer1 != -1) {
			enabled = enable();
		}
		return getResourceInputStream(enabled ? videoOk : videoKo);
	}

	/**Prototype. This function is called by {@link #getInputStream()} and is the core of this class.
	 * The main purpose of this function is toggle a boolean variable somewhere. 
	 * The value of that boolean variable is shown then as either a green tick mark or a red cross.
	 * However, this is just a cosmetic thing. Any Java code can be executed in this function, not only toggling a boolean variable.
	 * Recommended way to instantiate this class is as follows:
	 * <pre> VirtualFolder vf;
	 * [...]
	 * vf.addChild(new VirtualVideoAction(Messages.getString("PMS.3"), configuration.isMencoderNoOutOfSync()) {
	 *   public boolean enable() {
	 *   configuration.setMencoderNoOutOfSync(!configuration.isMencoderNoOutOfSync());
	 *   return configuration.isMencoderNoOutOfSync();
	 *   }
	 * }); </pre>
	 * @return If true, a green tick mark is shown as thumbnail. If false, a red cross is shown.
	 */
	public abstract boolean enable();

	@Override
	public String getName() {
		return name;
	}

	/**As this item is not a container, returns false.
	 * @return false
	 * @see net.pms.dlna.DLNAResource#isFolder()
	 */
	@Override
	public boolean isFolder() {
		return false;
	}

	/**Returns an invalid length as this item is not 
	 * TODO: (botijo) VirtualFolder returns 0 instead of -1.
	 * @return -1, an invalid length for an item.
	 * @see net.pms.dlna.DLNAResource#length()
	 */
	@Override
	public long length() {
		return -1; //DLNAMediaInfo.TRANS_SIZE;
	}

	public long lastModified() {
		return 0;
	}

	@Override
	public String getSystemName() {
		return getName();
	}

	/**Returns either a green tick mark or a red cross that represents the actual
	 * value of this item
	 * @see net.pms.dlna.DLNAResource#getThumbnailInputStream()
	 */
	@Override
	public InputStream getThumbnailInputStream() {
		return getResourceInputStream(enabled ? thumbnailIconOK : thumbnailIconKO);
	}

	/**@return PNG type, as the thumbnail can only be either a green tick mark or a red cross.
	 * @see #getThumbnailInputStream()
	 * @see net.pms.dlna.DLNAResource#getThumbnailContentType()
	 */
	@Override
	public String getThumbnailContentType() {
		return thumbnailContentType;
	}

	/**TODO: (botijo) Why is ext being set here?
	 * @return True, as this kind of item is always valid.
	 * @see net.pms.dlna.DLNAResource#isValid()
	 */
	@Override
	public boolean isValid() {
		setExt(FormatFactory.getAssociatedExtension("toto.mpg"));
		return true;
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
	 * @return The {@link org.fourthline.cling.support.model.item.Item Item}.
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

				res.setImportUri(getItemURI());
				res.setValue(thumbURL);

				result.addResource(res);
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
