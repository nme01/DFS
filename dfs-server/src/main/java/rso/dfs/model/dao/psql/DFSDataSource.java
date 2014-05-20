package rso.dfs.model.dao.psql;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import rso.dfs.commons.DFSProperties;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class DFSDataSource extends DriverManagerDataSource {
	
	private String hostAddress;

	public static final String driverClassName = "org.postgresql.Driver";
	public static final String url = "jdbc:postgresql://127.0.0.1:";
	public static final String urlTemplate = "jdbc:postgresql://%s:5432/rsodfs";
	
	

	public DFSDataSource(final String hostAddress) {
		// super(driverClassName, url, userName, password);
		super();
		this.hostAddress = hostAddress;
		setDriverClassName(driverClassName);
		setUrl(url + DFSProperties.getProperties().getDbport() + "/" + DFSProperties.getProperties().getDbname());
		setUsername(DFSProperties.getProperties().getDbuser());
		setPassword(DFSProperties.getProperties().getDbpassword());
	}
}
