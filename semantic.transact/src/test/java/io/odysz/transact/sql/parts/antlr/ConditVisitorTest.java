package io.odysz.transact.sql.parts.antlr;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.odysz.common.Utils;
import io.odysz.transact.sql.parts.condition.Condit;

public class ConditVisitorTest {
	@Test
	public void testLike() {
		String expr = "f.col LIKE 'abc'";
		test("f.col like '%abc%'", expr);

		expr = "f.col % 'abc'";
		test("f.col like '%abc%'", expr);

		expr = "f.col %~ 'abc'";
		test("f.col like '%abc'", expr);

		expr = "f.col %~ t.col2";
		test("f.col like concat('%', t.col2)", expr);
	}
		
	@Test
	public void testIsNull() {
		String expr = "i.nodeId = n.nodeId and i.handlingCmd is null and n.isFinish = false";
		test("i.nodeId = n.nodeId AND i.handlingCmd is null AND n.isFinish = false", expr);
	}

	@Test
	public void testIn() {
		String strExpr = "userId in ('1', '2')";
		String expect = "userId in ('1', '2')";
		Condit condt = ConditVisitor.parse(strExpr);
		String sql = condt.sql(null);
		Utils.logi(sql);
		assertEquals(sql, expect);
	}
	
	@Test
	public void testAndOrPrecedence() {
		String strExpr = "a.id = b.roleId and (a.c>d OR x> y) and a.c = '5'";
		String expect = "a.id = b.roleId AND (a.c > d OR x > y) AND a.c = '5'";
		Condit condt = ConditVisitor.parse(strExpr);
		String sql = condt.sql(null);
		// Utils.logi(sql);
		assertEquals(expect, sql);
	}
	
	private void test(String expected, String testing) {
		Condit condt = ConditVisitor.parse(testing);
		String sql = condt.sql(null);
		// Utils.logi(sql);
		assertEquals(expected, sql);
	}
	

}
