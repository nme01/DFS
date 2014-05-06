package rso.dfs.client.handlers;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

import rso.dfs.commons.DFSConstans;
import rso.dfs.generated.FilePart;
import rso.dfs.generated.FilePartDescription;
import rso.dfs.generated.PutFileParams;
import rso.dfs.generated.Service;
import rso.dfs.utils.DFSArrayUtils;
import rso.dfs.utils.DFSTSocket;
import rso.dfs.utils.IpConverter;


public class PutHandler extends HandlerBase {

	public PutHandler(String masterIpAddress) {
		super(masterIpAddress);
	}

	public void performPut(String filePath, long fileSize) throws Exception {

		PutFileParams putFileParams = null;
		byte[] dataBuffer;
		//long chunkSize = 0;
		long offset = 0;

		try (DFSTSocket dfstSocket = new DFSTSocket(masterIpAddress, DFSConstans.NAMING_SERVER_PORT_NUMBER)) {
			dfstSocket.open();
			TProtocol protocol = new TBinaryProtocol(dfstSocket);
			Service.Client serviceClient = new Service.Client(protocol);
			putFileParams = serviceClient.putFile(filePath, fileSize);

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
		
		try (DFSTSocket dfstSocket = new DFSTSocket("localhost", DFSConstans.STORAGE_SERVER_PORT_NUMBER);) {
			dfstSocket.open();
			TProtocol protocol = new TBinaryProtocol(dfstSocket);
			Service.Client serviceClient = new Service.Client(protocol);
			
			dataBuffer = Files.readAllBytes(Paths.get(filePath));
			
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
