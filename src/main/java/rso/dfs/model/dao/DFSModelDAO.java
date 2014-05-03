package rso.dfs.model.dao;

import java.util.Collection;
import java.util.List;

import rso.dfs.model.File;
import rso.dfs.model.FileOnServer;
import rso.dfs.model.Server;
import rso.dfs.model.ServerRole;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * @author Mateusz Statkiewicz
 * */
public interface DFSModelDAO {

	/**
	 * 
	 * */
	public void testDatabaseConnection();

	public int deleteFile(File file);

	public int deleteServer(Server server);

	public int deleteFileOnServer(FileOnServer fileOnServer);

	public File fetchFileById(int FileId);

	public File fetchFileByFileName(String fileName);

	public List<Server> fetchServersByRole(ServerRole role);

	public List<Server> fetchServersByFileId(long fileId);

	public long saveFile(File file);

	public void saveFileOnServer(FileOnServer fileOnServer);

	public void saveServer(Server server);

	public int updateFile(File file);

	public int updateServer(Server server);

}
