package rso.dfs.status;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jline.internal.Log;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rso.dfs.commons.DFSProperties;
import rso.dfs.generated.Service.Client;
import rso.dfs.model.File;
import rso.dfs.model.FileOnServer;
import rso.dfs.model.Server;
import rso.dfs.model.ServerRole;
import rso.dfs.model.dao.DFSRepository;
import rso.dfs.model.dao.psql.DFSRepositoryImpl;
import rso.dfs.server.ServerHandler;
import rso.dfs.server.storage.FileStorageHandler;
import rso.dfs.server.utils.SelectStorageServers;
import rso.dfs.utils.DFSClosingClient;

/**
 * This service will be responsible for checking Slaves and Shadows health. 
 * In case of shadow failure, it will elect new shadow.
 * In case of slave failure, it will order some slaves to replicate. 
 * @author sven
 */
public class ServerStatusCheckerService {
	
	private DFSRepository repository;
	private DateTime lastCheck;
	private Server me;
	private final static Logger log = LoggerFactory.getLogger(ServerStatusCheckerService.class);
	ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
	private ServerHandler serverHandler;
	private LinkedList<InfrastructureModificationCommand> taskQueue;
	
	public ServerStatusCheckerService(ServerHandler serverHandler) {
		this.serverHandler = serverHandler;
		this.repository = serverHandler.getRepository();
		this.lastCheck = new DateTime();
		this.taskQueue = new LinkedList<>();
	}
	
	public void runService()
	{
		exec.scheduleWithFixedDelay(new Runnable() {
		  @Override
		  public void run() {
			  log.debug("Run checker service");
			  checkAllShadowsAndSlaves();
		  }
		}, DFSProperties.getProperties().getPingEvery(), 
		   DFSProperties.getProperties().getPingEvery()/2, //TODO: quite arbitrary.. 
		   TimeUnit.MILLISECONDS);
	}
	
	public void stopService()
	{
		exec.shutdown(); //TODO: check if it works, maybe shoutdownNOW?
	}
	

