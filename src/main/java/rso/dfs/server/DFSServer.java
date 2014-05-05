package rso.dfs.Server;

import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rso.dfs.commons.DFSConstans;
import rso.dfs.generated.Service;
import rso.dfs.model.Server;
import rso.dfs.model.ServerRole;
import rso.dfs.model.dao.DFSRepository;
import rso.dfs.model.dao.psql.DFSRepositoryImpl;
import rso.dfs.utils.InetAddressUtils;

public class DFSServer {

	final static Logger log = LoggerFactory.getLogger(DFSServer.class);

	private ServerHandler handler;

	private Service.Processor procesor;

	// TODO : provide better naming... :(
	private Server me;

	private DFSRepository repository;

	public DFSServer(String[] args) throws UnknownHostException {

		me = new Server();
		me.setIp(InetAddressUtils.getInetAddressAsString());
		me.setLastConnection(new DateTime());
		if (ServerRole.getServerRole(args[0]) == ServerRole.MASTER) {
			me.setMemory(DFSConstans.NAMING_SERVER_MEMORY);
			me.setRole(ServerRole.MASTER);
		} else {
			me.setMemory(DFSConstans.STORAGE_SERVER_MEMORY);
			me.setRole(ServerRole.SLAVE);
		}

		repository = new DFSRepositoryImpl();
		handler = new ServerHandler(me);
		procesor = new Service.Processor(handler);

	}

	public static void main(String[] args) throws UnknownHostException, SocketException {
		// check server type
		if (args.length != 1) {
			log.error("Invalid usage");
			return;
		}
		DFSServer server = new DFSServer(args);
		// check database
		Server master = server.getRepository().getMasterServer();
		if (master == null) {
			server.getRepository().saveMaster(server.getServer());
		}
		// start service
		server.run();

	}

	private void run() {

		try {

			Runnable simple = new Runnable() {

				@Override
				public void run() {
					simple(procesor);

				}
			};
			new Thread(simple).start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void simple(Service.Processor processor) {
		try {
			TServerTransport serverTransport;
			// TODO: fix this because it's ugly
			if (me.getRole() == ServerRole.MASTER) {
				serverTransport = new TServerSocket(DFSConstans.NAMING_SERVER_PORT_NUMBER);
			} else {
				serverTransport = new TServerSocket(DFSConstans.STORAGE_SERVER_PORT_NUMBER);
			}

			TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

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
