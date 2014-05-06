package rso.dfs.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rso.dfs.model.ServerRole;
import rso.dfs.server.handler.FileStorageHandler;
import rso.dfs.server.handler.StorageHandler;
import rso.dfs.generated.CoreStatus;
import rso.dfs.generated.FilePart;
import rso.dfs.generated.FilePartDescription;
import rso.dfs.generated.GetFileParams;
import rso.dfs.generated.NewSlaveRequest;
import rso.dfs.generated.PutFileParams;
import rso.dfs.generated.Service;
import rso.dfs.generated.Service.Client;
import rso.dfs.generated.SystemStatus;
import rso.dfs.model.FileOnServer;
import rso.dfs.model.FileStatus;
import rso.dfs.model.Server;
import rso.dfs.model.dao.DFSModelDAO;
import rso.dfs.model.dao.DFSRepository;
import rso.dfs.model.dao.psql.DFSDataSource;
import rso.dfs.model.dao.psql.DFSModelDAOImpl;
import rso.dfs.model.dao.psql.DFSRepositoryImpl;
import rso.dfs.server.handler.FileStorageHandler;
import rso.dfs.utils.IpConverter;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * @author Mateusz Statkiewicz
 * */
public class ServerHandler implements Service.Iface {

	final static Logger log = LoggerFactory.getLogger(ServerHandler.class);

	/**
	 * This server. TODO : provide better naming... :(
	 * */
	private Server me;

	/**
	 * Provides storage operations.
	 * */

	private DFSRepository repository;
	private FileStorageHandler storageHandler;
	private CoreStatus coreStatus;
	private DFSModelDAO modelDAO;
	
	private StorageHandler storageHandler;

	public ServerHandler(Server me) {
		this(me, new DFSModelDAOImpl(new DFSDataSource()));

	}

	public ServerHandler(Server me, DFSModelDAO modelDAO) {
		this.me = me;
		this.modelDAO = modelDAO;
		this.storageHandler = new FileStorageHandler();
		repository = new DFSRepositoryImpl();
		this.storageHandler = new FileStorageHandler();
	}

	@Override
	public SystemStatus getStatus() throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CoreStatus registerSlave(NewSlaveRequest req) throws TException {
		modelDAO = new DFSModelDAOImpl(new DFSDataSource());
		Server server = new Server();
		// server.setIp(); //skad mam to wziac? do strukturki trzeba dodac? to
		// samo nizej
		// server.setMemory(memory);
		// server.setRole();
		// modelDAO.saveServer(server);

		List<Long> ids = req.getFileIds();
		for (long fileId : ids) {
			if (modelDAO.fetchFileById(fileId) != null) {
				FileOnServer fileOnServer = new FileOnServer();
				fileOnServer.setFileId(fileId);
				fileOnServer.setServerId(server.getId());
				// fileOnServer.setPriority(0);
				modelDAO.saveFileOnServer(fileOnServer);
				ids.remove(fileId);
			}
			;
		}
		TTransport transport;

		transport = new TSocket("localhost", 9090); // skad wziac ipka i port?
		transport.open();

		TProtocol protocol = new TBinaryProtocol(transport);
		Service.Client client = new Service.Client(protocol);
		for (long fileId : ids) {
			client.removeFileSlave(fileId);
		}
		return coreStatus;
	}

	@Override
	public void updateCoreStatus(CoreStatus status) throws TException {
		coreStatus = status;

	}

	@Override
	public void becomeShadow(CoreStatus status) throws TException {
		// TODO Auto-generated method stub

	}

	@Override
	public CoreStatus getCoreStatus() throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void prepareForReceiving(long fileID, long size) throws TException {
		// TODO Auto-generated method stub

	}

