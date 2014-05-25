package rso.dfs.event;

import rso.dfs.model.FileOnServer;

public class UpdateFilesOnServers extends DFSEvent {

	private final FileOnServer fileOnServerToUpdate;

	public UpdateFilesOnServers(FileOnServer fileOnServerToUpdate, DBModificationType modificationType) {
		super(modificationType);
		this.fileOnServerToUpdate = fileOnServerToUpdate;
	}

	public FileOnServer getFileOnServerToUpdate() {
		return fileOnServerToUpdate;
	}

	@Override
	public void execute() {
		switch (modificationType) {
		case SAVE: {
			dao.saveFileOnServer(fileOnServerToUpdate);
			return;
		}
		case DELETE: {
			dao.deleteFileOnServer(fileOnServerToUpdate);
			return;
		}
		case UPDATE: {
			dao.deleteFileOnServer(fileOnServerToUpdate);
			return;
		}
		default:
			throw new IllegalStateException();
		}

	}

}
