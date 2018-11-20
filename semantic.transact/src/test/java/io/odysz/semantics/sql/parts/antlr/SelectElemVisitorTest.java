package io.odysz.semantics.sql.parts.antlr;

import static org.junit.Assert.*;

import org.junit.Test;

import io.odysz.semantics.sql.parts.select.SelectElem;

public class SelectElemVisitorTest {

	@Test
	public void testParse() {
		String strExpr = "f . col";
		String expect = "f.col";
		SelectElem selem = SelectElemVisitor.parse(strExpr);
		String sql = selem.sql();
		System.out.print(sql);
		assertEquals(sql, expect);
	}

}
