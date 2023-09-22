package org.apache.commons.io_odysz;

import static org.apache.commons.io_odysz.FilenameUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FilenameUtilsTest {

	@Test
	public void testConcatStringStringArray() {
	     assertEquals("/foo/bar", concat("/foo/", "bar"));
	     assertEquals("/foo/bar", concat("/foo", "bar"));
	     assertEquals("/bar", concat("/foo", "/bar"));
	     assertEquals("C:/bar", concat("/foo", "C:/bar"));
	     assertEquals("C:bar", concat("/foo", "C:bar"));
	     assertEquals("/foo/bar", concat("/foo/a/", "../bar"));
	     assertEquals(null, concat("/foo/", "../../bar"));
	     assertEquals("/bar", concat("/foo/", "/bar"));
	     assertEquals("/bar", concat("/foo/..", "/bar"));
	     assertEquals("/foo/bar/c.txt", concat("/foo", "bar/c.txt"));
	     assertEquals("/foo/c.txt/bar", concat("/foo/c.txt", "bar"));
	     
	     assertEquals("/git/semantic-jserv/docsync.jserv/src/test/res/WEB-INF",
	    	   concat("/git/semantic-jserv/docsync.jserv", "./src/test/res/WEB-INF"));
	}

}
