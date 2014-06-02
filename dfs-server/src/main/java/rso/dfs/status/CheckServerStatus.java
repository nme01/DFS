package rso.dfs.status;

import org.apache.thrift.transport.TTransportException;
import org.joda.time.DateTime;

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
		Integer timeoutInMs = DFSProperties.getProperties().getDefaultClientTimeout();
		if (args.length > 1)
		{
			try
			{
				timeoutInMs = Integer.parseInt(args[1]);
			}
			catch (NumberFormatException e)
			{
				System.err.println("Wrong number of milliseconds");
			}
		} 
		if(checkAlive(args[0],timeoutInMs))
		{
			System.exit(0);	
		}
		else
		{
			System.exit(1);
		}
	}

	public static boolean checkAlive(String IP) {
		return checkAlive(IP, 2000); //TODO: magicvalue
	}
	/**
	 * 2000 ms timeout on connection	
	 * @param string IP of the server
	 * @param timeoutInMs 
	 * @return 0 on success, not 0 on failure
	 */
	public static boolean checkAlive(String IP, Integer timeoutInMs) {
		DateTime dt = new DateTime();
		while((new DateTime().getMillis()) - dt.getMillis() < timeoutInMs)
		{
			try{
				try(DFSClosingClient cclient = 
						new DFSClosingClient(IP,
								DFSProperties.getProperties().getStorageServerPort(),timeoutInMs))
				{
					Client client = cclient.getClient();
					client.pingServer();
					return true;
				}
			}
			catch (Exception e) {
				//something wrong happens, try again
			}
			
			try {
				Thread.sleep(100l);
			} catch (InterruptedException e) {
				//do nothing
			}
		}
		
		return false;
	}

}
