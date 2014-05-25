package rso.dfs.event;

import rso.dfs.model.File;

public class UpdateFileEvent extends DFSEvent {

	private final File fileToUpdate;

	public UpdateFileEvent(File fileToUpdate, DBModificationType modificationType) {
		super(modificationType);
		this.fileToUpdate = fileToUpdate;
	}

	public File getFileToUpdate() {
		return fileToUpdate;
	}

	@Override
	public void execute() {
		switch (modificationType) {
		case SAVE: {
			dao.saveFileWithId(fileToUpdate);
			return;
		}
		case UPDATE: {
			dao.updateFile(fileToUpdate);
			return;
		}
		case DELETE: {
			dao.deleteFile(fileToUpdate);
			return;
		}
		default: {
			throw new IllegalStateException("");
		}
		}

	}

}
