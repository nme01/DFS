package rso.dfs.dummy.server;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;

import rso.dfs.dummy.generated.StorageService;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * 
 * */

public class StorageHandler implements StorageService.Iface {

	static Storage storage;
	static String storagePathString = "D:\\test\\storage";

	public StorageHandler() {
		storage = new Storage();
	}

	@Override
	public void putFile(int fileId, List<Byte> body) throws TException {

		Data data = new Data(fileId, (ArrayList<Byte>) body);
		storage.getData().add(data);

		writeToDisc(fileId, (ArrayList<Byte>) body);

	}

	private void writeToDisc(int fileId, ArrayList<Byte> body) {
		Path path = Paths.get(assemblyPath(fileId));
		byte[] bytes = new byte[body.size()];
		for (int i = 0; i < body.size(); ++i) {
			bytes[i] = body.get(i);
		}

		try {
			Files.write(path, bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public List<Byte> getFile(int fileId) throws TException {
		Path path = Paths.get(assemblyPath(fileId));
		byte[] bytes;
		try {
			bytes = Files.readAllBytes(path);
		} catch (IOException e) {
			throw new RuntimeException("Unable to read file. " + e);
		}
		ArrayList<Byte> body = new ArrayList<Byte>();
		for (byte b : bytes) {
			body.add(b);
		}
		return body;
	}

	private  static String assemblyPath(int fileId) {
		return storagePathString + FileSystems.getDefault().getSeparator() + fileId;
	}

}
