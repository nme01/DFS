package rso.dfs.client.handlers;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public abstract class HandlerBase {

	protected String masterIpAddress;

	protected final int defaultPortNumber;

	protected final int defaultOffset = 1000;
	
	public HandlerBase(String masterIpAddress, int defaultPortNumber) {
		super();
		this.masterIpAddress = masterIpAddress;
		this.defaultPortNumber = defaultPortNumber;
	}

}
