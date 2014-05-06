package rso.dfs.model;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class FileOnServer {

	private long fileId;

	private Long serverId;

	private Long priority;
	
	public FileOnServer() {}
	
	public FileOnServer(long fid, long slid, long prio) {
		fileId = fid;
		serverId = slid;
		priority = prio;
	}

	public long getFileId() {
		return fileId;
	}

	public void setFileId(long fileId) {
		this.fileId = fileId;
	}

	public Long getServerId() {
		return serverId;
	}

	public void setServerId(Long serverId) {
		this.serverId = serverId;
	}

	public Long getPriority() {
		return priority;
	}

	public void setPriority(Long priority) {
		this.priority = priority;
	}

}
