package rso.dfs.status;

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
import rso.dfs.model.dao.DFSRepository;
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
	private final static Logger log = LoggerFactory.getLogger(ServerStatusCheckerService.class);
	ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
	
	public ServerStatusCheckerService(DFSRepository repository) {
		this.repository = repository;
		this.lastCheck = new DateTime();
	}
	
	public void runService()
	{
		exec.scheduleAtFixedRate(new Runnable() {
		  @Override
		  public void run() {
			  checkAllShadowsAndSlaves();
		  }
		}, 0, DFSProperties.getProperties().getPingEvery(), TimeUnit.MILLISECONDS);
	}
	
	public void stopService()
	{
		exec.shutdown();
	}
	
	/**
	 * Checks whether server is alive. 
	 * If server is not responding it will check again in 1000ms.
	 * If second request also fails, returns false
	 *  
	 * @param IP server IP
	 * @return whether server is alive
	 */
	private boolean checkServerAliveTwice(String IP)
	{
		boolean alive = CheckServerStatus.checkAlive(IP);
		if(alive)
		{
			return true;
		}
		
		//slave's probably dead, try once again in few seconds
		try {
		    Thread.sleep(1000);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}	
		
		alive = CheckServerStatus.checkAlive(IP);
		if(alive)
		{
			return true;
		}
		
		return false;
		//slave is really dead.
		
	}
	
	private void slaveIsDown(Server checkedSlave)
	{
		//need to
		//- replicate all files which were on this slave
		List<File> filesOnSlave = repository.getFilesOnSlave(checkedSlave);
		for (File file: filesOnSlave)
		{
			/*
			 * check if file needs to be replicated
			 * FIXME: for now assume it needs to be replicated, 
			 * no "count file" is implemented
			 * 
			 *Long replicationFactor = DFSProperties.getProperties()
			 *    .getReplicationFactor();
			 *long filecopies = repository.getFileOfFileCopiesByID() -1;
			 */
			//
			
			//FIXME: t's ugly. Repository should have a method to get "FileOnServer"
			repository.deleteFileOnServer(new FileOnServer(file.getId(),checkedSlave.getId(), 0));
			
			List<Server> listOfBestStorageServers = null;
			try {
				listOfBestStorageServers = SelectStorageServers.getListOfBestStorageServers(repository, file.getSize());
			} catch (TException e) {
				//no servers available
				log.error(e.getMessage());
			}
			Server serverToPlaceFile = listOfBestStorageServers.get(0);
			
			List<Server> slavesWithFileCopy = repository.getSlavesByFile(file);
			//select one slave to replicate
			Server serverToGetFileFrom = slavesWithFileCopy.get(0);
			

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
				
			} catch (Exception e) {
				//TODO: and what if it is also down?
				log.error("Slave " + checkedSlave.getIp() + " is down,"
						+ "error while trying to replicate " + file 
						+ " from " + serverToGetFileFrom + " to " + serverToPlaceFile);
			} 
		}
		
		
	}
	
	private void checkAllShadowsAndSlaves()
	{
		lastCheck = new DateTime();
		
		List<Server> slaves = repository.getSlaves();
		for (Server checkedSlave: slaves)
		{
			if(!checkServerAliveTwice(checkedSlave.getIp()))
			{
				slaveIsDown(checkedSlave);
			}
						
		}
		
		List<Server> shadows = repository.getShadows();
		for (Server checkedShadow: shadows)
		{
			if(!checkServerAliveTwice(checkedShadow.getIp()))
			{
				shadowIsDown(checkedShadow);
			}
						
		}
	}

	private void shadowIsDown(Server checkedShadow) {
		//select one server to become shadow
		
		List<Server> listOfBestStorageServers = null;
		int filesize = 0;
		try {
			listOfBestStorageServers = SelectStorageServers.getListOfBestStorageServers
					(repository, filesize);
		} catch (TException e) {
			//no servers available
			log.error(e.getMessage());
		}
		Server serverToBecomeShadow = listOfBestStorageServers.get(0);
		
		log.debug("Shadow" + checkedShadow + " is down."
				+ " Making " + serverToBecomeShadow + " a new shadow..");

		//when new shadow is born, slave is dead.
		slaveIsDown(serverToBecomeShadow);
		
		//become shadow.
		try(DFSClosingClient cclient = 
				new DFSClosingClient(serverToBecomeShadow.getIp(),
						DFSProperties.getProperties().getStorageServerPort()))
		{
			Client client = cclient.getClient();
			client.becomeShadow(null); //FIXME: why there is corestatus here?
			
		} catch (Exception e) {
			//TODO: and what if it is also down?
			log.debug("Shadow" + checkedShadow + " is down."
					+ " Error while trying to make" + serverToBecomeShadow 
					+ " a new shadow. " + e.getMessage());
		} 
	}
	
}

