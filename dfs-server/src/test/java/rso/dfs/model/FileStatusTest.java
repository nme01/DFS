package rso.dfs.model;

import org.junit.Assert;
import org.junit.Test;

public class FileStatusTest {

	@Test
	public void getFileStatusTest() {
		Assert.assertEquals(FileStatus.UPLOAD, FileStatus.getFileStatus("U"));
		Assert.assertEquals(FileStatus.TO_DELETE, FileStatus.getFileStatus("D"));
		Assert.assertEquals(FileStatus.HELD, FileStatus.getFileStatus("H"));

	}
}
