package rso.dfs.client.commands;

import java.io.File;

import jline.internal.Log;
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
	public void performCommand(String line, String masterIP) {

		String[] tokens = line.split(" ");
		if (tokens.length != 3) {
			// raise error
			System.err.println("Get error, invalid number of args");
		}
		String filePathSrc = tokens[1];
		String filePathDst = tokens[2];
		
		File thisFile = new File(filePathSrc);
		long fileSize = thisFile.length();
		
		PutHandler handler = new PutHandler(masterIP);
		try {
			handler.performPut(filePathSrc,filePathDst, fileSize);
		} catch (Exception e) {
			Log.error(e);
			//e.printStackTrace();
		}
	}
	
	private String assembleFileName(final String line) {
		String[] tokens = line.split(" ");
		if (tokens.length != 2) {
			// raise error
		}
		return tokens[1];
	}

}
