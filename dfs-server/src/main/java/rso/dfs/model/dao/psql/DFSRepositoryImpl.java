package rso.dfs.model.dao.psql;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rso.dfs.model.File;
import rso.dfs.model.FileOnServer;
import rso.dfs.model.Server;
import rso.dfs.model.ServerRole;
import rso.dfs.model.dao.DFSModelDAO;
import rso.dfs.model.dao.DFSRepository;

/**
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class DFSRepositoryImpl implements DFSRepository {

	final static Logger log = LoggerFactory.getLogger(DFSRepository.class);

	private DFSModelDAO modelDAO;
	
	private DFSDataSource dataSource;

	public DFSRepositoryImpl() {
		dataSource = new DFSDataSource();
		modelDAO = new DFSModelDAOImpl(dataSource);
	}

	@Override
	public void deleteFile(final File file) {
		int numberOfAffectedRows = modelDAO.deleteFile(file);
	}
	
	@Override
	public Server getMasterServer() {
		// fetch master
		List<Server> list = modelDAO.fetchServersByRole(ServerRole.MASTER);

		if (list.size() > 1) {
			// raise fatal error AND WRITE LOG MESSAGE
			log.error("More than one master found in DB, core panic");
			System.exit(-1);
			
		} else if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public void saveServer(Server server) {
		log.debug("Saving server:" + server.toString());
		modelDAO.saveServer(server);
	}

	@Override
	public File getFileByFileName(String fileName) {
		log.debug("Fetching file: fileName=" + fileName);
		return modelDAO.fetchFileByFileName(fileName);
	}

	@Override
	public Server getSlaveByFile(File file) {
		log.debug("Fetching servers with file: " + file.getName());

		List<Server> servers = modelDAO.fetchServersByFileId(file.getId());

		// TODO :choose server !
		if (servers == null || servers.isEmpty()) {
			// raise fatal error
		}

		return servers.get(0);
	}

	@Override
	public List<Server> getSlaves() {
		log.debug("");
		return modelDAO.fetchServersByRole(ServerRole.SLAVE);
	}

	@Override
	public File getFileById(Integer fileId) {
		log.debug("");
		return modelDAO.fetchFileById(fileId);
	}

	@Override
	public void saveFileOnServer(FileOnServer fileOnServer) {
		log.debug("");
		modelDAO.saveFileOnServer(fileOnServer);
		
	}

	@Override
	public List<File> getFilesOnSlave(Server slave) {
		log.debug("");
		return modelDAO.fetchFilesOnServer(slave);
	}

	@Override
	public Integer saveFile(final File file) {
		log.debug("");
		return modelDAO.saveFile(file).intValue();
	}
	
	@Override
	public void updateFile(final File file) {
		log.debug("");
		int numberOfAffectedRows = modelDAO.updateFile(file);
	}

	@Override
	public void deleteFileOnServer(FileOnServer fileOnServer) {
		log.debug("");
		int numberOfAffectedRows = modelDAO.deleteFileOnServer(fileOnServer);
		
	}

	@Override
	public List<Server> getSlavesByFile(File file) {
		log.debug("");
		return modelDAO.fetchServersByFileId(file.getId());

	}

	@Override
	public void cleanDB() {
		modelDAO.cleanDB();
	}
	@Override
	public List<File> getAllFiles() {
		return modelDAO.fetchAllFiles();
	}

	@Override
	public Server getServerByIp(String ip) {
		return modelDAO.fetchServerByIp(ip);
	}
}
