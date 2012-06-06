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

import net.pms.util.PropertiesUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DLNADoc;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;

/**
 * As a server, PMS does two big things: (1) it defines and provides UPnP
 * services. And (2) it serves actual files that its services discovered.
 * This class fires up the server in charge of handling UPnP traffic.
 */
public class UpnpServer implements Runnable {
	// This has to be "MediaServer" to be recognized as one.
	private static final String DEVICE_TYPE_MEDIA_SERVER = "MediaServer";

	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpServer.class);

	private static final String ICON_RESOURCE_LARGE = "/resources/images/icon-256.png";
	private static final String ICON_RESOURCE_SMALL = "/resources/images/icon-32.png";
	private static final String ICON_MIMETYPE = "image/png";

	// TODO: Figure out a way to use one port only, to keep PMS configuration as before.
	// Trying not to overlap with the Netty HTTP service for now.
	private static final int STREAM_LISTEN_PORT = 5002;

	/**
	 * Starts the thread for the UPnP server.
	 *
	 * @throws Exception
	 */
    public static void startServer() throws Exception {
        // Start a user thread that runs the UPnP stack
        Thread serverThread = new Thread(new UpnpServer());
        serverThread.setDaemon(false);
        serverThread.start();
    }

    public void run() {
        try {

        	UpnpServiceConfiguration configuration = new DefaultUpnpServiceConfiguration(STREAM_LISTEN_PORT);
            final UpnpService upnpService = new UpnpServiceImpl(configuration);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    upnpService.shutdown();
                }
            });

            // Add the bound local device to the registry
            upnpService.getRegistry().addDevice(createDevice());

        } catch (Exception e) {
			LOGGER.error("An exception occured.", e);

			// TODO: exit() seems a bit rash here.
			//System.exit(1);
        }
    }

    /**
     * Returns the device that determines which UPnP services PMS will offer.
     * In order to be a proper UPnP MediaServer:1, PMS needs to at least offer
     * a ContentDirectory:1 and ConnectionManager:1 service.
     *
     * @return The {@link LocalDevice}
     * @throws ValidationException
     * @throws LocalServiceBindingException
     * @throws IOException
     */
    LocalDevice createDevice() throws ValidationException, LocalServiceBindingException, IOException {
    	String friendlyName = PropertiesUtil.getProjectProperties().get("project.name");
    	String description  = "UPnP/AV 1.0 Compliant Media Server";
    	String version = PropertiesUtil.getProjectProperties().get("project.version");
    	String modelName = "PMS";
    	String modelUrl = "http://www.ps3mediaserver.org/";
    	String manufacturerName = friendlyName;
    	String manufacturerUrl = "http://www.ps3mediaserver.org/";

    	// Set the details
        DeviceIdentity identity = new DeviceIdentity(UDN.uniqueSystemIdentifier(friendlyName));
        DeviceType type = new UDADeviceType(DEVICE_TYPE_MEDIA_SERVER, 1);
        ModelDetails modelDetails = new ModelDetails(modelName, description, version, modelUrl);
		ManufacturerDetails manufacturerDetails = new ManufacturerDetails(manufacturerName, manufacturerUrl);

		// Not sure what this does?
		DLNADoc doc1 = new DLNADoc("DMS", "1.50");
		DLNADoc doc2 = new DLNADoc("M-DMS", "1.50");
		DLNADoc[] dlnaDocs = new DLNADoc[] { doc1, doc2 };
		
		DeviceDetails details = new DeviceDetails(friendlyName, manufacturerDetails, modelDetails, dlnaDocs, null);

		// Define the icons for devices to show
        Icon iconLarge = new Icon(ICON_MIMETYPE, 256, 256, 8, getClass().getResource(ICON_RESOURCE_LARGE));
        Icon iconSmall = new Icon(ICON_MIMETYPE, 32, 32, 8, getClass().getResource(ICON_RESOURCE_SMALL));
        Icon[] icons = new Icon[] { iconLarge, iconSmall };

        // Bind the ContentDirectory:1 service
        LocalService<ContentDirectory> cdService = new AnnotationLocalServiceBinder().read(ContentDirectory.class);
        cdService.setManager(new DefaultServiceManager(cdService, ContentDirectory.class));

        // Bind the ConnectionManager:1 service
        LocalService<ConnectionManagerService> cmService = new AnnotationLocalServiceBinder().read(ConnectionManagerService.class);
        cmService.setManager(new DefaultServiceManager<ConnectionManagerService>(cmService, ConnectionManagerService.class));

        // Create the device
        return new LocalDevice(identity, type, details, icons, new LocalService[] { cdService, cmService });
    }

}
