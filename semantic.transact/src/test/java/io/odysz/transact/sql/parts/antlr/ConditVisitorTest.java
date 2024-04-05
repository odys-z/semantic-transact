package io.odysz.transact.sql.parts.antlr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.odysz.transact.sql.parts.condition.Condit;
import io.odysz.transact.x.TransException;

public class ConditVisitorTest {
	@Test
	public void testLike() throws TransException {
		String expr = "f.col LIKE 'abc'";
		test("f.col like '%abc%'", expr);

		expr = "f.col not like 'abc'";
		test("f.col not like '%abc%'", expr);

		expr = "f.col % 'abc'";
		test("f.col like '%abc%'", expr);

		expr = "f.col %~ 'abc'";
		test("f.col like '%abc'", expr);

		expr = "f.col %~ t.col2";
		test("f.col like concat('%', t.col2)", expr);
	}
		
	@Test
	public void testIsNull() throws TransException {
		String expr = "i.nodeId = n.nodeId and  n.isFinish = false";
		test("i.nodeId = n.nodeId AND n.isFinish = false", expr);
		expr = "i.nodeId = n.nodeId and i.handlingCmd is null and n.isFinish = false";
		test("i.nodeId = n.nodeId AND i.handlingCmd is null AND n.isFinish = false", expr);

		expr = "i.nodeId = n.nodeId and i.handlingCmd is not null and n.isFinish = false";
		test("i.nodeId = n.nodeId AND i.handlingCmd is not null AND n.isFinish = false", expr);
	}

	@Test
	public void testIn() throws TransException {
		String strExpr = "userId in ('1', '2')";
		String expect = "userId in ('1', '2')";
		Condit condt = ConditVisitor.parse(strExpr);
		String sql = condt.sql(null);
		assertEquals(sql, expect);

		strExpr = "userId not in ('1', '2')";
		expect = "userId not in ('1', '2')";
		condt = ConditVisitor.parse(strExpr);
		sql = condt.sql(null);
		assertEquals(sql, expect);
	}
	
	@Test
	public void testAndOrPrecedence() throws TransException {
		String strExpr = "a.id = b.roleId and (a.c>d OR x> y) and a.c = '5'";
		String expect = "a.id = b.roleId AND (a.c > d OR x > y) AND a.c = '5'";
		Condit condt = ConditVisitor.parse(strExpr);
		String sql = condt.sql(null);
		// Utils.logi(sql);
		assertEquals(expect, sql);
	}
	
	private void test(String expected, String testing) throws TransException {
		Condit condt = ConditVisitor.parse(testing);
		String sql = condt.sql(null);
		// Utils.logi(sql);
		assertEquals(expected, sql);
	}
	

}
