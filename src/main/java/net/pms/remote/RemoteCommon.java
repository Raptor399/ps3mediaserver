package net.pms.remote;

import java.math.BigInteger;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

public class RemoteCommon {
	
	// Opcodes
	public final static int CHAL_OP=0;
	public final static int SSDP_OP=1;
	public final static int PING_OP=2;
	public final static int AUTH_OP=3;
	public final static int PONG_OP=4;
	public final static int SPEED_OP=5;
	
	public static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
	
	public static byte[] intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }
	
	public static String toHex(byte[] bytes) {
	    BigInteger bi = new BigInteger(1, bytes);
	    return String.format("%0" + (bytes.length << 1) + "x", bi);
	}
	
	public static boolean ignoreLine(String str) {
		return (str.charAt(0)=='#')||(StringUtils.isEmpty(str));
	}
	
	private final static String chars="1234567890qwertyuiopasdfghjklzxcvbn()*+!#%&/";
	
	public static byte[] getSalt(Random rand) {
		int saltLen=rand.nextInt(10)+4;
		byte[] salt=new byte[saltLen];
		for(int i=0;i<saltLen;i++)
			salt[i]=(byte)chars.charAt(rand.nextInt(chars.length()));
		return salt;
	}
}
