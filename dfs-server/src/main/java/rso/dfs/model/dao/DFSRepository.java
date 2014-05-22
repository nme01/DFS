package rso.dfs.model.dao;

import java.util.List;

import rso.dfs.model.File;
import rso.dfs.model.FileOnServer;
import rso.dfs.model.Server;

/**
 * Repository
 * 
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public interface DFSRepository {

	public void deleteFile(final File file);

	public void deleteFileOnServer(final FileOnServer fileOnServer);
	
	public Server getMasterServer();

	public void saveServer(Server server);
	
	public File getFileByFileName(String fileName);
	
	public File getFileById(Integer fileId);

	public Server getSlaveByFile(File file);
	
	public List<Server> getSlavesByFile(File file);

	public List<Server> getSlaves();
	
	public List<File> getFilesOnSlave(Server server);
	
	public void saveFileOnServer(final FileOnServer fileOnServer);
	
	public Integer saveFile(final File file);

	public void updateFile(final File file);

	public void cleanDB();

	public List<File> getAllFiles();
	
}