package rso.dfs.server.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;

import rso.dfs.model.File;
import rso.dfs.model.Server;
import rso.dfs.model.dao.DFSRepository;

public class SelectStorageServers {
	public static List<Server> getListOfBestStorageServers(DFSRepository paramRepository, long size)
			throws TException {
		List<Server> slaves = paramRepository.getSlaves();
		
		Map<Server, Long> freeMemoryMap = new HashMap<Server, Long>();
	
		for (Server server : slaves) {
			long filesMemory = 0;
			for (File file : paramRepository.getFilesOnSlave(server)) {
				filesMemory += file.getSize();
			}
	
			if (server.getMemory() - filesMemory <= size)
				continue;
	
			freeMemoryMap.put(server, filesMemory); // po miejscu zajętym
			// freeMemoryMap.put(server, server.getMemory() - filesMemory); //
			// po miejscu wolnym
		}
		
		if (freeMemoryMap.size() < 1) {
			throw new TException("There are no storage servers with enough available space needed to process your request.");
		}
	
		List<Server> keys = new ArrayList<Server>(freeMemoryMap.keySet());
		final Map sortmap = freeMemoryMap;
		Collections.sort(keys, new Comparator() {
			public int compare(Object left, Object right) {
				Long leftVal = (Long) sortmap.get((Server) left);
				Long rightVal = (Long) sortmap.get((Server) right);
	
				return leftVal.compareTo(rightVal);
			}
		});
		return keys;
	}
}
