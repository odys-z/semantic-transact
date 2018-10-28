package io.odysz.semantics.sql.parts;

public class ExprBuilder {

	private String loperand;
	private String roperand;

	public ExprBuilder(String logic) {
		// TODO Auto-generated constructor stub
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
