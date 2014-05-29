package rso.dfs.client.commands;

import jline.internal.Log;
import rso.dfs.client.handlers.GetHandler;
import rso.dfs.client.handlers.error.FileNotFoundError;
import rso.dfs.client.handlers.error.FileOperationError;

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
		return "Get file from file system.\n\t\tUsage:\n\t\t\t get file_name_in_DFS file_name_in_local_FS";
	}

	@Override
	public void performCommand(String line, String masterIP) {

		String[] tokens = line.split(" ");
		if (tokens.length != 3) {
			// raise error
			System.err.println("Get error, invalid number of args");
			return;
		}
		String filePathSrc = tokens[1];
		String filePathDst = tokens[2];

		GetHandler handler = new GetHandler(masterIP);
		try {
			handler.performGet(filePathSrc, filePathDst);
		} catch (FileNotFoundError fileNotFoundError) {
			System.err.println("File not found.");
			return;
		} catch (FileOperationError fileOperationError) {
			// Error should contain clear good reason which can be put
			// in front of the user's face.
			System.err.println(fileOperationError.getMessage());
			// voila!
			return;

		} catch (Exception e) {
			// TODO: handle exception
			// this place is probably the best to catch exceptions'n'shit
			Log.error(e);
			e.printStackTrace();
			return;
		}

	}

}
