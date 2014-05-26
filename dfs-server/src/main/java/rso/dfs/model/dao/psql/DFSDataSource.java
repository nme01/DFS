package rso.dfs.model.dao.psql;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import rso.dfs.commons.DFSProperties;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class DFSDataSource extends DriverManagerDataSource {

	private String hostAddress;

	private String url;

	public static final String driverClassName = "org.postgresql.Driver";
	public static final String urlTemplate = "jdbc:postgresql://%s:%d/%s";

	public DFSDataSource(final String hostAddress) {
		super();
		this.hostAddress = hostAddress;
		this.url = String.format(urlTemplate, hostAddress, DFSProperties.getProperties().getDbport(), DFSProperties.getProperties().getDbname());
		setDriverClassName(driverClassName);
		setUrl(url);
		setUsername(DFSProperties.getProperties().getDbuser());
		setPassword(DFSProperties.getProperties().getDbpassword());
	}
}
