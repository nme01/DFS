package rso.dfs.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class IpConverter {

	/**
	 * 
	 * */
	public static int getIntegerIpformString(String dottedString) throws UnknownHostException {
		return pack(InetAddress.getByName(dottedString).getAddress());
	}

	private static int pack(byte[] bytes) {
		int val = 0;
		for (int i = 0; i < bytes.length; i++) {
			val <<= 8;
			val |= bytes[i] & 0xff;
		}
		return val;
	}

	/**
	 * 
	 * */
	public static String getStringIpFromInteger(Integer integer) throws UnknownHostException {
		return InetAddress.getByAddress(unpack(integer)).getHostAddress();
	}

	private static byte[] unpack(int bytes) {
		return new byte[] { (byte) ((bytes >>> 24) & 0xff), (byte) ((bytes >>> 16) & 0xff), (byte) ((bytes >>> 8) & 0xff), (byte) ((bytes) & 0xff) };
	}
}
