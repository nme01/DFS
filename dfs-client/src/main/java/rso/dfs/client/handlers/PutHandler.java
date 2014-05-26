package rso.dfs.client.handlers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

import rso.dfs.commons.DFSProperties;
import rso.dfs.generated.FilePart;
import rso.dfs.generated.FilePartDescription;
import rso.dfs.generated.PutFileParams;
import rso.dfs.generated.Service;
import rso.dfs.utils.DFSClosingClient;
import rso.dfs.utils.DFSTSocket;


public class PutHandler extends HandlerBase {

	public PutHandler(String masterIpAddress) {
		super(masterIpAddress);
	}

	public void performPut(String filePathSrc, String filePathDst, long fileSize) throws Exception {

		PutFileParams putFileParams = null;
		byte[] dataBuffer;
		//long chunkSize = 0;
		long offset = 0;
		try {
			File hm = new File(filePathSrc);
			if(hm.canRead()){
				System.err.println("File is inreachable");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("There is no file");
			return;
		}
		
		
		try (DFSTSocket dfstSocket = new DFSTSocket(masterIpAddress, DFSProperties.getProperties().getNamingServerPort())) {
			dfstSocket.open();
			TProtocol protocol = new TBinaryProtocol(dfstSocket);
			Service.Client serviceClient = new Service.Client(protocol);
			putFileParams = serviceClient.putFile(filePathDst, fileSize);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			if (!putFileParams.isCanPut()) // excuse me?
				throw new Exception("Can't insert another new file into the system.");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
			
		FilePart chunk = null;
		FilePartDescription fileDesc = null;
		
		try (DFSClosingClient ccClient = new DFSClosingClient(putFileParams.getSlaveIp(), 
				DFSProperties.getProperties().getStorageServerPort())) {
			Service.Client serviceClient = ccClient.getClient();
			
			dataBuffer = Files.readAllBytes(Paths.get(filePathSrc));
			
			chunk = new FilePart();
			chunk.setFileId(putFileParams.getFileId());
			chunk.setOffset(offset);
			chunk.setData(dataBuffer);
			
			fileDesc = serviceClient.sendFilePartToSlave(chunk);
			if (fileDesc.getOffset() != offset + dataBuffer.length)
				throw new Exception("Error writing data to the spectrum! Expected offset: " + dataBuffer.length + ", got: " + fileDesc.getOffset());
			
			offset = fileDesc.getOffset();
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

	}

}
