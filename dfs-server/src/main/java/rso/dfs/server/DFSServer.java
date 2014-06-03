package rso.dfs.server;

import java.io.File;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rso.dfs.commons.DFSProperties;
import rso.dfs.event.DFSTask;
import rso.dfs.generated.CoreStatus;
import rso.dfs.generated.NewSlaveRequest;
import rso.dfs.generated.Service;
import rso.dfs.generated.Service.Client;
import rso.dfs.model.Server;
import rso.dfs.model.ServerRole;
import rso.dfs.model.dao.DFSRepository;
import rso.dfs.model.dao.psql.DFSRepositoryImpl;
import rso.dfs.model.dao.psql.EmptyRepository;
import rso.dfs.server.storage.EmptyStorageHandler;
import rso.dfs.server.storage.FileStorageHandler;
import rso.dfs.server.storage.StorageHandler;
import rso.dfs.utils.DFSClosingClient;
import rso.dfs.utils.InetAddressUtils;

public class DFSServer {

	final static Logger log = LoggerFactory.getLogger(DFSServer.class);

	private ServerHandler serviceHandler;

	private Service.Processor procesor;

	// TODO : provide better naming... :(
	private Server me;

	private DFSRepository repository;

	private StorageHandler storageHandler;

	public DFSServer(String[] args) throws UnknownHostException {

		me = new Server();
		me.setLastConnection(new DateTime());
		if (ServerRole.getServerRole(args[0]) == ServerRole.MASTER) {
			
			if(args.length < 2 )
			{
				log.error("Insufficient number of arguments. You should provide IP as a second argument");
				System.exit(1);
			}
			
			log.info("Master IP is: "+ args[1]);
			me.setIp(args[1]);
			// queue for messages from master to
			// thread that updates shadows' databases

			BlockingQueue<DFSTask> blockingQueue = new LinkedBlockingQueue<>();

			repository = new DFSRepositoryImpl(me, blockingQueue);

			// init db FIXME: for now clean db, it's probably not the best
			// solution
			repository.cleanDB();

			me.setMemory(DFSProperties.getProperties().getNamingServerMemory());
			me.setRole(ServerRole.MASTER);
			me.setIp(args[1]);

			try {
				repository.saveServer(me);
			} catch (Exception e) {
				log.error("Saving the server to repository failed with an error: " + e.getMessage());
				System.exit(1);
			}

			// create empty object for storage handler

			storageHandler = new EmptyStorageHandler();
			serviceHandler = new ServerHandler(
					me, 
					new CoreStatus(me.getIp(), new ArrayList<String>()), 
					storageHandler, 
					repository);
			
			//Run Server checking service
			
		} else {
			
			if(args.length < 3 )
			{
				log.error("Insufficient number of arguments. "
						+ "You should provide master IP as a second argument "
						+ "and slave IP as a third argument");
				System.exit(1);
			}
			
			// create empty object for slave
			repository = new EmptyRepository();

			me.setMemory(DFSProperties.getProperties().getStorageServerMemory());
			me.setRole(ServerRole.SLAVE);
			// FIXME: simplifying assumption: IP will be given by user who runs
			// slave as 3rd arg.
			me.setIp(args[2]);
			log.info("Master IP is " + args[1] + "; Slave ip is " + args[2]);

			// if master is not on the same server, clean db. It was used only to run on same host
			/*if (!args[1].equals("127.0.0.1") && !args[1].equals("localhost") && !args[1].equals(args[2])) {
				repository.cleanDB();
			}*/

			Server master = new Server();
			master.setIp(args[1]);
			master.setLastConnection(new DateTime());
			master.setRole(ServerRole.MASTER);

			// repository.saveServer(master);

			storageHandler = new FileStorageHandler();
			serviceHandler = new ServerHandler(
					me, 
					new CoreStatus(master.getIp(), new ArrayList<String>()), 
					storageHandler, 
					repository);
			
		}
		procesor = new Service.Processor(serviceHandler);

	}

	/**
	 * 
	 * @param args
	 *            : M | S <MasterIP> <SlaveIP>
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	public static void main(String[] args) throws UnknownHostException, SocketException {
		// check server type
		if (args.length < 1) {
			log.error("Invalid usage");
			return;
		}
		DFSServer server = new DFSServer(args);

		// start service
		server.run();

	}

	private void run() {
		// TODO: is running server in another thread needed?
		/*
		 * try {
		 * 
		 * Runnable simple = new Runnable() {
		 * 
		 * @Override public void run() { } }; new Thread(simple).start(); }
		 * catch (Exception e) { e.printStackTrace(); }
		 */
		simple(procesor);
	}

	public void simple(Service.Processor processor) {
		try {
			log.debug("Starting threaded server");
			TServerTransport serverTransport;
			// TODO: fix this because it's ugly and probably not needed.
			if (me.getRole() == ServerRole.MASTER) {
				serverTransport = new TServerSocket(DFSProperties.getProperties().getNamingServerPort());
			} else {
				serverTransport = new TServerSocket(DFSProperties.getProperties().getStorageServerPort());
			}

			TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

			if (me.getRole() == ServerRole.SLAVE) {
				serviceHandler.registerToMaster();
			}

			log.debug("Starting server: " + me.toString());
			server.serve();
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public DFSRepository getRepository() {
		return repository;
	}

	public Server getServer() {
		return me;
	}

}
