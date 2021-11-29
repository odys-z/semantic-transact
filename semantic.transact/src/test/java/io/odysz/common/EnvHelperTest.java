package io.odysz.common;

import static org.junit.Assert.*;

import org.junit.Test;

public class EnvHelperTest {

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


}
