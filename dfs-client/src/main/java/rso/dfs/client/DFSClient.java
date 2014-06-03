package rso.dfs.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.thrift.transport.TTransportException;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;
import jline.internal.Log;
import rso.dfs.client.commands.ClientAction;
import rso.dfs.client.commands.ExitCommand;
import rso.dfs.client.commands.GetCommand;
import rso.dfs.client.commands.HelpCommand;
import rso.dfs.client.commands.ListContentCommand;
import rso.dfs.client.commands.PutCommand;
import rso.dfs.client.commands.RemoveCommand;
import rso.dfs.commons.DFSProperties;
import rso.dfs.generated.CoreStatus;
import rso.dfs.generated.FilePart;
import rso.dfs.generated.Service;
import rso.dfs.generated.Service.Client;
import rso.dfs.utils.DFSClosingClient;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class DFSClient {

	private List<ClientAction> clientActionList;
	private final String welcomeMessage = "Welcome to RSO DFS Client";
	private String masterIPAddress = null;
	
	private void initialize(String args[]) {
		if (args.length < 1)
		{
			//FIXME: use some logger or sth
			System.err.println("You should provide ip address as arg"); 
			System.exit(-1);
		}
		
		//get master ip address
		
		
		
		try (DFSClosingClient ccClient = new DFSClosingClient(args[0], 
				DFSProperties.getProperties().getStorageServerPort())) {
			Service.Client serviceClient = ccClient.getClient();
			CoreStatus coreStatus = serviceClient.getCoreStatus();
			masterIPAddress = coreStatus.getMasterAddress();
			System.out.println("Master IP is " + masterIPAddress);
		}catch (Exception e){
			System.err.println("Service is not available on given ip: " + args[0] + ", exiting...");
			System.exit(-1);
		}
		
		
		
		try(DFSClosingClient cclient = 
				new DFSClosingClient(masterIPAddress,
						DFSProperties.getProperties().getStorageServerPort(),2000)) //FIXME: magicnumber
		{
			Client client = cclient.getClient();
			client.pingServer();
		} catch (Exception e) {
			System.err.println("Naming service is not available on ip: " + masterIPAddress + ", exiting...");
			System.exit(-1);
		}
		
		clientActionList = new ArrayList<>();
		clientActionList.add(new HelpCommand(clientActionList));
		clientActionList.add(new GetCommand());
		clientActionList.add(new PutCommand());
		clientActionList.add(new RemoveCommand());
		clientActionList.add(new ExitCommand());
		clientActionList.add(new ListContentCommand());
	}

	private Collection<String> getCommandList() {
		ArrayList<String> commands = new ArrayList<>();

		for (ClientAction clientAction : clientActionList) {
			commands.add(clientAction.getCommandName());
		}

		return commands;
	}

	public void run() throws IOException {
		printWelcomeMessage();

		ConsoleReader reader = new ConsoleReader(System.in, System.out, 
				new UnsupportedTerminal());
		// TODO: flavored terminals do not work under eclipse -> in development
		// we use UnsupportedTerminal
		// but UnsupportedTerminal does not provide autocomplete feature :(

		// TODO: in production use this line
		// ConsoleReader reader = new ConsoleReader(System.in, System.out,
		// TerminalFactory.get());

		// TODO: or just
		// ConsoleReader reader = new ConsoleReader();

		reader.setBellEnabled(false);
		List completors = new LinkedList();

		completors.add(new StringsCompleter(getCommandList()));
		reader.addCompleter(new ArgumentCompleter(completors));

		String line;
		PrintWriter out = new PrintWriter(System.out);

		while ((line = readLine(reader, "")) != null) {
			boolean commandPerformed = false;
			for (ClientAction clientAction : clientActionList) {
				if (clientAction.correspondsToString(line)) {
					try {
						clientAction.performCommand(line,masterIPAddress);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.error(e);
						//e.printStackTrace();
					}
					commandPerformed = true;
				}
			}
			if (!commandPerformed) {
				if (line.isEmpty()) {

				} else {
					System.out.println("Invalid command, for assistance type help");
				}
			}
			out.flush();
		}
	}

	private void printWelcomeMessage() {
		System.out.println(welcomeMessage);

	}

	private String readLine(ConsoleReader reader, String promtMessage) throws IOException {
		try{
			String line = reader.readLine(promtMessage + "\ndfs> ");
			return line.trim();
		} catch (NullPointerException e){
			return null;
		}
	}

	public static void main(String[] args) throws IOException {
		DFSClient dfsClient = new DFSClient();
		dfsClient.initialize(args);
		dfsClient.run();
	}
}
