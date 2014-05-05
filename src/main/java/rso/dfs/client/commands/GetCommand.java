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
	public void performCommand(String line) throws Exception {
		String filePath = assemblyFileName(line);

		GetHandler handler = new GetHandler(masterIpAddress);
		try {
			handler.performGet(filePath);
		} catch (Exception e) {
			
		}
	
	}

	private String assemblyFileName(final String line) {
		String[] tokens = line.split(" ");
		if (tokens.length != 2) {
			// raise error
		}
		File file = new File(tokens[1]);
		return file.getAbsolutePath();
	}

}
