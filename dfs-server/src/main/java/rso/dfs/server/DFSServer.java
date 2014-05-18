package rso.dfs.server;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rso.dfs.commons.DFSConstans;
import rso.dfs.generated.NewSlaveRequest;
import rso.dfs.generated.Service;
import rso.dfs.generated.Service.Client;
import rso.dfs.model.Server;
import rso.dfs.model.ServerRole;
import rso.dfs.model.dao.DFSRepository;
import rso.dfs.model.dao.psql.DFSRepositoryImpl;
import rso.dfs.utils.DFSClosingClient;
import rso.dfs.utils.InetAddressUtils;
import rso.dfs.utils.IpConverter;

public class DFSServer {

	final static Logger log = LoggerFactory.getLogger(DFSServer.class);

	private ServerHandler handler;

	private Service.Processor procesor;

	// TODO : provide better naming... :(
	private Server me;

	private DFSRepository repository;

	public DFSServer(String[] args) throws UnknownHostException {

		repository = new DFSRepositoryImpl();
		
		me = new Server();
		me.setIp(InetAddressUtils.getInetAddressAsString());
		me.setLastConnection(new DateTime());
		if (ServerRole.getServerRole(args[0]) == ServerRole.MASTER) {
			
			//init db FIXME: for now clean db, it's probably not the best solution
			repository.cleanDB();
			
			me.setMemory(DFSConstans.NAMING_SERVER_MEMORY);
			me.setRole(ServerRole.MASTER);
			
			repository.saveServer(me);
		} else {
			me.setMemory(DFSConstans.STORAGE_SERVER_MEMORY);
			me.setRole(ServerRole.SLAVE);
			//FIXME: simplifying assumption: IP will be given by user who runs slave as 3rd arg. 
			me.setIp(args[2]);

			//FIXME: temporary for localtesting. Later: dropDB by cleandb repo method
			Server masterServer = repository.getMasterServer();
			if(masterServer == null)
			{
				Server master = new Server();
				master.setIp(args[1]);
				master.setLastConnection(new DateTime());
				master.setRole(ServerRole.MASTER);
				repository.saveServer(master);
			}
						
		}
		

		handler = new ServerHandler(me);
		procesor = new Service.Processor(handler);

	
	}

	/**
	 * 
	 * @param args: M | S <MasterIP> <SlaveIP>
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

		try {

			Runnable simple = new Runnable() {

				@Override
				public void run() {
					

				}
			};
			new Thread(simple).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		simple(procesor);
	}

	public void simple(Service.Processor processor) {
		try {
			TServerTransport serverTransport;
			// TODO: fix this because it's ugly and probably not needed.
			if (me.getRole() == ServerRole.MASTER) {
				serverTransport = new TServerSocket(DFSConstans.NAMING_SERVER_PORT_NUMBER);
			} else {
				serverTransport = new TServerSocket(DFSConstans.STORAGE_SERVER_PORT_NUMBER);
			}

			TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));
						
			if(me.getRole() == ServerRole.SLAVE)
			{
				String masterIP = repository.getMasterServer().getIp();
				//register new slave

				try(DFSClosingClient cclient = 
						new DFSClosingClient(masterIP, 
								DFSConstans.NAMING_SERVER_PORT_NUMBER))
				{

					Client client = cclient.getClient();
					//
					ArrayList<Integer> fileList = new ArrayList<Integer>();
					String slaveIP = me.getIp();
					NewSlaveRequest request = new NewSlaveRequest(slaveIP, fileList);
					log.debug("Slave will register to master");
					client.registerSlave(request); //TODO: do sth with result: CoreStatus
					log.debug("I, humble slave with IP address " + me.getIp() + ", registered to master at " + masterIP);
				}
			}
			
			log.debug("Starting server: serverRole=" + me.toString());
			server.serve();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public DFSRepository getRepository() {
		return repository;
	}

	public Server getServer() {
		return me;
	}
}
