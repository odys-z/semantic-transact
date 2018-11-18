package io.odysz.semantics.sql.parts.condition;

import org.antlr.v4.runtime.tree.TerminalNode;

import io.odysz.semantics.sql.parts.Logic.op;

public class Predicate extends AbsPart {

	private boolean negative = false;

	public Predicate(op op, String lop, String rop) {
	}


	public Predicate() {
	}


	public void not(TerminalNode not) {
		negative = not.getText() != null && not.getText().length() > 0;
	}
}
