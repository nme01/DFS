package rso.dfs.Server;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.thrift.TException;

import rso.dfs.ServerRole;
import rso.dfs.dbManager.DbManager;
import rso.dfs.generated.CoreStatus;
import rso.dfs.generated.FilePart;
import rso.dfs.generated.FilePartDescription;
import rso.dfs.generated.GetFileParams;
import rso.dfs.generated.NewSlaveRequest;
import rso.dfs.generated.PutFileParams;
import rso.dfs.generated.Service;
import rso.dfs.generated.SystemStatus;
public class ServerHandler implements Service.Iface {

	private DbManager dbManager;
	
	public ServerHandler(){
		dbManager =  new DbManager();
	}
	
	public ServerHandler(DbManager dbm){
		this.dbManager = dbm;
	}

	@Override
	public SystemStatus getStatus() throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CoreStatus registerSlave(NewSlaveRequest req) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateCoreStatus(CoreStatus status) throws TException {
		// TODO Auto-generated method stub
		
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
		int fileId = dbManager.getFileId(filepath);
		dbManager.deleteFile(fileId);
		ArrayList<InetAddress> servers = (ArrayList<InetAddress>) dbManager.getServersByFile(fileId);
		// TODO: removeFileSlave on every slave
		return false;
	}

	@Override
	public FilePartDescription sendFileToSlaveRequest(long fileId)
			throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilePartDescription sendFilePartToSlave(FilePart filePart)
			throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilePart getFileFromSlave(FilePartDescription filePartDescription)
			throws TException {
		// TODO Auto-generated method stub
		return null;
	}

}
