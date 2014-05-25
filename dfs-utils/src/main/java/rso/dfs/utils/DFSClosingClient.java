package rso.dfs.utils;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import rso.dfs.commons.DFSProperties;
import rso.dfs.generated.Service;
import rso.dfs.generated.Service.Client;

/**
 * After writing: it is really similar to DfsTSocket.
 * @author sven
 */
public class DFSClosingClient implements AutoCloseable {

	TTransport transport;
	Service.Client client;
	
	public DFSClosingClient(String host, int portNumber) throws TTransportException
	{
		this(host, portNumber, DFSProperties.getProperties().getDefaultClientTimeout());
	}
	
	public DFSClosingClient(String host, int portNumber, int timeout) throws TTransportException {
		transport = new TSocket(host, portNumber, timeout);
		try {
			transport.open();
		} catch (TTransportException e) {
			//just throw it further
			throw e;
		}
		TProtocol protocol = new TBinaryProtocol(transport);
		
		client = new Client(protocol);

	}
	
	public Client getClient()
	{
		return client;
	}
	
	@Override
	public void close() throws Exception {
		transport.close();
	}

}
