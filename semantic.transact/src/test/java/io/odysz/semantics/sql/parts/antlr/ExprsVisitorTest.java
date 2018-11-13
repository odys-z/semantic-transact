package io.odysz.semantics.sql.parts.antlr;

import static org.junit.Assert.*;

import org.junit.Test;

import io.odysz.semantics.sql.parts.condition.Condit;

public class ExprsVisitorTest {

	@Test
	public void testParse() {
		String strExpr = "f.funcId = rf.funcId and rf.roleId = 'r001'";
		Condit condt = ExprsVisitor.parse(strExpr);
		assertEquals(condt.toString(), strExpr);
	}

	@Test
	public void testOp() {
		fail("Not yet implemented");
	}

	@Test
	public void testRop() {
		fail("Not yet implemented");
	}

	@Test
	public void testLop() {
		fail("Not yet implemented");
	}

}