package rso.dfs.model.dao.psql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import rso.dfs.model.FilesOnServer;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class FilesOnServersRowMapper implements ParameterizedRowMapper<FilesOnServer> {

	@Override
	public FilesOnServer mapRow(ResultSet rs, int rowNum) throws SQLException {
		FilesOnServer filesOnServer = new FilesOnServer();
		filesOnServer.setFileId(rs.getLong("file_id"));
		filesOnServer.setServerIp(rs.getString("server_ip"));
		return filesOnServer;
	}
}
