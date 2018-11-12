package io.odysz.semantics.sql.parts;

import io.odysz.semantics.sql.parts.Logic.op;

@Deprecated
public class ExprBuilder {

	private String loperand;
	private String roperand;

	public ExprBuilder(String logicFormat, Object[] args) {
	}

	public ExprBuilder(String logicFormat) {
	}

	public ExprBuilder(op op, String loperand, String roperand) {
	}

	public ExprBuilder left(String operand) {
		this.loperand = operand;
		return this;
	}

	public ExprBuilder right(String operand) {
		this.roperand = operand;
		return this;
	}

}
