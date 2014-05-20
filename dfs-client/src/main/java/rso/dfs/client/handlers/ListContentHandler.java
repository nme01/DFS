package rso.dfs.client.handlers;

import java.util.ArrayList;
import java.util.List;

import rso.dfs.commons.DFSProperties;
import rso.dfs.generated.Service;
import rso.dfs.utils.DFSClosingClient;

public class ListContentHandler extends HandlerBase {

	public ListContentHandler(String masterIpAddress) {
		super(masterIpAddress);
		// TODO Auto-generated constructor stub
	}

	public void performLS() {
		List<String> fileList = new ArrayList<>();
		try (DFSClosingClient closingClient = new DFSClosingClient(masterIpAddress, DFSProperties.getProperties().getNamingServerPort())) {
			Service.Client serviceClient = closingClient.getClient();
			fileList = serviceClient.listFileNames();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (String fileName : fileList) {
			System.out.println(fileName);
		}

	}

}
