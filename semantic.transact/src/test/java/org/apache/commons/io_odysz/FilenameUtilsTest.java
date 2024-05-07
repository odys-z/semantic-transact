package org.apache.commons.io_odysz;

import static org.apache.commons.io_odysz.FilenameUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FilenameUtilsTest {

	@Test
	public void testConcatStringStringArray() {
//		try {
	     assertPathEquals("/foo/bar", concat("/foo/", "bar"));
	     assertPathEquals("/foo/bar", concat("/foo", "bar"));
	     assertPathEquals("/bar", concat("/foo", "/bar"));
	     assertPathEquals("C:/bar", concat("/foo", "C:/bar"));
	     assertPathEquals("C:bar", concat("/foo", "C:bar"));
	     assertPathEquals("/foo/bar", concat("/foo/a/", "../bar"));
	     assertPathEquals(null, concat("/foo/", "../../bar"));
	     assertPathEquals("/bar", concat("/foo/", "/bar"));
	     assertPathEquals("/bar", concat("/foo/..", "/bar"));
	     assertPathEquals("/foo/bar/c.txt", concat("/foo", "bar/c.txt"));
	     assertPathEquals("/foo/c.txt/bar", concat("/foo/c.txt", "bar"));
	     
	     assertPathEquals("/git/semantic-jserv/docsync.jserv/src/test/res/WEB-INF",
	    	   concat("/git/semantic-jserv/docsync.jserv", "./src/test/res/WEB-INF"));
//		}
//		catch (AssertionError e) {
//	     assertEquals("\\foo\\bar", concat("/foo/", "bar"));
//	     assertEquals("\\foo\\bar", concat("/foo", "bar"));
//	     assertEquals("\\bar", concat("/foo", "/bar"));
//	     assertEquals("C:\\bar", concat("/foo", "C:/bar"));
//	     assertEquals("C:bar", concat("/gfoo", "C:bar"));
//	     assertEquals("\\foo\\bar", concat("/foo/a/", "../bar"));
//	     assertEquals(null, concat("\\foo/", "../../bar"));
//	     assertEquals("\\bar", concat("/foo/", "/bar"));
//	     assertEquals("\\bar", concat("/foo/..", "/bar"));
//	     assertEquals("\\foo\\bar\\c.txt", concat("/foo", "bar/c.txt"));
//	     assertEquals("\\foo\\c.txt\\bar", concat("/foo/c.txt", "bar"));
//	     
//	     assertEquals("\\git\\semantic-jserv\\docsync.jserv\\src\\test\\res\\WEB-INF",
//	    	   concat("/git/semantic-jserv/docsync.jserv", "./src/test/res/WEB-INF"));
//			
//		}
	}

	public static void assertPathEquals(String expect, String actual) {
		try {
			assertEquals(expect, actual);
		} catch (AssertionError e) {
			assertEquals(expect.replaceAll("/", "\\\\"), actual);
		}
		
	}

}
