package rso.dfs.server.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Adam Papros<adam.papros@gmail.com>
 * 
 * */
public class FileStorageHandler implements StorageHandler {

	private static final String prefix = "tmp";

	@Override
	public byte[] readFile(long fileId, long offset) {
//		Path path = Paths.get(assemblePath(fileId));
//		byte[] bytes;
//		try {
//			bytes = Files.readAllBytes(path);
//		} catch (IOException e) {
//			throw new RuntimeException("Unable to read file. " + e);
//		}
		
		Path path = Paths.get(assemblePath(fileId));
		File inputFile = new File(assemblePath(fileId));
		byte [] bytes = new byte[0];
		try {
			FileInputStream fis = new FileInputStream(inputFile);
			fis.read(bytes, (int)offset, (int)(Files.size(path) - offset)); // TODO: chunk size
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
			// ...
		}
		return bytes;
	}

	@Override
	public void writeFile(long fileId, byte[] fileBody) {
		Path path = Paths.get(assemblePath(fileId));
		try {
			Files.write(path, fileBody);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String assemblePath(long fileId) {
		File file = new File(prefix + FileSystems.getDefault().getSeparator() + fileId);
		return file.getAbsolutePath();
	}

	@Override
	public void deleteFile(long fileId) {
		File file = new File(prefix + FileSystems.getDefault().getSeparator() + fileId);
		file.delete();
	}
	
	@Override
	public long getFileSize(long fileId) {
		Path path = Paths.get(assemblePath(fileId));
		long fileSize = 0;
		try {
			fileSize = Files.size(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileSize;
	}

	@Override
	public void createFile(long fileId) {
		File file = new File(assemblePath(fileId));
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
