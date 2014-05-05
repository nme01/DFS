package rso.dfs.utils;

import org.apache.thrift.transport.TSocket;

/**
 * @author Adam Papros <adam.papros@gmial.com>
 * */
public class DFSTSocket extends TSocket implements AutoCloseable {

	public DFSTSocket(String host, int port) {
		super(host, port);

	}

	public DFSTSocket(String host, int port, int timeout) {
		super(host, port, timeout);
	}

	@Override
	public void close() {
		// TODO check if it works
		super.close();
	}

}
