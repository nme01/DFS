package rso.dfs.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransportException;
import org.joda.time.DateTime;
import org.joda.time.field.DecoratedDurationField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;

import rso.dfs.commons.DFSProperties;
import rso.dfs.event.DFSTask;
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
import rso.dfs.model.Query;
import rso.dfs.model.Server;
import rso.dfs.model.ServerRole;
import rso.dfs.model.dao.DFSRepository;
import rso.dfs.model.dao.psql.DFSDataSource;
import rso.dfs.model.dao.psql.DFSModelDAOImpl;
import rso.dfs.model.dao.psql.DFSRepositoryImpl;
import rso.dfs.server.storage.EmptyStorageHandler;
import rso.dfs.server.storage.StorageHandler;
import rso.dfs.server.utils.SelectStorageServers;
import rso.dfs.status.MasterChecker;
import rso.dfs.status.ServerStatusCheckerService;
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
	 * Core status checking services.
	 */
	private ServerStatusCheckerService serverCheckerService = null;
	private MasterChecker masterCheckerService = null;
	
	/**
	 * Contains statuses which files where used;
	 * key -> fileId;
	 * value -> value needed to double timeout delete
	 * */
	private HashMap<Integer, Integer> files = new HashMap<>();	
	
	public ServerHandler(Server me, CoreStatus cs, StorageHandler storageHandler, DFSRepository repository) {
		this.me = me;
		this.storageHandler = storageHandler;
		this.repository = repository;
		this.coreStatus = cs; // FIXME:
																		// temp
																		// just
																		// not
																		// to be
																		// null
		if(me.getRole() == ServerRole.MASTER)
		{
			log.debug("Starting ServerStatusCheckerService form Master");
			serverCheckerService = new ServerStatusCheckerService(this);
			serverCheckerService.runService();
		}
	}

	@Override
	public List<String> listFileNames() throws TException {
		log.debug("New LS request.");
		List<File> files = repository.getAllFiles();
		
		if(DFSProperties.getProperties().isDebug())
		{
			List<String> fileNames = new ArrayList<>();
			
			fileNames.add("All files list: ");
			for (File f : files) {
				String suffix = ""; 
				if(f.getStatus().equals(FileStatus.TO_DELETE))
				{
					suffix = " (to delete)"; 
				}
				else if (f.getStatus().equals(FileStatus.UPLOAD))
				{
					suffix = " (uploaded)";
				}
				
				fileNames.add(f.getId() + ": " + f.getName() + suffix);
			}
			
			for(Server s: repository.getSlaves())
			{
				fileNames.add("Files on slave " + s.getIp() + ":");
				for(File f: repository.getFilesOnSlave(s))
				{
					fileNames.add(f.getId() + ": " + f.getName());
				}
			}
			return fileNames;
		}
		
		List<String> fileNames = new ArrayList<>();
		for (File f : files) {
			fileNames.add(f.getName());
		}
		
		return fileNames;
	}

	@Override
	public SystemStatus getStatus() throws TException {
		log.debug("Got getStatus request");
		SystemStatus ss = new SystemStatus();
		ss.setFilesNumber(repository.getAllFiles().size());		
		Server master = repository.getMasterServer();
		
		log.debug("Master server is " + master);
		
		ss.addToServersStatuses(
				new ServerStatus(
						ServerType.Master,master.getFilesNumber(), 
						master.getFreeMemory(), 
						master.getMemory()-master.getFreeMemory(), 
						master.getIp()));
		
		for (Server server : repository.getShadows())
			ss.addToServersStatuses(
					new ServerStatus(
							ServerType.Shadow, 
							server.getFilesNumber(),
							server.getFreeMemory(), 
							server.getMemory()-server.getFreeMemory(), 
							server.getIp()));
		for (Server server : repository.getSlaves())
			ss.addToServersStatuses(
					new ServerStatus(
							ServerType.Slave, 
							server.getFilesNumber(), 
							server.getFreeMemory(), 
							server.getMemory()-server.getFreeMemory(), 
							server.getIp()));
		return ss;
	}

	/**
	 * TODO: Make some shadows
	 */
	@Override
	public CoreStatus registerSlave(NewSlaveRequest req) throws TException {
		log.info("Got registerSlave request! Slave IP: " + req.getSlaveIP() + "; list of files {" + req.getFileIds() + "}");
		
		Server server = null;
	 	try{
	 		server = repository.getServerByIp(req.getSlaveIP());
	 		server.setRole(ServerRole.SLAVE);
	 		server.setLastConnection(new DateTime());
	 		repository.updateServer(server);
		}
		catch(org.springframework.dao.EmptyResultDataAccessException e)
		{
			log.info("Server not found in repository");
		}
			 		 	
		
	 	List<Server> shadows = null;
	 	try
	 	{
	 		 shadows= repository.getShadows();
	 	}
	 	catch(Exception e)
	 	{
	 		log.error(e.getMessage());
	 	}
	 		 	
		if(server == null)
		{
			server = new Server();
	
			server.setIp(req.getSlaveIP());
			server.setMemory(DFSProperties.getProperties().getStorageServerMemory()); 
			server.setRole(ServerRole.SLAVE);
			server.setLastConnection(new DateTime());
			try {
				log.debug("Saving the server in the repository.");
				repository.saveServer(server);
			} catch (Exception e) {
				log.error("Saving the server in the repository failed: " + e.getMessage() + ".");
			}
		}
		else
		{
			if(server.getRole() == ServerRole.DOWN)
			{
				log.info("Inactive server " + server + " registered as active again.");
			}
			server.setRole(ServerRole.SLAVE);
		}
		



		List<Integer> ids = req.getFileIds();
		List<Integer> idsToRemove = new LinkedList<>();
		for (int fileId : ids) {
			
			File fileById = null;
			try{
				fileById = repository.getFileById(fileId);
			}
			catch(EmptyResultDataAccessException e)
			{
				//it's not unusual..
			}
			
			
			if (fileById != null) {
				log.debug("FileID: fileId" + " " + fileById.getName() 
						+ " will be added as FileOnServer " + server.getIp());
				FileOnServer fileOnServer = new FileOnServer();
				fileOnServer.setFileId(fileId);
				fileOnServer.setServerId(server.getId());
				fileOnServer.setPriority(0l);
				log.debug("registerSlave: registering fileid " + fileId);
				repository.saveFileOnServer(fileOnServer);
				idsToRemove.add(fileId);
			}
		}
		
		ids.removeAll(idsToRemove);
		if(ids.size() != 0)
		{
			log.debug(ids.size() + " files to remove from server");
		}
		
		//edge case - not visible from master, visible for client - removeFileSlave will disconnect the client...
		try (DFSClosingClient cclient = new DFSClosingClient(req.getSlaveIP(), DFSProperties.getProperties().getStorageServerPort())) {
			Client client = cclient.getClient();
			for (Integer fileId : ids) {
				client.removeFileSlave(fileId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Now that we have a clean storage unit, let's fill it with files with insufficient replication factor.
		List<File> allFiles = repository.getAllFiles();
		for (File f: allFiles) {
			List<Server> slavesWithF = repository.getSlavesByFile(f); 
			if (slavesWithF.size() < DFSProperties.getProperties().getReplicationFactor() && !slavesWithF.contains(server)) {
		
				//Replicate!
				//1. Pick a slave,
				//2. Take a copy.
				final File replFile = f;
				final NewSlaveRequest r = req;
				
				Thread thread = new Thread(new Runnable() {
					File f = replFile;
					NewSlaveRequest req = r;
					
					public void run() {
						List<Server> slaves = repository.getSlavesByFile(f);
						Server mainRepl = slaves.get(0);
						repository.saveFileOnServer(new FileOnServer(f.getId(), repository.getServerByIp(req.getSlaveIP()).getId(), -(repository.getFileOnServer(slaves.get(slaves.size() - 1).getId(), f.getId()).getPriority() + 1)));
						try (DFSClosingClient cclient = new DFSClosingClient(req.getSlaveIP(), DFSProperties.getProperties().getStorageServerPort())) {
							Client client = cclient.getClient();
							client.replicate(f.getId(), mainRepl.getIp(), f.getSize());
						} catch (Exception e) {
							//Lost connection to the slave... Unfortunate?
							//Not sure if return here is the correct way, but
							//walking around loops is even less productive.
							repository.deleteFileOnServer(new FileOnServer(f.getId(), repository.getServerByIp(req.getSlaveIP()).getId(), 0));
						}
					}
				});
				thread.start();
				
			}
		}

		debugSleep();
		return coreStatus;
	}

	@Override
	public void updateCoreStatus(CoreStatus status) throws TException {
		log.debug(
				me.getIp() + " coreStatus set to: master address: " 
				+ coreStatus.getMasterAddress() 
				+ Arrays.toString(coreStatus.getShadowsAddresses().toArray()));
		coreStatus = status;
	}
	
	/**
	 * Updates slaves core status
	 * @throws TException
	 */
	public void massiveUpdateCoreStatus() throws TException {
		//get core status from DB
		Server masterServer = repository.getMasterServer();
		List<Server> shadows = repository.getShadows();
		List<String> shadowsAddresses = new ArrayList<String>();
		for(Server shadow: shadows)
		{
			shadowsAddresses.add(shadow.getIp());
		}
		coreStatus = new CoreStatus(masterServer.getIp(), shadowsAddresses);
		
		for(Server shadow: shadows)
		{
			try (DFSClosingClient cclient = new DFSClosingClient(
					shadow.getIp(), DFSProperties.getProperties().getStorageServerPort())) {
				Client client = cclient.getClient();
				client.updateCoreStatus(coreStatus);
			} catch (Exception e) {
				log.debug("Failed to update corestatus of shadow " + shadow.getIp());
			}
		}
		
		List<Server> slaves = repository.getSlaves();
		for(Server slave: slaves)
		{
			try (DFSClosingClient cclient = new DFSClosingClient(
					slave.getIp(), DFSProperties.getProperties().getStorageServerPort())) {
				Client client = cclient.getClient();
				client.updateCoreStatus(coreStatus);
			} catch (Exception e) {
				log.debug("Failed to update corestatus of slave " + slave.getIp());
			}
		}
		
	}

	@Override
	public void becomeShadow(CoreStatus status) throws TException {
		log.debug(me.getIp() + ": becoming shadow...");
		me.setRole(ServerRole.SHADOW);
		masterCheckerService = new MasterChecker(this);
		masterCheckerService.runService();
		log.debug(me.getIp() + ": I am shadow now");
	}

	@Override
	public CoreStatus getCoreStatus() throws TException {
		log.debug(
				me.getIp() + " returned coreStatus: master address: " 
				+ coreStatus.getMasterAddress() 
				+ Arrays.toString(coreStatus.getShadowsAddresses().toArray()));
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
				
				log.debug(me.getIp() + ": Starting replication (" + fid + "," + sip + "," + fsize + ").");
				
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
						log.debug("Got offset " + filePart.getOffset() + " and a buffer of length " + filePart.getData().length + " bytes.");
						if (filePart.getData().length == 0) {
							break;
						}
	
						try {
							storageHandler.writeFile(fileID, (int)offset, filePart.getData());
						} catch (Exception e) {
							log.error("Caught an unexpected exception: " + e.getMessage());
							e.printStackTrace();
							return;
						}
						
						offset += filePart.getData().length;
					}
				} catch (TTransportException tte) {
					try (DFSClosingClient ccClient1 = new DFSClosingClient(coreStatus.getMasterAddress(), DFSProperties.getProperties().getNamingServerPort())) {
						log.info(me.getIp() + ": Replication failed from main replication node " + slaveIP + ".");
						Service.Client serviceClient = ccClient1.getClient();
						slaveIP = serviceClient.replicationFailure(fileID, slaveIP);
						if (slaveIP == "0.0.0.0") {
							storageHandler.deleteFile(fileID);
							return;
						}
					} catch (TTransportException tte1) {
						log.error("Replication failed: could not connect to master server!");
						return;
					} catch (TException te1) {
						log.error("Replication failed: replicationFailure called has thrown an exception (shouldn't happen!).");
						return;
					} catch (Exception e1) {
						log.error("Replication failed: unidentified exception has been thrown.");
						e1.printStackTrace();
						return;
					}
				} catch (Exception e) {
					log.error(me.getIp() + ": Caught an unidentified exception: " + e.getMessage());
					e.printStackTrace();
					storageHandler.deleteFile(fileID);
					return;
				}			
	
				log.debug(me.getIp() + ": Updating master database.");
				try (DFSClosingClient ccClient = new DFSClosingClient(coreStatus.getMasterAddress(), DFSProperties.getProperties().getNamingServerPort())) {
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
	// TODO: add replFailure handling to the method above and implement an appropriate way to
	// handle being selected as a new primary node
	public String replicationFailure(int fileID, String slaveIP) {
		
		/**
		 * So who can use this method?
		 * 	- Secondary replication nodes to inform the master that the primary node has failed;
		 * 	- ...?
		 * And what do we have to do here as master?
		 * 	- Wait (with a timeout) until putFileFailure selects a new primary node and then return
		 * 	  with an updated address.
		 */
		
		File failedFile = new File();
		failedFile.setId(fileID);
		int timeoutCounter = 0;
		Server newMainRepl;
		while(true) {
			try {
				do {
					newMainRepl = null;
					Thread.sleep(1000);
					List<Server> serverList = repository.getSlavesByFile(failedFile);
					List<FileOnServer> fosList = new ArrayList<FileOnServer>();
					for (Server s: serverList) {
						if (repository.getFileOnServer(s.getId(), fileID).getPriority() < 0) {
							newMainRepl = s;
							break;
						}
					}
					timeoutCounter++;
				} while (newMainRepl == null && timeoutCounter < 5);
				break;
			} catch (InterruptedException ie) {}
		}
		
		if (newMainRepl != null) return newMainRepl.getIp(); 
		
		FileOnServer fos = new FileOnServer();
		fos.setFileId(fileID);
		fos.setServerId(repository.getServerByIp(slaveIP).getId());
		repository.deleteFileOnServer(fos);
		return "0.0.0.0";
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

		debugSleep();
		return getFileParams;
	}

	@Override
	public GetFileParams getFileFailure(GetFileParams gfp) throws TException {
		// redirect the client to another slave
		
		File f = new File(); f.setId(gfp.getFileId());
		List<Server> slaves = repository.getSlavesByFile(f);
		List<FileOnServer> slavesWithFile = new ArrayList<FileOnServer>();
		for (Server s: slaves) {
			slavesWithFile.add(repository.getFileOnServer(s.getId(), gfp.getFileId()));
		}
		
		int ind = (slavesWithFile.lastIndexOf(new FileOnServer(gfp.getFileId(), repository.getServerByIp(gfp.getSlaveIp()).getId(), 0)) + 1);
		if (ind >= slavesWithFile.size())
			gfp.setSlaveIp("0.0.0.0");
		else
			gfp.setSlaveIp(repository.getServerById(slavesWithFile.get(ind).getServerId()).getIp());
		
		return gfp;
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
		log.info("Master has received a file upload request for file " + filepath);
		String mainReplIP = null;
		
		List<Server> keys;
		try { 
			keys = SelectStorageServers.getListOfBestStorageServers(repository, size);
		} catch (TException e) {
			log.error("No available storage servers registered on the master.");
			throw e;
		}
	
		Iterator i = keys.iterator();
		long replDegree = DFSProperties.getProperties().getReplicationFactor();
		long totalReplDegree = replDegree;
		PutFileParams putFileParams = new PutFileParams();
		putFileParams.setCanPut(true);
		
		boolean succeeded = false;
		while (!succeeded && i.hasNext()) {
			try {
				Server mainRepl = (Server) i.next();
				
				log.debug(me.getIp() + ": Main replica being saved on " + mainRepl.getIp());
				
				mainReplIP = mainRepl.getIp();
				File file = new File();
				file.setName(filepath);
				file.setSize(size);
				file.setStatus(FileStatus.UPLOAD);
				Integer fileId = repository.saveFile(file);
				
		
				repository.saveFileOnServer(new FileOnServer(fileId, mainRepl.getId(), -1));
		
				putFileParams.setCanPut(true);
				putFileParams.setSlaveIp(mainRepl.getIp());
				putFileParams.setFileId(fileId);
				putFileParams.setSize(size);
				
				try (DFSClosingClient dfscClient = new DFSClosingClient(mainRepl.getIp(), 
						DFSProperties.getProperties().getStorageServerPort())) {
					Service.Client serviceClient = dfscClient.getClient();
					serviceClient.sendFileToSlaveRequest(fileId);
					succeeded = true;
				} catch (TException te) {
					// Cycle through and select another primary replication node
				} catch (Exception e) {
					// Same.
				}
			} catch (org.springframework.dao.DuplicateKeyException e) {
				log.error("File exists!");
				throw new TException("File with this name already exists!");
			}
		}
	
		log.debug(me.getIp() + ": Remaining number of replicas: " + (replDegree - 1));
		
		
		// TODO: what to do when there is not enough slaves to replicate
		while (i.hasNext() && --replDegree > 0) {
			Server secRepl = (Server) i.next();
			if (secRepl.getRole() == ServerRole.DOWN) continue;
			
			try (DFSClosingClient dfscClient = new DFSClosingClient(secRepl.getIp(), 
					DFSProperties.getProperties().getStorageServerPort())) {
				Service.Client serviceClient = dfscClient.getClient();
				log.debug(me.getIp() + ": Replicating to " + secRepl.getIp());
				serviceClient.replicate(putFileParams.getFileId(), mainReplIP, size);
				repository.saveFileOnServer(new FileOnServer(putFileParams.getFileId(), secRepl.getId(), -(totalReplDegree - replDegree + 1)));
			} catch (Exception e) {
				log.error(me.getIp() + ": Replication failed on slave #" + secRepl.getId() + ": " + e.getMessage());
				replDegree++;
				try (DFSClosingClient dfscClient = new DFSClosingClient(secRepl.getIp(), 
						DFSProperties.getProperties().getStorageServerPort())) {
					Service.Client serviceClient = dfscClient.getClient();
					serviceClient.pingServer();
				} catch (TException te) {
					secRepl.setRole(ServerRole.DOWN);
					repository.updateServer(secRepl);
				} catch (Exception e1) {
					secRepl.setRole(ServerRole.DOWN);
					repository.updateServer(secRepl);
				}
			}
		}
		log.debug(me.getIp() + ": returning from putFile");

		debugSleep();
		return putFileParams;
	}

	/**
	 * Invoked on master by client. Report a failure that occurred during uploading of a file.
	 * 
	 * @param filepath
	 * @param size
	 * @return PutFileParams
	 */

	@Override
	public PutFileParams putFileFailure(PutFileParams pfp) throws TException {
		/**
		 * Master has to:
		 * 	- ping the slave;
		 * 		- if down, 'down' the slave;
		 * 		- if up, leave it there (probable transient problems on the link between client and slave);
		 * 	- pause replication (done by replicationFailure);
		 * 	- select a new main replicating slave (done by replicationFailure);
		 * 	- redirect the rest of slaves to the new main replicating slave (done by replicationFailure);
		 * 	- send updated PutFileParams back to the client.
		 * Optimistically, replication won't have to begin from the very beginning
		 * of a file (nor will client's performPut), but realistically, we gotta start there.
		 */
		
		try (DFSClosingClient dfscClient = new DFSClosingClient(pfp.getSlaveIp(), 
				DFSProperties.getProperties().getStorageServerPort())) {
			Service.Client serviceClient = dfscClient.getClient();
			serviceClient.pingServer();
		} catch (Exception e) {

			// No response
			// Mark the slave as inactive (will anyone drop him from the database?)
			Server mainRepl = repository.getServerByIp(pfp.getSlaveIp());
			mainRepl.setRole(ServerRole.DOWN);
			repository.updateServer(mainRepl);
			
			// Drop the link
			repository.deleteFileOnServer(new FileOnServer(pfp.getFileId(), repository.getServerByIp(pfp.getSlaveIp()).getId(), 0));
			
			// Select a new primary node from the current replication pool
			// - get a list of secondaries
			// - pick one!
			// - set its priority to -1
			// - set the returning putFileParams' data
			// Since this method is called by the client, there's no possibility the file has been completed
			// on any node.
			
			File tmp = new File();
			tmp.setId(pfp.getFileId());
			List<Server> slavesWithFile = repository.getSlavesByFile(tmp);
			List<FileOnServer> fosByPrio = new ArrayList<FileOnServer>();
			FileOnServer newMainRepl = repository.getFileOnServer(slavesWithFile.get(0).getId(), pfp.getFileId());
			for (Server s: slavesWithFile) {
				FileOnServer x = repository.getFileOnServer(s.getId(), pfp.getFileId()); 
				if (x.getPriority() > newMainRepl.getPriority())
					newMainRepl = x;
			}

			newMainRepl.setPriority(-1L);
			repository.updateFileOnServer(newMainRepl);
			for (Server s: slavesWithFile) {
				if (s.getId() == newMainRepl.getServerId()) {
					pfp.setSlaveIp(s.getIp());
					break;
				}
			}
			
			// Select a new secondary node
			try {
				List<Server> slavesWithoutFile = SelectStorageServers.getListOfBestStorageServers(repository, (int)repository.getFileById(pfp.getFileId()).getSize());
				slavesWithoutFile.removeAll(slavesWithFile);
				
				Iterator i = slavesWithoutFile.iterator();
				while (i.hasNext()) {
					Server nextReplSlave = (Server)i.next();
					try (DFSClosingClient dfscClient = new DFSClosingClient(nextReplSlave.getIp(), 
							DFSProperties.getProperties().getStorageServerPort())) {
						Service.Client serviceClient = dfscClient.getClient();
						serviceClient.replicate(pfp.getFileId(), pfp.getSlaveIp(), pfp.getSize());
						repository.saveFileOnServer(new FileOnServer(pfp.getFileId(), nextReplSlave.getId(), -(slavesWithFile.size() + 1)));
						return pfp;
					} catch (Exception e1) {
						//Try again.
					}
				}
			} catch (TException te) {
				log.error("This shouldn't happen! The repository reports no available servers in the service!");
				throw new TException("No available servers");
			}
			

		}

		// Got a response
		File tmp = new File();
		tmp.setId(pfp.getFileId());
		List<Server> slavesWithFile = repository.getSlavesByFile(tmp);
		List<FileOnServer> fosByPrio = new ArrayList<FileOnServer>();
		FileOnServer newMainRepl = repository.getFileOnServer(slavesWithFile.get(0).getId(), pfp.getFileId());
		for (Server s: slavesWithFile) {
			FileOnServer x = repository.getFileOnServer(s.getId(), pfp.getFileId()); 
			if (x.getPriority() > newMainRepl.getPriority() && x.getPriority() != -1)
				newMainRepl = x;
		}
		long tmpPrio = newMainRepl.getPriority();
		newMainRepl.setPriority(-1L);
		repository.updateFileOnServer(newMainRepl);
		repository.updateFileOnServer(new FileOnServer(pfp.getFileId(), repository.getServerByIp(pfp.getSlaveIp()).getId(), tmpPrio));
		for (Server s: slavesWithFile) {
			if (s.getId() == newMainRepl.getServerId()) {
				pfp.setSlaveIp(s.getIp());
				break;
			}
		}
		return pfp;
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

		file.setName(null);
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
		debugSleep();
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
		log.debug("sFPTS: filePart.getData().length: " + filePart.getData().length);
		if (filePart.getData().length == 0) {
			try (DFSClosingClient dfsclient = new DFSClosingClient(coreStatus.getMasterAddress(), DFSProperties.getProperties().getNamingServerPort())) {
				log.info("sendFilePartToSlave: upload completed. Updating master database.");
				Service.Client client = dfsclient.getClient();
				client.fileUploadSuccess(filePart.getFileId(), me.getIp());
				return new FilePartDescription(filePart.getFileId(), filePart.getOffset());
			} catch (TException te) {
				log.error("sendFilePartToSlave: Critical error: could not modify the state of the uploaded file.");
				throw new TException("Uploading file failed: could not modify the state of the uploaded file in the rmeote repository.");
			} catch (Exception e) {
				log.error("sendFilePartToSlave: Critical error: lost connection to master.");
				throw new TException("Uploading file failed: storage server lost connection with the naming server.");
			}
		}
		
		if (filePart.getOffset() > storageHandler.getFileSize(filePart.getFileId()))
			return new FilePartDescription(filePart.getFileId(), storageHandler.getFileSize(filePart.getFileId()));
		
		log.debug(me.getIp() + ": sendFilePartToSlave: acquiring lock (#" + syncCounter++ + ")");
		try {
			// TODO: check if the filePart is from the expected client
			awaitingSlavesLock.lock();
			log.debug(me.getIp() + ": sendFilePartToSlave: acquired lock (#" + syncCounter++ + ")");
			
			byte[] dataBuffer = filePart.data.array();
			log.info("Got a file part of fileId " + filePart.fileId + " of size " + filePart.data.array().length + " bytes.");
			storageHandler.writeFile(filePart.getFileId(), (int)filePart.getOffset(), dataBuffer);
			log.info("Written to storage.");
			
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
			
			log.debug(me.getIp() + ": sendFilePartToSlave: returning with FPD(" + filePart.fileId + "," + filePart.offset + "), dataBuffer.length: " + dataBuffer.length); 
			return new FilePartDescription(filePart.fileId, filePart.offset + dataBuffer.length);

		} catch (Exception e) {
			log.error("sendFilePartToSlave: Caught an exception while receiving the file from client: (" + e.getClass().getName() + ") " + e.getMessage());
			awaitingSlavesLock.unlock();
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
		/*if (filePartDescription.getOffset() >= repository.getFileById(filePartDescription.getFileId()).getSize()) {
			FilePart filePart = new FilePart();
			filePart.setFileId(-2);
			filePart.setData(new byte[0]);
			log.debug(me.getIp() + ": Client requested an offset beyond the file's scope.");
			return filePart;
		}*/

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
		} catch (Exception e) {
			log.error("Caught an unexpected exception: " + e.getMessage());
			e.printStackTrace();
			FilePart filePart = new FilePart();
			filePart.setFileId(-1);
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
		
		log.debug("fileUploadSuccess: fileID:" + fileID + ";slaveIP: " + slaveIP); 
		
		rso.dfs.model.File f = repository.getFileById(fileID);

		f.setStatus(FileStatus.HELD);
		Server s = repository.getServerByIp(slaveIP);
		if(s == null)
		{
			log.error("FileUploadSuccess - server with that IP is not in repository: " + slaveIP);
			return;
		}
		FileOnServer fos = repository.getFileOnServer(s.getId(), fileID);
		log.debug("fileUploadSuccess: fos.sid: " + fos.getServerId() + ", fos.fid: " + fos.getFileId() + ", fos.priority: " + Math.abs(fos.getPriority()));
		fos.setPriority(Math.abs(fos.getPriority()));
		repository.updateFileOnServer(fos); 
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

	public void registerToMaster()
	{
		log.debug("Registering slave server");
	
		// possible warning: thrift method executed locally
		String masterIP = coreStatus.getMasterAddress();
	
		// register new slave
	
		try (DFSClosingClient cclient = new DFSClosingClient(masterIP, DFSProperties.getProperties().getNamingServerPort())) {
			Client client = cclient.getClient();
	
			//list files!
			String directory = DFSProperties.getProperties().getDirectory();
	
			ArrayList<Integer> fileList = new ArrayList<Integer>();
			ArrayList<Long> fileSizes = new ArrayList<Long>();
			
			java.io.File[] files = new java.io.File(directory).listFiles();
	
			for (java.io.File file : files) {
			    if (file.isFile()) {
			    	Integer nameasnumber = null;
			    	try
			    	{
			    		nameasnumber = Integer.parseInt(file.getName());
			    	}
			    	catch (NumberFormatException e)
			    	{
			    		//not an integer
			    		log.debug("In working directory \"" + directory + 
			    				"\" there is a file with name not parsable to Integer:" 
			    				+ file.getName());
			    		continue;
			    	}
			    	
			    	fileList.add(nameasnumber);
		    		fileSizes.add(file.length());
			    }
			}
			
			String slaveIP = me.getIp();
			NewSlaveRequest request = new NewSlaveRequest(slaveIP, fileList, fileSizes);
			log.debug("Slave will register to master");
			CoreStatus coreStatus = client.registerSlave(request);
	
			// possible warning: thrift method executed locally
			updateCoreStatus(coreStatus);
			
			log.debug("I, humble slave with IP address " + me.getIp() + ", registered to master at " + masterIP);
		} catch (TTransportException tte) {
			log.error("Unable to connect to the requested server. (" + tte.getMessage() + ")");
			System.exit(1);
		} catch (TException te) {
			log.error("Caught an exception from the remote end during slave registration: " + te.getMessage());
			System.exit(1);
		} catch (Exception e) {
			log.error("Unable to connect to the requested server. (" + e.getMessage() + ")");
			System.exit(1);
		}
		debugSleep();
	}
	
	/**
	 * With this request master makes slave register again.
	 */
	@Override
	public void forceRegister(CoreStatus cs) throws TException {
		log.debug(me.getIp() + ": Got forceRegister request for master" + cs.getMasterAddress());
		updateCoreStatus(cs);
		registerToMaster();
	}
	
	public void makeShadow(String slaveIpAddress){
		final String slaveIp = slaveIpAddress;
		Server serverByIp = repository.getServerByIp(slaveIpAddress);
		serverByIp.setRole(ServerRole.SHADOW);
		repository.updateServer(serverByIp);
		
		Thread thread = new Thread(new Runnable() {
			public void run(){
				log.info("Order " + slaveIp + " to become Shadow");
				// make dump
				String dbPass = DFSProperties.getProperties().getDbpassword();
				String dbName = DFSProperties.getProperties().getDbname();
				String dbUser = DFSProperties.getProperties().getDbuser();
				
				DFSModelDAOImpl slaveDao = new DFSModelDAOImpl(new DFSDataSource(slaveIp));
				slaveDao.cleanDB();
				
				String cmd = "PGPASSWORD=" + dbPass + " pg_dump -h 127.0.0.1 -U " + dbUser + 
						" | PGPASSWORD=" + dbPass + " psql -h " + slaveIp + " -d " + dbName + " -U " + dbUser;
				try (DFSClosingClient dfscClient = new DFSClosingClient(slaveIp, DFSProperties.getProperties().getStorageServerPort())) {
					Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd}).waitFor();
					log.debug("... Database dumped.");
					DFSRepositoryImpl.dbSemaphore.acquire(DFSRepositoryImpl.MAX_THREADS);
					
					String seqnames[] = {"servers_id_seq", "files_id_seq", "log_id_sec"};
					for(String seq: seqnames) {
						slaveDao.setSeqVals(seq, repository.getDAO().fetchSeqVals(seq));
					}
					
					List<Query> queries = slaveDao.fetchAllQueries();
					Long actualVersion = queries.get(queries.size()-1).getId();
					List<Query> queriesToUpdate = repository.getQueriesAfter(actualVersion);
					for(Query sql : queriesToUpdate){
						slaveDao.executeQuery(sql.getSql());
					}
					Server slave = repository.getServerByIp(slaveIp);
					
					repository.addShadow(slave);
					
					Service.Client serviceClient = dfscClient.getClient();
					
					log.debug("Executing becomeShadow on " + slaveIp + ".");
					serviceClient.becomeShadow(coreStatus);
					
					log.debug("Performing system-wide core status update.");
					massiveUpdateCoreStatus();
					
				} catch (TTransportException tte) {
					log.error("Unable to connect to the requested storage server. (" + tte.getMessage() + ")");
					DFSRepositoryImpl.dbSemaphore.release(DFSRepositoryImpl.MAX_THREADS);
					return;
				} catch (TException te) {
					log.error("Caught an exception from the remote end during shadow conversion: " + te.getMessage());
					DFSRepositoryImpl.dbSemaphore.release(DFSRepositoryImpl.MAX_THREADS);
					return;
				} catch (IOException ioe) {
					log.error("Unable to dump the database. (" + ioe.getMessage() + ")");
					DFSRepositoryImpl.dbSemaphore.release(DFSRepositoryImpl.MAX_THREADS);
					return;
				} catch (InterruptedException ie) {
					//TODO: idk
				} catch (Exception e) {
					log.error("Transient error occured on the socket: " + e.getMessage());
					DFSRepositoryImpl.dbSemaphore.release(DFSRepositoryImpl.MAX_THREADS);
					return;
				} finally {
					DFSRepositoryImpl.dbSemaphore.release(DFSRepositoryImpl.MAX_THREADS);
				}
			}
		});
		thread.start();
	}
	
	void debugSleep()
	{
		try {
			TimeUnit.SECONDS.sleep(DFSProperties.getProperties().getDebugWaitTime());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Server getServer() {
		return me;
	}

	public void setServer(Server me) {
		this.me = me;
	}

	public void becomeMaster() {
		log.debug("Me, " + me + ", becoming a master.");
		this.me.setRole(ServerRole.MASTER);
		
		masterCheckerService.stopService();
		
		log.debug(me.getIp() + ", new master, getting a repo...");
		//first, I'll get myself a repo (maybe inform first?)
		BlockingQueue<DFSTask> blockingQueue = new LinkedBlockingQueue<>();
		repository = new DFSRepositoryImpl(me, blockingQueue);
		Server masterServer = repository.getMasterServer();
		masterServer.setRole(ServerRole.DOWN);
		repository.updateServer(masterServer);
		log.debug(me.getIp() + ", new master, got a repo.");
		
		repository.updateServer(me);
		storageHandler = new EmptyStorageHandler(); //just not to do serving any more

		log.debug(me.getIp() + ", new master, updating core status of followers..");
		try {
			DFSRepositoryImpl.dbSemaphore.acquire(DFSRepositoryImpl.MAX_THREADS);
		
		try {
			massiveUpdateCoreStatus();
		} catch (TException e) {
			e.printStackTrace();
		}
		
		List<Server> shadows = repository.getShadows();
		
		log.debug(me.getIp() + ", new master, will tell shadows what to do.");
		for(Server shadow: shadows)
		{
			String shadowIP = shadow.getIp();
			
			DFSModelDAOImpl shadowDAO = new DFSModelDAOImpl(new DFSDataSource(shadowIP));
			
			try (DFSClosingClient dfscClient = new DFSClosingClient(shadowIP, DFSProperties.getProperties().getStorageServerPort())) {
							
				List<Query> queries = shadowDAO.fetchAllQueries();
				Long actualVersion = queries.get(queries.size()-1).getId();
				List<Query> queriesToUpdate = repository.getQueriesAfter(actualVersion);
				for(Query sql : queriesToUpdate){
					shadowDAO.executeQuery(sql.getSql());
				}
				repository.addShadow(shadow);
			} catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}
		log.debug(me.getIp() + ", new master, told shadows what to do.");

		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		finally
		{
			DFSRepositoryImpl.dbSemaphore.release(DFSRepositoryImpl.MAX_THREADS);
		}
		
		serverCheckerService = new ServerStatusCheckerService(this);
		serverCheckerService.runService();
		log.debug(me.getIp() + ", new master, rund followers checking service.");
	}
	
}
