package rso.dfs.model.dao.psql;

import java.net.InetAddress;
import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import rso.dfs.model.FileStatus;
import rso.dfs.model.ServerRole;
import rso.dfs.model.dao.DFSModelDAO;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class DFSModelDAOImpl extends JdbcDaoSupport implements DFSModelDAO {

	public DFSModelDAOImpl(DriverManagerDataSource dataSource) {
		super();
		setDataSource(dataSource);
	}

	@Override
	public void testDatabaseConnection() {
		getJdbcTemplate().execute("select * from files");

	}

	@Override
	public Collection<Pair<InetAddress, Long>> getServersByRoleMemory(ServerRole r) {

		return null;
	}

	@Override
	public FileStatus getFileStatus(int FileId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<InetAddress> getServersByRole(ServerRole r) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<InetAddress> getServersByFile(int fileId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getFileId(String fileName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int addNewFile(String name, long size) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int deleteFile(int fileId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateFileStatus(int fileId, FileStatus newStatus) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int addNewServer(InetAddress ip, int memory, ServerRole role) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int addFileServerLink(InetAddress ip, int id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int deleteFileServerLink(InetAddress ip, int id) {
		// TODO Auto-generated method stub
		return 0;
	}

}
