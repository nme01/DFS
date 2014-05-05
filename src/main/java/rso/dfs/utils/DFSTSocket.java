package rso.dfs.utils;

import org.apache.thrift.transport.TSocket;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class DFSTSocket extends TSocket implements AutoCloseable {

	public DFSTSocket(String host, int portNumber) {
		super(host, portNumber);

	}

	public DFSTSocket(String host, int portNumber, int timeout) {
		super(host, portNumber, timeout);
	}

}
