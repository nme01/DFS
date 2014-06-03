package rso.dfs.event;

import rso.dfs.model.FileOnServer;

public class UpdateFilesOnServersTask extends DFSTask {

	private final FileOnServer fileOnServerToUpdate;

	public UpdateFilesOnServersTask(FileOnServer fileOnServerToUpdate, DBModificationType modificationType) {
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
			dao.updateFileOnServer(fileOnServerToUpdate);
			return;
		}
		default:
			throw new IllegalStateException();
		}

	}
	
	@Override
	public String toString() {
		return "UpdateFilesOnServersTask [dao=" + dao + ", modificationType=" + modificationType + "]";
	}

}
