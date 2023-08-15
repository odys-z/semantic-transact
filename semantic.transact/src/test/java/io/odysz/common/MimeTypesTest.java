package io.odysz.common;

import static org.junit.Assert.*;

import static io.odysz.common.MimeTypes.*;

import org.junit.Test;

public class MimeTypesTest {

	@Test
	public void testIsImgVideo() {
		assertTrue(isImgVideo("image/png,data..."));
		assertTrue(isImgVideo("image/jpeg,data..."));
		assertTrue(isImgVideo("video/mp4,data..."));
		assertFalse(isImgVideo("audio/mp3,data..."));
	}

	@Test
	public void testIsAudio() {
		assertTrue(isAudio("audio/mp3,data..."));
		assertFalse(isAudio("image/jpeg,data..."));
	}

	@Test
	public void testIsVideo() {
		assertTrue(isVideo("video/mp4,data..."));
		assertFalse(isVideo("image/jpeg,data..."));
	}

	@Test
	public void testIsPdf() {
		assertTrue(isPdf("application/pdf,data..."));
		assertFalse(isPdf("image/pdf,data..."));
	}

}
