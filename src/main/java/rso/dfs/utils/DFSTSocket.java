package rso.dfs.utils;

import org.apache.thrift.transport.TSocket;

import rso.dfs.commons.DFSConstans;

/**
 * @author Adam Papros <adam.papros@gmial.com>
 * */
public class DFSTSocket extends TSocket implements AutoCloseable {

	public DFSTSocket(String host) {
		super(host, DFSConstans.SERVICE_PORT_NUMBER);

	}

	public DFSTSocket(String host, int timeout) {
		super(host, DFSConstans.SERVICE_PORT_NUMBER, timeout);
	}

	@Override
	public void close() {
		// TODO check if it works
		super.close();
	}

}
