package rso.dfs.utils;

import org.junit.Assert;
import org.junit.Test;

public class DFSArrayUtilsTest {

	@Test
	public void concatTest() {
		byte[] a = new byte[] { 2, 12, 12, 23, 1, 1 };
		byte[] b = new byte[] { 9, 99, 123 };
		byte[] expecteds = new byte[] { 2, 12, 12, 23, 1, 1, 9, 99, 123 };

		byte[] c = DFSArrayUtils.concat(a, b);
		Assert.assertArrayEquals(expecteds, c);

	}
}
