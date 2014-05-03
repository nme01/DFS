package rso.dfs;

import rso.dfs.model.dao.DFSModelDAO;
import rso.dfs.model.dao.psql.DFSDataSource;
import rso.dfs.model.dao.psql.DFSModelDAOImpl;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * 
 * */

public class App {
	public static void main(String[] agrs) {
		System.out.println("Hello Dummy RSO DFS!");
		System.out.println("To run Dummy RSO DFS:");
		System.out.println(" -run NameServer");
		System.out.println(" -run StorageServer");
		System.out.println(" -run Client app");

		DFSDataSource dataSource = new DFSDataSource();
		DFSModelDAO modelDAO = new DFSModelDAOImpl(dataSource);

	}
}