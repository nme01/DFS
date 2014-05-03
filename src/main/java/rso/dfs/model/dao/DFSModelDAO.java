package rso.dfs.model.dao;

import java.net.InetAddress;
import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;

import rso.dfs.model.FileStatus;
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

	/**
	 * to moze nawet zamiast tej funkcji ponizej
	 * 
	 * @param r
	 * @return
	 */
	public Collection<Pair<InetAddress, Long>> getServersByRoleMemory(ServerRole r);

	public FileStatus getFileStatus(int FileId);

	/**
	 * @param r
	 *            - server role
	 * @return collection of addresses with ip numbers of servers
	 */
	public Collection<InetAddress> getServersByRole(ServerRole r);

	/**
	 * @param fileId
	 * @return
	 */
	public Collection<InetAddress> getServersByFile(int fileId);

	/**
	 * @param fileName
	 * @return
	 */
	public Integer getFileId(String fileName);

	/**
	 * @param name
	 * @param size
	 * @return
	 */
	public int saveNewFile(String name, long size);

	/**
	 * @param fileId
	 * @return
	 */
	public int deleteFile(int fileId);

	/**
	 * @param fileId
	 * @param newStatus
	 * @return
	 */
	public int updateFileStatus(int fileId, FileStatus newStatus);

	/**
	 * @param ip
	 * @param memory
	 * @param role
	 * @return
	 */
	public int saveNewServer(InetAddress ip, int memory, ServerRole role);

	public int saveFileServerLink(InetAddress ip, int id);

	/**
	 * @param ip
	 * @param id
	 * @return
	 */
	public int deleteFileServerLink(InetAddress ip, int id);

}
