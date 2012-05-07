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

import org.teleal.cling.binding.annotations.UpnpAction;
import org.teleal.cling.binding.annotations.UpnpInputArgument;
import org.teleal.cling.binding.annotations.UpnpOutputArgument;
import org.teleal.cling.binding.annotations.UpnpService;
import org.teleal.cling.binding.annotations.UpnpServiceId;
import org.teleal.cling.binding.annotations.UpnpServiceType;
import org.teleal.cling.binding.annotations.UpnpStateVariable;

/**
 * TODO: Have a look at {@link org.teleal.cling.support.connectionmanager.ConnectionManagerService}
 * instead of implementing this from scratch.
 */
@UpnpService(
		serviceId = @UpnpServiceId("ContentDirectory"),
		serviceType = @UpnpServiceType(value = "ContentDirectory", version = 1)
)
public class ConnectionManager {

	@UpnpStateVariable(defaultValue = "0")
	private int A_ARG_TYPE_ConnectionID;

	@UpnpStateVariable(defaultValue = "")
	private String currentConnectionIDs = "";

	@UpnpStateVariable(defaultValue = "")
	private String sourceProtocolInfo = "";

	@UpnpStateVariable(defaultValue = "")
	private String sinkProtocolInfo = "";

	@UpnpStateVariable(defaultValue = "0")
	private int A_ARG_TYPE_RcsID = 0;

	@UpnpStateVariable(defaultValue = "0")
	private int A_ARG_TYPE_AVTransportID = 0;

	@UpnpStateVariable(defaultValue = "")
	private String A_ARG_TYPE_ProtocolInfo = "";

	@UpnpStateVariable(defaultValue = "")
	private String A_ARG_TYPE_ConnectionManager = "";

	@UpnpStateVariable(defaultValue = "")
	private String A_ARG_TYPE_Direction = "";

	@UpnpStateVariable(defaultValue = "")
	private String A_ARG_TYPE_ConnectionStatus = "";

	@UpnpAction(out = {
			@UpnpOutputArgument(name = "A_ARG_TYPE_RcsID"),
			@UpnpOutputArgument(name = "A_ARG_TYPE_AVTransportID"),
			@UpnpOutputArgument(name = "A_ARG_TYPE_ProtocolInfo"),
			@UpnpOutputArgument(name = "A_ARG_TYPE_ConnectionManager"),
			@UpnpOutputArgument(name = "A_ARG_TYPE_ConnectionID"),
			@UpnpOutputArgument(name = "A_ARG_TYPE_Direction"),
			@UpnpOutputArgument(name = "A_ARG_TYPE_ConnectionStatus")
		})
	public void getCurrentConnectionInfo(
			@UpnpInputArgument(name = "A_ARG_TYPE_ConnectionID") int connectionID) {

	}

	@UpnpAction(out = {
			@UpnpOutputArgument(name = "A_ARG_TYPE_ConnectionID"),
			@UpnpOutputArgument(name = "A_ARG_TYPE_AVTransportID"),
			@UpnpOutputArgument(name = "A_ARG_TYPE_RcsID")
		})
	public void prepareForConnection(
			@UpnpInputArgument(name = "A_ARG_TYPE_ProtocolInfo") String remoteProtocolInfo,
			@UpnpInputArgument(name = "A_ARG_TYPE_ConnectionManager") String peerConnectionManager,
			@UpnpInputArgument(name = "A_ARG_TYPE_ConnectionID") int peerConnectionID,
			@UpnpInputArgument(name = "A_ARG_TYPE_Direction") String direction) {
		
	}

	@UpnpAction(out = @UpnpOutputArgument(name = "ConnectionIDs"))
	public String getCurrentConnectionIDs() {
		return currentConnectionIDs;
	}

	@UpnpAction
	public void connectionComplete(@UpnpInputArgument(name = "A_ARG_TYPE_ConnectionID") int connectionID) {
	}

	@UpnpAction(out = {
			@UpnpOutputArgument(name = "SourceProtocolInfo"),
			@UpnpOutputArgument(name = "SinkProtocolInfo")
		})
	public void getProtocolInfo() {
		
	}
}
