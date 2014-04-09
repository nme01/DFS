package rso.dfs.nameServer;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import rso.dfs.generated.NamingService;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * 
 * */

public class NameServer {

	static NameHandler handler;

	static NamingService.Processor procesor;

	static final int portNumber = 9090;

	public static void main(String[] args) {
		try {
			handler = new NameHandler();
			procesor = new NamingService.Processor(handler);

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

	public static void simple(NamingService.Processor processor) {
		try {
			TServerTransport serverTransport = new TServerSocket(portNumber);
			TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

			System.out.println("Starting Naming server...");
			server.serve();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
