package io.odysz.semantics.sql.parts.antlr;

import static org.junit.Assert.*;

import org.junit.Test;

import io.odysz.semantics.sql.parts.condition.Condit;

public class ConditVisitorTest {

	@Test
	public void testParse() {
		String strExpr = "f.funcId = rf.funcId and rf.roleId = 'r001'";
		Condit condt = ConditVisitor.parse(strExpr);
		assertEquals(condt.toString(), strExpr);
	}

}
