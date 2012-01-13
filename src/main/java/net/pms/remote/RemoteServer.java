package net.pms.remote;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.swing.JComponent;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.external.AdditionalFolderAtRoot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteServer implements Runnable {
	
	public final static String DEFAULT_USER_AGENT="PMS";
	
	public static ArrayList<RemoteServer> parse() throws Exception {
		ArrayList<RemoteServer> res=new ArrayList<RemoteServer>();
		File f=new File((String) PMS.getConfiguration().getProfileDirectory()+File.separator+"PMS.srv");
		BufferedReader in = new BufferedReader(new FileReader(f));
		String str;
		RemoteServer srv=null;
		int line=0;
		while ((str = in.readLine()) != null) {
			line++;
			str=str.trim();
			if(RemoteCommon.ignoreLine(str))
				continue;
			if(str.startsWith("{")) {
				if(srv!=null) // syntax error, throw exception
					throw new Exception("Syntax error in remote srv file @ line "+line);
				srv=new RemoteServer();
			}
			if(str.startsWith("}")) {
				if(srv==null) // error, but we are nice skip this
					continue;
				logger.debug("add "+srv.toString());
				srv.start();
				res.add(srv);
				srv=null;
			}
			if(srv==null) // error here skip and hope to get in sync again
				continue;
			if(str.startsWith("name="))
				srv.setDispName(str.substring(5));
			if(str.startsWith("addr="))
				srv.setAddr(str.substring(5));
			if(str.startsWith("port="))
				srv.setPort(str.substring(5));
			if(str.startsWith("user="))
				srv.setUser(str.substring(5));
			if(str.startsWith("pwd="))
				srv.setPwd(str.substring(4));
			if(str.startsWith("ua="))
				srv.setUA(str.substring(3));
		}
		return res;
	}
	
	private Socket sock;
	private String usr;
	private String pwd;
	private String addr;
	private String ua;
	private int port;
	private MessageDigest md5;
	private Thread t;
	private DLNAResource root;
	private String name;
	
	private static final Logger logger = LoggerFactory.getLogger(RemoteServer.class);
	
	public RemoteServer() throws NoSuchAlgorithmException {
		port=7890;
		ua=DEFAULT_USER_AGENT;
		md5 = MessageDigest.getInstance("MD5");
	}
	
	public void start() {
		t=new Thread(this);
		t.start();
	}
	
	public void setDispName(String n) {
		name=n;
	}
	
	public void setUser(String s) {
		usr=s;
	}
	
	public void setPwd(String s) {
		pwd=s;
	}
	
	public void setAddr(String s) {
		addr=s;
	}
	
	public void setPort(String s) {
		try {
			int rPort=Integer.parseInt(s);
			port=rPort;
		}
		catch (Exception e) {
		}
	}
	
	public void setUA(String s) {
		ua=s;
	}
	
	public String getDispName() {
		return name;
	}

	private void authenticate() {
		if(usr!=null&&pwd!=null) {
			String authStr=addr+":"+String.valueOf(port)+","+usr+",";
			String speed=",true";
			try {
				byte[] op=RemoteCommon.intToByteArray(3);
				byte[] size=new byte[4];
				sock.getInputStream().read(size);
				int len=RemoteCommon.byteArrayToInt(size,0);
				logger.debug("read salt size "+len);
				if(len<4) // weird
					return;
				byte[] salt=new byte[len];
				sock.getInputStream().read(salt);
				logger.debug("read salt "+new String(salt));
				String saltStr=pwd+new String(salt);
				byte[] digest=md5.digest(saltStr.getBytes());
				String realAuth=authStr+RemoteCommon.toHex(digest)+speed;
				byte[] outLen=RemoteCommon.intToByteArray(realAuth.length());
				sock.getOutputStream().write(op);
				sock.getOutputStream().write(outLen);
				sock.getOutputStream().write(realAuth.getBytes());
			}
			catch (Exception e) {
				return;
			}
		}
	}
	
	public void run() {
		try {
			logger.trace("connect "+toString());
			sock = new Socket(addr,port);
			logger.trace("connected ");
		} catch (Exception e) {
			logger.error("Could not start static "+e);
			return;
		}
		for(;;) {
			byte[] op=new byte[4];
			try {
				if(sock.getInputStream().read(op)<0)
					throw new Exception("Stream closed");
				int rop=RemoteCommon.byteArrayToInt(op,0);
				if(rop==0) { // challenge
					authenticate();
					continue;
				}
				if(rop==1) {
					byte[] size=new byte[4];
					sock.getInputStream().read(size);
					int x=RemoteCommon.byteArrayToInt(size,0);
					byte[] buf=new byte[x];
					int len=sock.getInputStream().read(buf);
					continue;
				}
				if(rop==2) {
					byte[] nop=RemoteCommon.intToByteArray(4);
					sock.getOutputStream().write(nop);
					continue;
				}
			}
			catch (Exception e) {
				return;
			}
		}
	}
	
	public String dlnaAction(String msg,String action) throws Exception {
		URL u=new URL("http://"+addr+":"+port+"/upnp/control/content_directory");
		URLConnection uc=u.openConnection();
		uc.setRequestProperty("Content-Type","text/xml; charset=\"utf-8\"");
		uc.setRequestProperty("Content-Length",String.valueOf(msg.length()));
		uc.setRequestProperty("SOAPACTION",action);
		uc.setRequestProperty("User-Agent", ua);
		uc.setDoOutput(true);   
		uc.setDoInput(true);
		
		logger.trace("send "+msg);
		uc.connect();
		DataOutputStream output = new DataOutputStream(uc.getOutputStream());
		output.writeBytes(msg);   
		output.flush ();   
		output.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
		StringBuilder page=new StringBuilder();
		String str;
		logger.trace("start read");
		while ((str = in.readLine()) != null) {
			//	page.append("\n");
			page.append(str.trim());
			page.append("\n");
		}
		in.close();
		logger.trace("got out "+page.toString());
		return page.toString();
	}

	public DLNAResource getChild() {
		return root;
	}
	
	public String toString() {
		return "RemoteServer "+name+"@"+addr+":"+port+" user "+usr;
	}
}
