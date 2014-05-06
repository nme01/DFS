package apache.trift;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import rso.dfs.FileStatus;
import rso.dfs.ServerRole;
import rso.dfs.dbManager.DbManager;

public class DbManagerMock extends DbManager{
	
	private InetAddress master;
	private List<InetAddress> shadows;
	private List<InetAddress> slaves;
	
	public DbManagerMock(){
		shadows = new ArrayList<InetAddress>();
		slaves = new ArrayList<InetAddress>();
		try {
			master = InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, 1, 1});
			slaves.add(InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, 1, 2}));
			slaves.add(InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, 1, 3}));
			shadows.add(InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, 1, 4}));
			shadows.add(InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, 1, 5}));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public Collection<InetAddress> getServersByRole(ServerRole r) {
		switch(r){
		case Master:
			return new ArrayList<InetAddress>(Arrays.asList(this.master));
		case Slave:
			return slaves;
		case Shadow:
		default:
			return shadows;
		}
	}
	
	@Override
	public Collection<InetAddress> getServersByFile(int fileId) {
		return slaves;
	}
	
	@Override
	public int getFileId(String fileName) {
		int id = -1;
		if(fileName.equals("pizza")){
			id = 1;
		}
		if(fileName.equals("ciacho")){
			id = 2;
		}
		return id;
	}
	
	@Override
	public int addNewFile(String name, long size) {
		return 3;
	}
	
}
