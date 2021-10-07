package io.odysz.common;

import static org.junit.Assert.*;

import org.junit.Test;

public class EnvHelperTest {

	@Test
	public void testReplace() {
		String home = System.getenv("HOME");
		assertEquals(home + "/v", EnvHelper.replaceEnv("$HOME/v"));
		
		System.setProperty("VOLUME", "volume");
		assertEquals("volume/v", EnvHelper.replaceEnv("$VOLUME/v"));
		
		assertEquals("volume", EnvHelper.startVar("$VOLUME/v"));
	}

	@Test
	public void testIsRelativePath() {
		assertFalse(EnvHelper.isRelativePath("/"));
		assertFalse(EnvHelper.isRelativePath("$"));
		assertTrue(EnvHelper.isRelativePath("home"));
		assertFalse(EnvHelper.isRelativePath("$HOME/v"));
	}

}
