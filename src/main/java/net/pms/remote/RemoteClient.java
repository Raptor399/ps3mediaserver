package net.pms.remote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.configuration.ConversionException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.RootFolder;
import net.pms.network.RequestHandler;
import net.pms.network.UPNPHelper;

public class RemoteClient extends Thread {
	final public static int DEFAULT_PORT=7890;
	private final static String ALIVE = "ssdp:alive";
	
	// Timer values
	private final static int DELAY=17000;
	private final static int PERIOD=18000;
	private final static int RECHALLENGE_CNT=9; // every 3rd minute
	
	private final static int NORMAL=0;
	private final static int WEB=1;
	
	private static final Logger logger = LoggerFactory.getLogger(RemoteClient.class);
	
	private ServerSocket sock;
	private ArrayList<ClientData> clients;
	private boolean doAuth;
	private int port;
	
	private HashMap<String,String> users;
	private HashMap<String,String> groups;
	private Random rand;
	private MessageDigest md5;
	private Timer timer;
	
	
	public RemoteClient() {
		this(DEFAULT_PORT);
	}
	
	public RemoteClient(int port) {
		try {
			if(port==0)
				port=DEFAULT_PORT;
			logger.info("Starting remote client on port "+port);
			sock=new ServerSocket(port);
			clients=new ArrayList<ClientData>();
			doAuth=true;
			this.port=port;
			users=new HashMap<String,String>();
			groups=new HashMap<String,String>();
			rand=new Random(System.currentTimeMillis());
			md5 = MessageDigest.getInstance("MD5");
			//doAuth=Boolean.getBoolean((String) PMS.getConfiguration().getCustomProperty("AUTH"));
			readAuth();
			timer=new Timer();
			timer.schedule(new TimerTask() {
				public void run() {
					try {
						resendSSDP();
					} catch (IOException e) {
					}
				}
			}, DELAY,PERIOD);
		} catch (Exception e) {
			logger.error("remote client start error "+e);
		}
	}
	
	private void readAuth() {
		BufferedReader in;
		try {
			File f=new File((String) PMS.getConfiguration().getCustomProperty("cred.path"));
			in = new BufferedReader(new FileReader(f));
			String str;
			while ((str = in.readLine()) != null) {
				str=str.trim();
				if(RemoteCommon.ignoreLine(str))
					continue;
				String[] s=str.split("\\s*=\\s*",2);
				if(s.length<2)
					continue;
				String[] s1=s[0].split("\\.");
				if(s1.length<2)
					continue;
				if(!s1[0].equalsIgnoreCase("client"))
					continue;
				String[] s2=s[1].split(",");
				if(s2.length<2)
					continue;
				users.put(s2[0], s2[1]);
				groups.put(s2[0], s1[1]);
			}
		}
    	catch (Exception e) {} 
    }
	
	
	private void sendMsg(ClientData cd,String nt,String msg) throws IOException {
		String ssdpMsg=UPNPHelper.ssdpMsg(nt,msg);
		Socket s=cd.authSocket;
		String repStr="http://" + PMS.get().getServer().getHost() + ":" + PMS.get().getServer().getPort();
		ssdpMsg=ssdpMsg.replaceAll(repStr, "http://"+cd.myPubAddr);
		byte[] i=RemoteCommon.intToByteArray(RemoteCommon.SSDP_OP);
		byte[] len=RemoteCommon.intToByteArray(ssdpMsg.length());
		s.getOutputStream().write(i);
		s.getOutputStream().write(len);
		s.getOutputStream().write(ssdpMsg.getBytes());
	}
	
	private final static String chars="1234567890qwertyuiopasdfghjklzxcvbn()*+!#%&/";
	
	private byte[] challenge(Socket s) throws IOException {
		int saltLen=rand.nextInt(10)+4;
		byte[] op=RemoteCommon.intToByteArray(RemoteCommon.CHAL_OP);
		byte[] salt=new byte[saltLen];
		byte[] sLen=RemoteCommon.intToByteArray(saltLen);
		for(int i=0;i<saltLen;i++)
			salt[i]=(byte)chars.charAt(rand.nextInt(chars.length()));
		logger.trace("send salt "+new String(salt)+" len "+saltLen);
		s.getOutputStream().write(op);
		s.getOutputStream().write(sLen);
		s.getOutputStream().write(salt);
		return salt;
	}
	
	private ClientData doAuth(Socket s,ClientData cd) throws IOException {
		byte[] op=new byte[4];
		byte[] salt=challenge(s); 
		s.getInputStream().read(op);
		if(RemoteCommon.byteArrayToInt(op,0)!=RemoteCommon.AUTH_OP)
			return null;
		byte[] size=new byte[4];
		s.getInputStream().read(size);
		int len=RemoteCommon.byteArrayToInt(size,0);
		byte[] buf=new byte[len];
		s.getInputStream().read(buf);
		String[] str=new String(buf).split(",");
		if(str.length<3)
			return null;
		logger.trace("user name "+str[1]+" pwd "+str[2]+"!");
		String pwd=users.get(str[1]);
		if(pwd==null)
			return null;
		String spwd=pwd+new String(salt);
		byte[] saltedpwd=md5.digest(spwd.getBytes());
		String digest=RemoteCommon.toHex(saltedpwd);
		if(!str[2].equals(digest))
			return null;
		if(cd!=null) 
			return cd;
		cd=new ClientData();
		cd.authSocket=s;
		cd.myPubAddr=str[0];
		cd.user=str[1];
		cd.pwd=str[2];
		cd.lastSSDP=System.currentTimeMillis();
		cd.first=true;
		cd.rechallenge=RECHALLENGE_CNT;
		cd.speed=0.0;
		cd.root=null;
		cd.type=NORMAL;
		if(str.length>3) 
			if(str[3].equalsIgnoreCase("true")) // no speed check
				return cd;
		cd.speed=sendSpeed(s);
		return cd;
	}
	
