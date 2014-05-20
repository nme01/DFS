package rso.dfs.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransportException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rso.dfs.commons.DFSProperties;
import rso.dfs.generated.CoreStatus;
import rso.dfs.generated.FilePart;
import rso.dfs.generated.FilePartDescription;
import rso.dfs.generated.GetFileParams;
import rso.dfs.generated.NewSlaveRequest;
import rso.dfs.generated.PutFileParams;
import rso.dfs.generated.Service;
import rso.dfs.generated.Service.Client;
import rso.dfs.generated.SystemStatus;
import rso.dfs.model.File;
import rso.dfs.model.FileOnServer;
import rso.dfs.model.FileStatus;
import rso.dfs.model.Server;
import rso.dfs.model.ServerRole;
import rso.dfs.model.dao.DFSRepository;
import rso.dfs.model.dao.psql.DFSRepositoryImpl;
import rso.dfs.server.handler.FileStorageHandler;
import rso.dfs.utils.DFSArrayUtils;
import rso.dfs.utils.DFSClosingClient;
import rso.dfs.utils.DFSTSocket;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * @author Mateusz Statkiewicz
 * */
public class ServerHandler implements Service.Iface {

	final static Logger log = LoggerFactory.getLogger(ServerHandler.class);

	/**
	 * This server
	 * TODO : provide better naming... :(
	 * */
	private Server me;

	/**
	 * Provides data access layer.
	 * (FOR MASTER-NAMING SERVERS)
	 * */
	private DFSRepository repository;

	/**
	 * Provides storage features.
	 * (FOR SLAVE-STORAGE SERVERS)
	 * */
	private FileStorageHandler storageHandler;
	
	/**
	 * 
	 * */
	private CoreStatus coreStatus;
	
	public ServerHandler(Server me) {
		this.me = me;
		this.storageHandler = new FileStorageHandler();
		this.repository = new DFSRepositoryImpl();
		this.coreStatus = new CoreStatus("", new ArrayList<String>()); //FIXME: temp just not to be null
	}

