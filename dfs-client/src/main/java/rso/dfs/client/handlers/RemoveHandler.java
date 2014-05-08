package rso.dfs.client.handlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

import rso.dfs.commons.DFSConstans;
import rso.dfs.generated.FilePart;
import rso.dfs.generated.FilePartDescription;
import rso.dfs.generated.GetFileParams;
import rso.dfs.generated.Service;
import rso.dfs.utils.DFSArrayUtils;
import rso.dfs.utils.DFSTSocket;
import rso.dfs.utils.IpConverter;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class RemoveHandler extends HandlerBase {

	public RemoveHandler(String masterIpAddress) {
		super(masterIpAddress);
	}

	public void performRemove(String filePath) throws Exception {

		try (DFSTSocket dfstSocket = new DFSTSocket(masterIpAddress, DFSConstans.NAMING_SERVER_PORT_NUMBER)) {
			dfstSocket.open();
			TProtocol protocol = new TBinaryProtocol(dfstSocket);
			Service.Client serviceClient = new Service.Client(protocol);
			boolean removeFileSuccess = serviceClient.removeFile(filePath);
			if(!removeFileSuccess)
			{
				System.out.print("File does not exist!");
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}


}
