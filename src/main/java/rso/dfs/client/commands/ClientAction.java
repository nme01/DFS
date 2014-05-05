package rso.dfs.client.commands;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public interface ClientAction {

	public String getCommandName();

	public String getHelp();

	public void performCommand(String line) throws Exception;

	public boolean correspondsToString(String s);

}
