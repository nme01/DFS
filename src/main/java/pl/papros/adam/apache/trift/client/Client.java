package pl.papros.adam.apache.trift.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import pl.papros.adam.apache.trift.generated.NamingService;
import pl.papros.adam.apache.trift.generated.StorageService;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * 
 * */

public class Client {

	public static String command = "put";
	public static String pathString = "D:\\test\\test";

	static final int nameServerPort = 9090;
	static final int storageServerPort = 9091;

	public static void main(String[] args) throws IOException {

		// get file

		Path path = Paths.get(pathString);
		byte[] bytes = Files.readAllBytes(path);
		ArrayList<Byte> body = new ArrayList<Byte>();
		for (byte b : bytes) {
			body.add(b);
		}

		if (command.equals("put")) {
			put(body);
		} else if (command.equalsIgnoreCase("get")) {
			get();
		}

	}

	private static void put(ArrayList<Byte> body) {
		try {
			TTransport transport;

			transport = new TSocket("localhost", nameServerPort);
			transport.open();

			TProtocol protocol = new TBinaryProtocol(transport);
			NamingService.Client namingClient = new NamingService.Client(
					protocol);

			int fileId = performPut(namingClient, pathString);
			transport.close();

			transport = new TSocket("localhost", storageServerPort);
			transport.open();

			protocol = new TBinaryProtocol(transport);
			StorageService.Client storageClient = new StorageService.Client(
					protocol);

			performPutFile(storageClient, fileId, body);
			transport.close();

		} catch (TException x) {
			x.printStackTrace();
		}
	}

	private static void get() {

	}

	private static int performPut(NamingService.Client client, String fileName)
			throws TException {
		return client.put(fileName);
	}

	private static void performPutFile(StorageService.Client client,
			int fileId, ArrayList<Byte> body) throws TException {

		client.putFile(fileId, body);

	}
}
