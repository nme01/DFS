package rso.dfs.model.dao.psql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import rso.dfs.model.File;
import rso.dfs.model.FileOnServer;
import rso.dfs.model.Query;
import rso.dfs.model.Server;
import rso.dfs.model.ServerRole;
import rso.dfs.model.dao.DFSModelDAO;
import rso.dfs.model.dao.DFSRepository;
import rso.dfs.model.dao.psql.mapper.FileRowMapper;
import rso.dfs.model.dao.psql.mapper.FilesOnServersRowMapper;
import rso.dfs.model.dao.psql.mapper.QueryRowMapper;
import rso.dfs.model.dao.psql.mapper.ServerRowMapper;

/**
 * WARNING: THIS IS DATA ACCESS LAYER REMEMBER TO USE {@link DFSRepository}
 * instead of this.
 * 
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class DFSModelDAOImpl extends JdbcDaoSupport implements DFSModelDAO {

	final static Logger log = LoggerFactory.getLogger(DFSModelDAOImpl.class);

	public void insertIntoLogTable(String sqlQuery) {
		final String query = "insert into log (sql) values(?)";
		getJdbcTemplate().update(query, new Object[] { sqlQuery });
	}

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
		int result = getJdbcTemplate().update(query, new Object[] { file.getId() });
		insertIntoLogTable("delete from files where id =" + file.getId());
		return result;

	}

	@Override
	public int deleteServer(Server server) {
		final String query = "delete from servers where id = ?";
		int result = getJdbcTemplate().update(query, new Object[] { server.getId() });
		insertIntoLogTable("delete from servers where id = " + server.getId());
		return result;
	}

	@Override
	public int deleteFileOnServer(FileOnServer fileOnServer) {
		final String query = "delete from files_on_servers where file_id = ? and server_id = ?";
		int result = getJdbcTemplate().update(query, new Object[] { fileOnServer.getFileId(), fileOnServer.getServerId() });
		insertIntoLogTable("delete from files_on_servers where file_id = " + fileOnServer.getFileId() + " and server_id = " + fileOnServer.getServerId());
		return result;
	}

	@Override
	public File fetchFileById(Integer fileId) {
		final String query = "select id, name, size, status from files where id=?";
		return getJdbcTemplate().queryForObject(query, new Object[] { fileId }, new FileRowMapper());

	}

	@Override
	public File fetchFileByFileName(String fileName) {
		final String query = "select id, name, size, status from files where name=?";
		File result = null;
		try {
			result = getJdbcTemplate().queryForObject(query, new Object[] { fileName }, new FileRowMapper());
		} catch (EmptyResultDataAccessException e) {
			// null file
			logger.info("File " + fileName + " not found on server.");
		}
		return result;
	}

	@Override
	public Server fetchServerByIp(String ip) {
		final String query = "select id, ip, role, memory, last_connection, filesNumber, freeMemory from servers_vw where ip=?";
		return getJdbcTemplate().queryForObject(query, new Object[] { ip }, new ServerRowMapper());
	}

	@Override
	public List<Server> fetchServersByRole(ServerRole role) {
		final String query = "select id, ip, role, memory, last_connection, filesNumber, freeMemory from servers_vw where role=?  ORDER BY ip";
		return getJdbcTemplate().query(query, new Object[] { role.getCode() }, new ServerRowMapper());
	}

	@Override
	public List<Server> fetchServersByFileId(Integer fileId) {
		final String query = "select id, ip, role, memory, last_connection, filesNumber, freeMemory from servers_vw , files_on_servers where files_on_servers.server_id=servers_vw.id and files_on_servers.file_id = ? order by files_on_servers.priority";
		return getJdbcTemplate().query(query, new Object[] { fileId }, new ServerRowMapper());

	}
	
	@Override
	public Server fetchServerById(Long serverId) {
		final String query = "select id, ip, role, memory, last_connection, filesNumber, freeMemory from servers_vw where servers_vw.id = ?";
		return getJdbcTemplate().queryForObject(query, new Object[] { serverId }, new ServerRowMapper());

	}
	
	@Override
	public List<File> fetchFilesOnServer(Server server) {
		final String query = "select id, name, size, status from files join files_on_servers on id=files_on_servers.file_id where files_on_servers.server_id=?";
		return getJdbcTemplate().query(query, new Object[] { server.getId() }, new FileRowMapper());
	}

	@Override
	public Integer saveFile(final File file) {
		final String query = "insert into files (name, size, status) values(?, ?, ?)";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		getJdbcTemplate().update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(query, new String[] { "id" });
				ps.setString(1, file.getName());
				ps.setLong(2, file.getSize());
				ps.setString(3, file.getStatus().getCode());

				return ps;
			}
		}, keyHolder);
		insertIntoLogTable("insert into files (name, size, status) values('" + file.getName() + "'," + file.getSize() + ",'" + file.getStatus().getCode() + "')");
		return keyHolder.getKey().intValue();
	}

	@Override
	public void saveFileOnServer(FileOnServer fileOnServer) {
		final String query = "insert into files_on_servers (file_id, server_id, priority) values(?, ?, ?)";
		getJdbcTemplate().update(query, new Object[] { fileOnServer.getFileId(), fileOnServer.getServerId(), fileOnServer.getPriority() });
		insertIntoLogTable("insert into files_on_servers (file_id, server_id, priority) values(" + fileOnServer.getFileId() + "," + fileOnServer.getServerId() + "," + fileOnServer.getPriority() + ")");
	}

	@Override
	public Long saveServer(final Server server) {
		final String query = "insert into servers (ip, role, memory, last_connection) values(?,?,?,?)";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		getJdbcTemplate().update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(query, new String[] { "id" });
				ps.setString(1, server.getIp());
				ps.setString(2, server.getRole().getCode());
				ps.setLong(3, server.getMemory());
				ps.setTimestamp(4, new Timestamp(server.getLastConnection().getMillis()));

				return ps;
			}
		}, keyHolder);
		log.debug("Server saved in DAO. Key value is: " + keyHolder.getKey().longValue());
		insertIntoLogTable("insert into servers (ip, role, memory, last_connection) values('" + server.getIp() + "','" + server.getRole().getCode() + "'," + server.getMemory() + ",'" + new Timestamp(server.getLastConnection().getMillis()) + "')");
		return keyHolder.getKey().longValue();
	}

	@Override
	public int updateFile(File file) {
		final String query = "update files set name=?, size=?, status=? where id=?";
		insertIntoLogTable("update files set name='" + file.getName() + "', size=" + file.getSize() + ", status='" + file.getStatus().getCode() + "' where id=" + file.getId());
		return getJdbcTemplate().update(query, new Object[] { file.getName(), file.getSize(), file.getStatus().getCode(), file.getId() });

	}

	@Override
	public int updateServer(Server server) {
		final String query = "update servers set role=?, memory=?, last_connection=? where ip=?";
		insertIntoLogTable("update servers set role='" + server.getRole().getCode() + "', memory=" + server.getMemory() + ", last_connection='" + new Timestamp(server.getLastConnection().getMillis()) + "' where ip='" + server.getIp() + "'");
		return getJdbcTemplate().update(query, new Object[] { server.getRole().getCode(), server.getMemory(), new Timestamp(server.getLastConnection().getMillis()), server.getIp() });
	}

	@Override
	public void cleanDB() {
		final String query3 = "TRUNCATE files_on_servers CASCADE";
		getJdbcTemplate().update(query3);

		final String query = "TRUNCATE files CASCADE";
		getJdbcTemplate().update(query);

		final String query2 = "TRUNCATE servers CASCADE";
		getJdbcTemplate().update(query2);

	}

	@Override
	public List<File> fetchAllFiles() {
		final String query = "select id, name, size, status from files";
		return getJdbcTemplate().query(query, new FileRowMapper());
	}

	@Override
	public List<Query> fetchAllQueries() {
		final String query = "select version, sql from log order by version";
		return getJdbcTemplate().query(query, new QueryRowMapper());
	}

	@Override
	public List<Query> fetchQueriesAfter(long version) {
		final String query = "select version, sql from log where version > ? order by version";
		return getJdbcTemplate().query(query, new Object[] { version }, new QueryRowMapper());
	}

	@Override
	public void saveFileWithId(File file) {
		if (file.getId() == null) {
			throw new IllegalArgumentException("fileId should be set.");
		}
		final String query = "insert into files (id, name, size, status) values(?, ?, ?, ?)";
		getJdbcTemplate().update(query, new Object[] { file.getId(), file.getName(), file.getSize(), file.getStatus().getCode() });
		insertIntoLogTable("insert into files (id, name, size, status) values("+file.getId()+",'"+file.getName()+"',"+file.getSize()+",'"+file.getStatus().getCode()+"')");
	}

	@Override
	public void saveServerWithId(Server server) {
		if (server.getId() == null) {
			throw new IllegalArgumentException("serverId should be set.");
		}
		final String query = "insert into servers (id, ip, role, memory, last_connection) values(?,?,?,?,?)";
		getJdbcTemplate().update(query, new Object[] { server.getId(), server.getIp(), server.getRole().getCode(), server.getMemory(), new Timestamp(server.getLastConnection().getMillis()) });
		insertIntoLogTable("insert into servers (id, ip, role, memory, last_connection) values("+server.getId()+",'"+server.getIp()+"','"+server.getRole().getCode()+"',"+server.getMemory()+","+new Timestamp(server.getLastConnection().getMillis())+")");
	}
	
	@Override
	public void executeQuery(String sql) {
		getJdbcTemplate().update(sql);
		insertIntoLogTable(sql);
	}

	@Override
	public int updateFileOnServer(FileOnServer fileOnServer) {
		final String query = "update files_on_servers set priority= ? where file_id= ? and server_id= ?";
		int arows = getJdbcTemplate().update(query, new Object[] { fileOnServer.getPriority(), fileOnServer.getFileId(), fileOnServer.getServerId() });
		insertIntoLogTable("update files_on_servers set priority="+fileOnServer.getPriority()+" where file_id="+fileOnServer.getFileId()+ " and server_id=" + fileOnServer.getServerId());
		return arows;
	}
	
	@Override
	public FileOnServer fetchFos(Long serverid, Integer fileId) {
		final String query = "select file_id, server_id, priority from files_on_servers where file_id= ? and server_id= ?";
		FileOnServer result = null;
		try {
			result = getJdbcTemplate().queryForObject(query, new Object[] { fileId,serverid }, new FilesOnServersRowMapper());
		} catch (EmptyResultDataAccessException e) {
			// null file
			logger.info("FileOnServer (fid=" + fileId + ",sid="+serverid+") not found on server.");
		}
		return result;
	}

	@Override
	public Integer fetchSeqVals(String seq_name) {
		final String query = "select curval(?);";
		return getJdbcTemplate().queryForInt(query, new Object[] { seq_name});
	}

	@Override
	public void setSeqVals(String seq_name, Integer value) {
			final String query = "select setval(?,?);";
			getJdbcTemplate().update(query, new Object[] { seq_name, value });
	}

}
