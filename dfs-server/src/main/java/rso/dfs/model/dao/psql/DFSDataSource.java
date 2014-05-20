package rso.dfs.model.dao.psql;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import rso.dfs.commons.DFSProperties;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class DFSDataSource extends DriverManagerDataSource {

	public static final String driverClassName = "org.postgresql.Driver";
	public static final String url = "jdbc:postgresql://127.0.0.1:";

	public DFSDataSource() {
		// super(driverClassName, url, userName, password);
		super();
		setDriverClassName(driverClassName);
		setUrl(url + DFSProperties.getProperties().getDbport() + "/" + DFSProperties.getProperties().getDbname());
		setUsername(DFSProperties.getProperties().getDbuser());
		setPassword(DFSProperties.getProperties().getDbpassword());
	}
}
