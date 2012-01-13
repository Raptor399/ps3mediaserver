package net.pms.remote;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;

public class RemoteItem extends DLNAResource {
	
	private String url;
	private String name;
	private String thumb;
	
	public RemoteItem(String streamUrl,String name,String albumArt) {
		url=streamUrl;
		this.name=name;
		this.thumb=albumArt;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		URL u=new URL(url);
		return u.openStream();
	}
	
	public InputStream getThumbnailInputStream() {
		try {
			return downloadAndSend(thumb,true);
		}
		catch (Exception e) {
			return null;
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSystemName() {
		return getName();
	}

	@Override
	public boolean isFolder() {
		return false;
	}

	@Override
	public boolean isValid() {
		checktype();
		return true;
	}

	@Override
	public long length() {
		return DLNAMediaInfo.TRANS_SIZE;
	}

}
