package rso.dfs.model.dao.psql;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class DFSDataSource extends DriverManagerDataSource {

	public static final String driverClassName = "org.postgresql.Driver";
	public static final String url = "jdbc:postgresql://127.0.0.1:5432/rsodfs";
	public static final String userName = "rsodfs";
	public static final String password = "rsodfs11";

	public DFSDataSource() {
		// super(driverClassName, url, userName, password);
		super();
		setDriverClassName(driverClassName);
		setUrl(url);
		setUsername(userName);
		setPassword(password);
	}
}
