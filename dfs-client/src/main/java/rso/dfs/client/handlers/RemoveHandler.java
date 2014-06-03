package rso.dfs.client.handlers;

import jline.internal.Log;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

import rso.dfs.commons.DFSProperties;
import rso.dfs.generated.Service;
import rso.dfs.utils.DFSTSocket;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class RemoveHandler extends HandlerBase {

	public RemoveHandler(String masterIpAddress) {
		super(masterIpAddress);
	}

	public void performRemove(String filePath) throws Exception {

		try (DFSTSocket dfstSocket = new DFSTSocket(masterIpAddress, DFSProperties.getProperties().getNamingServerPort())) {
			dfstSocket.open();
			TProtocol protocol = new TBinaryProtocol(dfstSocket);
			Service.Client serviceClient = new Service.Client(protocol);
			boolean removeFileSuccess = serviceClient.removeFile(filePath);
			if(!removeFileSuccess)
			{
				System.out.print("File does not exist!");
			}
			
		}catch (Exception e) {
			Log.error(e);
			//e.printStackTrace();
		}
	}


}
