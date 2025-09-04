package io.odysz.common;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;

import static io.odysz.common.DateFormat.*;

import org.junit.jupiter.api.Test;

class DateFormatTest {

	@Test
	void testIsEarly() throws ParseException {
		assertFalse(early(jour0, "1911-10-10"));
		assertTrue(early(jour0, "1911-10-11"));
		assertTrue(early("", "1911-10-11"));
		assertTrue(early(null, "1911-10-11"));
		assertTrue(early("1911-10-11", "1949-10-01"));
		assertFalse(early("", "1911-10-10"));
		assertTrue(early(null, "1949-10-01"));
		assertFalse(early("1911-10-10", null));
		assertFalse(early("1949-10-01", null));

		assertTrue(early(null, "1911-10-10 12:30:00"));
		assertTrue(early("1911-10-10", "1911-10-10 12:30:00"));
		assertTrue(early(jour0, "1911-10-10 12:30:00"));
		assertTrue(early("1949-10-01 00:00:00", "1949-10-10 00:00:01"));
		assertFalse(early("1949-10-01 01:00:00", "1949-10-01 00:00:01"));
	}

}
