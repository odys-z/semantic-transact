package io.odysz.transact.sql.parts.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.odysz.semantics.ISemantext;
import io.odysz.transact.sql.parts.Logic;
import io.odysz.transact.sql.parts.Logic.op;
import io.odysz.transact.sql.parts.Logic.type;
import io.odysz.transact.sql.parts.antlr.ConditVisitor;


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
		super(op, lop, rop);
		this.logitype = Logic.type.empty;
	}

	public Condit(Logic.type type, List<Condit> condts) {
		super();
		this.logitype = type;
		this.condts = condts;
	}

	public Condit(Predicate predicate) {
		// this.predict = predicate;
		super(predicate);
		this.logitype = Logic.type.empty;
	}

	public Condit and(Condit and) {
//		if (condts == null || condts.size() == 0) {
//			if (condts == null) {
//				condts = new ArrayList<Condit>();
//			}
//			condts.add(this);
//			condts.add(and);
//			return new Condit(type.and, condts);
//		}
//		else {	
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
			else { // empty logic, and to it
				ArrayList<Condit> ands = new ArrayList<Condit>();
				ands.add(this);
				ands.add(and);
				return new Condit(Logic.type.and, ands);
			}

//		}
			
	}

	public Condit or(String logic, String from, String... to) {
		// TODO
		// TODO
		// TODO
		// TODO
		// TODO
		// TODO
		return this;
	}

	/** Additional information of left alias for generating sql. */
	String lAlias;
	/** Additional information of right alias for generating sql. */
	String rAlias;
	/**<p>Sometimes conditions's table name or alias are ignored by client.
	 * This method can be called by {@link io.odysz.transact.sql.parts.select.JoinTabl}
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
	public String sql(ISemantext sctx) {
		// handling with 3 grammar rule: search_condition, search_condition_and, search_condition_not
		// 1. search_condition_not
//		if (predict != null)
//			return predict.sql(sctx); // TODO debug: no "not"?

		// 2. search_condition_and
		if (logitype == type.empty) {
			return super.sql(sctx);
		}
		else if (logitype == type.and) {
			if (condts != null && condts.size() > 0) {
				String sql = condts.stream()
					.map(cdt -> cdt.sql(sctx))
					.collect(Collectors.joining(" AND "));
				return sql;
			}
		}
		// 3. search_conditon_not
		else if (logitype == type.or) {
			if (condts != null && condts.size() > 0) {
				String sql = condts.stream()
					.map(cdt -> cdt.sql(sctx))
					.collect(Collectors.joining(" OR "));
				return sql;
			}
		}
//		else if (predict == null && condts == null)
//			return "";
		// 4. search_condition - CAN'T reach here, it's Predicat's business.
		// return super.sql(sctx);

		return null;
	}

}
