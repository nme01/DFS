package rso.dfs.model;


/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class FilesOnServers {

	private long fileId;
	
	private String serverIp;

	public long getFileId() {
		return fileId;
	}

	public void setFileId(long fileId) {
		this.fileId = fileId;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

}
