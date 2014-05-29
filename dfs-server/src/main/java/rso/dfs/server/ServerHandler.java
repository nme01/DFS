package rso.dfs.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
import rso.dfs.generated.ServerStatus;
import rso.dfs.generated.ServerType;
import rso.dfs.generated.Service;
import rso.dfs.generated.Service.Client;
import rso.dfs.generated.SystemStatus;
import rso.dfs.model.File;
import rso.dfs.model.FileOnServer;
import rso.dfs.model.FileStatus;
import rso.dfs.model.Server;
import rso.dfs.model.ServerRole;
import rso.dfs.model.dao.DFSRepository;
import rso.dfs.server.storage.StorageHandler;
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
	 * This server TODO : provide better naming... :(
	 * */
	private Server me;

	/**
	 * Provides data access layer. (FOR MASTER-NAMING SERVERS)
	 * */
	private DFSRepository repository;

	/**
	 * Provides storage features. (FOR SLAVE-STORAGE SERVERS)
	 * */
	private StorageHandler storageHandler;

	/**
	 * 
	 * */
	private CoreStatus coreStatus;
	
	/**
	 * Map of replicating slaves waiting for a file.
	 * */
	private Lock awaitingSlavesLock = new ReentrantLock();
	private Map<Integer, List<Condition>> awaitingSlaves = new HashMap<Integer, List<Condition>>();
	private Long syncCounter = 1L;

	/**
	 * Contains statuses which files where used;
	 * key -> fileId;
	 * value -> value needed to double timeout delete
	 * */
	private HashMap<Integer, Integer> files = new HashMap<>();	
	
	public ServerHandler(Server me, CoreStatus cs) {
		this.me = me;
		this.storageHandler = storageHandler;
		this.repository = repository;
		this.coreStatus = cs; // FIXME:
																		// temp
																		// just
																		// not
																		// to be
																		// null
	}

	@Override
	public List<String> listFileNames() throws TException {
		log.debug("New LS request.");
		List<File> files = repository.getAllFiles();

		List<String> fileNames = new ArrayList<>();
		for (File f : files) {
			fileNames.add(f.getName());
		}
		return fileNames;
	}

	@Override
	public SystemStatus getStatus() throws TException {
		SystemStatus ss = new SystemStatus();
		ss.setFilesNumber(repository.getAllFiles().size());		
		Server master = repository.getMasterServer();
		ss.addToServersStatuses(new ServerStatus(ServerType.Master,master.getFilesNumber(), master.getFreeMemory(), master.getMemory()-master.getFreeMemory()));
		for (Server server : repository.getShadows())
			ss.addToServersStatuses(new ServerStatus(ServerType.Shadow, server.getFilesNumber(), server.getFreeMemory(), server.getMemory()-server.getFreeMemory()));
		for (Server server : repository.getSlaves())
			ss.addToServersStatuses(new ServerStatus(ServerType.Slave, server.getFilesNumber(), server.getFreeMemory(), server.getMemory()-server.getFreeMemory()));
		return ss;
	}

	@Override
	public CoreStatus registerSlave(NewSlaveRequest req) throws TException {
		log.info("Got registerSlave request! Slave IP: " + req.getSlaveIP() + "; list of files {" + req.getFileIds() + "}");
		Server server = new Server();

		server.setIp(req.getSlaveIP());
		server.setMemory(DFSProperties.getProperties().getStorageServerMemory()); // FIXME:
																					// not
																					// really
																					// i
																					// guess
		server.setRole(ServerRole.SLAVE);
		server.setLastConnection(new DateTime());
		repository.saveServer(server);

		List<Integer> ids = req.getFileIds();
		for (int fileId : ids) {
			if (repository.getFileById(fileId) != null) {
				FileOnServer fileOnServer = new FileOnServer();
				fileOnServer.setFileId(fileId);
				fileOnServer.setServerId(server.getId());
				// fileOnServer.setPriority(0);
				repository.saveFileOnServer(fileOnServer);
				ids.remove((Integer) fileId); // FIXME: is removing element
												// legal in java?
			}
			;
		}

		try (DFSClosingClient cclient = new DFSClosingClient(req.getSlaveIP(), DFSProperties.getProperties().getStorageServerPort())) {
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
		return coreStatus;
	}

	@Override
	public void prepareForReceiving(int fileID, long size) throws TException {
		// TODO Auto-generated method stub

	}

	/**
	 * Invoked on slave by master. Master force slave to replicate data
	 * (identified by @param fileId) from invoked to another slave (identified
	 * by @param slaveIP )
	 * */
	@Override
	public void replicate(int fileID, String slaveIP, long size) throws TException {

		final int fid = fileID;
		final String sip = slaveIP;
		final long fsize = size;
	
		Thread thread = new Thread(new Runnable() {
			int fileID = fid;
			String slaveIP = sip;
			long size = fsize;
	
			public void run() {
				
				log.debug(me.getIp() + ": Starting replication.");
				
				FilePartDescription filePartDescription = new FilePartDescription();
				filePartDescription.setFileId(fileID);
				filePartDescription.setOffset(0);
	
				ArrayList<FilePart> fileParts = new ArrayList<>();
	
				log.debug(me.getIp() + ": Acquiring main holding slave's operation handler.");
				
				try (DFSClosingClient ccClient = new DFSClosingClient(slaveIP, DFSProperties.getProperties().getStorageServerPort())) {
					Service.Client serviceClient = ccClient.getClient();
					log.debug(me.getIp() + ": Main holding slave's operation handler acquired.");
	
					FilePart filePart = null;
					long offset = 0;
					while (offset < size) {
						filePartDescription.setOffset(offset);
						log.debug(me.getIp() + ": Downloading fileid " + fileID + ", offset " + offset);
						filePart = serviceClient.getFileFromSlave(filePartDescription);
						log.debug("Got offset " + filePart.getOffset());
						if (filePart.getData().length == 0) {
							break;
						}
						offset += filePart.getData().length;
	
						fileParts.add(filePart);
					}
				} catch (Exception e) {
					log.error(me.getIp() + ": Caught an unidentified exception: " + e.getMessage());
					e.printStackTrace();
				}
	
				log.debug(me.getIp() + ": Writing the file to storage.");
				byte[] dataBuffer = new byte[0];
				for (FilePart filePart : fileParts) {
					dataBuffer = DFSArrayUtils.concat(dataBuffer, filePart.getData());
				}
				
				try {
					storageHandler.writeFile(fileID, dataBuffer); // TODO: the
																// entire file
																// goes through
																// the memory at
																// the moment!
				} catch (Exception e) {
					log.error("Caught an unexpected exception: " + e.getMessage());
					e.printStackTrace();
					return;
				}
	
				log.debug(me.getIp() + ": Updating master database.");
				try (DFSClosingClient ccClient = new DFSClosingClient(repository.getMasterServer().getIp(), DFSProperties.getProperties().getNamingServerPort())) {
					Service.Client serviceClient = ccClient.getClient();
					serviceClient.fileUploadSuccess(fileID, me.getIp());
				} catch (Exception e) {
					log.error("Caught an unidentified exception: " + e.getMessage());
					e.printStackTrace();
				}
	
			}
	
		});
		thread.start();
		return;
	}

	@Override
	public boolean isFileUsed(int fileID) throws TException {
		if (files.containsKey(fileID))
		{
		int counter = files.get(fileID);
		if (counter<DFSProperties.getProperties().getDeleteCounter())	
			{
			files.put(fileID, counter+1);
			return true;
			}
		}
		return false;
	}

	@Override
	public void removeFileSlave(int fileID) throws TException {
		storageHandler.deleteFile(fileID);
		files.remove(fileID);
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
		if (file == null) {
			// TODO: it's a temporary sign of 'file not found' case. Exception
			// in da future.
			return new GetFileParams(-1, "", 0);
		}

		Server slave = repository.getSlaveByFile(file);

		GetFileParams getFileParams = new GetFileParams();
		getFileParams.setFileId(file.getId());
		getFileParams.setSize(file.getSize());
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
		if (me.getRole() != ServerRole.MASTER) {
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
	
		String mainReplIP = null;
		Map<Server, Long> freeMemoryMap = new HashMap<Server, Long>();
	
		for (Server server : slaves) {
			long filesMemory = 0;
			for (File file : repository.getFilesOnSlave(server)) {
				filesMemory += file.getSize();
			}
	
			if (server.getMemory() - filesMemory <= size)
				continue;
	
			freeMemoryMap.put(server, filesMemory); // po miejscu zajętym
			// freeMemoryMap.put(server, server.getMemory() - filesMemory); //
			// po miejscu wolnym
		}
		
		if (freeMemoryMap.size() < 1) {
			throw new TException("There are no available storage servers to process your request.");
		}
	
		List<Server> keys = new ArrayList<Server>(freeMemoryMap.keySet());
		final Map sortmap = freeMemoryMap;
		Collections.sort(keys, new Comparator() {
			public int compare(Object left, Object right) {
				Long leftVal = (Long) sortmap.get((Server) left);
				Long rightVal = (Long) sortmap.get((Server) right);
	
				return leftVal.compareTo(rightVal);
			}
		});
	
		Iterator i = keys.iterator();
		long replDegree = 3L; // serverConfig.getReplDegree();
		PutFileParams putFileParams = new PutFileParams();
		putFileParams.setCanPut(true);
		
		try {
			Server mainRepl = (Server) i.next();
			
			log.debug(me.getIp() + ": Main replica being saved on " + mainRepl.getIp());
			
			mainReplIP = mainRepl.getIp();
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
		} catch (org.springframework.dao.DuplicateKeyException e) {
			log.error("File exists!");
			throw new TException("File with this name already exists!");
		}
	
		log.debug(me.getIp() + ": Remaining number of replicas: " + (keys.size() - 1));
		
		while (i.hasNext() && replDegree-- > 0) {
			Server secRepl = (Server) i.next();
			try (DFSTSocket dfstSocket = new DFSTSocket(secRepl.getIp(), DFSProperties.getProperties().getStorageServerPort())) {
				dfstSocket.open();
				TProtocol protocol = new TBinaryProtocol(dfstSocket);
				Service.Client serviceClient = new Service.Client(protocol);
				log.debug(me.getIp() + ": Replicating to " + secRepl.getIp());
				serviceClient.replicate(putFileParams.getFileId(), mainReplIP, size);
			} catch (Exception e) {
				log.error(me.getIp() + ": Replication failed on slave #" + secRepl.getId() + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
		log.debug(me.getIp() + ": returning from putFile");
		return putFileParams;
	}

	@Override
	public PutFileParams putFileFailure(String filepath, long size) throws TException {
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
		if (file == null) {
			/*
			 * TODO: result of false indicates unability to remove the file,
			 * maybe exception should be thrown with description of special
			 */
			return false;
		}

		file.setStatus(FileStatus.TO_DELETE);
		repository.updateFile(file);

		Thread thread = new Thread(new Runnable() {
			public void run() {
				List<Server> servers = repository.getSlavesByFile(file);
				boolean isFileUsed = true;
				while(isFileUsed)
				{
					isFileUsed = false;
					for (Server server : servers) {
						try (DFSClosingClient dfsclient = new DFSClosingClient(server.getIp(), DFSProperties.getProperties().getStorageServerPort())) {

							Service.Client client = dfsclient.getClient();
							isFileUsed = isFileUsed || client.isFileUsed(file.getId());
							} catch (TTransportException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
					};
					if (isFileUsed)
						try {
							TimeUnit.SECONDS.sleep(DFSProperties.getProperties().getIsFileUsedTimeout());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}	
				}
				for (Server server : servers) {
					try (DFSClosingClient dfsclient = new DFSClosingClient(server.getIp(), DFSProperties.getProperties().getStorageServerPort())) {

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
		log.debug(me.getIp() + ": sendFilePartToSlave: acquiring lock (#" + syncCounter++ + ")");
		try {
			// TODO: check if the filePart is from the expected client
			awaitingSlavesLock.lock();
			log.debug(me.getIp() + ": sendFilePartToSlave: acquired lock (#" + syncCounter++ + ")");
			
			byte[] dataBuffer = filePart.data.array();
			log.info("Got a file part of fileId " + filePart.fileId + " of size " + filePart.data.array().length + " bytes.");
			storageHandler.writeFile(filePart.fileId, dataBuffer);
			
			List<Condition> slavesAwaitingFileID = awaitingSlaves.get(filePart.getFileId());
			if (slavesAwaitingFileID != null) {
				for(Condition c : slavesAwaitingFileID) {
					c.signal();
					log.debug(me.getIp() + ": sendFilePartToSlave: signal (#" + syncCounter++ + ")");
				}
				slavesAwaitingFileID.clear(); // TODO: check whether this can have game breaking complications
			}
			awaitingSlavesLock.unlock();
			log.debug(me.getIp() + ": sendFilePartToSlave: released lock (#" + syncCounter++ + ")");
			
			return new FilePartDescription(filePart.fileId, filePart.offset + dataBuffer.length);

		} catch (Exception e) {
			log.error("Caught an exception while receiving the file from client: (" + e.getClass().getName() + ") " + e.getMessage());
		}

		return new FilePartDescription(-1, 0);
	}

	/**
	 * Dummy version sending whole file
	 * 
	 * @return FilePart - when whole file was sent data is set to [].
	 * */
	@Override
	public FilePart getFileFromSlave(FilePartDescription filePartDescription) throws TException {

		log.debug(me.getIp() + ": getFileFromSlave running.");
		
		//magic
		Condition fileReady = awaitingSlavesLock.newCondition();
		
		files.put(filePartDescription.fileId, 0);
		
		if (me.getRole() == rso.dfs.model.ServerRole.MASTER) { 
			// TODO: make sure you aren't requesting an offset beyond the scope of the file 
			return new FilePart(-1, 0, ByteBuffer.allocate(0));
		}

		log.debug(me.getIp() + ": Requested offset: " + filePartDescription.getOffset() + ", file size: " + storageHandler.getFileSize(filePartDescription.getFileId()));
		
		awaitingSlavesLock.lock();
		log.debug(me.getIp() + ": getFileFromSlave: acquired lock (#" + syncCounter++ + ")");
		while (filePartDescription.getOffset() >= storageHandler.getFileSize(filePartDescription.getFileId())) {
			log.debug(me.getIp() + ": Waiting for offset " + filePartDescription.getOffset() + " from file " + filePartDescription.getFileId());
			
			// TODO: handle timeouts
			// put cond var on a queue
			// sendFileParttoSlave will remove and signal them
			try {
				List<Condition> slavesAwaitingFileID = awaitingSlaves.get(filePartDescription.getFileId());
				if (slavesAwaitingFileID == null) {
					slavesAwaitingFileID = new ArrayList<Condition>();
					awaitingSlaves.put(filePartDescription.getFileId(), slavesAwaitingFileID);
				}
				slavesAwaitingFileID.add(fileReady);
				log.debug(me.getIp() + ": getFileFromSlave: await (#" + syncCounter++ + ")");
				fileReady.await();
			} catch (InterruptedException ie) {
				/* Just cycle through again, the thread will sleep if the file isn't ready. 
				 * TODO: Check when InterruptedException might screw things up. */
			}
		}
		log.debug(me.getIp() + ": getFileFromSlave: released lock (#" + syncCounter++ + ")");
		awaitingSlavesLock.unlock();
		
		log.debug(me.getIp() + ": File " + filePartDescription.getFileId() + ", offset: " + filePartDescription.getOffset() + " is ready.");

		byte[] dataToSend = new byte[0];
		
		try {
			dataToSend = storageHandler.readFile(filePartDescription.getFileId(), filePartDescription.getOffset());
			if (filePartDescription.getOffset() >= dataToSend.length) {
				FilePart filePart = new FilePart();
				filePart.setFileId(filePartDescription.getFileId());
				filePart.setData(new byte[0]);
				log.debug(me.getIp() + ": Something's wrong :|");
				return filePart;
			}
		} catch (Exception e) {
			log.error("Caught an unexpected exception: " + e.getMessage());
			e.printStackTrace();
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
		Server s = repository.getServerByIp(slaveIP);
		FileOnServer fos = new FileOnServer(fileID,s.getId(),0);

		repository.saveFileOnServer(fos);
		repository.updateFile(f);
	}

	@Override
	public void pingServer() throws TException {
		// This method does nothing.
	}

	public DFSRepository getRepository() {
		return repository;
	}

	public void setRepository(DFSRepository repository) {
		this.repository = repository;
	}

	public StorageHandler getStorageHandler() {
		return storageHandler;
	}

	public void setStorageHandler(StorageHandler storageHandler) {
		this.storageHandler = storageHandler;
	}

	
}
