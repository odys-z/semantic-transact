package io.odysz.semantics.sql.parts.condition;

import io.odysz.semantics.sql.parts.Logic.op;

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

}
