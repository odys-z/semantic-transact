package io.odysz.semantics.meta;

import static org.junit.Assert.*;

import org.junit.Test;

import io.odysz.semantics.meta.ColMeta.coltype;

public class ColTypeTest {

	@Test
	public void testColType() {
		TableMeta t = new TableMeta("t0")
			.col("c01", "text")
			.col("c02", "varchar")
			.col("c03", "VARCHAR2")
			.col("c04", "date")
			.col("c05", "datetime")
			.col("c06", "timestamp")
			.col("c07", "int")
			.col("c08", "Float")
			.col("c09", "char")
			.col("c0a", "clob")
			.col("c0b", "BLOB")
			.col("c0c", "bin")
			;
		
		assertEquals(coltype.text, t.coltype("c01"));
		assertTrue(t.isQuoted("c01"));
		assertEquals(coltype.text, t.coltype("c02"));
		assertTrue(t.isQuoted("c02"));
		assertEquals(coltype.text, t.coltype("c03"));
		assertTrue(t.isQuoted("c03"));
		assertEquals(coltype.datetime, t.coltype("c04"));
		assertTrue(t.isQuoted("c04"));
		assertEquals(coltype.datetime, t.coltype("c05"));
		assertTrue(t.isQuoted("c05"));
		assertEquals(coltype.datetime, t.coltype("c06"));
		assertTrue(t.isQuoted("c06"));
		assertEquals(coltype.number, t.coltype("c07"));
		assertFalse(t.isQuoted("c07"));
		assertEquals(coltype.number, t.coltype("c08"));
		assertFalse(t.isQuoted("c08"));
		assertEquals(coltype.text, t.coltype("c09"));
		assertTrue(t.isQuoted("c09"));
		assertEquals(coltype.clob, t.coltype("c0a"));
		assertFalse(t.isQuoted("c0a"));
		assertEquals(coltype.bin, t.coltype("c0b"));
		assertFalse(t.isQuoted("c0b"));
		assertEquals(coltype.bin, t.coltype("c0c"));
		assertFalse(t.isQuoted("c0c"));
		
		
	}

}
