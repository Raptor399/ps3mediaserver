/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2011  Zsombor G.
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
package net.pms.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MAC Address filter class filter remote hosts based on their MAC address.
 * 
 * FIXME: The filter cannot determine the MAC address of a remote client reliably.
 * This is because of how TCP/IP works.
 * See: http://stackoverflow.com/questions/839973/how-to-get-a-clients-mac-address-from-httpservlet
 *
 * @since 1.50.1
 */
public class MacFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(MacFilter.class);

	/**
	 * Stores the unprocessed MAC address filter string.
	 */
	private String rawFilter = null;

	/**
	 * Keeps track of the addresses for which log messages have already been issued.
	 */
	private Set<String> logged = new HashSet<String>();

	/**
	 * Default constructor creates an empty filter.
	 */
	public MacFilter() {
	}

	/**
	 * Constructor to create a MAC address filter based on a comma separated
	 * string of MAC addresses. For example:
	 * <code>00:0C:6E:D2:11:E6,20:FC:6E:CC:FF:E6</code>
	 *
	 * @param filter The comma separated string of MAC addresses. 
	 */
	public MacFilter(String filter) {
		setRawFilter(filter);
	}

	/**
	 * Returns the unprocessed MAC address filter string.
	 *
	 * @return The string.
	 */
	public String getRawFilter() {
		return rawFilter;
	}

	/**
	 * Sets the unprocessed MAC address filter string and resets the filter
	 * with respect to logging.
	 *
	 * @param filter The filter string.
	 */
	public synchronized void setRawFilter(String filter) {
		// Do not reset logging if the filter does not change
		if ((filter == null && rawFilter == null) ||
				(filter != null && filter.equals(rawFilter))) {
			return;
		}

		// Reset logging
		logged.clear();
		rawFilter = filter;
	}

	@Override
	public String toString() {
		return "MacFilter: " + getRawFilter();
	}

	/**
	 * Returns whether or not an internet address is allowed to connect
	 * according to the MAC address filter.
	 *
	 * @param addr The internet address.
	 * @return True if connecting is allowed, false otherwise. 
	 */
	public boolean allowed(InetAddress addr) {
		// Determine if this is the first encounter for this IP address
		boolean log = isFirstDecision(addr);

		if (rawFilter == null || "".equals(rawFilter)) {
			if (log) {
				LOGGER.info("No MAC filter specified, access granted to " + addr);
			}
			return true;
		}

		String macAddress = inetAddressToMacAddress(addr);
		
		if (macAddress == null) {
			if (log) {
				LOGGER.info("Cannot determine MAC address, access denied for " + addr);
			}
			return false;
		}

		String[] filters = rawFilter.split(",");

		for (String f: filters) {
			if (macAddress.equals(f.toUpperCase())) {
				if (log) {
					LOGGER.info("Access granted to MAC address " + macAddress
							+ " for " + addr + " by rule " + rawFilter);
				}
				return true;
			}
		}

		if (log) {
			LOGGER.info("Access denied to MAC address " + macAddress + " for " + addr);
		}
		return false;
	}

	/**
	 * Translate an internet address to a MAC address in the format of e.g.
	 * <code>00:0C:6E:D2:11:E6</code>. Note that his will only work for
	 * addresses on the same local subnet since MAC addresses further away
	 * than one hop cannot be determined.
	 *
	 * @param address The internet address to translate.
	 * @return The MAC address, or <code>null</code> if no MAC address could
	 * 			be determined.
	 */
	private String inetAddressToMacAddress(InetAddress address) {
		String macAddress = null;
		NetworkInterface ni;

		try {
			ni = NetworkInterface.getByInetAddress(address);

			if (ni != null) {
				byte[] hardwareAddress = ni.getHardwareAddress();

				if (hardwareAddress != null) {
				    StringBuilder sb = new StringBuilder(3 * hardwareAddress.length - 1);
				    boolean addSeparator = false;

				    // Convert individual bytes to one string as "FF:FF:FF:FF:FF:FF"
				    for (byte b : hardwareAddress) {
				    	if (addSeparator) {
					        sb.append(":");
				    	}
				        sb.append("0123456789ABCDEF".charAt((b & 0xF0) >> 4));
				        sb.append("0123456789ABCDEF".charAt((b & 0x0F)));
				    }

				    macAddress = sb.toString();
				} else {
					LOGGER.trace("Hardware address not found for " + address);
				}
			} else {
				// FIXME: We seemingly always end up here, making the MAC filter useless.
				LOGGER.trace("Network Interface is null for " + address);
			}
		} catch (SocketException e) {
			LOGGER.trace("Network Interface cannot be determined for " + address);
		}

		return macAddress;
	}

	/**
	 * Returns whether or not this is the first time the filter is making a
	 * decision for this internet address.
	 *
	 * @param addr The internet address.
	 * @return True if this is the first time, false otherwise.
	 */
	private synchronized boolean isFirstDecision(InetAddress addr) {
		String ip = addr.getHostAddress();

		if (!logged.contains(ip)) {
			logged.add(ip);
			return true;
		}
		return false;
	}
}
