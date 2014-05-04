package rso.dfs.model.dao.psql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import rso.dfs.model.FileOnServer;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class FilesOnServersRowMapper implements ParameterizedRowMapper<FileOnServer> {

	@Override
	public FileOnServer mapRow(ResultSet rs, int rowNum) throws SQLException {
		FileOnServer fileOnServer = new FileOnServer();
		fileOnServer.setFileId(rs.getLong("file_id"));
		fileOnServer.setServerIp(rs.getString("server_ip"));
		fileOnServer.setPriority(rs.getLong("priority"));
		return fileOnServer;
	}
}
