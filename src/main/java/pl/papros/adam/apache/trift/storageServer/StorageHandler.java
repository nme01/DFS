package pl.papros.adam.apache.trift.storageServer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;

import pl.papros.adam.apache.trift.generated.*;

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
		Path path = Paths.get(storagePathString
				+ FileSystems.getDefault().getSeparator() + fileId);
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
		// TODO Auto-generated method stub
		return null;
	}

}
