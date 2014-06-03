package rso.dfs.client.handlers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jline.internal.Log;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransportException;

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
		long offset = 0;
		int chunkSize = DFSProperties.getProperties().getFilePartSize().intValue();
		byte[] dataBuffer = new byte[chunkSize];
		
		File f = new File(filePathSrc);
		if (!f.exists() || f.isDirectory()) {
			System.err.println("Unable to read the requested file.");
			return;
		}
		
		
		try (DFSClosingClient dfscClient = new DFSClosingClient(masterIpAddress, DFSProperties.getProperties().getNamingServerPort())) {
			Service.Client serviceClient = dfscClient.getClient();
			putFileParams = serviceClient.putFile(filePathDst, fileSize);

		} catch (TTransportException tte) {
			Log.error("Unable to connect to the naming server. Please restart the application and try again. (" + tte.getMessage() + ")");
			return;
			//e.printStackTrace();
		} catch (TException te) {
			System.err.println("Caught an exception from the remote end while attempting to upload a file: " + te.getMessage());
			return;
		} 
		
		if (!putFileParams.isCanPut()) { // excuse me?
			Log.error("Can't insert another new file into the system.");
			return;
		}
			
		FilePart chunk = null;
		FilePartDescription fileDesc = null;
		File file = new File(filePathSrc);
		Path path = Paths.get(file.getAbsolutePath());
		int bytesRead = 0;
		boolean succeeded = false;
		
		while (!succeeded) {
			try (DFSClosingClient ccClient = new DFSClosingClient(putFileParams.getSlaveIp(), 
					DFSProperties.getProperties().getStorageServerPort())) {
				Service.Client serviceClient = ccClient.getClient();
				
				RandomAccessFile raf = new RandomAccessFile(new File(path.toString()), "r");
				int remainingSize = (int)Math.min(Files.size(path) - offset, DFSProperties.getProperties().getFilePartSize().intValue());
				
				while (offset != Files.size(path)) {
					System.err.println("[PUT] Read " + remainingSize + " bytes from file " + filePathSrc + ", offset " + offset + ".");
					dataBuffer = new byte[remainingSize];
					raf.seek(offset);
					bytesRead = raf.read(dataBuffer, 0, remainingSize);
					
					chunk = new FilePart();
					chunk.setFileId(putFileParams.getFileId());
					chunk.setOffset(offset);
					chunk.setData(dataBuffer);
					
					fileDesc = serviceClient.sendFilePartToSlave(chunk);
					if (fileDesc.getOffset() != offset + dataBuffer.length)
						throw new Exception("Error writing data to the spectrum! Expected offset: " + (offset + dataBuffer.length) + ", got: " + fileDesc.getOffset());
					
					offset = fileDesc.getOffset();
					remainingSize = (int)Math.min(Files.size(path) - offset, DFSProperties.getProperties().getFilePartSize().intValue());
					
					System.err.println("[PUT] Got an offset of " + offset + " bytes in response.");
					//Thread.sleep(10000);
				}
				
				chunk = new FilePart();
				chunk.setFileId(putFileParams.getFileId());
				chunk.setOffset(offset);
				chunk.setData(new byte[0]);
				fileDesc = serviceClient.sendFilePartToSlave(chunk);
				succeeded = true;
				
			} catch (TTransportException tte) {
				System.err.println("There was an error connecting to the associated storage server!");
				try (DFSClosingClient dfscClient = new DFSClosingClient(masterIpAddress, DFSProperties.getProperties().getNamingServerPort())) {
					Service.Client serviceClient = dfscClient.getClient();
					putFileParams = serviceClient.putFileFailure(putFileParams);

				} catch (TTransportException te) {
					System.err.println("Unable to contact the naming service server. Please reload the client application and try again.");
					return;
				} catch (TException te) {
					System.err.println("Caught an exception from the remote end: " + te.getMessage());
					return;
				} 
				
			} catch (IOException ioe) {
				System.err.println("[ERROR] There was an issue reading the requested file.");
				return;
			} catch (TException te) {
				System.err.println("[ERROR] Caught an exception from the remote end: " + te.getMessage());
				Log.error(te);
				//te.printStackTrace();
				return;
			}
		}

	}

}
