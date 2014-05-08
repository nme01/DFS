package rso.dfs.model.dao.psql;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rso.dfs.model.File;
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
	public Server getMasterServer() {
		// fetch master
		List<Server> list = modelDAO.fetchServersByRole(ServerRole.MASTER);

		if (list.size() > 1) {
			// raise fatal error
			System.exit(-123);
		} else if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public void saveMaster(Server server) {
		log.debug("Saving master:" + server.toString());
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
}
