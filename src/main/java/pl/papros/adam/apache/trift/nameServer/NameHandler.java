package pl.papros.adam.apache.trift.nameServer;

import java.util.ArrayList;

import org.apache.thrift.TException;

import pl.papros.adam.apache.trift.generated.*;

public class NameHandler implements NamingService.Iface {

	static ArrayList<File> files;

	public NameHandler() {
		files = new ArrayList<File>();
	}

	@Override
	public int put(String fileName) throws TException {
		return files.size();
	}

	@Override
	public int get(String fileName) throws TException {
		// TODO Auto-generated method stub
		return 0;
	}

}
