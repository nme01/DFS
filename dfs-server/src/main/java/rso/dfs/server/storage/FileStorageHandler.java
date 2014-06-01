package rso.dfs.server.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rso.dfs.commons.DFSProperties;
import rso.dfs.server.ServerHandler;

/**
 * @author Adam Papros<adam.papros@gmail.com>
 * 
 * */
public class FileStorageHandler implements StorageHandler {

	private static final String prefix = DFSProperties.getProperties().getDirectory();
	private final static Logger log = LoggerFactory.getLogger(FileStorageHandler.class);

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
		byte [] bytes = new byte[0];
		int fileSize = 0;
		try {
			RandomAccessFile raf = new RandomAccessFile(new File(path.toString()), "r");
			raf.read(bytes, (int)offset, DFSProperties.getProperties().getFilePartSize().intValue());
			raf.close();
		} catch (Exception e) {
			log.error("readFile: error reading file " + assemblePath(fileId) + ", offset " + (int)offset + ", size " + fileSize);
			e.printStackTrace();
			// ...
		}
		return bytes;
	}

	@Override
	public void writeFile(long fileId, int offset, byte[] fileBody) {
		Path path = Paths.get(assemblePath(fileId));
		try {
			RandomAccessFile raf = new RandomAccessFile(new File(path.toString()), "r");
			raf.write(fileBody, (int)offset, DFSProperties.getProperties().getFilePartSize().intValue());
			raf.close();
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
			//e.printStackTrace();
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
