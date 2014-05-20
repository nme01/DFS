package rso.dfs.model.dao.psql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import rso.dfs.model.File;
import rso.dfs.model.FileStatus;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class FileRowMapper implements ParameterizedRowMapper<File> {

	@Override
	public File mapRow(ResultSet resultSet, int rowNum) throws SQLException {
		File file = new File();
		file.setId(((Long)resultSet.getLong("id")).intValue());
		file.setName(resultSet.getString("name"));
		file.setSize(resultSet.getLong("size"));
		file.setStatus(FileStatus.getFileStatus(resultSet.getString("status")));

		return file;
	}
}
