package rso.dfs.server;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rso.dfs.commons.DFSConstans;
import rso.dfs.generated.Service;

public class DFSServer {

	final static Logger log = LoggerFactory.getLogger(DFSServer.class);

	static ServerHandler handler;

	static Service.Processor procesor;

	public static void main(String[] args) {
		try {
			handler = new ServerHandler();

			procesor = new Service.Processor(handler);

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

	public static void simple(Service.Processor processor) {
		try {
			TServerTransport serverTransport = new TServerSocket(DFSConstans.SERVICE_PORT_NUMBER);
			TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

			log.debug("Starting Naming server...");
			server.serve();
		} catch (Exception e) {
			log.error(e.toString());
		}
	}
}