	@Override
	public SystemStatus getStatus() throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CoreStatus registerSlave(NewSlaveRequest req) throws TException {
		log.info("Got registerSlave request! Slave IP: " + req.getSlaveIP() + "; list of files {" + req.getFileIds() +  "}");
		Server server = new Server();
		
		server.setIp(req.getSlaveIP());
		server.setMemory(DFSProperties.getProperties().getStorageServerMemory()); //FIXME: not really i guess
		server.setRole(ServerRole.SLAVE);
		server.setLastConnection(new DateTime());
		repository.saveServer(server);

		List<Integer> ids = req.getFileIds();
		for (int fileId : ids) {
			if (repository.getFileById(fileId)!= null) {
				FileOnServer fileOnServer = new FileOnServer();
				fileOnServer.setFileId(fileId);
				fileOnServer.setServerId(server.getId());
				// fileOnServer.setPriority(0);
				repository.saveFileOnServer(fileOnServer);
				ids.remove((Integer)fileId); //FIXME: is removing element legal in java?
			};
		}
		
		try(DFSClosingClient cclient = new DFSClosingClient(req.getSlaveIP(), 
				DFSProperties.getProperties().getStorageServerPort()))
		{
			Client client = cclient.getClient();
			for (Integer fileId : ids) {
				client.removeFileSlave(fileId);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	public void replicate(int fileID, String slaveIP) throws TException {
		
		final int fid = fileID;
		final String sip = slaveIP;
		final long sid = me.getId();
		
		Thread thread = new Thread(new Runnable() {
			int fileID = fid;
			String slaveIP = sip;
			long slaveID = sid;
			public void run() {
				FilePartDescription filePartDescription = new FilePartDescription();
				filePartDescription.setFileId(fileID);
				filePartDescription.setOffset(0);

				ArrayList<FilePart> fileParts = new ArrayList<>();
				
				try (DFSClosingClient ccClient = new DFSClosingClient(slaveIP, 
						DFSConstans.STORAGE_SERVER_PORT_NUMBER)) {
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
				
				byte[] dataBuffer = new byte[0];
				for (FilePart filePart : fileParts) {
					dataBuffer = DFSArrayUtils.concat(dataBuffer, filePart.getData());
				}
				storageHandler.writeFile(fileID, dataBuffer); // TODO: the entire file goes through the memory at the moment!
				
				repository.saveFileOnServer(new FileOnServer(fileID, slaveID, 0));
			}
			
		});
		thread.start();
		return;
	}

	@Override
	public boolean isFileUsed(int fileID) throws TException {
		// to w ogole potrzebne??
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeFileSlave(int fileID) throws TException {
		storageHandler.deleteFile(fileID);
	}

	/**
	 * Invoked on master by client. Return to client info about desired file.
	 * 
	 * @param fileName
	 * @return GetFileParams
	 */
	@Override
	public GetFileParams getFile(String fileName) throws TException {
		log.debug("getFile Invoked on file " + fileName);
		File file = repository.getFileByFileName(fileName);
		if(file == null)
		{
			//TODO: it's a temporary sign of 'file not found' case. Exception in da future.
			return new GetFileParams(-1,"");
		}

		Server slave = repository.getSlaveByFile(file);

		GetFileParams getFileParams = new GetFileParams();
		getFileParams.setFileId(file.getId());

		getFileParams.setSlaveIp(slave.getIp());
		
		return getFileParams;
	}

	@Override
	public GetFileParams getFileFailure(String filepath) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Invoked on master by client. Return to client info where to put new file.
	 * Add new file to DB.
	 * 
	 * @param filepath
	 * @param size
	 * @return PutFileParams
	 */
	@Override
	public PutFileParams putFile(String filepath, long size) throws TException {
		if(me.getRole() != ServerRole.MASTER){
			// TODO: ILLEGAL
			log.error("putFile attempted on a server different than the current master.");
			PutFileParams retval = new PutFileParams();
			retval.canPut = false;
			retval.fileId = 0;
			retval.slaveIp = "";
			return retval;
		}
		log.info("Master received put file request for file " + filepath);
		List<Server> slaves = repository.getSlaves();
		
		String slaveAddress = null;
		long slaveId = -1;
		Map<Server, Long> freeMemoryMap = new HashMap<Server, Long>();
		
		for (Server server : slaves) {
			long filesMemory = 0;
			for (File file : repository.getFilesOnSlave(server)) {
				filesMemory += file.getSize();
			}
			
			if (server.getMemory() - filesMemory <= size)
				continue;
			
			freeMemoryMap.put(server, filesMemory); // po miejscu zajętym
			//freeMemoryMap.put(server, server.getMemory() - filesMemory); // po miejscu wolnym
		}
		
		List<Server> keys = new ArrayList<Server>(freeMemoryMap.keySet());
		final Map sortmap = freeMemoryMap;
		Collections.sort(keys,
				new Comparator() {
					public int compare(Object left, Object right) {
						Long leftVal = (Long)sortmap.get((Server)left);
						Long rightVal = (Long)sortmap.get((Server)right);
						
						return leftVal.compareTo(rightVal);
					}
		});
		
		Iterator i = keys.iterator();
		long replDegree = 3L; // serverConfig.getReplDegree();
		PutFileParams putFileParams = new PutFileParams();
		putFileParams.setCanPut(true);
		if (!i.hasNext()) {
			putFileParams.setCanPut(false);
		} else {
			try {
				Server mainRepl = (Server)i.next();
				File file = new File();
				file.setName(filepath);
				file.setSize(size);
				file.setStatus(FileStatus.UPLOAD);
				Integer fileId = repository.saveFile(file);
				
				repository.saveFileOnServer(new FileOnServer(fileId, mainRepl.getId(), 0));

				putFileParams.setCanPut(true);
				putFileParams.setSlaveIp(mainRepl.getIp());
				putFileParams.setFileId(fileId);
				replDegree--;
			}
			catch (org.springframework.dao.DuplicateKeyException e)
			{
				log.error("File exists!");
				PutFileParams retval = new PutFileParams();
				retval.canPut = false;
				retval.fileId = 0;
				retval.slaveIp = "";
				return retval;
			}
			catch (Exception e) {
				//TODO: wysypka UnknownHostException (??);
				e.printStackTrace();
			}
		}
		
		if (i.hasNext() && replDegree-- > 0) {
			Server secRepl = (Server)i.next();
			try (DFSTSocket dfstSocket = new DFSTSocket(secRepl.getIp(), DFSProperties.getProperties().getStorageServerPort())) {
				dfstSocket.open();
				TProtocol protocol = new TBinaryProtocol(dfstSocket);
				Service.Client serviceClient = new Service.Client(protocol);
				serviceClient.replicate(putFileParams.getFileId(), secRepl.getIp());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
		final File file = repository.getFileByFileName(filepath);
		if(file == null)
		{
			/*TODO: result of false indicates unability to remove the file, 
			 *   maybe exception should be thrown with description of special  
			 */ 
			return false;
		}
		
		file.setStatus(FileStatus.TO_DELETE);

		repository.updateFile(file);

		Thread thread = new Thread(new Runnable() {
			public void run() {
				List<Server> servers = repository.getSlavesByFile(file);
				for (Server server : servers) {
					try(DFSClosingClient dfsclient = new DFSClosingClient(server.getIp(), DFSProperties.getProperties().getStorageServerPort()))
					{
						
						Service.Client client = dfsclient.getClient();
						try {
							client.removeFileSlave((int) file.getId());
							FileOnServer fos = new FileOnServer();
							fos.setFileId(file.getId());
							fos.setServerId(server.getId());
							repository.deleteFileOnServer(fos);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} catch (TTransportException e1) {
						e1.printStackTrace();
					} catch (Exception e1) {
						e1.printStackTrace();
					}

				}
				repository.deleteFile(file);

			}
		});
		thread.start();
		return true;
	}

	/**
	 * Invoked on a slave by the master. Orders the slave to secure a transfer
	 * handler for a file from a certain client.
	 * 
	 * @param fileId
	 * @return FilePartDescription
	 */
	@Override
	public FilePartDescription sendFileToSlaveRequest(int fileId) throws TException {
		storageHandler.createFile(fileId);
		return new FilePartDescription(fileId, (long) 0);
	}

	/**
	 * Invoked on a slave by a client. Saves a part of a file onto storage.
	 * 
	 * @param filePart
	 * @return FilePartDescription
	 */
	@Override
	public FilePartDescription sendFilePartToSlave(FilePart filePart) throws TException {
		try {
			// TODO: check if the filePart is from the expected client
			byte[] dataBuffer = filePart.data.array();
			log.info("Got a file part of fileId " + filePart.fileId + " of size " + filePart.data.array().length + " bytes.");
			storageHandler.writeFile(filePart.fileId, dataBuffer);

			return new FilePartDescription(filePart.fileId, filePart.offset + dataBuffer.length);

		} catch (Exception e) {
			// welcome to mcdonalds, do you want fries with that
		}

		return null;
	}

	/**
	 * Dummy version sending whole file
	 * 
	 * @return FilePart - when whole file was sent data is set to [].
	 * */
	@Override
	public FilePart getFileFromSlave(FilePartDescription filePartDescription)
			throws TException {

		if (me.getRole() == rso.dfs.model.ServerRole.MASTER || filePartDescription.getOffset() >= repository.getFileById(filePartDescription.getFileId()).getSize()) {
			return new FilePart(-1, 0, ByteBuffer.allocate(0));
		}
		
		while (filePartDescription.getOffset() >= storageHandler.getFileSize(storageHandler.getFileSize(filePartDescription.getFileId()))) {
			//TODO: handle timeouts
			//put cond var on a queue
			//sendFileParttoSlave will remove and signal them
		}
			
		byte[] dataToSend = storageHandler.readFile(filePartDescription.getFileId(), filePartDescription.getOffset());
		if (filePartDescription.getOffset() >= dataToSend.length) {
			FilePart filePart = new FilePart();
			filePart.setFileId(filePartDescription.getFileId());
			filePart.setData(new byte[0]);
			return filePart;
		}

		// assemble filePart
		FilePart filePart = new FilePart();
		filePart.setFileId(filePartDescription.getFileId());
		filePart.setData(dataToSend);
		return filePart;
	}

	@Override
	public void fileUploadSuccess(int fileID, String slaveIP) throws TException {
		rso.dfs.model.File f = repository.getFileById(fileID);
		f.setStatus(FileStatus.HELD);
		repository.updateFile(f);
		//TODO repository not implements Files_on_servers interface
		//which i will use here
	}

}
