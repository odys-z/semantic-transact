package io.odysz.semantics.meta;

import static org.junit.Assert.*;

import org.junit.Test;

import io.odysz.transact.x.TransException;

public class TableMetaTest {

	@Test
	public void testClone() {
		TableMeta tabl = new TableMeta("tab1")
			.col("c01", "text")
			.col("c02", "varchar")
			;

		T_SyntityMeta sub1 = new T_SyntityMeta("tab1");
		try {
			sub1.clone(tabl);
		} catch (TransException e) {
			tabl.col("forSubClass", "varchar2");
			try {
				sub1.clone(tabl);
			} catch (TransException e1) {
				fail("should found the field");
			}
		}
	}

}
