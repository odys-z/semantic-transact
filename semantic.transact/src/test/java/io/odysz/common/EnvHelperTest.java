package io.odysz.common;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Test;

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
		assertEquals(home + "/v", EnvPath.replaceEnv("$HOME/v"));
		
		System.setProperty("VOLUME", "volume");
		assertEquals("volume/v", EnvPath.replaceEnv("$VOLUME/v"));
	}

	@Test
	public void testExtfilePathHandler() throws Exception {
		setEnv2("VOLUME_HOME", "/home/ody/volume");

		String[] args = "$VOLUME_HOME/shares,uri,userId,cate,docName".split(",");
		String extroot = args[0];

		String encoded = EnvPath.encodeUri(extroot, "ody", "000001 f.txt");
		assertEquals("$VOLUME_HOME/shares/ody/000001 f.txt", encoded);

		String abspath = EnvPath.decodeUri("", encoded);
		assertEquals("/home/ody/volume/shares/ody/000001 f.txt", abspath);
		
		args = "upload,uri,userId,cate,docName".split(",");
		encoded = EnvPath.encodeUri(extroot, "admin", "000002 f.txt");
		assertEquals("$VOLUME_HOME/shares/admin/000002 f.txt", encoded);

		abspath = EnvPath.decodeUri(rtroot, encoded);
		// assertEquals("/home/ody/upload/admin/000003 f.txt", abspath);
		assertEquals("/home/ody/volume/shares/admin/000002 f.txt", abspath);
		
		// Override
		System.setProperty("VOLUME_HOME", "/home/alice/vol");
		abspath = EnvPath.decodeUri(rtroot, encoded);
		assertEquals("/home/alice/vol/shares/admin/000002 f.txt", abspath);
	}
	
	/**Only Linux/MacOs
	 * https://stackoverflow.com/a/40682052/7362888
	 * @param newenv
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void setEnv2(String key, String value) {
	    try {
	        Map<String, String> env = System.getenv();
	        Class<?> cl = env.getClass();
	        Field field = cl.getDeclaredField("m");
	        field.setAccessible(true);
	        Map<String, String> writableEnv = (Map<String, String>) field.get(env);
	        writableEnv.put(key, value);
	    } catch (Exception e) {
	        throw new IllegalStateException("Failed to set environment variable", e);
	    }
	}


}
