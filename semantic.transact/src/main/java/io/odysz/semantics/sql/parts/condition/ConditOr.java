package io.odysz.semantics.sql.parts.condition;

import java.util.ArrayList;

import io.odysz.semantics.sql.parts.Logic.op;

public class ConditOr extends ConditAnd {
	ArrayList<AbsPart> ors;

	public ConditOr(op op, String lop, String rop) {
		super(op, lop, rop);
	}

	@Override
	public Condit or(String logic, String from, String... to) {
		
		return this;
	}
}
