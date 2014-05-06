package rso.dfs.client.commands;

import java.io.File;

import rso.dfs.client.handlers.PutHandler;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class PutCommand extends ClientActionBase {

	@Override
	public String getCommandName() {
		return "put";
	}

	@Override
	public String getHelp() {
		return "help for put command";
	}

	@Override
	public void performCommand(String line) {
		// TODO Auto-generated method stub
		String filePath = assemblyFileName(line);
		File thisFile = new File(filePath);
		long fileSize = thisFile.length();
		
		PutHandler handler = new PutHandler(masterIpAddress);
		try {
			handler.performPut(filePath, fileSize);
		} catch (Exception e) {

		}
	}
	
	private String assemblyFileName(final String line) {
		String[] tokens = line.split(" ");
		if (tokens.length != 2) {
			// raise error
		}
		return tokens[1];
	}

}
