package rso.dfs.client.commands;

import java.util.List;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class HelpCommand extends SimpleClientAction {

	private List<ClientAction> clientActionList;

	public HelpCommand(List<ClientAction> clientActionList) {
		this.clientActionList = clientActionList;
	}

	@Override
	public String getCommandName() {
		return "help";
	}

	@Override
	public String getHelp() {
		return "prints this information :)";
	}

	@Override
	public void performCommand() {
		System.out.println(getHelpInformation());

	}

	private String getHelpInformation() {
		StringBuilder builder = new StringBuilder();
		builder.append("RSO DFS Client App:\nAvailable commands:\n");
		for (ClientAction clientAction : clientActionList) {
			builder.append(clientAction.getCommandName());
			builder.append("\t-\t");
			builder.append(clientAction.getHelp());
			builder.append(System.lineSeparator());
		}
		return builder.toString();
	}
}
