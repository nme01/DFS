package rso.dfs.status;

import org.apache.thrift.transport.TTransportException;

import rso.dfs.commons.DFSProperties;
import rso.dfs.generated.CoreStatus;
import rso.dfs.generated.ServerStatus;
import rso.dfs.generated.Service;
import rso.dfs.generated.Service.Client;
import rso.dfs.generated.SystemStatus;
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
		 * Get master ip and get system status, then print it to stdout
		 */
		
		String masterIPAddress = null;
		try (DFSClosingClient ccClient = new DFSClosingClient(args[0], 
				DFSProperties.getProperties().getStorageServerPort())) {
			Service.Client serviceClient = ccClient.getClient();
			CoreStatus coreStatus = serviceClient.getCoreStatus();
			masterIPAddress = coreStatus.getMasterAddress();
			System.out.println("Master IP is " + masterIPAddress);
		}catch (Exception e){
			System.err.println("Service is not available on given ip: " + args[0] + ", exiting...");
			System.exit(-1);
		}
		
		SystemStatus status = null;
		
		try(DFSClosingClient cclient = 
				new DFSClosingClient(masterIPAddress,
						DFSProperties.getProperties().getStorageServerPort(),2000))
		{
			Client client = cclient.getClient();
			status = client.getStatus();
		} catch (Exception e) {
			System.err.println("Naming service is not available on ip: " + masterIPAddress + ", exiting...");
			System.exit(-1);
		}
		
		System.out.println("Status.filesNumber: " + status.getFilesNumber());
		System.out.println();
		for(ServerStatus ss: status.getServersStatuses())
		{
			System.out.println(ss);
		}
		
	}

}
