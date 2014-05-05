package rso.dfs.model.dao.psql;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import rso.dfs.model.File;
import rso.dfs.model.FileOnServer;
import rso.dfs.model.FileStatus;
import rso.dfs.model.Server;
import rso.dfs.model.ServerRole;

public class DFSModelDAOImplTest extends IntegrationTestBase {

	private File createTestFile() {
		File file = new File();

		file.setName("test_file_" + new DateTime().toString());
		file.setSize(1233);
		file.setStatus(FileStatus.UPLOAD);

		return file;
	}

	@Test(expected = EmptyResultDataAccessException.class)
	public void fileDAOTest() {

		File file = createTestFile();

		Long fileId = modelDAO.saveFile(file);

		File fetchedFile = modelDAO.fetchFileById(fileId);
		Assert.assertEquals(file.getName(), fetchedFile.getName());

		Assert.assertEquals(file.getSize(), fetchedFile.getSize());
		Assert.assertEquals(file.getStatus(), fetchedFile.getStatus());

		modelDAO.deleteFile(fetchedFile);

		fetchedFile = modelDAO.fetchFileById(fileId);

	}

	private Server createTestServer() {
		Server server = new Server();
		server.setIp("123.123.123.123");
		server.setMemory(1123);
		server.setRole(ServerRole.MASTER);
		server.setLastConnection(new DateTime());
		return server;
	}

	@Test(expected = EmptyResultDataAccessException.class)
	public void serverDAOTest() {

		Server server = createTestServer();

		Long serverId = modelDAO.saveServer(server);

		Server fetchedServer = modelDAO.fetchServerByIp(server.getIp());
		
		Assert.assertEquals(server.getIp(), fetchedServer.getIp());
		Assert.assertEquals(server.getMemory(), fetchedServer.getMemory());
		Assert.assertEquals(server.getLastConnection(), fetchedServer.getLastConnection());
		Assert.assertEquals(server.getRole(), fetchedServer.getRole());

		modelDAO.deleteServer(fetchedServer);

		fetchedServer = modelDAO.fetchServerByIp(server.getIp());

	}

	@Test
	public void fileOnServerDAOTest() {

		Server server = createTestServer();

		Long serverId = modelDAO.saveServer(server);

		server.setId(serverId);
		
		File file = createTestFile();

		Long fileId = modelDAO.saveFile(file);
		file = modelDAO.fetchFileByFileName(file.getName());

		FileOnServer fileOnServer = new FileOnServer();
		fileOnServer.setFileId(fileId);
		fileOnServer.setServerId(server.getId());
		fileOnServer.setPriority(123l);

		modelDAO.saveFileOnServer(fileOnServer);
		List<Server> servers = modelDAO.fetchServersByFileId(fileId);

		Server server2 = null;
		for (Server s : servers) {
			if (server.equals(s)) {
				server2 = s;
			}
		}
		Assert.assertNotNull(server2);
		Assert.assertEquals(server.getIp(), server2.getIp());

		modelDAO.deleteFileOnServer(fileOnServer);
		modelDAO.deleteServer(server);
		modelDAO.deleteFile(file);
	}

	@Test
	public void testDatabaseConnectionTestTest() {
		modelDAO.testDatabaseConnection();
	}
}
