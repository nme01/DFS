package rso.dfs.event;

import rso.dfs.model.Server;

public class UpdateServerTask extends DFSTask {

	private final Server serverToUpdate;

	public UpdateServerTask(Server serverToUpdate, DBModificationType modificationType) {
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
	
	@Override
	public String toString() {
		return "UpdateServerTask [dao=" + dao + ", modificationType=" + modificationType + "]";
	}

}
