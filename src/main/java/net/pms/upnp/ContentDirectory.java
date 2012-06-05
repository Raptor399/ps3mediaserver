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
package net.pms.upnp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teleal.cling.binding.annotations.UpnpService;
import org.teleal.cling.binding.annotations.UpnpServiceId;
import org.teleal.cling.binding.annotations.UpnpServiceType;
import org.teleal.cling.model.message.UpnpHeaders;
import org.teleal.cling.protocol.sync.ReceivingAction;
import org.teleal.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.teleal.cling.support.contentdirectory.ContentDirectoryException;
import org.teleal.cling.support.contentdirectory.DIDLParser;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.BrowseResult;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.SortCriterion;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

/**
 * This class implements the UPnP ContentDirectory service that allows a
 * renderer to query which content a directory contains.
 */
@UpnpService(
		serviceId = @UpnpServiceId("ContentDirectory"),
		serviceType = @UpnpServiceType(value = "ContentDirectory", version = 1)
)
public class ContentDirectory extends AbstractContentDirectoryService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContentDirectory.class);
	private Socket socket;

	/**
	 * Returns the browse result as per the UPnP spec. Quoting the spec:
	 * <p>
	 * The Browse() action is designed to allow the control point to navigate the
	 * "native" content hierarchy exposed by the CDS. This hierarchy could map
	 * onto an explicit physical hierarchy or a logical one. In addition, the
	 * Browse() action enables the following features while navigating the
	 * hierarchy:
	 * <ul>
	 * <li> <b>Metadata only browsing.</b> The metadata associated with a
	 * particular object can be retrieved.</li>
	 * <li> <b>Children object browsing.</b> The direct children of an object derived
	 * from a container can be retrieved.</li>
	 * <li> <b>Incremental navigation</b> i.e. the full hierarchy is never returned
	 * in one call since this is likely to flood the resources available to the control
	 * point (memory, network bandwidth, etc.). Also within a particular
	 * hierarchy level, the control point can restrict the number (and the
	 * starting offset) of objects returned in the result.</li>
	 * <li> <b>Sorting</b>. The result can be requested in a particular sort order.
	 * The available sort orders are expressed in the return value of the
	 * GetSortCapabilities() action.</li>
	 * <li> <b>Filtering</b>. The result data can be filtered to only include a subset
	 * of the properties available on the object. Note that certain properties may not
	 * be filtered out in order to maintain the validity of the result data
	 * fragment. If a non-filterable property is left out of the filter set, it
	 * will still be included in the result.</li>
	 * </ul>
	 * 
	 * @param objectID
	 * @param browseFlag
	 * @param filter
	 * @param firstResult
	 * @param maxResults
	 * @param orderBy
	 * @returns The {@link BrowseResult}.
	 * @throws ContentDirectoryException
	 */
	@Override
	public BrowseResult browse(String objectID, BrowseFlag browseFlag, String filter,
			long firstResult, long maxResults, SortCriterion[] orderBy)
			throws ContentDirectoryException {

		// TODO: Add IP filtering based on InternetAddress
//		InetSocketAddress remoteAddress = (InetSocketAddress) e.getChannel().getRemoteAddress();
//		InetAddress ia = remoteAddress.getAddress();
//
//		// Apply the IP filter
//		if (filterIp(ia)) {
//			e.getChannel().close();
//			logger.trace("Access denied for address " + ia + " based on IP filter");
//			return;
//		}

		LOGGER.trace("Received UPnP Browse Request: ObjectID: \"{}\", BrowseFlag: \"{}\", Filter: \"{}\", FirstResult: {}, MaxResults: {}, OrderBy: {}",
				new Object[] { objectID, browseFlag, filter, firstResult, maxResults, orderBy });

		UpnpHeaders headers = ReceivingAction.getRequestMessage().getHeaders();
		
		RendererConfiguration renderer = RendererConfiguration.getRendererConfiguration(headers);
		PMS.get().setRendererfound(renderer);

		long count = 0;
		long totalMatches = 0;

		DIDLContent didlContent = new DIDLContent();
		boolean directChildren = BrowseFlag.DIRECT_CHILDREN.equals(browseFlag);
		List<DLNAResource> resources;

		try {
			// FIXME: It is wrong to first get a list of resources and then convert
			// them to DIDLObjects since the relationship between container and its
			// children is lost. Introduce a new method getDidlObject(...) that
			// takes the browse arguments.
			resources = PMS.get().getRootFolder(renderer).getDLNAResources(objectID, directChildren,
								// FIXME: should be long according to UPnP spec!
								(int) firstResult, (int) maxResults, renderer);

			// FIXME: Move to search()!
//			if (searchCriteria != null && files != null) {
//				for (int i = files.size() - 1; i >= 0; i--) {
//					if (!files.get(i).getName().equals(searchCriteria)) {
//						files.remove(i);
//					}
//				}
//				if (files.size() > 0) {
//					files = files.get(0).getChildren();
//				}
//			}

			if (resources != null) {
				LOGGER.trace("Files found: " + resources.size());

				for (DLNAResource resource : resources) {
					if (renderer.isXBOX() && objectID != null) {
						resource.setFakeParentId(objectID);
					}

					if (resource.isCompatible(renderer) && (resource.getPlayer() == null || resource.getPlayer().isPlayerCompatible(renderer))) {
						DIDLObject didlObject = resource.getDidlObject(renderer, true);

						if (didlObject instanceof Container) {
							didlContent.addContainer((Container) didlObject);
						} else {
							didlContent.addItem((Item) didlObject);
						}
						count++;
						totalMatches++;
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("An exception occurred", e);
		}

		String xml = "";
		
		try {
			DIDLParser didlParser = new DIDLParser();

			// Careful: DIDLParser() is not thread safe.
			xml = didlParser.generate(didlContent);
			LOGGER.trace(xml);
		} catch (Exception e) {
			LOGGER.error("An exception occurred", e);
		}

		return new BrowseResult(xml, count, totalMatches);
	}

	/**
	 * TODO: Implement own search based on browse() above. 
	 */
    @Override
    public BrowseResult search(String containerId,
                               String searchCriteria, String filter,
                               long firstResult, long maxResults,
                               SortCriterion[] orderBy) throws ContentDirectoryException {
        return super.search(containerId, searchCriteria, filter, firstResult, maxResults, orderBy);
    }
}
