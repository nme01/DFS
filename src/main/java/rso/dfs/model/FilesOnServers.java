package rso.dfs.model;

import java.net.Inet4Address;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class FilesOnServers {

	private long fileId;
	private Inet4Address serverIp;

	public long getFileId() {
		return fileId;
	}

	public void setFileId(long fileId) {
		this.fileId = fileId;
	}

	public Inet4Address getServerIp() {
		return serverIp;
	}

	public void setServerIp(Inet4Address serverIp) {
		this.serverIp = serverIp;
	}

}
