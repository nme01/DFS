package rso.dfs.server.storage;

/**
 * @author Adam Papros<adam.papros@gmail.com>
 * 
 * */
public interface StorageHandler {

	public byte[] readFile(long fileId, long offset);

	public void writeFile(long fileId, byte[] fileBody);
	
	public void deleteFile(long fileId);
	
	public long getFileSize(long fileId);

	void createFile(long fileId);
	
}
