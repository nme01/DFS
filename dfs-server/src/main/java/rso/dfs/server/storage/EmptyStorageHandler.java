package rso.dfs.server.storage;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class EmptyStorageHandler implements StorageHandler {

	private String message = "Master or shadow server can not use storage handler.";

	@Override
	public byte[] readFile(long fileId) {
		throw new IllegalStateException(message);
	}

	@Override
	public void writeFile(long fileId, byte[] fileBody) {
		throw new IllegalStateException(message);

	}

	@Override
	public void deleteFile(long fileId) {
		throw new IllegalStateException(message);

	}

	@Override
	public void createFile(long fileId) {
		throw new IllegalStateException(message);

	}

}