	@Override
	public void replicate(long fileID, int slaveIP) throws TException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isFileUsed(long fileID) throws TException {
		// to w ogole potrzebne??
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeFileSlave(long fileID) throws TException {
		storageHandler.deleteFile(fileID);
	}

	/**
	 * Invoked on master by client. Return to client info about desired file.
	 * 
	 * @param fileName
	 * @return GetFileParams
	 */
	@Override
	public GetFileParams getFile(String fileName) throws TException {
		log.debug("getFile Invoked");
		rso.dfs.model.File file = repository.getFileByFileName(fileName);

		Server slave = repository.getSlaveByFile(file);

		GetFileParams getFileParams = new GetFileParams();
		getFileParams.setFileId(file.getId());

		try {
			getFileParams.setSlaveIp(IpConverter.getIntegerIpformString(slave
					.getIp()));
		} catch (UnknownHostException e) {
			e.printStackTrace();

		}
		return getFileParams;
	}

	@Override
	public GetFileParams getFileFailure(String filepath) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Invoked on master by client. Return to client info where to put new file.
	 * Add new file to DB.
	 * 
	 * @param filepath
	 * @param size
	 * @return PutFileParams
	 */
	@Override
	public PutFileParams putFile(String filepath, long size) throws TException {
		List<Server> slaves = modelDAO.fetchServersByRole(ServerRole.SLAVE);
		Collections.shuffle(slaves);
		String slaveAddress = null;
		long slaveId = -1;
		for (Server server : slaves) {
			long filesMemory = 0;
			for (rso.dfs.model.File file : modelDAO.fetchFilesOnServer(server)) {
				filesMemory += file.getSize();
			}
			if (server.getMemory() - filesMemory <= size)
				continue;

			slaveAddress = server.getIp();
			slaveId = server.getId();
			break;
		}

		PutFileParams putFileParams = new PutFileParams();
		putFileParams.setCanPut(true);
		if (slaveAddress == null) {
			putFileParams.setCanPut(false);
		} else {
			try {
				rso.dfs.model.File file = new rso.dfs.model.File();
				file.setName(filepath);
				file.setSize(size);
				file.setStatus(FileStatus.UPLOAD);
				Long fileid = modelDAO.saveFile(file);
				modelDAO.saveFileOnServer(new FileOnServer(fileid, slaveId, 0));

				putFileParams.setCanPut(true);
				putFileParams.setSlaveIp(IpConverter
						.getIntegerIpformString(slaveAddress));
				putFileParams.setFileId(fileid);
			} catch (Exception e) {
				//wysypka;
			}
		}
		return putFileParams;
	}

	@Override
	public PutFileParams putFileFailure(String filepath, long size)
			throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Invoked on master by client. Order master to remove file.
	 * 
	 * @param filepath
	 * @return boolean
	 */
	@Override
	public boolean removeFile(String filepath) throws TException {
		final rso.dfs.model.File file = modelDAO.fetchFileByFileName(filepath);
		file.setStatus(FileStatus.TO_DELETE);
		modelDAO.updateFile(file);
		
		Thread thread = new Thread(new Runnable() {
			public void run() {
				List<Server> servers = modelDAO.fetchServersByFileId(file
						.getId());
				for (Server server : servers) {
					Service.Client client = new Client(
							getTprotocol(server.getIp()));
					try {
						client.removeFileSlave((int) file.getId());
					} catch (TException e1) {
						e1.printStackTrace();
					}
					FileOnServer fos = new FileOnServer();
					fos.setFileId(file.getId());
					try {
						fos.setServerId((long) IpConverter
								.getIntegerIpformString(server.getIp()));
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
					
					modelDAO.deleteFileOnServer(fos);
				}

				modelDAO.deleteFile(file);
			}
		});
		thread.start();
		return true;
	}

	/**
	 * Invoked on a slave by the master. Orders the slave to secure a transfer
	 * handler for a file from a certain client.
	 * 
	 * @param fileId
	 * @return FilePartDescription
	 */
	@Override
	public FilePartDescription sendFileToSlaveRequest(long fileId)
			throws TException {

		File file = new File(FileStorageHandler.assemblePath(fileId));
		try {
			file.createNewFile();
		} catch (IOException ioe) {
			return null;
		}
		return new FilePartDescription((int) fileId, (long) 0);
	}

	/**
	 * Invoked on a slave by a client. Saves a part of a file onto storage.
	 * 
	 * @param filePart
	 * @return FilePartDescription
	 */
	@Override
	public FilePartDescription sendFilePartToSlave(FilePart filePart)
			throws TException {
		try {
			// TODO: check if the filePart is from the expected client
			byte[] dataBuffer = filePart.data.array();
			log.info("Got a file part of fileid " + filePart.fileId + " of size " + filePart.data.array().length + " bytes.");
			storageHandler.writeFile(filePart.fileId, dataBuffer);
			
			return new FilePartDescription(filePart.fileId, filePart.offset + dataBuffer.length);

		} catch (Exception e) {
			// welcome to mcdonalds, do you want fries with that
		}

		return null;
	}

	/**
	 * Dummy version sending whole file
	 * 
	 * @return null - when whole file was sent (filePartDescription.getOffset is
	 *         equal to file size.
	 * */
	@Override
	public FilePart getFileFromSlave(FilePartDescription filePartDescription)
			throws TException {

		if (me.getRole() == rso.dfs.model.ServerRole.MASTER) {
			return new FilePart(-1, 0, ByteBuffer.allocate(0));
		}

		byte[] dataToSend = storageHandler.readFile(filePartDescription
				.getFileId());
		if (filePartDescription.getOffset() >= dataToSend.length) {
			FilePart filePart = new FilePart();
			filePart.setFileId(filePartDescription.getFileId());
			filePart.setData(new byte[0]);
			return filePart;
		}

		// assemble filePart
		FilePart filePart = new FilePart();
		filePart.setFileId(filePartDescription.getFileId());
		filePart.setData(dataToSend);
		return filePart;
	}

	private TProtocol getTprotocol(String ip) {
		TTransport transport;
		transport = new TSocket(ip, 9090);
		try {
			transport.open();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
		TProtocol protocol = new TBinaryProtocol(transport);
		return protocol;
	}
}
