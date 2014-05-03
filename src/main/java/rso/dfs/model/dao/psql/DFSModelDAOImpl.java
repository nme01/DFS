package rso.dfs.model.dao.psql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import rso.dfs.model.File;
import rso.dfs.model.FileOnServer;
import rso.dfs.model.Server;
import rso.dfs.model.ServerRole;
import rso.dfs.model.dao.DFSModelDAO;
import rso.dfs.model.dao.psql.mapper.FileRowMapper;
import rso.dfs.model.dao.psql.mapper.ServerRowMapper;

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
	public int deleteFile(File file) {
		final String query = "delete from files where id = ?";
		return getJdbcTemplate().update(query, new Object[] { file.getId() });

	}

	@Override
	public int deleteServer(Server server) {
		final String query = "delete from servers where ip = ?";
		return getJdbcTemplate().update(query, new Object[] { server.getIp() });

	}

	@Override
	public int deleteFileOnServer(FileOnServer fileOnServer) {
		final String query = "delete from files_on_servers where file_id = ? and server_ip = ?";
		return getJdbcTemplate().update(query, new Object[] { fileOnServer.getFileId(), fileOnServer.getServerIp() });

	}

	@Override
	public File fetchFileById(int fileId) {
		final String query = "select id, name, size, status from files where id=?";
		return getJdbcTemplate().queryForObject(query, new Object[] { fileId }, new FileRowMapper());

	}

	@Override
	public File fetchFileByFileName(String fileName) {
		final String query = "select id, name, size, status from files where name=?";
		return getJdbcTemplate().queryForObject(query, new Object[] { fileName }, new FileRowMapper());

	}

	@Override
	public List<Server> fetchServersByRole(ServerRole role) {
		final String query = "select ip, role, memory, last_connection from servers where role=?";
		return getJdbcTemplate().query(query, new Object[] { role.getRoleChar() }, new ServerRowMapper());
	}

	@Override
	public List<Server> fetchServersByFileId(long fileId) {
		// TODO implement JOIN
		final String query = "select ip, role, memory, last_connection from servers , files_on_servers where files_on_servers.server_ip= servers.ip and files_on_servers.file_id = ?";
		return getJdbcTemplate().query(query, new Object[] { fileId }, new ServerRowMapper());

	}

	@Override
	public long saveFile(final File file) {
		final String query = "insert into files (name, size, status) values(?, ?, ?)";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		getJdbcTemplate().update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(query, new String[] { "id" });
				ps.setString(1, file.getName());
				ps.setLong(2, file.getSize());
				ps.setString(3, file.getStatus().getStatusChar());

				return ps;
			}
		}, keyHolder);
		return keyHolder.getKey().longValue();
	}

	@Override
	public void saveFileOnServer(FileOnServer fileOnServer) {
		final String query = "insert into files_on_servers (file_id, sever_ip) values(?, ?)";
		getJdbcTemplate().update(query, new Object[] { fileOnServer.getFileId(), fileOnServer.getServerIp() });

	}

	@Override
	public void saveServer(Server server) {
		final String query = "insert into servers (ip, role, memory, last_connection) values(?, ?,?,?)";
		getJdbcTemplate().update(query, new Object[] { server.getIp(), server.getRole().getRoleChar(), server.getLastConnection() });

	}

	@Override
	public int updateFile(File file) {
		final String query = "update files set name=?, size=?, status=? where id=?";
		return getJdbcTemplate().update(query, new Object[] { file.getName(), file.getSize(), file.getStatus(), file.getId() });

	}

	@Override
	public int updateServer(Server server) {
		final String query = "update servers set role=?, memory=?, last_connection where ip=?";
		return getJdbcTemplate().update(query, new Object[] { server.getRole().getRoleChar(), server.getMemory(), server.getLastConnection(), server.getIp() });
	}

}
