package rso.dfs.server.storage;

/**
 * @author Adam Papros<adam.papros@gmail.com>
 * 
 * */
public interface StorageHandler {

	public byte[] readFile(long fileId);

	public void writeFile(long fileId, byte[] fileBody);
	
	public void deleteFile(long fileId);

	void createFile(long fileId);
	
}
