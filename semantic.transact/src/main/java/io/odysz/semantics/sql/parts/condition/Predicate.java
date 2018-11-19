package io.odysz.semantics.sql.parts.condition;

import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import io.odysz.semantics.sql.parts.Logic;

public class Predicate extends AbsPart {

	private boolean negative = false;
	private Logic.op op;
	private ExprPart l;
	private ExprPart r;
	private boolean brace;
	private Condit search_condit;

	public Predicate(Logic.op op, ExprPart exprPart, String nnn) {
		// TODO Auto-generated constructor stub
	}

	public Predicate() {
	}

	public Predicate(Logic.op op, ExprPart lexpr, ExprPart rexpr) {
		this.op = op;
		this.l = lexpr;
		this.r = rexpr;
	}

	public Predicate(Logic.op inlike, ExprPart expression,
			List<ExprPart> expressions, boolean... not) {
	}

	public Predicate(Logic.op op, String from, String nnn) {
		this.op = op;
		this.l = new ExprPart(from);
		this.r = new ExprPart(nnn);
	}

	/**Use this to visit predicate : '(' search_condition ')';
	 * @param search_condit
	 */
	public Predicate(Condit search_condit) {
		this.brace = true;
		this.search_condit = search_condit;
	}

	public void not(TerminalNode not) {
		negative = not != null && not.getText() != null && not.getText().length() > 0;
	}

	public String sql() {
		if (brace && search_condit != null)
			return String.format("(%s)", search_condit.sql());
		else
			return String.format("%s %s", l.sql(), op.sql(op, r.sql()));
	}
}
