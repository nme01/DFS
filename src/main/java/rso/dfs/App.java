package rso.dfs;

import rso.dfs.model.dao.DFSModelDAO;
import rso.dfs.model.dao.psql.DFSDataSource;
import rso.dfs.model.dao.psql.DFSModelDAOImpl;
import rso.dfs.server.DFSServer;
import rso.dfs.client.DFSClient;


/**
 * @author Adam Papros <adam.papros@gmail.com>
 * 
 * */

public class App {
	public static void main(String[] args) {
		System.out.println("Hello Dummy RSO DFS!");
		System.out.println("To run Dummy RSO DFS:");
		System.out.println(" -run NameServer");
		System.out.println(" -run StorageServer");
		System.out.println(" -run Client app");

		DFSDataSource dataSource = new DFSDataSource();
		DFSModelDAO modelDAO = new DFSModelDAOImpl(dataSource);
		
		if (args.length == 2) {
			if (args[1] == "NameServer") {
				String[] newArgs = {"M"};
				try {
					DFSServer.main(newArgs);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
			else if (args[1] == "StorageServer") {
				String[] newArgs = {"L"};
				try {
					DFSServer.main(newArgs);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
			else if (args[1] == "Client") {
				try {
					DFSClient.main(new String[]{""});
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
		
	}
}