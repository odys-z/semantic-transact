package io.odysz.transact.sql.parts.condition;

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

	public String sql() {
		// FIXME what about unary operand?
		if (logic == null)
			return lexp == null ? "" : lexp;
		else return String.format("%s %s %s",
				lexp == null ? "" : lexp,
				logic.sql(logic, rexp == null ? "" : rexp));
	}

}