	private ClientData authenticate(Socket s) throws IOException {
		/*if(!doAuth)
			return true;*/
		InetSocketAddress sa=(InetSocketAddress)s.getRemoteSocketAddress();
		String addr=sa.getAddress().getHostAddress();
		ClientData cd=findClientAddr(addr);
		logger.trace("addr is "+addr+" cd "+cd);
		byte[] op=new byte[4];
		if(cd!=null) { // already authenticated
			// "ping" the client
			byte[] i=RemoteCommon.intToByteArray(RemoteCommon.PING_OP);
			try {
				cd.authSocket.getOutputStream().write(i);
				cd.authSocket.getInputStream().read(op);
				if(RemoteCommon.byteArrayToInt(op,0)!=RemoteCommon.PONG_OP) 
					throw new Exception("Bad ping reply");
			}
			catch (Exception e) {
				logger.trace("auth is gone "+e);
				cleanClient(cd);
				return null;
			}
			cd.first=false;
			cd.socket=s;
			return cd;
		}
		// No Client request authentication
		cd=doAuth(s,null);
		if(cd==null) {
			logger.trace("auth failed");
			return null;
		}
		cd.addr=addr;
		clients.add(cd);
		sendInit(cd);
		return cd;	
	}
	
	private double sendSpeed(Socket s) throws IOException {
		byte[] buf=new byte[10000];
		byte[] op=RemoteCommon.intToByteArray(RemoteCommon.SPEED_OP);
		s.getOutputStream().write(op);
		for(int i=0;i<10;i++)
			s.getOutputStream().write(buf);
		s.getInputStream().read(op);
		return (8*10*buf.length*1.0)/(RemoteCommon.byteArrayToInt(op, 0)*1000*1.0);
		
	}
	
	private void sendInit(ClientData cd) throws IOException {
		sendMsg(cd,"upnp:rootdevice", ALIVE);
		sendMsg(cd, PMS.get().usn(), ALIVE);
		sendMsg(cd, "urn:schemas-upnp-org:device:MediaServer:1", ALIVE);
		sendMsg(cd, "urn:schemas-upnp-org:service:ContentDirectory:1", ALIVE);
		sendMsg(cd, "urn:schemas-upnp-org:service:ConnectionManager:1", ALIVE);
	}
	
	public void run() {
		for(;;) {
			try {
				Socket clientSocket = sock.accept();
				ClientData cd=authenticate(clientSocket);
				if(cd==null) {
					logger.trace("close sock");
					clientSocket.close();
					continue;
				}	
				if(cd.first)
					continue;
				RequestHandler request = new RequestHandler(clientSocket);
				Thread thread = new Thread(request);
				thread.start();
			}
			catch (Exception e) {
				logger.trace("oops "+e);
				return;
			}
		}
	}
	
	private ClientData findClientAddr(String addr) {
		for(int i=0;i<clients.size();i++) {
			ClientData cd=clients.get(i);
			if(addr.equals(cd.addr))
				if(cd.authSocket.isConnected())
					return cd;
		}
		return null;
	}
	
	private void cleanClient(ClientData cd) {
		clients.remove(cd);
	}
	
	private ClientData findClient(Socket s) {
		if(clients==null)
			return null;
		for(int i=0;i<clients.size();i++) {
			ClientData cd=clients.get(i);
			if(s==cd.socket)
				return cd;
		}
		return null;
	}

	private void resendSSDP() throws IOException {
		long now=System.currentTimeMillis();
		for(int i=clients.size()-1;i>0;i--) {
			ClientData cd=clients.get(i);
			if((--cd.rechallenge)==0) {
				if(doAuth(cd.authSocket,cd)==null) {
					clients.remove(i);
					continue;
				}
				cd.rechallenge=RECHALLENGE_CNT;
			}
			if((now-cd.lastSSDP)>PERIOD)
				sendInit(cd);
		}			
	}
	
	public String reMap(Socket s,String msg) {
		ClientData cd=findClient(s);
		if(cd==null)
			return msg;
		String repStr="http://" + PMS.get().getServer().getHost() + ":" + PMS.get().getServer().getPort();
		return msg.replaceAll(repStr, "http://"+cd.myPubAddr);
	}
	
	public RootFolder getRoot(Socket s) {
		ClientData cd=findClient(s);
		if(cd==null)
			return null;
		if(cd.root==null) {
			String group=groups.get(cd.user);
			if(group==null||group.length()==0) // no group found say username is group
				group=cd.user;
			cd.root=new RootFolder(group);
		}
		return cd.root;
	}
	
	public void setBitRate(RendererConfiguration r,Socket s) {
		ClientData cd=findClient(s);
		if(cd==null)
			return;
		int br=(int)(cd.speed*1024);
		r.setMaxVideoBitrate(String.valueOf(br));
	}
	
	public void addWebClient(String usr,String pwd,String salt,InetSocketAddress addr) {
		
	}
	
	private class ClientData {
		public String name;
		public Socket socket;
		public Socket authSocket;
		public String user;
		public String pwd;
		public String myPubAddr;
		public String addr;
		public long lastSSDP;
		public boolean first;
		public int rechallenge;
		public double speed;
		public RootFolder root;
		public int type;
	}

}
