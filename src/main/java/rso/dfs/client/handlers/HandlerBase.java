package rso.dfs.client.handlers;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public abstract class HandlerBase {

	protected String masterIpAddress;

	public HandlerBase(String masterIpAddress) {
		super();
		this.masterIpAddress = masterIpAddress;
	}

}
