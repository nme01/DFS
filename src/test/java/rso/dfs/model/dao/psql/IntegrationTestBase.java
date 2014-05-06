package rso.dfs.model.dao.psql;

import rso.dfs.model.dao.DFSModelDAO;

public abstract class IntegrationTestBase {

	// TODO : change this to test data source
	private DFSDataSource dataSource;

	protected DFSModelDAO modelDAO;

	public IntegrationTestBase() {
		super();
		dataSource = new DFSDataSource();
		dataSource.setPassword("rsodfs11");
		dataSource.setUrl("jdbc:postgresql://127.0.0.1:5432/rsodfs");
		dataSource.setUsername("rsodfs");
		modelDAO = new DFSModelDAOImpl(dataSource);

	}
	
}
