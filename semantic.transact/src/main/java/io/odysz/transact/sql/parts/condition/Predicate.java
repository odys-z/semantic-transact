package io.odysz.transact.sql.parts.condition;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.tree.TerminalNode;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.Query;
import io.odysz.transact.sql.parts.AbsPart;
import io.odysz.transact.sql.parts.Logic;
import io.odysz.transact.sql.parts.antlr.ExprsVisitor;
import io.odysz.transact.x.TransException;

/**Basically a logic expression. For predicate definition, see {@link io.odysz.transact.sql.parts.antlr.PredicatVisitor}.
 * @author odys-z@github.com
 */
public class Predicate extends AbsPart {

	protected boolean empty = false;
	public boolean empty() { return empty; }
	
	private boolean negative = false;
	public boolean negative() { return negative; }

	private Logic.op op;
	public Logic.op logic() { return op; }

	private ExprPart l;
	private ExprPart r;
	private boolean brace;
	private Condit search_condit;

	private Query inSelect;

	public Predicate(Logic.op op, ExprPart lexpr, String rop) {
		this.op = op;
		this.l = lexpr;
		// this.r = new ExprPart(rop);
		this.r = rop == null ? null : ExprsVisitor.parse(rop);
	}

	public Predicate() {
	}

	public Predicate(Predicate cpy) {
		if (cpy == null)
			empty = true;
		else {
			empty = false;
			this.op = cpy.op;
			this.l = cpy.l;
			this.r = cpy.r;
			this.brace = cpy.brace;
			this.search_condit = cpy.search_condit;
			this.inSelect = cpy.inSelect;
		}
	}

	public Predicate(Logic.op op, ExprPart lexpr, ExprPart rexpr) {
		this.op = op;
		this.l = lexpr;
		this.r = rexpr;
	}

	public Predicate(Logic.op inlike, ExprPart expression,
			List<ExprPart> inlikes, boolean... not) {
		op = inlike;
		l = expression;
		String rstr = inlikes.stream()
			.map(var -> {
				try {
					return var.sql(null);
				} catch (TransException e) {
					e.printStackTrace();
					return e.getMessage();
				}
			}) // variable resolving goes here 
			.collect(Collectors.joining(", "));
		r = new ExprPart(rstr);
	}

	public Predicate(Logic.op op, String lp, String rp) {
		this.op = op;
		this.l = lp == null ? null : ExprsVisitor.parse(lp);
		this.r = rp == null ? null :
			// for IN's rp, it's an array of expressions, ExprsVisitor can't parse it
			op == Logic.op.in ? new ExprPart(rp)
							  : ExprsVisitor.parse(rp);
	}

	public Predicate(Logic.op op, String lp, ExprPart rp) {
		this.op = op;
		// this.l = new ExprPart(from);
		this.l = lp == null ? null : ExprsVisitor.parse(lp);
		this.r = rp;
	}

	/**Use this to visit predicate : '(' search_condition ')';
	 * @param search_condit
	 */
	public Predicate(Condit search_condit) {
		this.brace = true;
		this.search_condit = search_condit;
	}

	public Predicate(Logic.op in, String col, Query s) throws TransException {
		this.op = in;
		if (in != Logic.op.in && in != Logic.op.notin)
			throw new TransException("Currently only '(not) in' operator is supported for select condition. select:\n %s", s.sql(null));
		// this.l = new ExprPart(col);
		this.l = col == null ? null : ExprsVisitor.parse(col);
		this.inSelect = s;
	}

	public void not(TerminalNode not) {
		negative = not != null && not.getText() != null && not.getText().length() > 0;
	}

	@Override
	public String sql(ISemantext sctx) throws TransException {
		if (empty) return "";
		else if ((op == Logic.op.in || op == Logic.op.notin) && inSelect != null)
			return Stream.of(l,
					op == Logic.op.notin ? new ExprPart("not") : null,
					new ExprPart("in ("), inSelect, new ExprPart(")"))
					.map(e -> {
						try {
							return e == null ? "" : e.sql(sctx);
						} catch (TransException e1) {
							e1.printStackTrace();
							return "";
						}
					})
					.collect(Collectors.joining(" "));

		if (brace && search_condit != null)
			return String.format("(%s)", search_condit.sql(sctx));
		else {
			// return String.format("%s %s", l.sql(sctx), op.sql(sctx, op, r.sql(sctx), negative));
			// FIXME Should pedicate's operands always can't escape ' ? 
			return String.format("%s %s", l.escape(escape).sql(sctx),
					op.sql(sctx, op, r.escape(escape).sql(sctx), negative));
		}
	}

	/** escape ' with ''<br>
	 * Predicate's default escape is false?*/
	boolean escape = false;
	/** enable escape - replace ' with ''. (default true)
	 * @param esc
	 * @return this
	 */
	public Predicate escape(boolean esc) {
		escape = esc;
		return this;
	}
}
