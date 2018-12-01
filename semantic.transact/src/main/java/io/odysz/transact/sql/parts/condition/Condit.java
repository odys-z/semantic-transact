package io.odysz.transact.sql.parts.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.odysz.semantics.Semantext;
import io.odysz.transact.sql.parts.Logic;
import io.odysz.transact.sql.parts.Logic.op;
import io.odysz.transact.sql.parts.Logic.type;
import io.odysz.transact.sql.parts.antlr.ConditVisitor;


/**For grammar definition, see {@link ConditVisitor} 
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
	 */
	protected Predicate predict;

	public Condit(op op, String lop, String rop) {
		super(op, lop, rop);
	}

	public Condit(Logic.type type, List<Condit> condts) {
		super();
		this.logitype = type;
		this.condts = condts;
	}

	public Condit(Predicate predicate) {
		this.predict = predicate;
		// condts = new ArrayList<Condit>();
	}

	public Condit and(Condit and) {
		if (condts == null || condts.size() == 0) {
			logitype = type.and;
			if (condts == null)
				condts = new ArrayList<Condit>();
			condts.add(and);
		}
		else {	
			if (logitype == type.or)
				// and is prior to the other ors
				condts.get(condts.size() - 1).and(and);
			else if (logitype == type.and)
				condts.add(and);
			else if (logitype == type.not) {
				Condit left = new Condit(type.not, condts);
				condts = new ArrayList<Condit>();
				condts.add(left);
				logitype = type.and;
				condts.add(and);
			}
		}
			
		return this;
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

	@Override
	public String sql(Semantext sctx) {
		// handling with 3 grammar rule: search_condition, search_condition_and, search_condition_not
		// 1. search_condition_not
		if (predict != null)
			return predict.sql(sctx);
		// 2. search_condition_and
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
		return super.sql(sctx);
	}

}
