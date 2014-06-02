package rso.dfs.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressUtils {

	public static InetAddress getInetAddress() throws UnknownHostException {
		// FIXME: fix this
		throw new IllegalStateException("Nie, nie, nie. Adresem nie jest localhost");
		//return InetAddress.getByName("localhost");
	}

	public static String getInetAddressAsString() throws UnknownHostException {
		// FIXME: fix this
		throw new IllegalStateException("Nie, nie, nie. Adresem nie jest localhost ");
		//return InetAddress.getByName("localhost").getHostAddress();
	}

}
