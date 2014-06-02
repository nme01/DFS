package rso.dfs.model.dao.psql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import rso.dfs.commons.DFSProperties;
import rso.dfs.server.ServerHandler;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class DFSDataSource extends DriverManagerDataSource {

	private String hostAddress;

	private String url;

	public static final String driverClassName = "org.postgresql.Driver";
	public static final String urlTemplate = "jdbc:postgresql://%s:%d/%s";


	final static Logger log = LoggerFactory.getLogger(DriverManagerDataSource.class);
	
	public DFSDataSource(final String hostAddress) {
		super();
		log.info("Starting DFSDataSource with IP: " + hostAddress);
		this.hostAddress = hostAddress;
		this.url = String.format(urlTemplate, hostAddress, DFSProperties.getProperties().getDbport(), DFSProperties.getProperties().getDbname());
		setDriverClassName(driverClassName);
		setUrl(url);
		setUsername(DFSProperties.getProperties().getDbuser());
		setPassword(DFSProperties.getProperties().getDbpassword());
	}
}