	private void checkAllShadowsAndSlaves()
	{
		lastCheck = new DateTime();
		
		/**TODO: check input queue from master main thread**/
		
		/** TODO: algorithm? For now - if it's not available, delete.
		 * Maybe: if no answer in 60 seconds or another longer time than ~"now"?
		 */
		
		int connectedServers = 0;
		
		//log.debug("Ping begins. Pinging slaves...");
		List<Server> slaves = repository.getSlaves();
		if(slaves == null)
		{
			log.error("WAT? Repository returned null on getSlaves() request. "
					+ "Expected: list of servers (even empty)");
			slaves = new ArrayList<Server>();
		}
		//log.debug("Number of slaves to ping: " + slaves.size());
		for (Server checkedSlave: slaves)
		{
			//log.debug("Checking slave with IP: " + checkedSlave.getIp());
			if(!checkServerAliveTwice(checkedSlave.getIp()))
			{
				slaveIsDown(checkedSlave);
			}
			else
			{
				checkedSlave.setLastConnection(new DateTime());
				connectedServers++;
			}
						
		}
		
		//log.debug("Pinging shadows...");
		List<Server> shadows = repository.getShadows();
		if(shadows == null)
		{
			log.error("WAT? Repository returned null on getSlaves() request. "
					+ "Expected: list of servers (even empty)");
			shadows = new ArrayList<Server>();
		}
		//log.debug("Number of shadows to ping: " + shadows.size());
		for (Server checkedShadow: shadows)
		{
			log.debug("Checking shadow with IP: " + checkedShadow.getIp());
			if(!checkServerAliveTwice(checkedShadow.getIp()))
			{
				shadowIsDown(checkedShadow);
			}
			else
			{
				checkedShadow.setLastConnection(new DateTime());
				connectedServers++;
			}
		}
		
		//log.debug("Pinging down servers...");
		List<Server> downservers = repository.getDownServers();

		log.debug("Number of down servers to ping: " + downservers.size());
		if(downservers == null)
		{
			log.error("WAT? Repository returned null on getSlaves() request. "
					+ "Expected: list of servers (even empty)");
			downservers = new ArrayList<Server>();
		}
		
		if(connectedServers  == 0 )
		{
			//wait a while
			try {
				Thread.sleep(DFSProperties.getProperties().getPingEvery());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for (Server downServer: downservers)
		{
			log.debug("Checking downServer with IP: " + downServer.getIp());
			if(CheckServerStatus.checkAlive(downServer.getIp()))
			{
				if(connectedServers > 0)
				{
					log.info("DownServer with IP: " + downServer.getIp() + " has risen from dead. Forcing to reregister...");
					reinitServer(downServer);
				}
				else
				{
					log.info("DownServer with IP: " + downServer.getIp() + " has risen from dead. "
							+ "It means I have reasen from dead. Waiting for old shadow electing me..");
				}
			}
						
		}
		
		//get servers and remove shadows
		while( repository.getShadows().size() < DFSProperties.getProperties().getShadowCount())
		{
			//try making a new shadow
			boolean success = makenewshadow();
			if(!success)
			{
				break;
			}
		}
	}

	private boolean makenewshadow()
	{

		boolean shadowMade = false;
		
		//select one server to become shadow
		List<Server> listOfBestStorageServers = null;
		int filesize = 0;
		try {
			listOfBestStorageServers = SelectStorageServers.getListOfBestStorageServers
					(repository, filesize);
		} catch (TException e) {
			//no servers available
			log.error(e.getMessage());
			return shadowMade;
		}
		
		int i = 0;	
		while(!shadowMade)
		{
			if(i>=listOfBestStorageServers.size())
			{
				break;
			}
			Server serverToBecomeShadow = listOfBestStorageServers.get(i++);
			
			log.info(" Making " + serverToBecomeShadow + " a new shadow..");
	
			//when new shadow is born, slave is dead.
			slaveIsDown(serverToBecomeShadow);
			
			//become shadow.
			try{
				serverHandler.makeShadow(serverToBecomeShadow.getIp());
				shadowMade = true;
			}
			catch (Exception e) {
				//TODO: and what if it is also down?
				log.error(" Error while trying to make" + serverToBecomeShadow 
						+ " a new shadow. " + e.getMessage());
				e.printStackTrace();
				continue;
			} 
		}
		return shadowMade;
	}
	
	private void reinitServer(Server downServer) {
		log.debug("Trying to reinitialize " + downServer.getIp());
		
		try(DFSClosingClient cclient = 
				new DFSClosingClient(downServer.getIp(),
						DFSProperties.getProperties().getStorageServerPort()))
		{
			Client client = cclient.getClient();
			client.forceRegister(serverHandler.getCoreStatus());
		} catch (Exception e) {
		    log.debug("Reinitializing " + downServer.getIp() + " failed");
			return;
		} 
		
	}

	/**
	 * Checks whether server is alive. 
	 * If server is not responding it will check again in 1000ms.
	 * If second request also fails, returns false.
	 *  
	 * @param IP server IP
	 * @return whether server is alive
	 */
	private boolean checkServerAliveTwice(String IP)
	{
		final long timeout = 1000;
		
		boolean alive = CheckServerStatus.checkAlive(IP);
		if(alive)
		{
			return true;
		}
		
		//slave's probably dead, try once again in few seconds
		try {
		    Thread.sleep(timeout);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}	
		
		alive = CheckServerStatus.checkAlive(IP);
		if(alive)
		{
			return true;
		}
		
		//server is really dead.
		return false;
	}
	
	/**
	 * Procedure taken when slave is down
	 * @param checkedSlave
	 */
	private void slaveIsDown(Server checkedSlave)
	{
		log.info("Slave " + checkedSlave + " is down.");
		
		//mark slave as down.
		checkedSlave.setRole(ServerRole.DOWN);
		repository.updateServer(checkedSlave);
		
		//remove all it's files ASAP - WARNING - probably it's NOT FAULT-RESISTANT.
		List<File> filesOnSlave = repository.getFilesOnSlave(checkedSlave);
		
		for (File file: filesOnSlave)
		{
			//TODO: it's ugly. Repository should have a method to get "FileOnServer"
			repository.deleteFileOnServer(new FileOnServer(file.getId(),checkedSlave.getId(), 0));
		}
		
		//replicate every file
		for (File file: filesOnSlave)
		{
			
			 // check if file needs to be replicated 
			 
			 Long replicationFactor = DFSProperties.getProperties()
			     .getReplicationFactor();

			 List<Server> slavesWithFileCopy = repository.getSlavesByFile(file);
			 long filecopies = slavesWithFileCopy.size();
			
			 if ( replicationFactor <= filecopies)
			 {
				 //no need to replicate
				 continue;
			 }
			
			List<Server> listOfBestStorageServers = null;
			try {
				listOfBestStorageServers = SelectStorageServers.getListOfBestStorageServers(repository, file.getSize());
			} catch (TException e) {
				//no servers available
				log.error(e.getMessage());
			}
			
			boolean replicated = false;
			int i = -1; //for iteration purposes
			while (!replicated)
			{
				++i;
				if (i >= listOfBestStorageServers.size())
				{
					log.error("Not enough slaves to replicate a file");
					break;
				}
				Server serverToPlaceFile = listOfBestStorageServers.get(i);
				if(slavesWithFileCopy.contains(serverToPlaceFile))
				{
					//do not replicate two times to the same server
					continue;
				}

				//select one slave to replicate
				Server serverToGetFileFrom = slavesWithFileCopy.get(0);
				repository.saveFileOnServer(new FileOnServer(file.getId(), serverToGetFileFrom.getId(), -(slavesWithFileCopy.size() + 1)));
				
				//replicate from one server to another
				log.debug("Slave " + checkedSlave.getIp() + " is down,"
						+ "trying to replicate " + file 
						+ " from " + serverToGetFileFrom + " to " + serverToPlaceFile);
				
				try(DFSClosingClient cclient = 
						new DFSClosingClient(serverToPlaceFile.getIp(),
								DFSProperties.getProperties().getStorageServerPort()))
				{
					Client client = cclient.getClient();
					client.replicate(file.getId(), serverToGetFileFrom.getIp(), file.getSize());
					//FIXME insert FILEONSERVER
					replicated = true;
				} catch (Exception e) {
					log.info("Slave " + checkedSlave.getIp() + " is down,"
							+ "error while trying to replicate " + file 
							+ " from " + serverToGetFileFrom + " to " + serverToPlaceFile);
					repository.deleteFileOnServer(new FileOnServer(file.getId(), serverToGetFileFrom.getId(), 0));
				} 
				
			}
			
		}
		
		
	}
	
	private void shadowIsDown(Server checkedShadow) {
		log.info("Shadow " + checkedShadow + " is down. Setting role to down "
				+ "and selecting new slave to become a shadow");
		
		checkedShadow.setRole(ServerRole.DOWN);
		repository.updateServer(checkedShadow);
		
		//TODO: add remove shadow to interface 
		//disconnect shadow from repository
		((DFSRepositoryImpl)repository).removeShadow(checkedShadow);
		
	}

	public LinkedList<InfrastructureModificationCommand> getTaskQueue() {
		return taskQueue;
	}

	public void setTaskQueue(LinkedList<InfrastructureModificationCommand> taskQueue) {
		this.taskQueue = taskQueue;
	}
	
}

