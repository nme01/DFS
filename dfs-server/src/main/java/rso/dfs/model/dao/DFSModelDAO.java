package rso.dfs.model.dao;

import java.util.List;

import rso.dfs.model.File;
import rso.dfs.model.FileOnServer;
import rso.dfs.model.Query;
import rso.dfs.model.Server;
import rso.dfs.model.ServerRole;

/**
 * WARNING: THIS IS DATA ACCESS LAYER REMEMBER TO USE {@link DFSRepository}
 * instead of this.
 * 
 * @author Adam Papros <adam.papros@gmail.com>
 * @author Mateusz Statkiewicz
 * */
public interface DFSModelDAO {

	public void testDatabaseConnection();

	public int deleteFile(File file);

	public int deleteServer(Server server);

	public int deleteFileOnServer(FileOnServer fileOnServer);

	public File fetchFileById(Integer fileId);

	public File fetchFileByFileName(String fileName);

	public Server fetchServerByIp(String ip);

	public List<Server> fetchServersByRole(ServerRole role);

	public List<File> fetchFilesOnServer(Server server);

	public List<Server> fetchServersByFileId(Integer fileId);

	public Integer fetchSeqVals(String seq_name);
	
	public void setSeqVals(String seq_name, Integer value);

	
	/**
	 * Saves file.
	 * 
	 * @return generated id
	 * */
	public Integer saveFile(File file);

	/**
	 * Saves file. WARNING: fileId should be set.
	 * */
	public void saveFileWithId(File file);

	/**
	 * Saves fileOnServer.
	 * */
	public void saveFileOnServer(FileOnServer fileOnServer);

	/**
	 * Saves server.
	 * 
	 * @return generated id
	 * */
	public Long saveServer(Server server);

	/**
	 * Saves server. WARNING: serverId should be set.
	 * */
	public void saveServerWithId(Server server);

	public int updateFile(File file);

	public int updateFileOnServer(FileOnServer fileOnServer);

	public int updateServer(Server server);

	public void cleanDB();

	public List<File> fetchAllFiles();

	public List<Query> fetchQueriesAfter(long version);

	public List<Query> fetchAllQueries();
	
	public void executeQuery(String sql);

	public FileOnServer fetchFos(Long serverId, Integer fileId);

	public Server fetchServerById(Long serverId);


}
