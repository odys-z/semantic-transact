package io.odysz.semantics.sql.parts.condition;

import java.util.ArrayList;
import java.util.List;

import io.odysz.semantics.sql.parts.Logic;
import io.odysz.semantics.sql.parts.Logic.op;
import io.odysz.semantics.sql.parts.Logic.type;


public class Condit extends Predicate {

	private type logitype;
	private List<Condit> condts;

	// TODO bug?
	Predicate predict;


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
		return this;
	}

	public String sql() {
		// TODO Auto-generated method stub
		return "";
	}

}
