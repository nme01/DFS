package rso.dfs.client.handlers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import jline.internal.Log;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransportException;

import rso.dfs.client.handlers.error.FileNotFoundError;
import rso.dfs.client.handlers.error.FileOperationError;
import rso.dfs.client.handlers.error.SlaveNotAlive;
import rso.dfs.commons.DFSProperties;
import rso.dfs.generated.FilePart;
import rso.dfs.generated.FilePartDescription;
import rso.dfs.generated.GetFileParams;
import rso.dfs.generated.Service;
import rso.dfs.utils.DFSArrayUtils;
import rso.dfs.utils.DFSClosingClient;
import rso.dfs.utils.DFSTSocket;

/**
 * Performs get operation.
 * 
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class GetHandler extends HandlerBase {

	public GetHandler(String masterIpAddress) {
		super(masterIpAddress);
	}

	public void performGet(String filePathSrc, String filePathDst) throws Exception {

		GetFileParams getFileParams = null;

		try (DFSClosingClient dfscClient = new DFSClosingClient(masterIpAddress, DFSProperties.getProperties().getNamingServerPort())) {
			Service.Client serviceClient = dfscClient.getClient();
			getFileParams = serviceClient.getFile(filePathSrc);

		} catch (TTransportException tte) {
			Log.error("Unable to connect to the naming server. Please restart the application and try again. (" + tte.getMessage() + ")");
			return;
			//e.printStackTrace();
		} catch (TException te) {
			Log.error("There was an error processing your download request by the naming server: " + te.getMessage());
			return;
		} 

		// TODO: it's temporary handling of 'file not found case'
		if (getFileParams.getFileId() < 0) {
			throw new FileNotFoundError();
		}

		// assembly filePartDescription
		FilePartDescription filePartDescription = new FilePartDescription();
		filePartDescription.setFileId(getFileParams.getFileId());
		filePartDescription.setOffset(0);
		
		RandomAccessFile raf = new RandomAccessFile(new File(filePathDst), "rws");

		boolean succeeded = false;
		while (!succeeded) {
			try (DFSClosingClient ccClient = new DFSClosingClient(getFileParams.getSlaveIp(), DFSProperties.getProperties().getStorageServerPort())) {
				Service.Client serviceClient = ccClient.getClient();
	
				FilePart filePart = null;
				long offset = 0;
				while (offset < getFileParams.getSize()) {
					raf.seek(offset);
					System.err.println("Requesting offset: " + offset);
					filePartDescription.setOffset(offset);
					filePart = serviceClient.getFileFromSlave(filePartDescription);
					offset += filePart.getData().length;
					
					if (filePart.getData().length == 0) {
						if(filePart.getFileId() == -2) {
							System.err.println("Requested an offset beyond the scope of the file!");
							raf.close();
							return;
						}
						break;
					}
					
					raf.write(filePart.getData(), 0, filePart.getData().length);
					//Thread.sleep(10000);
				}
				succeeded = true;
			} catch (TException te) {
				System.err.println("Could not establish a connection with the requested storage server. Retrying.");
				try (DFSClosingClient ccClient1 = new DFSClosingClient(masterIpAddress, DFSProperties.getProperties().getNamingServerPort())) {
					Service.Client client = ccClient1.getClient();
					getFileParams = client.getFileFailure(getFileParams);
				} catch (TTransportException tte) {
					System.err.println("Could not establish a connection with the naming server. Please restart the application and try again. (" + tte.getMessage() + ")");
					raf.close();
					return;
				}
			} catch (Exception e) {
				// map error and try again
				raf.close();
				throw new SlaveNotAlive();
	
			}
		}
		raf.close();

	}

}
