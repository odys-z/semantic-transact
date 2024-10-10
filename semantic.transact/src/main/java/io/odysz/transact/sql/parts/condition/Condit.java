package io.odysz.transact.sql.parts.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.Query;
import io.odysz.transact.sql.parts.Logic;
import io.odysz.transact.sql.parts.Logic.op;
import io.odysz.transact.sql.parts.Logic.type;
import io.odysz.transact.sql.parts.antlr.ConditVisitor;
import io.odysz.transact.x.TransException;


/**Logical Conditioning, a {@link Predicate} tree.
 * For grammar definition, see {@link ConditVisitor}. 
 * @author ody
 *
 */
public class Condit extends Predicate {

	protected type logitype;
	protected List<Condit> condts;

	/**When this is not null, this object is representing search_condition_not:<pre>
search_condition_not
    : NOT? predicate
    ;</pre>
	protected Predicate predict;
	 */

	public Condit(op op, String lop, String rop) {
		super(op, lop, rop); // FIXME op = eq, lop = e.pid, rop = ch.entfk, line 1:8 mismatched input '<EOF>' expecting '.'
		this.logitype = Logic.type.empty;
	}

	public Condit(op op, String lop, ExprPart rop) {
		super(op, lop, rop);
		this.logitype = Logic.type.empty;
	}

	public Condit(op op, ExprPart lop, ExprPart rop) {
		super(op, lop, rop);
		this.logitype = Logic.type.empty;
	}

	public Condit(Logic.type type, List<Condit> condts) {
		super();
		this.logitype = type;
		this.condts = condts;
	}

	public Condit(Predicate predicate) {
		super(predicate);
		this.logitype = Logic.type.empty;
	}
	
	public Condit(op op, String lop, Query rop) throws TransException {
		super(op, lop, rop);
		this.logitype = Logic.type.empty;
	}
	
	public boolean isEmpty() {
		return (condts == null || condts.size() == 0) && super.empty;
	}

	public Condit and(Condit and) {
		if (logitype == type.or) {
			// and is prior to the other ors
			condts.get(condts.size() - 1).and(and);
			return this;
		}
		else if (logitype == type.and) {
			condts.add(and);
			return this;
		}else if (logitype == type.not) {
			Condit left = new Condit(type.not, condts);
			condts = new ArrayList<Condit>();
			condts.add(left);
			logitype = type.and;
			condts.add(and);
			return this;
		}
		else { // empty logic, AND with it
			ArrayList<Condit> ands = new ArrayList<Condit>();
			ands.add(this);
			ands.add(and);
			return new Condit(Logic.type.and, ands);
		}
			
	}
	
	public Condit or(Condit or) {
		if (logitype == type.or) {
			condts.add(or);
			return this;
		}
		else if (logitype == type.and) {
			condts.get(condts.size() - 1).or(or);
			return this;
		}else if (logitype == type.not) {
			// shouldn't reach here
			Condit left = new Condit(type.not, condts);
			condts = new ArrayList<Condit>();
			condts.add(left);
			logitype = type.or;
			condts.add(or);
			return this;
		}
		else { // empty logic, OR with it
			ArrayList<Condit> ands = new ArrayList<Condit>();
			ands.add(this);
			ands.add(or);
			return new Condit(Logic.type.or, ands);
		}
	}

	/** Additional information of left alias for generating sql. */
	String lAlias;
	/** Additional information of right alias for generating sql. */
	String rAlias;
	private int priority = 0;

	/**Set the priority 1 more higher than parent
	 * - call this only when composing sql(conditions won't changing)
	 * @param parentLogic
	 * @return
	 */
	private Condit prio(Logic.type parentLogic) {
		if (parentLogic == Logic.type.and && logitype == Logic.type.or
			// call this only when composing sql(conditions won't changing)
			&& condts != null && condts.size() > 1)
			this.priority = 1;

		return this;
	}
	
	private String lbrace() {
		String l = "";
		if (condts != null && condts.size() > 0)
			for (int b = 0; b < priority; b++)
				l += "(";
		return l;
	}

	private String rbrace() {
		String r = "";
		if (condts != null && condts.size() > 0)
			for (int b = 0; b < priority; b++)
				r += ")";
		return r;
	}

	/**<p>Sometimes conditions's table name or alias are ignored by client.
	 * This method can be called by {@link io.odysz.transact.sql.parts.JoinTabl}
	 * to supply additional information when generating sql.<p>
	 * <p><b>TODO DESIGN MEMO</b><br>
	 * {@link Condit}s are parsed from sometimes from where clause, sometimes from join-on clause,
	 * so it's not have enough information to parse ignored alias.<br>
	 * Should we implement this fault tolerance?
	 * </p> @Deprecated
	 * @param lt left operand's alias (table name)
	 * @param rt right operand's alias (table name)
	 * @return this
	 */
	public Condit sqlTbl(String lt, String rt) {
		this.lAlias = lt;
		this.rAlias = lt;
		return this;
	}

	@Override
	public String sql(ISemantext sctx) throws TransException {
		// handling with 3 grammar rule: search_condition, search_condition_and, search_condition_not
		// 1. search_condition_not

		// 2. search_condition_and
		if (logitype == type.empty) {
			return super.sql(sctx);
		}
		else if (logitype == type.and) {
			if (condts != null && condts.size() > 0) {
				String sql = condts.stream()
					.map(cdt -> {
						try {
							cdt.prio(logitype);
							return cdt.sql(sctx);
						} catch (TransException e) {
							e.printStackTrace();
							return e.getMessage();
						}
					})
					.collect(Collectors.joining(" AND ", lbrace(), rbrace()));
				return sql;
			}
		}
		// 3. search_conditon_not
		else if (logitype == type.or) {
			if (condts != null && condts.size() > 0) {
				String sql = condts.stream()
					.map(cdt -> {
						try {
							return cdt.sql(sctx);
						} catch (TransException e) {
							e.printStackTrace();
							return e.getMessage();
						}
					})
					.collect(Collectors.joining(" OR ", lbrace(), rbrace()));
				return sql;
			}
		}
		return null;
	}
}
