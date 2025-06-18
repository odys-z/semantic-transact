package io.odysz.common;

import static org.junit.jupiter.api.Assertions.*;

import static io.odysz.common.LangExt.eq;

import org.apache.commons.io_odysz.FilenameUtilsTest;
import org.junit.jupiter.api.Test;

public class EnvHelperTest {

	private static String rtroot = "src/test/res/";

	@Test
	public void testReplace() {
		String home = System.getenv("HOME");
		// for Windows
		if (home == null) {
			home = "~";
			System.setProperty("HOME", home);
		}
		FilenameUtilsTest.assertPathEquals(FilenameUtils.winpath2unix(home + "/v"), EnvPath.replaceEnv("$HOME/v"));
		
		System.setProperty("VOLUME", "volume");
		FilenameUtilsTest.assertPathEquals("volume/v", EnvPath.replaceEnv("$VOLUME/v"));
		
		assertEquals("c:/Alice/sqlite-main.db", FilenameUtils.winpath2unix("c:\\Alice\\sqlite-main.db"));
		
		EnvPath.extendEnv("VOLUME_HOME", "c:\\Alice");
		FilenameUtilsTest.assertPathEquals("c:/Alice/v", EnvPath.replaceEnv("$VOLUME_HOME/v"));

		System.setProperty("VOLUME_Y", "../deploy-Y");
		assertTrue(eq("../deploy-Y/ody/0001 1.jpg", EnvPath.replaceEnv("$VOLUME_Y/ody/0001 1.jpg"))
				|| eq("..\\deploy-Y\\ody\\0001 1.jpg", EnvPath.replaceEnv("$VOLUME_Y/ody/0001 1.jpg")));
	}

	@Test
	public void testExtfilePathHandler() throws Exception {
		// setEnv2("VOLUME_HOME", "/home/ody/volume");
		EnvPath.extendEnv("VOLUME_HOME", "/home/ody/volume");

		String[] args = "$VOLUME_HOME/shares,uri,userId,cate,docName".split(",");
		String extroot = args[0];

		// String encoded = EnvPath.encodeUri(extroot, "ody", "000001 f.txt");
		String encoded = FilenameUtils.concat(extroot, "ody", "000001 f.txt");
		FilenameUtilsTest.assertPathEquals("$VOLUME_HOME/shares/ody/000001 f.txt", encoded);

		String abspath = EnvPath.decodeUri("", encoded);
		FilenameUtilsTest.assertPathEquals("/home/ody/volume/shares/ody/000001 f.txt", abspath);
		
		args = "upload,uri,userId,cate,docName".split(",");
		// encoded = EnvPath.encodeUri(extroot, "admin", "000002 f.txt");
		encoded = FilenameUtils.concat(extroot, "admin", "000002 f.txt");
		FilenameUtilsTest.assertPathEquals("$VOLUME_HOME/shares/admin/000002 f.txt", encoded);

		abspath = EnvPath.decodeUri(rtroot, encoded);
		FilenameUtilsTest.assertPathEquals("/home/ody/volume/shares/admin/000002 f.txt", abspath);
		
		// Override
		System.setProperty("VOLUME_HOME", "/home/alice/vol");
		abspath = EnvPath.decodeUri(rtroot, encoded);
		FilenameUtilsTest.assertPathEquals("/home/alice/vol/shares/admin/000002 f.txt", abspath);
	}
	
}
