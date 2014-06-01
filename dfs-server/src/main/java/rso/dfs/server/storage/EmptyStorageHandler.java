package rso.dfs.server.storage;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class EmptyStorageHandler implements StorageHandler {

	private String message = "Master or shadow servers do not act as storage.";

	@Override
	public void writeFile(long fileId, int offset, byte[] fileBody) {
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

	@Override
	public byte[] readFile(long fileId, long offset) {
		throw new IllegalStateException(message);
	}

	@Override
	public long getFileSize(long fileId) {
		throw new IllegalStateException(message);
	}

}
