package rso.dfs.model.dao.psql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import rso.dfs.model.Server;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class ServerRowMapper implements ParameterizedRowMapper<Server> {
	
	@Override
	public Server mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}
