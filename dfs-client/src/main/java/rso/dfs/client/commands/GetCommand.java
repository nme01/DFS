package rso.dfs.client.commands;

import java.io.File;

import rso.dfs.client.handlers.GetHandler;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class GetCommand extends ClientActionBase {

	public GetCommand() {
	}

	@Override
	public String getCommandName() {
		return "get";
	}

	@Override
	public String getHelp() {
		return "help for get command";
	}

	@Override
	public void performCommand(String line, String masterIP) throws Exception {

		String[] tokens = line.split(" ");
		if (tokens.length != 3) {
			// raise error
			System.err.println("Get error, invalid number of args");
		}
		String filePathSrc = tokens[1];
		String filePathDst = tokens[2];
		
		GetHandler handler = new GetHandler(masterIP);
		try {
			handler.performGet(filePathSrc,filePathDst);
		} catch (Exception e) {

		}

	}

}
