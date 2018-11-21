package io.odysz.semantics.sql.parts.antlr;

import static org.junit.Assert.*;

import org.junit.Test;

import io.odysz.common.Utils;
import io.odysz.semantics.sql.parts.condition.Condit;

public class ConditVisitorTest {
	@Test
	public void testLike() {
		String expr = "f.col LIKE 'abc'";
		String expect = "f.col like '%abc%'";
		test(expr, expect);

		expr = "f.col % 'abc'";
//		expect = "f.col like '%abc'";
		test(expr, expect);

		expr = "f.col %~ 'abc'";
		expect = "f.col like '%abc'";
		test(expr, expect);
	}

	@Test
	public void testIn() {
		String strExpr = "userId in ('1', '2')";
		String expect = "userId in ('1', '2')";
		Condit condt = ConditVisitor.parse(strExpr);
		String sql = condt.sql();
		Utils.logi(sql);
		assertEquals(sql, expect);
	}
	
	@Test
	public void testAndOrPrecedence() {
		String strExpr = "a.id = b.roleId and (a.c>d OR x> y) and a.c = '5'";
		String expect = "a.id = b.roleId AND (a.c > d OR x > y) AND a.c = '5'";
		Condit condt = ConditVisitor.parse(strExpr);
		String sql = condt.sql();
		Utils.logi(sql);
		assertEquals(sql, expect);
	}
	
	private void test(String expect, String actual) {
		Condit condt = ConditVisitor.parse(expect);
		String sql = condt.sql();
		Utils.logi(sql);
		assertEquals(sql, actual);
	}
	

}
