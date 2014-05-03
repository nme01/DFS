package rso.dfs.model.dao.psql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import rso.dfs.model.FilesOnServers;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class FilesOnServersRowMapper implements ParameterizedRowMapper<FilesOnServers> {

	@Override
	public FilesOnServers mapRow(ResultSet rs, int rowNum) throws SQLException {
		FilesOnServers filesOnServers = new FilesOnServers();
		filesOnServers.setFileId(rs.getLong("file_id"));
		filesOnServers.setServerIp(rs.getString("server_ip"));
		return filesOnServers;
	}
}
