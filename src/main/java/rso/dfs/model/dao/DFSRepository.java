package rso.dfs.model.dao;

import rso.dfs.model.File;
import rso.dfs.model.Server;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public interface DFSRepository {

	public Server getMasterServer();

	public void saveMaster(Server server);
	
	public File getFileByFileName(String fileName);

	public Server getSlaveByFile(File file);
	
}
