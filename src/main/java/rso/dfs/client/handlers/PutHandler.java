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

		try (DFSTSocket dfstSocket = new DFSTSocket(masterIpAddress, DFSConstans.NAMING_SERVER_PORT_NUMBER)) {
			dfstSocket.open();
			TProtocol protocol = new TBinaryProtocol(dfstSocket);
			Service.Client serviceClient = new Service.Client(protocol);
			putFileParams = serviceClient.putFile(filePath, fileSize);

		}catch (Exception e) {
			e.printStackTrace();
		}

		FilePartDescription filePartDescription = new FilePartDescription();
		filePartDescription.setFileId(putFileParams.getFileId());
		filePartDescription.setOffset(0);

		ArrayList<FilePart> fileParts = new ArrayList<>();

		try (DFSTSocket dfstSocket = new DFSTSocket("localhost", DFSConstans.STORAGE_SERVER_PORT_NUMBER)) {
			dfstSocket.open();
			TProtocol protocol = new TBinaryProtocol(dfstSocket);
			Service.Client serviceClient = new Service.Client(protocol);

			FilePart filePart = null;
			long offset = 0;
			while (true) {
				filePartDescription.setOffset(offset);
				filePart = serviceClient.getFileFromSlave(filePartDescription);
				if (filePart == null) {
					throw new Exception("Received an empty FilePart!");
					break;
				}
				offset += filePart.getData().length;
				
				fileParts.add(filePart);
			}

		}catch (Exception e){
			e.printStackTrace();
		}

		byte[] fileBody = createFileBody(fileParts);

		
		saveFileBody(filePath, fileBody);

	}

	private byte[] createFileBody(ArrayList<FilePart> fileParts) {
		byte[] bs = new byte[0];
		for (FilePart f : fileParts) {
			bs = DFSArrayUtils.concat(bs, f.getData());
		}
		return bs;
	}

	private void saveFileBody(String filePathString, byte[] body) {
		Path path = Paths.get(filePathString);
		try {
			Files.write(path, body);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
