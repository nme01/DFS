package rso.dfs.model.dao.psql;

import rso.dfs.model.dao.DFSModelDAO;

public abstract class IntegrationTestBase {

	// TODO : change this to test data source
	private DFSDataSource dataSource;

	protected DFSModelDAO modelDAO;

	public IntegrationTestBase() {
		super();
		dataSource = new DFSDataSource();
		dataSource.setPassword("test_rso_dfs11");
		dataSource.setUrl("jdbc:postgresql://localhost:5432/test_rso_dfs");
		dataSource.setUsername("test_rso_dfs");
		modelDAO = new DFSModelDAOImpl(dataSource);

	}
	
}
