package rso.dfs.Server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import rso.dfs.ServerRole;
import rso.dfs.dbManager.DbManager;
import rso.dfs.dummy.generated.NamingService;
import rso.dfs.generated.CoreStatus;
import rso.dfs.generated.FilePart;
import rso.dfs.generated.FilePartDescription;
import rso.dfs.generated.GetFileParams;
import rso.dfs.generated.NewSlaveRequest;
import rso.dfs.generated.PutFileParams;
import rso.dfs.generated.Service;
import rso.dfs.generated.SystemStatus;
import rso.dfs.model.*;
import rso.dfs.model.dao.DFSModelDAO;
import rso.dfs.model.dao.psql.DFSModelDAOImpl;
import rso.dfs.model.dao.psql.DFSDataSource;

public class ServerHandler implements Service.Iface {

	private DbManager dbManager;
	private CoreStatus coreStatus;
	private DFSModelDAO modelDAO;
	
	public ServerHandler(){
		dbManager =  new DbManager();
		modelDAO = new DFSModelDAOImpl(new DFSDataSource());
		
	}
	
	public ServerHandler(DbManager dbm){
		this.dbManager = dbm;
		modelDAO = new DFSModelDAOImpl(new DFSDataSource());
		
	}

	@Override
	public SystemStatus getStatus() throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CoreStatus registerSlave(NewSlaveRequest req) throws TException {
		modelDAO = new DFSModelDAOImpl(new DFSDataSource());
		Server server = new Server();
		//server.setIp();			//skad mam to wziac? do strukturki trzeba dodac? to samo nizej
		//server.setMemory(memory);
		//server.setRole();
		//modelDAO.saveServer(server);
		
		List<Integer> ids= req.getFileIds();
		for (int fileId : ids)
		{
			if (modelDAO.fetchFileById((long) fileId) != null)
				{
				FileOnServer fileOnServer = new FileOnServer();
				fileOnServer.setFileId(fileId);
				fileOnServer.setServerIp(server.getIp());
				//fileOnServer.setPriority(0);
				modelDAO.saveFileOnServer(fileOnServer);
				ids.remove(fileId);
				};
		}
		TTransport transport;

		transport = new TSocket("localhost",9900); //skad wziac ipka i port?
		transport.open();

		TProtocol protocol = new TBinaryProtocol(transport);
		Service.Client client = new Service.Client(protocol);
		for (int fileId : ids)
		{
			client.removeFileSlave(fileId);
		}
		return coreStatus;
	}

	@Override
	public void updateCoreStatus(CoreStatus status) throws TException {
		coreStatus = status;
		
	}

	@Override
	public void becomeShadow(CoreStatus status) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CoreStatus getCoreStatus() throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void prepareForReceiving(int fileID, long size) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void replicate(int fileID, int slaveIP) throws TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isFileUsed(int fileID) throws TException {
		// to w ogole potrzebne??
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeFileSlave(int fileID) throws TException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Invoked on master by client. Return to client info about desired file.
	 * 
	 * @param filepath
	 * @return GetFileParams
	 */
	@Override
	public GetFileParams getFile(String filepath) throws TException {
		int fileId = dbManager.getFileId(filepath);
		if(fileId == -1){
			return null;
		}
		GetFileParams getFileParams = new GetFileParams();
		getFileParams.setFileId(fileId);
		ArrayList<InetAddress> slaves = (ArrayList<InetAddress>) dbManager.getServersByFile(fileId);
		// get first slave which has desired file
		int slaveIp = ByteBuffer.wrap(slaves.get(0).getAddress()).getInt();
		getFileParams.setSlaveIp(slaveIp);
		return getFileParams;
	}

	@Override
	public GetFileParams getFileFailure(String filepath) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Invoked on master by client. Return to client info where to put new file. Add new file to DB.
	 * 
	 * @param filepath
	 * @param size
	 * @return PutFileParams
	 */
	@Override
	public PutFileParams putFile(String filepath, long size) throws TException {
		ArrayList<Pair<InetAddress, Long>> slaves = (ArrayList<Pair<InetAddress, Long>>) dbManager.getServersByRoleMemory(ServerRole.Slave);
		Collections.shuffle(slaves);
		int slaveAddress = 0;
		for(Pair<InetAddress, Long> server : slaves){
			// TODO check if there is space on slave
			if(server.getRight() > size){
				slaveAddress = ByteBuffer.wrap(server.getLeft().getAddress()).getInt();
				break;
			}
		}
		PutFileParams putFileParams = new PutFileParams();
		if(slaveAddress == 0){
			putFileParams.setCanPut(false);
			return putFileParams;
		}
		int newFileId = dbManager.addNewFile(filepath, size);
		// TODO put fileId, change PutFileParams class
		putFileParams.setCanPut(true);
		putFileParams.setSlaveIp(slaveAddress);
		// TODO receiveFile to slave1 and getFile to slave2?
		return putFileParams;
	}

	@Override
	public PutFileParams putFileFailure(String filepath, long size)
			throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Invoked on master by client. Order master to remove file.
	 * 
	 * @param filepath
	 * @return boolean
	 */
	@Override
	public boolean removeFile(String filepath) throws TException {
		int fileId = dbManager.getFileId(filepath); // TODO: not long?
		dbManager.deleteFile(fileId);
		ArrayList<InetAddress> servers = (ArrayList<InetAddress>) dbManager.getServersByFile(fileId);
		// TODO: removeFileSlave on every slave
		return false;
	}

	/**
	 * Invoked on a slave by the master. Orders the slave to secure a transfer handler for a file from a certain client.
	 * 
	 * @param fileId
	 * @return FilePartDescription
	 */
	@Override
	public FilePartDescription sendFileToSlaveRequest(long fileId)
			throws TException {
		
		File file = new File("/tmp/" + Long.toString(fileId));
		try {
			file.createNewFile();
		} catch (IOException ioe) {
			return null;
		}
		return new FilePartDescription((int)fileId, (long)0);
	}

	/**
	 * Invoked on a slave by a client. Saves a part of a file onto storage.
	 * @param filePart
	 * @return FilePartDescription
	 */
	@Override
	public FilePartDescription sendFilePartToSlave(FilePart filePart)
			throws TException {
		try {
			//TODO: check if the filePart is from the expected client
			final FileOutputStream fis = new FileOutputStream( "/tmp/" + Integer.toString(filePart.fileId), true );
			FileChannel fc = fis.getChannel();
			ByteBuffer[] bba = {filePart.bufferForData()};
			
			if (fc.write(bba, (int)(filePart.getOffset()), filePart.bufferForData().capacity()) != filePart.bufferForData().capacity() ) {
				//TODO: find a way to properly handle exceptions
			}
			
			return new FilePartDescription(filePart.fileId, filePart.offset);
			
		} catch(Exception e){
			//??? stop whining
		}
		
		return null;
	}

	@Override
	public FilePart getFileFromSlave(FilePartDescription filePartDescription)
			throws TException {
		// TODO Auto-generated method stub
		return null;
	}

}
