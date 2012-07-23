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
package net.pms.network;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.pms.api.PmsConfiguration;
import net.pms.api.PmsCore;
import net.pms.dlna.DLNAMediaDatabase;
import net.pms.util.PropertiesUtil;

@Singleton
public class HTMLConsole {
	private final PmsCore pmsCore;
	private final PmsConfiguration configuration;

	@Inject
	HTMLConsole(PmsCore pmsCore, PmsConfiguration configuration) {
		this.pmsCore = pmsCore;
		this.configuration = configuration;
	}

	public String servePage(String resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>" + PropertiesUtil.getProjectProperties().get("project.name") + " HTML Console</title></head><body>");
		DLNAMediaDatabase database = pmsCore.getDatabase();
	
		if (resource.equals("compact") && configuration.getUseCache()) {
			database.compact();
			sb.append("<p align=center><b>Database compacted!</b></p><br>");
		}

		if (resource.equals("scan") && configuration.getUseCache()) {
			if (!database.isScanLibraryRunning()) {
				database.scanLibrary();
			}
			if (database.isScanLibraryRunning()) {
				sb.append("<p align=center><b>Scan in progress! you can also <a href=\"stop\">stop it</a></b></p><br>");
			}
		}

		if (resource.equals("stop") && configuration.getUseCache() && database.isScanLibraryRunning()) {
			sb.append("<p align=center><b>Scan stopped!</b></p><br>");
		}

		sb.append("<p align=center><img src='/images/thumbnail-256.png'><br>" + PropertiesUtil.getProjectProperties().get("project.name") + " HTML console<br><br>Menu:<br>");
		sb.append("<a href=\"home\">Home</a><br>");
		sb.append("<a href=\"scan\">Scan folders</a><br>");
		sb.append("<a href=\"compact\">Shrink cache database (not recommended)</a>");
		sb.append("</p></body></html>");
		return sb.toString();
	}
}
