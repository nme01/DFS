package rso.dfs.model;

import java.net.Inet4Address;
import java.security.Timestamp;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class Server {

	private Inet4Address ip;
	private ServerRole role;
	private Timestamp lastConnection;

	public Inet4Address getIp() {
		return ip;
	}

	public void setIp(Inet4Address ip) {
		this.ip = ip;
	}

	public ServerRole getRole() {
		return role;
	}

	public void setRole(ServerRole role) {
		this.role = role;
	}

	public Timestamp getLastConnection() {
		return lastConnection;
	}

	public void setLastConnection(Timestamp lastConnection) {
		this.lastConnection = lastConnection;
	}

}
