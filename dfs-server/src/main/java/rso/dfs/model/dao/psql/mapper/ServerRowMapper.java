package rso.dfs.model.dao.psql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.DateTime;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import rso.dfs.model.Server;
import rso.dfs.model.ServerRole;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class ServerRowMapper implements ParameterizedRowMapper<Server> {

	@Override
	public Server mapRow(ResultSet rs, int rowNum) throws SQLException {
		Server server = new Server();
		server.setId(rs.getLong("id"));
		server.setIp(rs.getString("ip"));
		server.setMemory(rs.getLong("memory"));
		server.setLastConnection(new DateTime(rs.getTimestamp("last_connection").getTime()));
		server.setRole(ServerRole.getServerRole(rs.getString("role")));
		server.setFreeMemory(rs.getLong("freeMemory"));
		server.setFilesNumber(rs.getInt("filesNumber"));

		return server;
	}
}
