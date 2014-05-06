package rso.dfs.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;
import rso.dfs.client.commands.ClientAction;
import rso.dfs.client.commands.ExitCommand;
import rso.dfs.client.commands.GetCommand;
import rso.dfs.client.commands.HelpCommand;
import rso.dfs.client.commands.PutCommand;
import rso.dfs.client.commands.RemoveCommand;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class DFSClient {

	private List<ClientAction> clientActionList;
	private final String welcomeMessage = "Welcome to RSO DFS Client";

	private void initialize() {
		clientActionList = new ArrayList<>();
		clientActionList.add(new HelpCommand(clientActionList));
		clientActionList.add(new GetCommand());
		clientActionList.add(new PutCommand());
		clientActionList.add(new RemoveCommand());
		clientActionList.add(new ExitCommand());
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

		ConsoleReader reader = new ConsoleReader(System.in, System.out, new UnsupportedTerminal());
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
						clientAction.performCommand(line);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
		String line = reader.readLine(promtMessage + "\ndfs> ");
		return line.trim();
	}

	public static void main(String[] args) throws IOException {
		DFSClient dfsClient = new DFSClient();
		dfsClient.initialize();
		dfsClient.run();
	}
}
