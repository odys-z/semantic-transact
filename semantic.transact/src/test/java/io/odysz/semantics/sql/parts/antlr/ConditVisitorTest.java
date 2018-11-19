package io.odysz.semantics.sql.parts.antlr;

import static org.junit.Assert.*;

import org.junit.Test;

import io.odysz.semantics.sql.parts.condition.Condit;

public class ConditVisitorTest {

	@Test
	public void testParse() {
		// String strExpr = "f.funcId = rf.funcId and rf.roleId = 'r001'";
		// String strExpr = "F.FUNCID = RF.FUNCID AND RF.ROLEID = 'r001'";

		String strExpr = "a.id = b.roleId and (a.c>d OR x> y) and a.c = '5'";
		String expect = "a.id = b.roleId AND (a.c > d OR x > y) AND a.c = '5'";
		Condit condt = ConditVisitor.parse(strExpr);
		String sql = condt.sql();
		System.out.print(sql);
		assertEquals(sql, expect);
	}

}
