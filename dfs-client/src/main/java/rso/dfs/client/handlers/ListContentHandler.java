package rso.dfs.client.handlers;

import java.util.List;

import rso.dfs.commons.DFSProperties;
import rso.dfs.generated.Service;
import rso.dfs.utils.DFSClosingClient;

public class ListContentHandler extends HandlerBase {

	public ListContentHandler(String masterIpAddress) {
		super(masterIpAddress);
		// TODO Auto-generated constructor stub
	}

	public List<String> performLS() {
		try (DFSClosingClient closingClient = new DFSClosingClient(masterIpAddress, DFSProperties.getProperties().getNamingServerPort())) {
			Service.Client serviceClient = closingClient.getClient();
			// serviceClient. get Files List
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
