package rso.dfs.model;

import org.junit.Assert;
import org.junit.Test;

public class ServerRoleTest {

	@Test
	public void getServerRoleTest() {
		Assert.assertEquals(ServerRole.MASTER, ServerRole.getServerRole("M"));
		Assert.assertEquals(ServerRole.SHADOW, ServerRole.getServerRole("H"));
		Assert.assertEquals(ServerRole.SLAVE, ServerRole.getServerRole("L"));

	}
}
