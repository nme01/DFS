package rso.dfs.client.commands;

import rso.dfs.client.handlers.ListContentHandler;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class ListContentCommand extends ClientActionBase {

	public ListContentCommand() {
	}

	@Override
	public String getCommandName() {
		return "ls";
	}

	@Override
	public String getHelp() {
		return "Lists content";
	}

	@Override
	public void performCommand(String line, String masterIP) throws Exception {
		ListContentHandler handler = new ListContentHandler(masterIP);
		handler.performLS();

	}

}
