package rso.dfs.model.dao.psql;

import java.util.List;

import rso.dfs.model.File;
import rso.dfs.model.FileOnServer;
import rso.dfs.model.Query;
import rso.dfs.model.Server;
import rso.dfs.model.dao.DFSRepository;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class EmptyRepository implements DFSRepository {

	private String message = "Slave server can not use repository.";

	@Override
	public void deleteFile(File file) {
		throw new IllegalStateException(message);

	}

	@Override
	public void deleteFileOnServer(FileOnServer fileOnServer) {
		throw new IllegalStateException("Slave server can not use repository");
	}

	@Override
	public Server getMasterServer() {
		throw new IllegalStateException(message);
	}

	@Override
	public File getFileByFileName(String fileName) {
		throw new IllegalStateException(message);
	}

	@Override
	public Server getSlaveByFile(File file) {
		throw new IllegalStateException(message);
	}

	@Override
	public List<Server> getSlavesByFile(File file) {
		throw new IllegalStateException(message);
	}

	@Override
	public List<Server> getSlaves() {
		throw new IllegalStateException(message);
	}

	@Override
	public List<File> getFilesOnSlave(Server server) {
		throw new IllegalStateException(message);
	}

	@Override
	public void saveFileOnServer(FileOnServer fileOnServer) {
		throw new IllegalStateException(message);
	}

	@Override
	public Integer saveFile(File file) {
		throw new IllegalStateException(message);
	}

	@Override
	public void updateFile(File file) {
		throw new IllegalStateException(message);
	}

	@Override
	public void saveServer(Server server) {
		throw new IllegalStateException(message);

	}

	@Override
	public File getFileById(Integer fileId) {
		throw new IllegalStateException(message);
	}

	@Override
	public void cleanDB() {
		throw new IllegalStateException(message);

	}

	@Override
	public List<File> getAllFiles() {
		throw new IllegalStateException(message);
	}

	@Override
	public Server getServerByIp(String ip) {
		throw new IllegalStateException(message);
	}

	@Override
	public List<Query> getQueriesAfter(long version) {
		throw new IllegalStateException(message);
	}

	@Override
	public List<Query> getAllQueries() {
		throw new IllegalStateException(message);
	}

	@Override
	public List<Server> getShadows() {
		throw new IllegalStateException(message);
	}

}
