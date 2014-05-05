package rso.dfs.client.commands;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public abstract class ClientActionBase implements ClientAction {

	protected String masterIpAddress;

	@Override
	public boolean correspondsToString(String s) {
		return s.contains(getCommandName());
	}

	@Override
	public boolean equals(Object obj) {
		ClientAction clientAction = (ClientAction) obj;
		return getCommandName().equalsIgnoreCase(clientAction.getCommandName());
	}
}
