package rso.dfs.model.dao.psql;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class DFSDataSource extends DriverManagerDataSource {
	
	private String hostAddress;

	public static final String driverClassName = "org.postgresql.Driver";
	
	public static final String urlTemplate = "jdbc:postgresql://%s:5432/rsodfs";
	
	public static final String userName = "rsodfs";
	
	public static final String password = "rsodfs11";

	public DFSDataSource(final String hostAddress) {
		// super(driverClassName, url, userName, password);
		super();
		this.hostAddress = hostAddress;
		setDriverClassName(driverClassName);
		setUrl(String.format(urlTemplate, hostAddress));
		setUsername(userName);
		setPassword(password);
	}
}
