package rso.dfs.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressUtils {

	public static InetAddress getInetAddress() throws UnknownHostException {
		// TODO: fix this
		return InetAddress.getByName("localhost");
	}

	public static String getInetAddressAsString() throws UnknownHostException {
		// TODO: fix this
		return InetAddress.getByName("localhost").getHostAddress();
	}

}
