package rso.dfs.event;

import rso.dfs.model.dao.DFSModelDAO;

public abstract class DFSTask implements Executable{

	protected DFSModelDAO dao;

	protected DBModificationType modificationType;

	public DFSTask(DBModificationType modificationType) {
		this.modificationType = modificationType;
	}
	public DFSModelDAO getDao() {
		return dao;
	}

	public void setDao(DFSModelDAO dao) {
		this.dao = dao;
	}

	public DBModificationType getModificationType() {
		return modificationType;
	}

	public void setModificationType(DBModificationType modificationType) {
		this.modificationType = modificationType;
	}

}
