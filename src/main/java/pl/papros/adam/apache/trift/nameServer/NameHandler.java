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
		File f = new File(fileName, files.size());
		files.add(f);
		return f.getId();
	}

	@Override
	public int get(String fileName) throws TException {
		for (File f : files) {
			if (fileName.equals(f.getName())) {
				return f.getId();
			}
		}
		throw new RuntimeException("unable to find file, fileName=" + fileName);
	}

}
