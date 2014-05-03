package rso.dfs.dummy.server;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import rso.dfs.dummy.generated.StorageService;

/**
 * @author Adam Papros <adam.papros@gmail.com> 
 * TODO:DELETE THIS SHIT
 * @deprecated
 * */
@Deprecated
public class StorageServer {

	static StorageHandler handler;

	static StorageService.Processor procesor;

	static final int portNumber = 9091;

	public static void main(String[] args) {
		try {
			handler = new StorageHandler();
			procesor = new StorageService.Processor(handler);

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

	public static void simple(StorageService.Processor processor) {
		try {
			TServerTransport serverTransport = new TServerSocket(portNumber);
			TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

			System.out.println("Starting storage server...");
			server.serve();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
