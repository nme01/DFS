package rso.dfs.client.handlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

import rso.dfs.commons.DFSProperties;
import rso.dfs.generated.FilePart;
import rso.dfs.generated.FilePartDescription;
import rso.dfs.generated.GetFileParams;
import rso.dfs.generated.Service;
import rso.dfs.utils.DFSArrayUtils;
import rso.dfs.utils.DFSClosingClient;
import rso.dfs.utils.DFSTSocket;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class GetHandler extends HandlerBase {

	public GetHandler(String masterIpAddress) {
		super(masterIpAddress);
	}

	public void performGet(String filePathSrc, String filePathDst) throws Exception {

		GetFileParams getFileParams = null;

		try (DFSTSocket dfstSocket = new DFSTSocket(masterIpAddress, DFSProperties.getProperties().getNamingServerPort())) {
			dfstSocket.open();
			TProtocol protocol = new TBinaryProtocol(dfstSocket);
			Service.Client serviceClient = new Service.Client(protocol);
			getFileParams = serviceClient.getFile(filePathSrc);

		}catch (Exception e) {
			e.printStackTrace();
		}

		//TODO: it's temporary handling of 'file not found case'
		if(getFileParams.getFileId() < 0)
		{
			System.out.print("File not found!");
			return;
		}
		
		;
		
		// assembly filePartDescription
		FilePartDescription filePartDescription = new FilePartDescription();
		filePartDescription.setFileId(getFileParams.getFileId());
		filePartDescription.setOffset(0);

		ArrayList<FilePart> fileParts = new ArrayList<>();

		try (DFSClosingClient ccClient = new DFSClosingClient(getFileParams.getSlaveIp(), 
				DFSProperties.getProperties().getStorageServerPort())) {
			Service.Client serviceClient = ccClient.getClient();

			FilePart filePart = null;
			long offset = 0;
			while (true) {
				filePartDescription.setOffset(offset);
				filePart = serviceClient.getFileFromSlave(filePartDescription);
				if (filePart.getData().length == 0) {
					break;
				}
				offset += filePart.getData().length;
				
				fileParts.add(filePart);
			}
		}catch (Exception e){
			e.printStackTrace();
		}

		byte[] fileBody = createFileBody(fileParts);

		
		saveFileBody(filePathDst, fileBody);

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