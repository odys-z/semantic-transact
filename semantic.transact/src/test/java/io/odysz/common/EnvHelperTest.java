package io.odysz.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.Map;

import org.apache.commons.io_odysz.FilenameUtils;
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
	}

	@Test
	public void testExtfilePathHandler() throws Exception {
		// setEnv2("VOLUME_HOME", "/home/ody/volume");
		EnvPath.extendEnv("VOLUME_HOME", "/home/ody/volume");

		String[] args = "$VOLUME_HOME/shares,uri,userId,cate,docName".split(",");
		String extroot = args[0];

		String encoded = EnvPath.encodeUri(extroot, "ody", "000001 f.txt");
		
		FilenameUtilsTest.assertPathEquals("$VOLUME_HOME/shares/ody/000001 f.txt", encoded);

		String abspath = EnvPath.decodeUri("", encoded);
		FilenameUtilsTest.assertPathEquals("/home/ody/volume/shares/ody/000001 f.txt", abspath);
		
		args = "upload,uri,userId,cate,docName".split(",");
		encoded = EnvPath.encodeUri(extroot, "admin", "000002 f.txt");
		FilenameUtilsTest.assertPathEquals("$VOLUME_HOME/shares/admin/000002 f.txt", encoded);

		abspath = EnvPath.decodeUri(rtroot, encoded);
		FilenameUtilsTest.assertPathEquals("/home/ody/volume/shares/admin/000002 f.txt", abspath);
		
		// Override
		System.setProperty("VOLUME_HOME", "/home/alice/vol");
		abspath = EnvPath.decodeUri(rtroot, encoded);
		FilenameUtilsTest.assertPathEquals("/home/alice/vol/shares/admin/000002 f.txt", abspath);
	}
	
	/**
	 * @deprecated no longer works in JDK 17.
	 * 
	 * Only Linux/MacOs
	 * https://stackoverflow.com/a/40682052/7362888
	 * @param newenv
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private static void setEnv2(String key, String value) {
	    try {
	        Map<String, String> env = System.getenv();
	        Class<?> cl = env.getClass();
	        Field field = cl.getDeclaredField("m");
	        // field.setAccessible(true);
	        field.trySetAccessible();
	        Map<String, String> writableEnv = (Map<String, String>) field.get(env);
	        writableEnv.put(key, value);
	    } catch (Exception e) {
	        throw new IllegalStateException("Failed to set environment variable", e);
	    }
	}


}
