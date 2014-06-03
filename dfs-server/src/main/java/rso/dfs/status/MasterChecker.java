package rso.dfs.status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jline.internal.Log;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rso.dfs.commons.DFSProperties;
import rso.dfs.event.DFSTask;
import rso.dfs.generated.CoreStatus;
import rso.dfs.generated.Service.Client;
import rso.dfs.model.File;
import rso.dfs.model.FileOnServer;
import rso.dfs.model.Server;
import rso.dfs.model.ServerRole;
import rso.dfs.model.dao.DFSRepository;
import rso.dfs.model.dao.psql.DFSRepositoryImpl;
import rso.dfs.server.ServerHandler;
import rso.dfs.server.storage.FileStorageHandler;
import rso.dfs.server.storage.StorageHandler;
import rso.dfs.server.utils.SelectStorageServers;
import rso.dfs.utils.DFSClosingClient;

/**
 * This service will be responsible for checking Slaves and Shadows health. 
 * In case of shadow failure, it will elect new shadow.
 * In case of slave failure, it will order some slaves to replicate. 
 * @author sven
 */
public class MasterChecker {
	
	private DFSRepository repository;
	private DateTime lastCheck;
	private final static Logger log = LoggerFactory.getLogger(MasterChecker.class);
	ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
	private ServerHandler serverHandler;
	
	
	public MasterChecker(ServerHandler serverHandler) {
		this.serverHandler = serverHandler;
		this.repository = serverHandler.getRepository();
		this.lastCheck = new DateTime();
	}
	
	public void runService()
	{
		exec.scheduleAtFixedRate(new Runnable() {
		  @Override
		  public void run() {
			  //log.debug("Run MasterChecker service");
			  checkMasterAndMaybeBecomeOne();
		  }
		}, DFSProperties.getProperties().getPingEvery(), 
		   DFSProperties.getProperties().getPingEvery(), 
		   TimeUnit.MILLISECONDS);
	}
	
	public void stopService()
	{
		exec.shutdown(); //TODO: check if it works, maybe shoutdownNOW?
	}
	

	private void checkMasterAndMaybeBecomeOne()
	{
		lastCheck = new DateTime();
		
		String masterAddress = null;
		try {
			 masterAddress = serverHandler.getCoreStatus().getMasterAddress();
		} catch (TException e) {
			//should not happen, it's local method execution
			e.printStackTrace();
			return;
		}
		
		//log.debug(serverHandler.getServer().getIp() + " is checking master alive..");
		
		if(!checkServerAliveTwice(masterAddress))
		{
			masterIsDown();
		}
		
		//log.debug("Ping ends.");
	}

	
	private void masterIsDown() {
		//first, check whether I am the right candidate for being master
		CoreStatus coreStatus = null;
		try {
			coreStatus = serverHandler.getCoreStatus();
		} catch (TException e) {
			// should not happen, it's local method execution
			e.printStackTrace();
			return;
		}
		List<String> shadowsAddresses = coreStatus.getShadowsAddresses();
		
		// Collections.sort will probably not sort it IP-wise, but as long as its'
		// SOME strict ordering, it's ok.
		//Collections.sort(shadowsAddresses);
		//MOVED TO DB /\: Order By ip
		
		if (shadowsAddresses.get(0).equals(serverHandler.getServer().getIp()))
		{
			log.info(serverHandler.getServer().getIp() + " says: I AM THE ONE");
			
			BlockingQueue<DFSTask> blockingQueue = new LinkedBlockingQueue<>();
			DFSRepositoryImpl temprepository = 
					new DFSRepositoryImpl(serverHandler.getServer(), blockingQueue);
			
			//checking if I am connected to anyone else
			int connectedServers = 0;
			Iterator<Server> iterator = temprepository.getShadows().iterator();
			iterator.next(); //skip first elem - this shadow server
			log.debug("Checking other shadows!");
			while(iterator.hasNext())
			{
				Server server = iterator.next();	
				log.debug("Checking " + server);
				if (CheckServerStatus.checkAlive(server.getIp()))
				{
					connectedServers++;
				}
			}
			
			iterator = temprepository.getSlaves().iterator();
			log.debug("Checking other slaves!");
			while(iterator.hasNext())
			{
				Server server = iterator.next();
				log.debug("Checking " + server);
				if (CheckServerStatus.checkAlive(server.getIp()))
				{
					connectedServers++;
				}
			}
			
			if(connectedServers > 0) //TODO: maybe > 1/2 of all servers?
			{
				log.debug("Becoming a master!");
				serverHandler.becomeMaster();
			}
			else
			{
				log.debug("Not becoming a master - no servers connected!");
			}
		}
		
		//FIXME: case if first shadow AND master are down
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
	
	
	
}

