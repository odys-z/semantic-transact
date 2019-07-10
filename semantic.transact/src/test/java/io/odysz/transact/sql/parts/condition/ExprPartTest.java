package io.odysz.transact.sql.parts.condition;

import static org.junit.Assert.*;

import org.junit.Test;

import io.odysz.transact.sql.parts.antlr.ExprsVisitor;
import io.odysz.transact.x.TransException;

public class ExprPartTest {

	@Test
	public void test() throws TransException {
		ExprPart e = ExprsVisitor.parse("0");
		assertEquals("0", e.sql(null));

		e = ExprsVisitor.parse("'abc'");
		assertEquals("'abc'", e.sql(null));

		e = ExprsVisitor.parse("3 * 'abc'");
		assertEquals("3 * 'abc'", e.sql(null));

		e = ExprsVisitor.parse("tabl.col + 1");
		assertEquals("tabl.col + 1", e.sql(null));

		e = ExprsVisitor.parse("tabl.col + 1 * 100");
		assertEquals("tabl.col + 1*100", e.sql(null));

		e = ExprsVisitor.parse("''");
		assertEquals("''", e.sql(null));

		e = ExprsVisitor.parse("'null'");
		assertEquals("'null'", e.sql(null));
	}
}
