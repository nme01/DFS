package rso.dfs.client.commands;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class ExitCommand extends SimpleClientAction {

	@Override
	public String getCommandName() {
		return "exit";
	}

	@Override
	public String getHelp() {
		return "help for exit command";
	}

	@Override
	public void performCommand() {
		System.out.println("Bye");
		System.exit(0);

	}

}
