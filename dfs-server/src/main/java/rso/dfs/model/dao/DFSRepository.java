package rso.dfs.model.dao;

import java.util.List;

import rso.dfs.model.File;
import rso.dfs.model.FileOnServer;
import rso.dfs.model.Query;
import rso.dfs.model.Server;

/**
 * Master and Shadows repository
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
	
	public Server getServerByIp(String ip);
	
	public List<Server> getSlavesByFile(File file);

	public List<Server> getSlaves();

	public List<Server> getDownServers();
	
	public List<File> getFilesOnSlave(Server server);
	
	public void saveFileOnServer(final FileOnServer fileOnServer);
	
	public Integer saveFile(final File file);

	public void updateFile(final File file);
	
	public void updateFileOnServer(final FileOnServer fileOnServer);
	
	public void updateServer(final Server server);

	public void cleanDB();

	public List<File> getAllFiles();
	
	public List<Query> getQueriesAfter(long version);

	public List<Query> getAllQueries();

	public List<Server> getShadows();
	
	public void executeQuery(String sql);
	
	public void addShadow(Server shadow);

	public FileOnServer getFileOnServer(Long long1, Integer fileId);

	public Server getServerById(Long serverId);
	
	public DFSModelDAO getDAO();
	
}
