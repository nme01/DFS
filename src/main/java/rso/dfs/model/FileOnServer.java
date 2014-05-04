package rso.dfs.model;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class FileOnServer {

	private long fileId;

	private String serverIp;

	private Long priority;

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

	public Long getPriority() {
		return priority;
	}

	public void setPriority(Long priority) {
		this.priority = priority;
	}

}
