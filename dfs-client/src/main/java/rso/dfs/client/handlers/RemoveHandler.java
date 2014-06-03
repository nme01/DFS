package rso.dfs.client.handlers;

import jline.internal.Log;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransportException;

import rso.dfs.commons.DFSProperties;
import rso.dfs.generated.Service;
import rso.dfs.utils.DFSClosingClient;
import rso.dfs.utils.DFSTSocket;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class RemoveHandler extends HandlerBase {

	public RemoveHandler(String masterIpAddress) {
		super(masterIpAddress);
	}

	public void performRemove(String filePath) throws Exception {

		try (DFSClosingClient dfscClient = new DFSClosingClient(masterIpAddress, DFSProperties.getProperties().getNamingServerPort())) {
			Service.Client serviceClient = dfscClient.getClient();
			boolean removeFileSuccess = serviceClient.removeFile(filePath);
			if(!removeFileSuccess)
			{
				System.out.print("File does not exist!");
			}
			
		} catch (TTransportException tte) {
			Log.error("Unable to connect to the naming server. Please restart the application and try again. (" + tte.getMessage() + ")");
			return;
			//e.printStackTrace();
		} catch (TException te) {
			Log.error("There was an error processing your removal request by the naming server: " + te.getMessage());
			return;
		} 
	}


}
