package rso.dfs.model.dao;

import java.util.List;

import rso.dfs.model.File;
import rso.dfs.model.FileOnServer;
import rso.dfs.model.Server;
import rso.dfs.model.ServerRole;

/**
 * WARNING: THIS IS DATA ACCESS LAYER
 * REMEMBER TO USE {@link DFSRepository} instead of this.
 * 
 * @author Adam Papros <adam.papros@gmail.com>
 * @author Mateusz Statkiewicz
 * */
public interface DFSModelDAO {

	public void testDatabaseConnection();

	public int deleteFile(File file);

	public int deleteServer(Server server);

	public int deleteFileOnServer(FileOnServer fileOnServer);

	public File fetchFileById(Long fileId);

	public File fetchFileByFileName(String fileName);

	public Server fetchServerByIp(String ip);
	
	public List<Server> fetchServersByRole(ServerRole role);
	
	public List<File> fetchFilesOnServer(Server server);
	
	public List<Server> fetchServersByFileId(long fileId);

	public Long saveFile(File file);

	public void saveFileOnServer(FileOnServer fileOnServer);

	public Long saveServer(Server server);

	public int updateFile(File file);

	public int updateServer(Server server);

}
