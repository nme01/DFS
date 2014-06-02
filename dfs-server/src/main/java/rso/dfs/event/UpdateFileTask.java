package rso.dfs.event;

import rso.dfs.model.File;

/**
 * 
 * @author Adam Papros <adam.papros@gmial.com>
 */
public class UpdateFileTask extends DFSTask {

	private final File fileToUpdate;

	public UpdateFileTask(File fileToUpdate, DBModificationType modificationType) {
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
	
	@Override
	public String toString() {
		return "UpdateFileTask [dao=" + dao + ", modificationType=" + modificationType + "]";
	}

}
