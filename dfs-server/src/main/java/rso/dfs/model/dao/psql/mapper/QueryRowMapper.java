package rso.dfs.model.dao.psql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import rso.dfs.model.Query;

public class QueryRowMapper  implements ParameterizedRowMapper<Query> {

	@Override
	public Query mapRow(ResultSet rs, int rowNum) throws SQLException {
		Query query = new Query();
		query.setId(rs.getLong("version"));
		query.setSql(rs.getString("sql"));
		return query;
	}
}
