package rso.dfs.status;

import org.apache.thrift.transport.TTransportException;

import rso.dfs.commons.DFSProperties;
import rso.dfs.generated.Service.Client;
import rso.dfs.utils.DFSClosingClient;

public class CheckSystemStatus {

	
	
	/**
	 * Checks whether server of given IP is alive
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1)
		{
			//FIXME: use some logger or sth
			System.err.println("You should provide ip address as arg"); 
			System.exit(-1);
		}

		/**
		 * Get master ip and check this goddamn status
		 */
		
		
	}

}
