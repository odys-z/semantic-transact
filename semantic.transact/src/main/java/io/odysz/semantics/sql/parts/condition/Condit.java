package io.odysz.semantics.sql.parts.condition;

import io.odysz.semantics.sql.parts.Logic.op;

public class Condit extends Predicate {

	// Predicate predict;

	public Condit(op op, String lop, String rop) {
		super(op, lop, rop);
	}

	public Condit and(Condit and) {
		return this;
	}

	public Condit or(String logic, String from, String... to) {
		return this;
	}

}
