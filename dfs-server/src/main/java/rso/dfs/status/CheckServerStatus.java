package rso.dfs.status;

import org.apache.thrift.transport.TTransportException;

import rso.dfs.commons.DFSProperties;
import rso.dfs.generated.Service.Client;
import rso.dfs.utils.DFSClosingClient;

public class CheckServerStatus {

	
	
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
		if(checkAlive(args[0]))
		{
			System.exit(0);
		}
		else
		{
			System.exit(1);
		}
	}

	/**
	 * 2000 ms timeout on connection
	 * @param string IP of the server
	 * @return 0 on success, not 0 on failure
	 */
	public static boolean checkAlive(String string) {
		int returnvar = 0;
		int timeoutInMs = 2000; 
		try{
			try(DFSClosingClient cclient = 
					new DFSClosingClient(string,
							DFSProperties.getProperties().getStorageServerPort(),timeoutInMs))
			{
				Client client = cclient.getClient();
				client.pingServer();
			}
		}
		catch (Exception e) {
			//something wrong happens, return failure
			returnvar = 1;
		} 
		//everything is fine, return true
		return (returnvar==0);
	}

}
