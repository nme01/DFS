package pl.papros.adam.apache.trift.nameServer;

import java.util.ArrayList;

import org.apache.thrift.TException;

import pl.papros.adam.apache.trift.generated.*;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * 
 * */

public class NameHandler implements NamingService.Iface {

	static ArrayList<File> files;

	public NameHandler() {
		files = new ArrayList<File>();
	}

	@Override
	public int put(String fileName) throws TException {
		files.add(new File(fileName, files.size()));
		return files.size();
	}

	@Override
	public int get(String fileName) throws TException {
		// TODO Auto-generated method stub
		return 0;
	}

}
