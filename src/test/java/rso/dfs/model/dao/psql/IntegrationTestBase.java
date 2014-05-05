package rso.dfs.model.dao.psql;

import rso.dfs.model.dao.DFSModelDAO;

public abstract class IntegrationTestBase {

	// TODO : change this to test data source
	private DFSDataSource dataSource;

	protected DFSModelDAO modelDAO;

	public IntegrationTestBase() {
		super();
		dataSource = new DFSDataSource();
		dataSource.setPassword("test_rsodfs11");
		dataSource.setUrl("jdbc:postgresql://localhost:5432/test_rsodfs");
		dataSource.setUsername("test_rsodfs");
		modelDAO = new DFSModelDAOImpl(dataSource);

	}
	
}
