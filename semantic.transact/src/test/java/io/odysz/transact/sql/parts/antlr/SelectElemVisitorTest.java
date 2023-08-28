package io.odysz.transact.sql.parts.antlr;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import io.odysz.transact.sql.parts.select.SelectElem;

public class SelectElemVisitorTest {

	@Test
	public void testParse() {
		String strExpr = "f . col";
		String expect = "f.col";
		SelectElem selem = SelectElemVisitor.parse(strExpr);
		String sql = selem.sql(null);
		assertEquals(sql, expect);
		
		// R is reserved, see TSqlLexer.g4
		strExpr = "r.roleId";
		selem = SelectElemVisitor.parse(strExpr);
		assertEquals("r.roleId", selem.sql(null));
	}

}
