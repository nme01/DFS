package rso.dfs.event;

import rso.dfs.model.Server;

public class UpdateServerEvent extends DFSEvent {

	private final Server serverToUpdate;

	public UpdateServerEvent(Server serverToUpdate, DBModificationType modificationType) {
		super(modificationType);
		this.serverToUpdate = serverToUpdate;
	}

	public Server getServerToUpdate() {
		return serverToUpdate;
	}

	@Override
	public void execute() {
		switch (modificationType) {
		case SAVE: {
			dao.saveServerWithId(serverToUpdate);
			return;
		}
		case DELETE: {
			dao.deleteServer(serverToUpdate);
			return;
		}
		case UPDATE: {
			dao.updateServer(serverToUpdate);
			return;
		}

		default:
			throw new IllegalStateException();
		}
	}

}
