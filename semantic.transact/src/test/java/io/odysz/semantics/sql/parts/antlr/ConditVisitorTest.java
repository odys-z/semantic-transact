package io.odysz.semantics.sql.parts.antlr;

import static org.junit.Assert.*;

import org.junit.Test;

import io.odysz.semantics.sql.parts.condition.Condit;

public class ConditVisitorTest {

	@Test
	public void testParse() {
		// String strExpr = "f.funcId = rf.funcId and rf.roleId = 'r001'";
		// String strExpr = "F.FUNCID = RF.FUNCID AND RF.ROLEID = 'r001'";
		String strExpr = "A = B";
		Condit condt = ConditVisitor.parse(strExpr);
		assertEquals(condt.sql(), strExpr);
	}

}
