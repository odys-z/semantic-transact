package io.odysz.transact.sql.parts.condition;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.Logic.op;

public class ExprPart extends AbsPart {
	private op logic;
	private String lexp;
	private String rexp;

	public ExprPart(op logic, String lexp, String rexp) {
		this.logic =logic;
		this.lexp = lexp;
		this.rexp = rexp;
	}

	public ExprPart(String id) {
		this.logic = null;
		lexp = id;
	}

	@Override
	public String sql(ISemantext ctx) {
		// FIXME what about unary operand?
		if (logic == null)
			return lexp == null ? "" : lexp;
		else {
//			Object lresulved = ctx == null ? lexp : ctx.resulvedVal(lexp);
//			Object rresulved = ctx == null ? rexp : ctx.resulvedVal(rexp);
//			return String.format("%s %s",
//				lexp == null ? "" : lresulved,
//				logic.sql(logic, rexp == null ? "" : (String) rresulved));
			return String.format("%s %s",
				lexp == null ? "" : lexp,
				logic.sql(logic, rexp == null ? "" : rexp));
		}
	}
}
